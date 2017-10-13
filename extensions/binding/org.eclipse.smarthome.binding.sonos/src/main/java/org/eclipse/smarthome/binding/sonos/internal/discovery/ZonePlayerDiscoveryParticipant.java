/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.sonos.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.sonos.internal.SonosBindingConstants;
import org.eclipse.smarthome.binding.sonos.internal.SonosXMLParser;
import org.eclipse.smarthome.binding.sonos.internal.config.ZonePlayerConfiguration;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZonePlayerDiscoveryParticipant} is responsible processing the
 * results of searches for UPNP devices
 *
 * @author Karel Goderis - Initial contribution
 */
@Component(immediate = true)
public class ZonePlayerDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(ZonePlayerDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SonosBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            String roomName = getSonosRoomName(device);
            if (roomName != null) {
                Map<String, Object> properties = new HashMap<>(3);
                String label = "Sonos device";
                try {
                    label = device.getDetails().getModelDetails().getModelName();
                } catch (Exception e) {
                    // ignore and use default label
                }
                label += " (" + roomName + ")";
                properties.put(ZonePlayerConfiguration.UDN, device.getIdentity().getUdn().getIdentifierString());
                properties.put(SonosBindingConstants.IDENTIFICATION, roomName);

                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
                        .withRepresentationProperty(ZonePlayerConfiguration.UDN).build();

                logger.debug("Created a DiscoveryResult for device '{}' with UDN '{}'",
                        device.getDetails().getFriendlyName(), device.getIdentity().getUdn().getIdentifierString());
                return result;
            }
        }
        return null;
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        if (device != null) {
            if (device.getDetails().getManufacturerDetails().getManufacturer() != null) {
                if (device.getDetails().getManufacturerDetails().getManufacturer().toUpperCase().contains("SONOS")) {

                    String modelName = getModelName(device);
                    if (modelName.equals("ZP80")) {
                        modelName = "PLAY3";
                    } else if (modelName.equals("ZP100")) {
                        modelName = "PLAY5";
                    }
                    ThingTypeUID thingUID = new ThingTypeUID(SonosBindingConstants.BINDING_ID, modelName);

                    // In case a new "unknown" Sonos player is discovered a generic ThingTypeUID will be used
                    if (!SonosBindingConstants.SUPPORTED_KNOWN_THING_TYPES_UIDS.contains(thingUID)) {
                        thingUID = SonosBindingConstants.ZONEPLAYER_THING_TYPE_UID;
                    }

                    logger.debug("Discovered a Sonos '{}' thing with UDN '{}'", thingUID,
                            device.getIdentity().getUdn().getIdentifierString());
                    return new ThingUID(thingUID, device.getIdentity().getUdn().getIdentifierString());
                }
            }
        }

        return null;
    }

    private String getModelName(RemoteDevice device) {
        return SonosXMLParser.extractModelName(device.getDetails().getModelDetails().getModelName());
    }

    private String getSonosRoomName(RemoteDevice device) {
        return SonosXMLParser.getRoomName(device.getIdentity().getDescriptorURL().toString());
    }

}
