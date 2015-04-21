/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.discovery;

import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.UDN;
import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.WEMO_INSIGHT_TYPE_UID;
import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.WEMO_LIGHTSWITCH_TYPE_UID;
import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.WEMO_MOTION_TYPE_UID;
import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.WEMO_SOCKET_TYPE_UID;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.wemo.handler.WemoHandler;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WemoDiscoveryParticipant} is responsible for discovering new and
 * removed Wemo devices. It uses the central {@link UpnpDiscoveryService}.
 *
 * @author Hans-Jörg Merk - Initial contribution
 * @author Kai Kreuzer - some refactoring for performance and simplification
 *
 */
public class WemoDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(WemoDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return WemoHandler.SUPPORTED_THING_TYPES;
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            String label = "WeMo Device";
            try {
                label = "WeMo " + device.getDetails().getModelDetails().getModelName();
            } catch (Exception e) {
            }

            Map<String, Object> properties = new HashMap<>(4);
            properties.put("label", label);
            properties.put(UDN, device.getIdentity().getUdn().getIdentifierString());

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
                    .build();
            return result;
        } else {
            return null;
        }
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        if (details != null) {
            ModelDetails modelDetails = details.getModelDetails();
            if (modelDetails != null) {
                String modelName = modelDetails.getModelName();
                if (modelName != null) {
                    if (modelName.startsWith("Socket")) {
                        logger.debug("Discovered a WeMo Socket thing with serialNumber '{}'", device.getDetails()
                                .getSerialNumber());
                        return new ThingUID(WEMO_SOCKET_TYPE_UID, device.getDetails().getSerialNumber());
                    }
                    if (modelName.startsWith("Insight")) {
                        logger.debug("Discovered a WeMo Insight thing with serialNumber '{}'", device.getDetails()
                                .getSerialNumber());
                        return new ThingUID(WEMO_INSIGHT_TYPE_UID, device.getDetails().getSerialNumber());
                    }
                    if (modelName.startsWith("LightSwitch")) {
                        logger.debug("Discovered a WeMo LightSwitch thing with serialNumber '{}'", device.getDetails()
                                .getSerialNumber());
                        return new ThingUID(WEMO_LIGHTSWITCH_TYPE_UID, device.getDetails().getSerialNumber());
                    }
                    if (modelName.startsWith("Motion")) {
                        logger.debug("Discovered a WeMo Motion thing with serialNumber '{}'", device.getDetails()
                                .getSerialNumber());
                        return new ThingUID(WEMO_MOTION_TYPE_UID, device.getDetails().getSerialNumber());
                    }
                }
            }
        }
        return null;
    }
}
