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
package org.apache.logging.log4j.layout.template.json.resolver;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.layout.template.json.JsonTemplateLayout;
import org.apache.logging.log4j.layout.template.json.util.JsonWriter;
import org.apache.logging.log4j.layout.template.json.util.Uris;

import java.util.Objects;

final class TemplateUriResolver implements EventResolver {

    private final TemplateResolver<LogEvent> resolver;

    TemplateUriResolver(
            final EventResolverContext context,
            final TemplateResolverConfig config) {
        String template = Uris.readUri(
                Objects.requireNonNull(config.getString("uri"), "The 'uri' field is required"),
                context.getCharset());
        resolver = TemplateResolvers.ofTemplate(
                // Additional fields must be stripped to prevent repeating them inside nested templates.
                // Consider additional config parameters to add additional fields in nested templates.
                EventResolverContext.newBuilder()
                        .from(context)
                        .setEventTemplateAdditionalFields(new JsonTemplateLayout.EventTemplateAdditionalField[0])
                        .build(),
                template);
    }

    static String getName() {
        return "templateUri";
    }

    @Override
    public void resolve(final LogEvent logEvent, final JsonWriter jsonWriter) {
        resolver.resolve(logEvent, jsonWriter);
    }

}
