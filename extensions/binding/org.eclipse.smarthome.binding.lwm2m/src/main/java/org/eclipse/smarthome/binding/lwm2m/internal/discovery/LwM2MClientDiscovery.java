/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lwm2m.internal.discovery;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.binding.lwm2m.Lwm2mBindingConstants;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.lwm2m.LwM2MServerService;
import org.eclipse.smarthome.io.transport.lwm2m.api.ClientRegistryObserver;
import org.eclipse.smarthome.io.transport.lwm2m.api.LwM2MClient;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

@Component(service = AbstractDiscoveryService.class, immediate = true, configurationPolicy = ConfigurationPolicy.OPTIONAL, name = "binding.lwm2m.discovery")
public class LwM2MClientDiscovery extends AbstractDiscoveryService implements ClientRegistryObserver {
    public LwM2MClientDiscovery() {
        super(0);
    }

    @Override
    protected void startScan() {
    }

    @Reference
    public void setLwM2MServerService(LwM2MServerService service) {
        Collection<LwM2MClient> clients = service.getClients();
        for (LwM2MClient client : clients) {
            clientRegistered(client);
        }
    }

    @Override
    public void clientRegistered(LwM2MClient client) {
        Map<String, Object> properties = new HashMap<>(3);
        ThingUID uid = new ThingUID(Lwm2mBindingConstants.THING_TYPE_BRIDGE, client.getEndpoint());
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withLabel(client.getAddress().getHostAddress()).build();
        thingDiscovered(discoveryResult);
    }

    @Override
    public void clientUnRegistered(LwM2MClient client) {
        ThingUID thingUID = new ThingUID(Lwm2mBindingConstants.THING_TYPE_BRIDGE, client.getEndpoint());
        thingRemoved(thingUID);
    }
}