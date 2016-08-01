/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.dto;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * The {@link DiscoveryResultDTOMapper} is an utility class to map discovery results into discovery result transfer
 * objects.
 */
public class DiscoveryResultDTOMapper {

    /**
     * Maps discovery result into discovery result data transfer object.
     *
     * @param discoveryResult the discovery result
     * @return the discovery result data transfer object
     */
    public static DiscoveryResultDTO map(DiscoveryResult discoveryResult) {
        ThingUID thingUID = discoveryResult.getThingUID();
        ThingUID bridgeUID = discoveryResult.getBridgeUID();

        return new DiscoveryResultDTO(thingUID.toString(), bridgeUID != null ? bridgeUID.toString() : null,
                discoveryResult.getThingTypeUID() != null ? discoveryResult.getThingTypeUID().toString() : null,
                discoveryResult.getLabel(), discoveryResult.getFlag(), discoveryResult.getProperties(),
                discoveryResult.getRepresentationProperty());
    }
}
