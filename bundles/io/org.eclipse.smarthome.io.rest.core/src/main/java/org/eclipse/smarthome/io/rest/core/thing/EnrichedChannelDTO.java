/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.smarthome.core.thing.dto.ChannelDTO;

/**
 * This is a data transfer object that is used to serialize channels with dynamic data like linked items.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
public class EnrichedChannelDTO extends ChannelDTO {

    final public Set<String> linkedItems;

    public EnrichedChannelDTO(ChannelDTO channelDTO, Set<String> linkedItems) {
        this.uid = channelDTO.uid;
        this.id = channelDTO.id;
        this.channelTypeUID = channelDTO.channelTypeUID;
        this.itemType = channelDTO.itemType;
        this.kind = channelDTO.kind;
        this.label = channelDTO.label;
        this.description = channelDTO.description;
        this.properties = channelDTO.properties;
        this.configuration = channelDTO.configuration;
        this.defaultTags = channelDTO.defaultTags;
        this.linkedItems = linkedItems != null ? new HashSet<>(linkedItems) : new HashSet<String>();
    }
}
