/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.util.ThingHelper;

/**
 * The {@link ThingDTOMapper} is an utility class to map things into data transfer objects (DTO).
 *
 * @author Stefan Bußweiler - Initial contribution
 * @author Kai Kreuzer - Added DTO to Thing mapping
 */
public class ThingDTOMapper {

    /**
     * Maps thing into thing data transfer object (DTO).
     *
     * @param thing the thing
     * @return the thing DTO object
     */
    public static ThingDTO map(Thing thing) {
        List<ChannelDTO> channelDTOs = new ArrayList<>();
        for (Channel channel : thing.getChannels()) {
            ChannelDTO channelDTO = ChannelDTOMapper.map(channel);
            channelDTOs.add(channelDTO);
        }

        String thingTypeUID = thing.getThingTypeUID().getAsString();
        String thingUID = thing.getUID().toString();
        final ThingUID bridgeUID = thing.getBridgeUID();

        return new ThingDTO(thingTypeUID, thingUID, thing.getLabel(), bridgeUID != null ? bridgeUID.toString() : null,
                channelDTOs, toMap(thing.getConfiguration()), thing.getProperties(), thing.getLocation());
    }

    /**
     * Maps thing DTO into thing
     *
     * @param thingDTO the thingDTO
     * @return the corresponding thing
     */
    public static Thing map(ThingDTO thingDTO) {
        ThingUID thingUID = new ThingUID(thingDTO.UID);
        ThingTypeUID thingTypeUID = thingDTO.thingTypeUID == null ? new ThingTypeUID("")
                : new ThingTypeUID(thingDTO.thingTypeUID);
        Thing thing = ThingBuilder.create(thingTypeUID, thingUID).build();
        return ThingHelper.merge(thing, thingDTO);
    }

    private static Map<String, Object> toMap(Configuration configuration) {
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
