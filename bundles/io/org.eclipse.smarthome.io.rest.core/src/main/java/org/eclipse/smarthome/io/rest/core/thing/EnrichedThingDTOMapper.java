/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing;

import java.net.URI;
import java.util.Locale;

import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.dto.ThingDTO;
import org.eclipse.smarthome.core.thing.dto.ThingDTOMapper;
import org.eclipse.smarthome.io.rest.core.item.EnrichedGroupItemDTO;
import org.eclipse.smarthome.io.rest.core.item.EnrichedItemDTO;
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
    public static EnrichedThingDTO map(Thing thing, URI uri, Locale locale) {

        ThingDTO thingDTO = ThingDTOMapper.map(thing);

        GroupItem groupItem = thing.getLinkedItem();
        EnrichedItemDTO groupItemDTO = groupItem != null ? EnrichedItemDTOMapper.map(groupItem, true, uri, locale)
                : null;

        String link = null != uri ? uri.toASCIIString() + ThingResource.PATH_THINGS + "/" + thingDTO.UID : null;

        return new EnrichedThingDTO(thingDTO, thing.getStatusInfo(), (EnrichedGroupItemDTO) groupItemDTO, link);
    }
}
