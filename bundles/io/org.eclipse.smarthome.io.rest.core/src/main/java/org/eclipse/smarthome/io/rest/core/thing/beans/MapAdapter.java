/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * This is a helper class, which allows better serialization for generic maps.
 * 
 * @author Dennis Nobel - Initial contribution
 */
public class MapAdapter extends XmlAdapter<MapAdapter.AdaptedMap, Map<String, Object>> {

    public static class AdaptedMap {
        public List<Entry> property = new ArrayList<Entry>();
    }

    public static class Entry {
        public String key;
        public String value;
    }

    @Override
    public AdaptedMap marshal(Map<String, Object> map) throws Exception {
        AdaptedMap adaptedMap = new AdaptedMap();
        for (Map.Entry<String, Object> mapEntry : map.entrySet()) {
            Entry entry = new Entry();
            entry.key = mapEntry.getKey();
            entry.value = String.valueOf(mapEntry.getValue());
            adaptedMap.property.add(entry);
        }
        return adaptedMap;
    }

    @Override
    public Map<String, Object> unmarshal(AdaptedMap adaptedMap) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Entry entry : adaptedMap.property) {
            map.put(entry.key, entry.value);
        }
        return map;
    }

}
