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
package org.apache.logging.log4j.core.appender.rolling;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.junit.Assert;
import org.junit.Test;

public class RollingFileAppenderLayoutTest {

    @Test
    public void testDefaultLayout() throws Exception {
        // @formatter:off
        assertThat(RollingFileAppender.newBuilder()
                .setName(RollingFileAppenderLayoutTest.class.getName())
                .setConfiguration(new DefaultConfiguration())
                .setFileName("log.txt")
                .setFilePattern("FilePattern")
                .setPolicy(OnStartupTriggeringPolicy.createPolicy(1))
                .setCreateOnDemand(true) // no need to clutter up test folder with another file
                .build().getLayout()).isNotNull();
        // @formatter:on
    }
}
