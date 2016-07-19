/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.sceneEvent;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link JSONEventImpl} is the implementation of the {@link Event}.
 *
 * @author Alexander Betker
 */
public class JSONEventImpl implements Event {

    private List<EventItem> eventItemList;

    /**
     * Creates a new {@link JSONEventImpl} from the given digitalSTROM-Event {@link JSONArray}.
     *
     * @param jsonEventArray
     */
    public JSONEventImpl(JsonArray jsonEventArray) {
        this.eventItemList = new LinkedList<EventItem>();
        for (int i = 0; i < jsonEventArray.size(); i++) {
            if (jsonEventArray.get(i) instanceof JsonObject) {
                this.eventItemList.add(new JSONEventItemImpl((JsonObject) jsonEventArray.get(i)));
            }
        }
    }

    @Override
    public List<EventItem> getEventItems() {
        return eventItemList;
    }
}
