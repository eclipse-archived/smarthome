/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.dto.ChannelDTO;
import org.eclipse.smarthome.core.thing.dto.ThingDTO;

/**
 * This is a data transfer object that is used to serialize things with dynamic data like the status.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Kai Kreuzer - Removed links and items
 *
 */
public class EnrichedThingDTO extends ThingDTO {

    public ThingStatusInfo statusInfo;
    // public List<EnrichedChannelDTO> channels;

    public EnrichedThingDTO(ThingDTO thingDTO, ThingStatusInfo statusInfo, Map<String, Set<String>> linkedItemsMap) {
        this.UID = thingDTO.UID;
        if (thingDTO.label != null) {
            this.label = thingDTO.label;
        }
        this.thingTypeUID = thingDTO.thingTypeUID;
        this.bridgeUID = thingDTO.bridgeUID;
        this.channels = new ArrayList<>();
        for (ChannelDTO channel : thingDTO.channels) {
            Set<String> linkedItems = linkedItemsMap != null ? linkedItemsMap.get(channel.id) : new HashSet<String>();
            this.channels.add(new EnrichedChannelDTO(channel, linkedItems));
        }
        this.configuration = thingDTO.configuration;
        this.properties = thingDTO.properties;
        this.statusInfo = statusInfo;
        if (thingDTO.location != null) {
            this.location = thingDTO.location;
        }
    }

}
