/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.homematic.internal.discovery;

import static org.eclipse.smarthome.binding.homematic.HomematicBindingConstants.THING_TYPE_BRIDGE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomegearDiscoveryParticipant} is responsible for discovering new Homegear gateways on the network.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@Component(immediate = true)
public class HomegearDiscoveryParticipant implements UpnpDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(HomegearDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_BRIDGE);
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            DeviceDetails details = device.getDetails();
            Map<String, Object> properties = new HashMap<>(3);
            properties.put("gatewayAddress", details.getBaseURL().getHost());

            logger.debug("Discovered a Homegear gateway with serial number '{}'", details.getSerialNumber());
            return DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(details.getModelDetails().getModelNumber()).build();
        }
        return null;
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        String modelName = details.getModelDetails().getModelName();
        if ("HOMEGEAR".equalsIgnoreCase(modelName)) {
            return new ThingUID(THING_TYPE_BRIDGE, details.getSerialNumber());
        }
        return null;
    }
}
