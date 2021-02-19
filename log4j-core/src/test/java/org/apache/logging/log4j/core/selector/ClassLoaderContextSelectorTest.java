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
package org.apache.logging.log4j.core.selector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.util.ReflectionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClassLoaderContextSelectorTest {

    private static final String PKG = ClassLoaderContextSelectorTest.class.getPackage().getName();

    private ClassLoader loader1, loader2, loader3;

    @BeforeEach
    public void setUp() throws Exception {
        loader1 = new TestClassLoader();
        loader2 = new TestClassLoader();
        loader3 = new TestClassLoader();
        assertThat(loader2).isNotSameAs(loader1);
        assertThat(loader3).isNotSameAs(loader1);
        assertThat(loader3).isNotSameAs(loader2);
    }

    @Test
    public void testMultipleClassLoaders() throws Exception {
        final Class<?> logging1 = loader1.loadClass(PKG + ".a.Logging1");
        final Field field1 = logging1.getDeclaredField("logger");
        final Logger logger1 = (Logger) ReflectionUtil.getStaticFieldValue(field1);
        assertThat(logger1).isNotNull();
        final Class<?> logging2 = loader2.loadClass(PKG + ".b.Logging2");
        final Field field2 = logging2.getDeclaredField("logger");
        final Logger logger2 = (Logger) ReflectionUtil.getStaticFieldValue(field2);
        assertThat(logger2).isNotNull();
        final Class<?> logging3 = loader3.loadClass(PKG + ".c.Logging3");
        final Field field3 = logging3.getDeclaredField("logger");
        final Logger logger3 = (Logger) ReflectionUtil.getStaticFieldValue(field3);
        assertThat(logger3).isNotNull();
        assertThat(logger2.getContext()).isNotSameAs(logger1.getContext());
        assertThat(logger3.getContext()).isNotSameAs(logger1.getContext());
        assertThat(logger3.getContext()).isNotSameAs(logger2.getContext());
    }
}
