/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
