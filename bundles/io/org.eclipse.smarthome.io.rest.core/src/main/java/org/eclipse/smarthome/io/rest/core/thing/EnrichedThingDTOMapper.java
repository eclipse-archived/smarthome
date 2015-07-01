/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing;

import java.net.URI;

import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.dto.ThingDTO;
import org.eclipse.smarthome.core.thing.dto.ThingDTOMapper;
import org.eclipse.smarthome.io.rest.core.item.EnrichedGroupItemDTO;
import org.eclipse.smarthome.io.rest.core.item.EnrichedItemDTOMapper;

/**
 * The {@link EnrichedThingDTOMapper} is an utility class to map things into enriched thing data transfer objects
 * (DTOs).
 */
public class EnrichedThingDTOMapper extends ThingDTOMapper {

    /**
     * Maps thing into enriched thing data transfer object.
     * 
     * @param thing the thing
     * @param uri the uri
     * @return the enriched thing DTO object
     */
    public static EnrichedThingDTO map(Thing thing, URI uri) {

        ThingDTO thingDTO = ThingDTOMapper.map(thing);

        GroupItem groupItem = thing.getLinkedItem();
        EnrichedGroupItemDTO groupItemDTO = groupItem != null ? (EnrichedGroupItemDTO) EnrichedItemDTOMapper.map(
                groupItem, true, uri) : null;

        return new EnrichedThingDTO(thingDTO, thing.getStatusInfo(), groupItemDTO);
    }
}
