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
package org.eclipse.smarthome.binding.tradfri.internal.discovery;

import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.*;
import static org.eclipse.smarthome.core.thing.Thing.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class identifies Tradfri gateways by their mDNS service information.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@Component(service = MDNSDiscoveryParticipant.class, immediate = true)
public class TradfriDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(TradfriDiscoveryParticipant.class);

    private static final String SERVICE_TYPE = "_coap._udp.local.";

    /**
     * RegEx patter to match the gateway name announced by mDNS
     * Possible values:
     * gw:001122334455, gw-001122334455, gw:00-11-22-33-44-55, gw-001122334455ServiceName
     *
     */
    private final Pattern GATEWAY_NAME_REGEX_PATTERN = Pattern.compile("(gw[:-]{1}([a-f0-9]{2}[-]?){6}){1}");

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(GATEWAY_TYPE_UID);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        if (service != null) {
            Matcher m = GATEWAY_NAME_REGEX_PATTERN.matcher(service.getName());
            if (m.find()) {
                return new ThingUID(GATEWAY_TYPE_UID, m.group(1).replaceAll("[^A-Za-z0-9_]", ""));
            }
        }
        return null;
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {
        ThingUID thingUID = getThingUID(service);
        if (thingUID != null) {
            if (service.getHostAddresses() != null && service.getHostAddresses().length > 0
                    && !service.getHostAddresses()[0].isEmpty()) {
                logger.debug("Discovered Tradfri gateway: {}", service);
                Map<String, Object> properties = new HashMap<>(4);
                properties.put(PROPERTY_VENDOR, "IKEA of Sweden");
                properties.put(GATEWAY_CONFIG_HOST, service.getHostAddresses()[0]);
                properties.put(GATEWAY_CONFIG_PORT, service.getPort());
                properties.put(PROPERTY_SERIAL_NUMBER, service.getName());
                String fwVersion = service.getPropertyString("version");
                if (fwVersion != null) {
                    properties.put(PROPERTY_FIRMWARE_VERSION, fwVersion);
                }
                return DiscoveryResultBuilder.create(thingUID).withProperties(properties).withLabel("TRÅDFRI Gateway")
                        .withRepresentationProperty(GATEWAY_CONFIG_HOST).build();
            } else {
                logger.warn("Discovered Tradfri gateway doesn't have an IP address: {}", service);
            }
        }
        return null;
    }
}
