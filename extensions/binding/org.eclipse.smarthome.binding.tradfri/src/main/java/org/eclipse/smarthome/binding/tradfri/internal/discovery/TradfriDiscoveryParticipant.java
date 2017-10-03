/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.binding.tradfri.GatewayConfig;
import org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mdns.discovery.MDNSDiscoveryParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class identifies Tradfri gateways by their mDNS service information.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class TradfriDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(TradfriDiscoveryParticipant.class);

    private static final String SERVICE_TYPE = "_coap._udp.local.";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(TradfriBindingConstants.GATEWAY_TYPE_UID);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        if (service != null) {
            String name = service.getName();
            if ((service.getType() != null) && service.getType().equals(getServiceType())
                    && (name.matches("gw:([a-f0-9]{2}[-]?){6}"))) {
                return new ThingUID(TradfriBindingConstants.GATEWAY_TYPE_UID, name.replaceAll("[^A-Za-z0-9_]", ""));
            }
        }
        return null;
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {

        if (service.getHostAddresses() != null && service.getHostAddresses().length > 0
                && !service.getHostAddresses()[0].isEmpty()) {
            String ip = service.getHostAddresses()[0];

            ThingUID thingUID = getThingUID(service);
            if (thingUID != null) {
                logger.debug("Discovered Tradfri gateway: {}", service);
                Map<String, Object> properties = new HashMap<>(4);
                properties.put(Thing.PROPERTY_VENDOR, "IKEA of Sweden");
                properties.put(GatewayConfig.HOST, ip);
                properties.put(GatewayConfig.PORT, service.getPort());
                String fwVersion = service.getPropertyString("version");
                if (fwVersion != null) {
                    properties.put(Thing.PROPERTY_FIRMWARE_VERSION, fwVersion);
                }
                return DiscoveryResultBuilder.create(thingUID).withProperties(properties).withLabel("TRÅDFRI Gateway")
                        .withRepresentationProperty(GatewayConfig.HOST).build();
            }
        }
        return null;
    }
}
