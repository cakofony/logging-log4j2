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
package org.apache.logging.log4j.core.async;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.categories.AsyncLoggers;
import org.apache.logging.log4j.core.CoreLoggerContexts;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.util.StringBuilders;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Category(AsyncLoggers.class)
public class AsyncLoggerConfigTest4 {

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("log4j2.is.webapp", "false");
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "AsyncLoggerConfigTest4.xml");
    }

    @AfterClass
    public static void afterClass() {
        System.clearProperty("log4j2.is.webapp");
    }

    @Test
    public void testParameters() throws Exception {
        final File file = new File("target", "AsyncLoggerConfigTest4.log");
        assertTrue("Deleted old file before test", !file.exists() || file.delete());

        final Logger log = LogManager.getLogger("com.foo.Bar");
        log.info("Additive logging: {} for the price of {}!", 2, 1);
        CoreLoggerContexts.stopLoggerContext(file); // stop async thread

        final BufferedReader reader = new BufferedReader(new FileReader(file));
        final String line1 = reader.readLine();
        final String line2 = reader.readLine();
        reader.close();
        file.delete();

        assertThat(line1, containsString("[2,1]"));
        assertThat(line2, containsString("[2,1]"));
    }

    @Plugin(name = "TestParametersPatternConverter", category = "Converter")
    @ConverterKeys("testparameters")
    public static final class TestParametersPatternConverter extends LogEventPatternConverter {

        private TestParametersPatternConverter() {
            super("Parameters", "testparameters");
        }

        public static TestParametersPatternConverter newInstance(final String[] options) {
            return new TestParametersPatternConverter();
        }

        @Override
        public void format(final LogEvent event, final StringBuilder toAppendTo) {
            toAppendTo.append('[');
            Object[] parameters = event.getMessage().getParameters();
            LOGGER.error("INVOKED WITH parameters {}", parameters);
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    StringBuilders.appendValue(toAppendTo, parameters[i]);
                    if (i != parameters.length - 1) {
                        toAppendTo.append(',');
                    }
                }
            }
            toAppendTo.append(']');
        }
    }
}
