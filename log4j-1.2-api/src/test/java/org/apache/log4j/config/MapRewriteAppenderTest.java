/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.log4j.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.ListAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.bridge.AppenderAdapter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test RewriteAppender
 */
public class MapRewriteAppenderTest {

    @BeforeClass
    public static void beforeClass() {
        System.setProperty(ConfigurationFactory.LOG4J1_CONFIGURATION_FILE_PROPERTY, "target/test-classes/log4j1-mapRewrite.xml");
    }

    @After
    public void after() {
        ThreadContext.clearMap();
    }

    @Test
    public void testRewrite() throws Exception {
        Logger logger = LogManager.getLogger("test");
        Map<String, String> map = new HashMap<>();
        map.put("message", "This is a test");
        map.put("hello", "world");
        logger.debug(map);
        LoggerContext context = (LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
        Configuration configuration = context.getConfiguration();
        Map<String, Appender> appenders = configuration.getAppenders();
        ListAppender eventAppender = null;
        for (Map.Entry<String, Appender> entry : appenders.entrySet()) {
            if (entry.getKey().equals("events")) {
                eventAppender = (ListAppender) ((AppenderAdapter.Adapter) entry.getValue()).getAppender();
            }
        }
        assertThat(eventAppender).describedAs("No Event Appender").isNotNull();
        List<LoggingEvent> events = eventAppender.getEvents();
        assertThat(events != null && events.size() > 0).describedAs("No events").isTrue();
        assertThat(events.get(0).getProperties()).describedAs("No properties in the event").isNotNull();
        assertThat(events.get(0).getProperties().containsKey("hello")).describedAs("Key was not inserted").isTrue();
        assertThat(events.get(0).getProperties().get("hello")).describedAs("Key value is incorrect").isEqualTo("world");
    }
}
