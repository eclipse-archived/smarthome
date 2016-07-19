/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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

/**
 * This is a data transfer object that is used to serialize things.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Thomas Höfer - Added thing and thing type properties
 * @author Stefan Bußweiler - Added new thing status handling
 * @author Simon Kaufmann - Added label
 *
 */
public class ThingDTO {

    public String label;
    public String bridgeUID;
    public Map<String, Object> configuration;
    public Map<String, String> properties;
    public String UID;
    public String thingTypeUID;
    public List<ChannelDTO> channels;
    public String location;

    public ThingDTO() {
    }

    public ThingDTO(String thingTypeUID, String UID, String label, String bridgeUID, List<ChannelDTO> channels,
            Configuration configuration, Map<String, String> properties, String location) {
        this.thingTypeUID = thingTypeUID;
        this.UID = UID;
        this.label = label;
        this.bridgeUID = bridgeUID;
        this.channels = channels;
        this.configuration = toMap(configuration);
        this.properties = properties;
        this.location = location;
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
