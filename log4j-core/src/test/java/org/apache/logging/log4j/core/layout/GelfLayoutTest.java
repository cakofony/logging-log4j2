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
package org.apache.logging.log4j.core.layout;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.layout.GelfLayout.CompressionType;
import org.apache.logging.log4j.core.lookup.JavaLookup;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.core.util.NetUtils;
import org.apache.logging.log4j.junit.UsingAnyThreadContext;
import org.apache.logging.log4j.test.appender.EncodingListAppender;
import org.apache.logging.log4j.test.appender.ListAppender;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@UsingAnyThreadContext
public class GelfLayoutTest {

    static ConfigurationFactory configFactory = new BasicConfigurationFactory();

    private static final String HOSTNAME = "TheHost";
    private static final String KEY1 = "Key1";
    private static final String KEY2 = "Key2";
    private static final String LINE1 = "empty mdc";
    private static final String LINE2 = "filled mdc";
    private static final String LINE3 = "error message";
    private static final String MDCKEY1 = "MdcKey1";
    private static final String MDCKEY2 = "MdcKey2";
    private static final String MDCVALUE1 = "MdcValue1";
    private static final String MDCVALUE2 = "MdcValue2";
    private static final String VALUE1 = "Value1";

    @AfterAll
    public static void cleanupClass() {
        ConfigurationFactory.removeConfigurationFactory(configFactory);
    }

    @BeforeAll
    public static void setupClass() {
        ConfigurationFactory.setConfigurationFactory(configFactory);
        final LoggerContext ctx = LoggerContext.getContext();
        ctx.reconfigure();
    }

    LoggerContext ctx = LoggerContext.getContext();

    Logger root = ctx.getRootLogger();

    private void testCompressedLayout(final CompressionType compressionType, final boolean includeStacktrace,
                                      final boolean includeThreadContext, String host, final boolean includeNullDelimiter,
                                      final boolean includeNewLineDelimiter) throws IOException {
        for (final Appender appender : root.getAppenders().values()) {
            root.removeAppender(appender);
        }
        // set up appenders
        final GelfLayout layout = GelfLayout.newBuilder()
            .setConfiguration(ctx.getConfiguration())
            .setHost(host)
            .setAdditionalFields(new KeyValuePair[] {
                new KeyValuePair(KEY1, VALUE1),
                new KeyValuePair(KEY2, "${java:runtime}"), })
            .setCompressionType(compressionType)
            .setCompressionThreshold(1024)
            .setIncludeStacktrace(includeStacktrace)
            .setIncludeThreadContext(includeThreadContext)
            .setIncludeNullDelimiter(includeNullDelimiter)
            .setIncludeNewLineDelimiter(includeNewLineDelimiter)
            .build();
        final ListAppender eventAppender = new ListAppender("Events", null, null, true, false);
        final ListAppender rawAppender = new ListAppender("Raw", null, layout, true, true);
        final ListAppender formattedAppender = new ListAppender("Formatted", null, layout, true, false);
        final EncodingListAppender encodedAppender = new EncodingListAppender("Encoded", null, layout, false, true);
        eventAppender.start();
        rawAppender.start();
        formattedAppender.start();
        encodedAppender.start();

        if (host == null) {
            host = NetUtils.getLocalHostname();
        }

        final JavaLookup javaLookup = new JavaLookup();

        // set appenders on root and set level to debug
        root.addAppender(eventAppender);
        root.addAppender(rawAppender);
        root.addAppender(formattedAppender);
        root.addAppender(encodedAppender);
        root.setLevel(Level.DEBUG);

        root.debug(LINE1);

        ThreadContext.put(MDCKEY1, MDCVALUE1);
        ThreadContext.put(MDCKEY2, MDCVALUE2);

        root.info(LINE2);

        final Exception exception = new RuntimeException("some error");
        root.error(LINE3, exception);

        formattedAppender.stop();

        final List<LogEvent> events = eventAppender.getEvents();
        final List<byte[]> raw = rawAppender.getData();
        final List<String> messages = formattedAppender.getMessages();
        final List<byte[]> raw2 = encodedAppender.getData();
        final String threadName = Thread.currentThread().getName();

        //@formatter:off
        assertJsonEquals("{" +
                        "\"version\": \"1.1\"," +
                        "\"host\": \"" + host + "\"," +
                        "\"timestamp\": " + GelfLayout.formatTimestamp(events.get(0).getTimeMillis()) + "," +
                        "\"level\": 7," +
                        "\"_thread\": \"" + threadName + "\"," +
                        "\"_logger\": \"\"," +
                        "\"short_message\": \"" + LINE1 + "\"," +
                        "\"_" + KEY1 + "\": \"" + VALUE1 + "\"," +
                        "\"_" + KEY2 + "\": \"" + javaLookup.getRuntime() + "\"" +
                        "}",
                messages.get(0));

        assertJsonEquals("{" +
                        "\"version\": \"1.1\"," +
                        "\"host\": \"" + host + "\"," +
                        "\"timestamp\": " + GelfLayout.formatTimestamp(events.get(1).getTimeMillis()) + "," +
                        "\"level\": 6," +
                        "\"_thread\": \"" + threadName + "\"," +
                        "\"_logger\": \"\"," +
                        "\"short_message\": \"" + LINE2 + "\"," +
                       (includeThreadContext ?
                            "\"_" + MDCKEY1 + "\": \"" + MDCVALUE1 + "\"," +
                           "\"_" + MDCKEY2 + "\": \"" + MDCVALUE2 + "\","
                                            :
                           "") +
                        "\"_" + KEY1 + "\": \"" + VALUE1 + "\"," +
                        "\"_" + KEY2 + "\": \"" + javaLookup.getRuntime() + "\"" +
                        "}",
                messages.get(1));
        //@formatter:on
        final byte[] compressed = raw.get(2);
        final byte[] compressed2 = raw2.get(2);
        final ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        final ByteArrayInputStream bais2 = new ByteArrayInputStream(compressed2);
        InputStream inflaterStream;
        InputStream inflaterStream2;
        switch (compressionType) {
        case GZIP:
            inflaterStream = new GZIPInputStream(bais);
            inflaterStream2 = new GZIPInputStream(bais2);
            break;
        case ZLIB:
            inflaterStream = new InflaterInputStream(bais);
            inflaterStream2 = new InflaterInputStream(bais2);
            break;
        case OFF:
            inflaterStream = bais;
            inflaterStream2 = bais2;
            break;
        default:
            throw new IllegalStateException("Missing test case clause");
        }
        final byte[] uncompressed = IOUtils.toByteArray(inflaterStream);
        final byte[] uncompressed2 = IOUtils.toByteArray(inflaterStream2);
        inflaterStream.close();
        inflaterStream2.close();
        final String uncompressedString = new String(uncompressed, layout.getCharset());
        final String uncompressedString2 = new String(uncompressed2, layout.getCharset());
        //@formatter:off
        final String expected = "{" +
                "\"version\": \"1.1\"," +
                "\"host\": \"" + host + "\"," +
                "\"timestamp\": " + GelfLayout.formatTimestamp(events.get(2).getTimeMillis()) + "," +
                "\"level\": 3," +
                "\"_thread\": \"" + threadName + "\"," +
                "\"_logger\": \"\"," +
                "\"short_message\": \"" + LINE3 + "\"," +
                "\"full_message\": \"" + String.valueOf(JsonStringEncoder.getInstance().quoteAsString(
                includeStacktrace ? GelfLayout.formatThrowable(exception).toString() : exception.toString())) + "\"," +
                (includeThreadContext ?
                      "\"_" + MDCKEY1 + "\": \"" + MDCVALUE1 + "\"," +
                       "\"_" + MDCKEY2 + "\": \"" + MDCVALUE2 + "\","
                    : "") +
                "\"_" + KEY1 + "\": \"" + VALUE1 + "\"," +
                "\"_" + KEY2 + "\": \"" + javaLookup.getRuntime() + "\"" +
                "}";
        //@formatter:on
        assertJsonEquals(expected, uncompressedString);
        assertJsonEquals(expected, uncompressedString2);
        if (includeNullDelimiter) {
            assertThat(uncompressedString.length() - 1).isEqualTo(uncompressedString.indexOf('\0'));
            assertThat(uncompressedString2.length() - 1).isEqualTo(uncompressedString2.indexOf('\0'));
        }
        if (includeNewLineDelimiter) {
            assertThat(uncompressedString.length() - 1).isEqualTo(uncompressedString.indexOf('\n'));
            assertThat(uncompressedString2.length() - 1).isEqualTo(uncompressedString2.indexOf('\n'));
        }
    }

