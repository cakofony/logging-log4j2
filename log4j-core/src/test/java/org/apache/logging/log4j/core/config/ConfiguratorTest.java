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
package org.apache.logging.log4j.core.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.net.URI;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("functional")
public class ConfiguratorTest {

    @Test
    public void testInitializeFromAbsoluteFilePath() {
        final String path = new File("src/test/resources/log4j-list.xml").getAbsolutePath();
        testInitializeFromFilePath(path);
    }

    @Test
    public void testInitializeFromRelativeFilePath() {
        final String path = new File("src/test/resources/log4j-list.xml").toString();
        testInitializeFromFilePath(path);
    }

    @Test
    public void testReconfigure() {
        final String path = new File("src/test/resources/log4j-list.xml").getAbsolutePath();
        try (final LoggerContext loggerContext = Configurator.initialize(getClass().getName(), null, path)) {
            assertThat(loggerContext.getConfiguration().<Appender>getAppender("List")).isNotNull();
            URI uri = loggerContext.getConfigLocation();
            assertThat(uri).describedAs("No configuration location returned").isNotNull();
            Configurator.reconfigure();
            assertThat(loggerContext.getConfigLocation()).describedAs("Unexpected configuration location returned").isEqualTo(uri);
        }
    }

    @Test
    public void testReconfigureFromPath() {
        final String path = new File("src/test/resources/log4j-list.xml").getAbsolutePath();
        try (final LoggerContext loggerContext = Configurator.initialize(getClass().getName(), null, path)) {
            assertThat(loggerContext.getConfiguration().<Appender>getAppender("List")).isNotNull();
            URI uri = loggerContext.getConfigLocation();
            assertThat(uri).describedAs("No configuration location returned").isNotNull();
            final URI location = new File("src/test/resources/log4j2-config.xml").toURI();
            Configurator.reconfigure(location);
            assertThat(loggerContext.getConfigLocation()).describedAs("Unexpected configuration location returned").isEqualTo(location);
        }
    }

    private void testInitializeFromFilePath(final String path) {
        try (final LoggerContext loggerContext = Configurator.initialize(getClass().getName(), null, path)) {
            assertThat(loggerContext.getConfiguration().<Appender>getAppender("List")).isNotNull();
        }
    }
}
