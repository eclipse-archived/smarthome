/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    public JSONApartmentImpl(JsonObject jObject) {
        if (jObject.get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES.getKey()) instanceof JsonArray) {
            JsonArray zones = (JsonArray) jObject.get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES.getKey());
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
