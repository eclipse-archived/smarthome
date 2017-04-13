/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.storage.json;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

/**
 * This StringObjectMapDeserializer deserializes a Map with String -> Object and replaces all numbers with BigDecimals.
 * WARNING: Objects MUST NOT be more complex than Java primitives and Strings.
 *
 * @author Stefan Triller - Initial Contribution
 */
public class StringObjectMapDeserializer implements JsonDeserializer<Map<String, Object>> {

    @Override
    public Map<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        Map<String, Object> map = new HashMap<String, Object>();

        JsonObject obj = json.getAsJsonObject();

        for (Map.Entry<String, JsonElement> me : obj.entrySet()) {
            String k = me.getKey();
            JsonElement v = me.getValue();

            if (v.isJsonPrimitive() && ((JsonPrimitive) v).isNumber()) {
                map.put(k, v.getAsBigDecimal());
            } else {
                Object value = context.deserialize(v, Object.class);
                map.put(k, value);
            }
        }
        return map;
    }

}
