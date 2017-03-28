/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing;

import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.dto.ThingDTO;
import org.eclipse.smarthome.core.thing.dto.ThingDTOMapper;

/**
 * The {@link EnrichedThingDTOMapper} is an utility class to map things into enriched thing data transfer objects
 * (DTOs).
 */
public class EnrichedThingDTOMapper extends ThingDTOMapper {

    /**
     * Maps thing into enriched thing data transfer object.
     *
     * @param thing the thing
     * @param thingStatusInfo the thing status information to be used for the enriched object
     * @param linkedItemsMap the map of linked items to be injected into the enriched object
     *
     * @return the enriched thing DTO object
     */
    public static EnrichedThingDTO map(Thing thing, ThingStatusInfo thingStatusInfo,
            Map<String, Set<String>> linkedItemsMap) {
        ThingDTO thingDTO = ThingDTOMapper.map(thing);
        return new EnrichedThingDTO(thingDTO, thingStatusInfo, linkedItemsMap);
    }
}
