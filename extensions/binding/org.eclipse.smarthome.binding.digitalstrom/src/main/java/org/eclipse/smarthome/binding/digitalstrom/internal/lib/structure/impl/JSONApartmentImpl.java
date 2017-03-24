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
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.Apartment;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.Zone;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link JSONApartmentImpl} is the implementation of the {@link Apartment}.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel - change from SimpleJSON to GSON
 * @author Matthias Siegele - change from SimpleJSON to GSON
 */
public class JSONApartmentImpl implements Apartment {

    private Map<Integer, Zone> zoneMap = new HashMap<Integer, Zone>();

    /**
     * Creates a new {@link JSONApartmentImpl} through the {@link JsonObject}.
     *
     * @param jObject of the server response, must not be null
     */
    public JSONApartmentImpl(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.ZONES.getKey()) instanceof JsonArray) {
            JsonArray zones = (JsonArray) jObject.get(JSONApiResponseKeysEnum.ZONES.getKey());
            for (int i = 0; i < zones.size(); i++) {
                if (zones.get(i) instanceof JsonObject) {
                    Zone zone = new JSONZoneImpl((JsonObject) zones.get(i));
                    zoneMap.put(zone.getZoneId(), zone);
                }
            }
        }
    }

    @Override
    public Map<Integer, Zone> getZoneMap() {
        return zoneMap;
    }
}
