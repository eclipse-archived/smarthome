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

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link ChannelDTOMapper} is an utility class to map channels into channel data transfer objects (DTOs).
 *
 * @author Stefan Bußweiler - Initial contribution
 */
public class ChannelDTOMapper {

    /**
     * Maps channel into channel DTO object.
     *
     * @param channel the channel
     * @return the channel DTO object
     */
    public static ChannelDTO map(Channel channel) {
        List<String> linkedItemNames = new ArrayList<>();
        for (Item item : channel.getLinkedItems()) {
            linkedItemNames.add(item.getName());
        }
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        String channelTypeUIDValue = channelTypeUID != null ? channelTypeUID.toString() : null;
        return new ChannelDTO(channel.getUID(), channelTypeUIDValue, channel.getAcceptedItemType().toString(),
                channel.getLabel(), channel.getDescription(), linkedItemNames, channel.getProperties(),
                channel.getConfiguration());
    }
}
