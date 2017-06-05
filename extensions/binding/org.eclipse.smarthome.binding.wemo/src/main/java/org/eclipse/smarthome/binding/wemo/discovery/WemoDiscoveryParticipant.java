/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.discovery;

import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.wemo.WemoBindingConstants;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
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
        return WemoBindingConstants.SUPPORTED_THING_TYPES;
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(2);
            String label = "WeMo Device";
            try {
                label = device.getDetails().getFriendlyName();
            } catch (Exception e) {
                // ignore and use default label
            }
            properties.put(UDN, device.getIdentity().getUdn().getIdentifierString());

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
                    .withRepresentationProperty(UDN).build();

            logger.debug("Created a DiscoveryResult for device '{}' with UDN '{}'",
                    device.getDetails().getFriendlyName(), device.getIdentity().getUdn().getIdentifierString());

            return result;
        } else {
            return null;
        }
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        if (device != null) {
            if (device.getDetails().getManufacturerDetails().getManufacturer() != null) {
                if (device.getDetails().getManufacturerDetails().getManufacturer().toUpperCase().contains("BELKIN")) {
                    if (device.getDetails().getModelDetails().getModelName() != null) {
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase().startsWith("socket")) {
                            logger.debug("Discovered a WeMo Socket thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_SOCKET, device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase().startsWith("insight")) {
                            logger.debug("Discovered a WeMo Insight thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_INSIGHT,
                                    device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase()
                                .startsWith("lightswitch")) {
                            logger.debug("Discovered a WeMo Lightswitch thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_LIGHTSWITCH,
                                    device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase().startsWith("motion")) {
                            logger.debug("Discovered a WeMo Motion thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_MOTION, device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase().startsWith("sensor")) {
                            logger.debug("Discovered a WeMo Motion thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_MOTION, device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase().startsWith("bridge")) {
                            logger.debug("Discovered a WeMo Bridge thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_BRIDGE, device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase().startsWith("maker")) {
                            logger.debug("Discovered a WeMo Maker thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_MAKER, device.getIdentity().getUdn().getIdentifierString());
                        }
                        if (device.getDetails().getModelDetails().getModelName().toLowerCase().startsWith("coffee")) {
                            logger.debug("Discovered a WeMo Coffe Maker thing with UDN '{}'",
                                    device.getIdentity().getUdn().getIdentifierString());
                            return new ThingUID(THING_TYPE_COFFEE, device.getIdentity().getUdn().getIdentifierString());
                        }
                    }
                }
            }
        }
        return null;
    }

}