/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.storage.json.internal;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.smarthome.config.core.Configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

/**
 * Deserializes a {@link Configuration} object.
 *
 * As opposed to Gson's default behavior, it ensures that all numbers are represented as {@link BigDecimal}s.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public class ConfigurationDeserializer implements JsonDeserializer<Configuration> {

    @Override
    public Configuration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        Configuration configuration = new Configuration();
        JsonObject configurationObject = json.getAsJsonObject();
        if (configurationObject.get("properties") != null) {
            JsonObject propertiesObject = configurationObject.get("properties").getAsJsonObject();
            for (Entry<String, JsonElement> entry : propertiesObject.entrySet()) {
                JsonElement value = entry.getValue();
                String key = entry.getKey();
                if (value.isJsonPrimitive()) {
                    JsonPrimitive primitive = value.getAsJsonPrimitive();
                    configuration.put(key, deserialize(primitive));
                } else if (value.isJsonArray()) {
                    JsonArray array = value.getAsJsonArray();
                    configuration.put(key, deserialize(array));
                } else {
                    throw new IllegalArgumentException(
                            "Configuration parameters must be primitives or arrays of primities only but was " + value);
                }
            }
        }
        return configuration;
    }

    private Object deserialize(JsonPrimitive primitive) {
        if (primitive.isString()) {
            return primitive.getAsString();
        } else if (primitive.isNumber()) {
            return primitive.getAsBigDecimal();
        } else if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        } else {
            throw new IllegalArgumentException("Unsupported primitive: " + primitive);
        }
    }

    private Object deserialize(JsonArray array) {
        List<Object> list = new LinkedList<>();
        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();
                list.add(deserialize(primitive));
            } else {
                throw new IllegalArgumentException("Multiples must only contain primitives but was " + element);
            }
        }
        return list;
    }

}
