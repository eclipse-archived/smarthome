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
package org.eclipse.smarthome.binding.hue.internal.discovery;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.THING_TYPE_BRIDGE;
import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_SERIAL_NUMBER;

import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.handler.HueBridgeHandlerConfig;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link HueBridgeDiscoveryParticipant} is responsible for discovering hue bridges via
 * UPnP. Incoming devices are checked for their model name to start with "Philips hue bridge".
 * This matches the old (round) and new (rectangle) hue bridge.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Thomas Höfer - Added representation
 */
@NonNullByDefault
@Component(service = UpnpDiscoveryParticipant.class, immediate = true)
public class HueBridgeDiscoveryParticipant implements UpnpDiscoveryParticipant {
    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_BRIDGE);
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid == null) {
            return null;
        }
        URL descriptorURL = device.getIdentity().getDescriptorURL();
        // Either use the serial number or the upnp UDN. The UDN would
        // be more unique but for the sake of backwards compatibility use
        // the serial number for now.
        String udn = device.getDetails().getSerialNumber(); // device.getIdentity().getUdn().getIdentifierString();

        // Friendly name is like "name (host)"
        String name = device.getDetails().getFriendlyName();
        // Cut out the pure name
        if (name.indexOf('(') - 1 > 0) {
            name = name.substring(0, name.indexOf('(') - 1);
        }
        // Add host+port
        String hostAndPort = descriptorURL.getHost();
        if (descriptorURL.getPort() != -1) {
            hostAndPort += ":" + String.valueOf(descriptorURL.getPort());
        }
        name = name + " (" + hostAndPort + ")";

        Map<String, Object> properties = new TreeMap<>();

        properties.put(HueBridgeHandlerConfig.HOST, hostAndPort);
        properties.put(PROPERTY_SERIAL_NUMBER, udn);

        return DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(name).withTTL(MIN_MAX_AGE_SECS)
                .withRepresentationProperty(PROPERTY_SERIAL_NUMBER).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        if (details != null) {
            ModelDetails modelDetails = details.getModelDetails();
            if (modelDetails != null) {
                String modelName = modelDetails.getModelName();
                if (modelName != null) {
                    if (modelName.startsWith("Philips hue bridge")) {
                        return new ThingUID(THING_TYPE_BRIDGE, details.getSerialNumber());
                    }
                }
            }
        }
        return null;
    }

}
