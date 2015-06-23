/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;

/**
 * The {@link ThingDTOMapper} is an utility class to map things into data transfer objects (DTO).
 * 
 * @author Stefan Bußweiler - Initial contribution
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

        String thingUID = thing.getUID().toString();
        String bridgeUID = thing.getBridgeUID() != null ? thing.getBridgeUID().toString() : null;

        return new ThingDTO(thingUID, bridgeUID, channelDTOs, thing.getConfiguration(), thing.getProperties());
    }

}
