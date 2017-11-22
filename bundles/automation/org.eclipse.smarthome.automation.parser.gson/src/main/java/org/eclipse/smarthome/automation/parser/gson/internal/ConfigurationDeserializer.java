/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation.parser.gson.internal;

import java.lang.reflect.Type;
import java.util.Map.Entry;

import org.eclipse.smarthome.config.core.Configuration;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

/**
 * This class create Configuration object from configuration json element.
 *
 * @author Yoradan Mihaylov - initial content
 */
public class ConfigurationDeserializer implements JsonDeserializer<Configuration> {

    @Override
    public Configuration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        Configuration configuration = new Configuration();
        JsonObject jo = (JsonObject) json;
        for (Entry<String, JsonElement> e : jo.entrySet()) {
            JsonPrimitive value = e.getValue().getAsJsonPrimitive();
            if (value.isString()) {
                configuration.put(e.getKey(), value.getAsString());
            } else if (value.isNumber()) {
                configuration.put(e.getKey(), value.getAsBigDecimal());
            } else if (value.isBoolean()) {
                configuration.put(e.getKey(), value.getAsBoolean());
            }
        }
        return configuration;
    }

}
