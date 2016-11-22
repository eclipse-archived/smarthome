/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.gson.internal;

import java.lang.reflect.Type;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * This class serializes elements of Configuration object into json as configuration object (not as
 * configuration.properties object).
 *
 * @author Yordan Mihaylov - initial content
 */
public class ConfigurationSerializer implements JsonSerializer<Configuration> {

    @Override
    public JsonElement serialize(Configuration src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = null;
        if (src != null) {
            Set<String> kyes = src.keySet();
            result = new JsonObject();
            if (kyes.size() > 0) {
                for (String propName : kyes) {
                    Object value = src.get(propName);
                    if (value instanceof String) {
                        result.addProperty(propName, (String) value);
                    } else if (value instanceof Number) {
                        result.addProperty(propName, (Number) value);
                    } else if (value instanceof Boolean) {
                        result.addProperty(propName, (Boolean) value);
                    }
                }
            }
        }
        return result;
    }

}
