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
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Deserializes the internal data structure of the {@link JsonStorage})
 *
 * The contained entities remain json objects and won't me deserialized to their corresponding types at this point.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public class OuterMapDeserializer implements JsonDeserializer<Map<String, Object>> {

    /**
     *
     * Finds out whether the given object is the outer JSON storage map or not.
     *
     * It must be
     * <li>a Map of Maps
     * <li>with 2 entries each
     * <li>with {@link JsonStorage#CLASS} and {@link JsonStorage#VALUE} being their keys
     *
     * @param obj the object to be analyzed
     * @return {@code true} if it is the outer storage map
     */
    private boolean isOuterMap(JsonObject obj) {
        for (Map.Entry<String, JsonElement> me : obj.entrySet()) {
            JsonElement v = me.getValue();
            if (!v.isJsonObject()) {
                return false;
            }
            Set<Entry<String, JsonElement>> entrySet = ((JsonObject) v).entrySet();
            if (entrySet.size() != 2) {
                return false;
            }
            Set<String> keys = entrySet.stream().map(e -> e.getKey()).collect(Collectors.toSet());
            if (!keys.contains(JsonStorage.CLASS) || !keys.contains(JsonStorage.VALUE)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Map<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        if (!isOuterMap(obj)) {
            throw new IllegalArgumentException("Object {} is not an outer map: " + obj);
        }
        return readOuterMap(obj, context);
    }

    private Map<String, Object> readOuterMap(JsonObject obj, JsonDeserializationContext context) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Map.Entry<String, JsonElement> me : obj.entrySet()) {
            String key = me.getKey();
            JsonObject value = me.getValue().getAsJsonObject();
            Map<String, Object> innerMap = new HashMap<>();
            innerMap.put(JsonStorage.CLASS, value.get(JsonStorage.CLASS).getAsString());
            innerMap.put(JsonStorage.VALUE, value.get(JsonStorage.VALUE));
            map.put(key, innerMap);
        }
        return map;
    }

}
