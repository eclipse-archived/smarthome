/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing;

import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.dto.ThingDTO;
import org.eclipse.smarthome.io.rest.core.item.EnrichedGroupItemDTO;

/**
 * This is a data transfer object that is used to serialize things with dynamic data like the status.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
public class EnrichedThingDTO extends ThingDTO {

    public ThingStatusInfo statusInfo;
    public EnrichedGroupItemDTO item;

    public EnrichedThingDTO(ThingDTO thingDTO, ThingStatusInfo statusInfo, EnrichedGroupItemDTO item) {
        this.UID = thingDTO.UID;
        this.bridgeUID = thingDTO.bridgeUID;
        this.channels = thingDTO.channels;
        this.configuration = thingDTO.configuration;
        this.properties = thingDTO.properties;
        this.statusInfo = statusInfo;
        this.item = item;
    }

}
