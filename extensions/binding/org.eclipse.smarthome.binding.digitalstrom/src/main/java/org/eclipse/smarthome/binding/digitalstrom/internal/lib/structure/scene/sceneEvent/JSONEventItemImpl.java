/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.sceneEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.EventPropertyEnum;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link JSONEventItemImpl} is the implementation of the {@link EventItem}.
 *
 * @author Alexander Betker
 */
public class JSONEventItemImpl implements EventItem {

    private String name = null;
    private Map<EventPropertyEnum, String> properties = new HashMap<EventPropertyEnum, String>();

    /**
     * Creates a new {@link JSONEventItemImpl} from the given digitalSTROM-Event-Item {@link JsonObject}.
     *
     * @param jsonEventItem
     */
    public JSONEventItemImpl(JsonObject jsonEventItem) {
        name = jsonEventItem.get(JSONApiResponseKeysEnum.EVENT_NAME.getKey()).getAsString();
        addProperty(EventPropertyEnum.EVENT_NAME, name);

        if (jsonEventItem.get(JSONApiResponseKeysEnum.EVENT_PROPERTIES.getKey()) instanceof JsonObject) {
            JsonObject propObj = (JsonObject) jsonEventItem.get(JSONApiResponseKeysEnum.EVENT_PROPERTIES.getKey());
            addProperty(EventPropertyEnum.SCENEID, propObj.get("sceneID").getAsString());
        }
        if (jsonEventItem.get(JSONApiResponseKeysEnum.EVENT_SOURCE.getKey()) instanceof JsonObject) {
            JsonObject sourceObj = (JsonObject) jsonEventItem.get(JSONApiResponseKeysEnum.EVENT_SOURCE.getKey());
            for (Entry<String, JsonElement> entry : sourceObj.entrySet()) {
                if (EventPropertyEnum.containsId(entry.getKey())) {
                    addProperty(EventPropertyEnum.getProperty(entry.getKey()), entry.getValue().getAsString());
                }
            }
        }
    }

    private void addProperty(EventPropertyEnum prop, String value) {
        properties.put(prop, value);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<EventPropertyEnum, String> getProperties() {
        return properties;
    }
}