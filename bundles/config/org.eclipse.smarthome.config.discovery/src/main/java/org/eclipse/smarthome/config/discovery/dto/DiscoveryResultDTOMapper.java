/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.dto;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * The {@link DiscoveryResultDTOMapper} is an utility class to map discovery results into discovery result transfer
 * objects.
 *
 * @author Stefan Bussweiler - Initial contribution
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

    /**
     * Maps discovery result data transfer object into discovery result.
     *
     * @param discoveryResultDTO the discovery result data transfer object
     * @return the discovery result
     */
    public static DiscoveryResult map(DiscoveryResultDTO discoveryResultDTO) {
        ThingUID thingUID = new ThingUID(discoveryResultDTO.thingUID);
        ThingTypeUID thingTypeUID = discoveryResultDTO.thingTypeUID != null
                ? new ThingTypeUID(discoveryResultDTO.thingTypeUID) : null;
        ThingUID bridgeUID = discoveryResultDTO.bridgeUID != null ? new ThingUID(discoveryResultDTO.bridgeUID) : null;

        return DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID).withBridge(bridgeUID)
                .withLabel(discoveryResultDTO.label)
                .withRepresentationProperty(discoveryResultDTO.representationProperty)
                .withProperties(discoveryResultDTO.properties).build();
    }
}
