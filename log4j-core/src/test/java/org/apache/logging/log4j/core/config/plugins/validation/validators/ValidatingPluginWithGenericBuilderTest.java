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
package org.apache.logging.log4j.core.config.plugins.validation.validators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.apache.logging.log4j.core.config.NullConfiguration;
import org.apache.logging.log4j.core.config.plugins.util.PluginBuilder;
import org.apache.logging.log4j.plugins.Node;
import org.apache.logging.log4j.plugins.util.PluginManager;
import org.apache.logging.log4j.plugins.util.PluginType;
import org.apache.logging.log4j.plugins.validation.ValidatingPluginWithGenericBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ValidatingPluginWithGenericBuilderTest {

    private PluginType<ValidatingPluginWithGenericBuilder> plugin;
    private Node node;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() throws Exception {
        final PluginManager manager = new PluginManager("Test");
        manager.collectPlugins();
        plugin = (PluginType<ValidatingPluginWithGenericBuilder>) manager.getPluginType("ValidatingPluginWithGenericBuilder");
        assertThat(plugin).describedAs("Rebuild this module to make sure annotation processing kicks in.").isNotNull();
        node = new Node(null, "Validator", plugin);
    }

    @Test
    public void testNullDefaultValue() throws Exception {
        final ValidatingPluginWithGenericBuilder validatingPlugin = (ValidatingPluginWithGenericBuilder) new PluginBuilder(plugin)
            .setConfiguration(new NullConfiguration())
            .setConfigurationNode(node)
            .build();
        assertThat(validatingPlugin).isNull();
    }

    @Test
    public void testNonNullValue() throws Exception {
        node.getAttributes().put("name", "foo");
        final ValidatingPluginWithGenericBuilder validatingPlugin = (ValidatingPluginWithGenericBuilder) new PluginBuilder(plugin)
            .setConfiguration(new NullConfiguration())
            .setConfigurationNode(node)
            .build();
        assertThat(validatingPlugin).isNotNull();
        assertThat(validatingPlugin.getName()).isEqualTo("foo");
    }
}
