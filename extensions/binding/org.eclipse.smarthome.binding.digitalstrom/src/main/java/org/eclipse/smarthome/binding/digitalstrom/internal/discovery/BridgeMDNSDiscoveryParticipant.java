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
package org.eclipse.smarthome.binding.digitalstrom.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.config.Config;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverconnection.DsAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverconnection.impl.DsAPIImpl;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BridgeMDNSDiscoveryParticipant} is responsible for discovering digitalSTROM-Server. It uses the central
 * {@link MDNSDiscoveryService}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
@Component(service = MDNSDiscoveryParticipant.class, immediate = true)
public class BridgeMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(BridgeMDNSDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(DigitalSTROMBindingConstants.THING_TYPE_DSS_BRIDGE);
    }

    @Override
    public String getServiceType() {
        return "_tcp.local.";
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {
        if (service.getApplication().contains("dssweb")) {
            ThingUID uid = getThingUID(service);

            if (uid != null) {
                String hostAddress = service.getName() + "." + service.getDomain() + ".";
                Map<String, Object> properties = new HashMap<>(2);
                properties.put(DigitalSTROMBindingConstants.HOST, hostAddress);
                return DiscoveryResultBuilder.create(uid).withProperties(properties)
                        .withRepresentationProperty(uid.getId()).withLabel("digitalSTROM-Server").build();
            }
        }
        return null;
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        if (service.getApplication().contains("dssweb")) {
            String hostAddress = service.getName() + "." + service.getDomain() + ".";
            DsAPI digitalSTROMClient = new DsAPIImpl(hostAddress, Config.DEFAULT_CONNECTION_TIMEOUT,
                    Config.DEFAULT_READ_TIMEOUT, true);
            Map<String, String> dsidMap = digitalSTROMClient.getDSID(null);
            String dSID = null;
            if (dsidMap != null) {
                dSID = dsidMap.get(JSONApiResponseKeysEnum.DSID.getKey());
            }
            if (StringUtils.isNotBlank(dSID)) {
                return new ThingUID(DigitalSTROMBindingConstants.THING_TYPE_DSS_BRIDGE, dSID);
            } else {
                logger.error("Can't get server dSID to generate thing UID. Please add the server manually.");
            }
        }
        return null;
    }
}
