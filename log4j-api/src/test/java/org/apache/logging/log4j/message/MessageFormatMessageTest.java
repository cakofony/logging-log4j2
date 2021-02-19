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

package org.apache.logging.log4j.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import org.apache.logging.log4j.junit.Mutable;
import org.apache.logging.log4j.util.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

@ResourceLock(value = Resources.LOCALE, mode = ResourceAccessMode.READ)
public class MessageFormatMessageTest {

    private static final String SPACE = Constants.JAVA_MAJOR_VERSION < 9 ? " " : "\u00a0";

    private static final int LOOP_CNT = 500;
    String[] array = new String[LOOP_CNT];

    @Test
    public void testNoArgs() {
        final String testMsg = "Test message {0}";
        MessageFormatMessage msg = new MessageFormatMessage(testMsg, (Object[]) null);
        String result = msg.getFormattedMessage();
        String expected = "Test message {0}";
        assertThat(result).isEqualTo(expected);
        final Object[] array = null;
        msg = new MessageFormatMessage(testMsg, array, null);
        result = msg.getFormattedMessage();
        expected = "Test message null";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testOneStringArg() {
        final String testMsg = "Test message {0}";
        final MessageFormatMessage msg = new MessageFormatMessage(testMsg, "Apache");
        final String result = msg.getFormattedMessage();
        final String expected = "Test message Apache";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testOneIntArgLocaleUs() {
        final String testMsg = "Test message {0,number,currency}";
        final MessageFormatMessage msg = new MessageFormatMessage(Locale.US, testMsg, 1234567890);
        final String result = msg.getFormattedMessage();
        final String expected = "Test message $1,234,567,890.00";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testOneIntArgLocaleFrance() {
        final String testMsg = "Test message {0,number,currency}";
        final MessageFormatMessage msg = new MessageFormatMessage(Locale.FRANCE, testMsg, 1234567890);
        final String result = msg.getFormattedMessage();
        final String expected = "Test message 1 234 567 890,00" + SPACE + "€";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testException() {
        final String testMsg = "Test message {0}";
        final MessageFormatMessage msg = new MessageFormatMessage(testMsg, "Apache", new NullPointerException("Null"));
        final String result = msg.getFormattedMessage();
        final String expected = "Test message Apache";
        assertThat(result).isEqualTo(expected);
        final Throwable t = msg.getThrowable();
        assertThat(t).describedAs("No Throwable").isNotNull();
    }

    @Test
    public void testUnsafeWithMutableParams() { // LOG4J2-763
        final String testMsg = "Test message {0}";
        final Mutable param = new Mutable().set("abc");
        final MessageFormatMessage msg = new MessageFormatMessage(testMsg, param);

        // modify parameter before calling msg.getFormattedMessage
        param.set("XYZ");
        final String actual = msg.getFormattedMessage();
        assertThat(actual).describedAs("Expected most recent param value").isEqualTo("Test message XYZ");
    }

    @Test
    public void testSafeAfterGetFormattedMessageIsCalled() { // LOG4J2-763
        final String testMsg = "Test message {0}";
        final Mutable param = new Mutable().set("abc");
        final MessageFormatMessage msg = new MessageFormatMessage(testMsg, param);

        // modify parameter after calling msg.getFormattedMessage
        msg.getFormattedMessage();
        param.set("XYZ");
        final String actual = msg.getFormattedMessage();
        assertThat(actual).describedAs("Should use initial param value").isEqualTo("Test message abc");
    }
}
