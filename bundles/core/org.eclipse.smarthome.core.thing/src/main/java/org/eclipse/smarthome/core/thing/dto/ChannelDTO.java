/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ChannelUID;

/**
 * This is a data transfer object that is used to serialize channels.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Chris Jackson - Added properties and configuration
 */
public class ChannelDTO {

    public List<String> linkedItems;
    public String uid;
    public String id;
    public String channelTypeUID;
    public String itemType;
    public String label;
    public String description;
    public Map<String, String> properties;
    public Map<String, Object> configuration;

    public ChannelDTO() {
    }

    public ChannelDTO(ChannelUID uid, String channelTypeUID, String itemType, String label, String description,
            List<String> linkedItems, Map<String, String> properties, Configuration configuration) {
        this.uid = uid.toString();
        this.id = uid.getId();
        this.channelTypeUID = channelTypeUID;
        this.itemType = itemType;
        this.label = label;
        this.description = description;
        this.linkedItems = linkedItems;
        this.properties = properties;
        this.configuration = toMap(configuration);
    }

    private Map<String, Object> toMap(Configuration configuration) {

        if (configuration == null) {
            return null;
        }

        Map<String, Object> configurationMap = new HashMap<>(configuration.keySet().size());
        for (String key : configuration.keySet()) {
            configurationMap.put(key, configuration.get(key));
        }
        return configurationMap;
    }
}
