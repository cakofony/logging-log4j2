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

import static org.apache.logging.log4j.util.Unbox.box;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.filter.ThreadContextMapFilter;
import org.apache.logging.log4j.junit.CleanUpFiles;
import org.apache.logging.log4j.junit.LoggerContextSource;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@CleanUpFiles({
        "target/test-xml.log",
        "target/test-xinclude.log",
        "target/test-json.log",
        "target/test-yaml.log",
        "target/test-properties.log"
})
class ConfigurationFactoryTest {

    static final String LOGGER_NAME = "org.apache.logging.log4j.test1.Test";
    static final String FILE_LOGGER_NAME = "org.apache.logging.log4j.test2.Test";
    static final String APPENDER_NAME = "STDOUT";

    /**
     * Runs various configuration checks on a configured LoggerContext that should match the equivalent configuration in
     * {@code log4j-test1.xml}.
     */
    static void checkConfiguration(final LoggerContext context) {
        final Configuration configuration = context.getConfiguration();
        final Map<String, Appender> appenders = configuration.getAppenders();
        // these used to be separate tests
        assertAll(() -> assertThat(appenders).isNotNull(),
                () -> assertThat(appenders.size()).isEqualTo(3),
                () -> assertThat(configuration.getLoggerContext()).isNotNull(),
                () -> assertThat(configuration.getLoggerConfig(Strings.EMPTY)).isEqualTo(configuration.getRootLogger()),
                () -> assertThatThrownBy(() -> configuration.getLoggerConfig(null)).isInstanceOf(NullPointerException.class));

        final Logger logger = context.getLogger(LOGGER_NAME);
        assertThat(logger.getLevel()).isEqualTo(Level.DEBUG);

        assertThat(logger.filterCount()).isEqualTo(1);
        final Iterator<Filter> filterIterator = logger.getFilters();
        assertThat(filterIterator.hasNext()).isTrue();
        assertThat(filterIterator.next() instanceof ThreadContextMapFilter).isTrue();

        final Appender appender = appenders.get(APPENDER_NAME);
        assertThat(appender instanceof ConsoleAppender).isTrue();
        assertThat(appender.getName()).isEqualTo(APPENDER_NAME);
    }

    static void checkFileLogger(final LoggerContext context, final Path logFile) throws IOException {
        final long currentThreadId = Thread.currentThread().getId();
        final Logger logger = context.getLogger(FILE_LOGGER_NAME);
        logger.debug("Greetings from ConfigurationFactoryTest in thread#{}", box(currentThreadId));
        final List<String> lines = Files.readAllLines(logFile);
        assertThat(lines.size()).isEqualTo(1);
        assertThat(lines.get(0).endsWith(Long.toString(currentThreadId))).isTrue();
    }

    @Test
    @LoggerContextSource("log4j-test1.xml")
    void xml(final LoggerContext context) throws IOException {
        checkConfiguration(context);
        final Path logFile = Paths.get("target", "test-xml.log");
        checkFileLogger(context, logFile);
    }

    @Test
    @LoggerContextSource("log4j-xinclude.xml")
    void xinclude(final LoggerContext context) throws IOException {
        checkConfiguration(context);
        final Path logFile = Paths.get("target", "test-xinclude.log");
        checkFileLogger(context, logFile);
    }

    @Test
    @Tag("json")
    @LoggerContextSource("log4j-test1.json")
    void json(final LoggerContext context) throws IOException {
        checkConfiguration(context);
        final Path logFile = Paths.get("target", "test-json.log");
        checkFileLogger(context, logFile);
    }

    @Test
    @Tag("yaml")
    @LoggerContextSource("log4j-test1.yaml")
    void yaml(final LoggerContext context) throws IOException {
        checkConfiguration(context);
        final Path logFile = Paths.get("target", "test-yaml.log");
        checkFileLogger(context, logFile);
    }

    @Test
    @LoggerContextSource("log4j-test1.properties")
    void properties(final LoggerContext context) throws IOException {
        checkConfiguration(context);
        final Path logFile = Paths.get("target", "test-properties.log");
        checkFileLogger(context, logFile);
    }
}