    @Test
    public void testLayoutGzipCompression() throws Exception {
        testCompressedLayout(CompressionType.GZIP, true, true, HOSTNAME, false, false);
    }

    @Test
    public void testLayoutNoCompression() throws Exception {
        testCompressedLayout(CompressionType.OFF, true, true, HOSTNAME, false, false);
    }

    @Test
    public void testLayoutZlibCompression() throws Exception {
        testCompressedLayout(CompressionType.ZLIB, true, true, HOSTNAME, false, false);
    }

    @Test
    public void testLayoutNoStacktrace() throws Exception {
        testCompressedLayout(CompressionType.OFF, false, true, HOSTNAME, false, false);
    }

    @Test
    public void testLayoutNoThreadContext() throws Exception {
        testCompressedLayout(CompressionType.OFF, true, false, HOSTNAME, false, false);
    }

    @Test
    public void testLayoutNoHost() throws Exception {
        testCompressedLayout(CompressionType.OFF, true, true, null, false, false);
    }

    @Test
    public void testLayoutNullDelimiter() throws Exception {
        testCompressedLayout(CompressionType.OFF, false, true, HOSTNAME, true, false);
    }

    @Test
    public void testLayoutNewLineDelimiter() throws Exception {
        testCompressedLayout(CompressionType.OFF, true, true, HOSTNAME, false, true);
    }

    @Test
    public void testFormatTimestamp() {
        assertThat(GelfLayout.formatTimestamp(0L).toString()).isEqualTo("0");
        assertThat(GelfLayout.formatTimestamp(1000L).toString()).isEqualTo("1.000");
        assertThat(GelfLayout.formatTimestamp(1001L).toString()).isEqualTo("1.001");
        assertThat(GelfLayout.formatTimestamp(1010L).toString()).isEqualTo("1.010");
        assertThat(GelfLayout.formatTimestamp(1100L).toString()).isEqualTo("1.100");
        assertThat(GelfLayout.formatTimestamp(1458741206653L).toString()).isEqualTo("1458741206.653");
        assertThat(GelfLayout.formatTimestamp(Long.MAX_VALUE).toString()).isEqualTo("9223372036854775.807");
    }

    private void testRequiresLocation(String messagePattern, Boolean requiresLocation) {
        GelfLayout layout = GelfLayout.newBuilder()
            .setMessagePattern(messagePattern)
            .build();

        assertThat(requiresLocation).isEqualTo(layout.requiresLocation());
    }

    @Test
    public void testRequiresLocationPatternNotSet() {
        testRequiresLocation(null, false);
    }

    @Test
    public void testRequiresLocationPatternNotContainsLocation() {
        testRequiresLocation("%m %n", false);
    }

    @Test
    public void testRequiresLocationPatternContainsLocation() {
        testRequiresLocation("%C %m %t", true);
    }
}
