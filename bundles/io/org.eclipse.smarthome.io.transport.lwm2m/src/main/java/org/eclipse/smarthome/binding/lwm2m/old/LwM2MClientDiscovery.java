/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lwm2m.old;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.server.client.ClientRegistryListener;
import org.eclipse.leshan.server.client.ClientUpdate;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class LwM2MClientDiscovery extends AbstractDiscoveryService implements ClientRegistryListener {
    private ServiceRegistration<?> reg = null;

    public LwM2MClientDiscovery() {
        super(0);
    }

    public void stop() {
        if (reg != null) {
            reg.unregister();
        }
        reg = null;
    }

    @Override
    protected void startScan() {
    }

    public void start(BundleContext bundleContext, Collection<Client> clients) {
        if (reg != null) {
            return;
        }
        reg = bundleContext.registerService(DiscoveryService.class.getName(), this, new Hashtable<String, Object>());
        for (Client client : clients) {
            registered(client);
        }

        createTestBridge();
    }

    @Override
    public void registered(Client client) {
        Map<String, Object> properties = new HashMap<>(3);
        ThingUID uid = new ThingUID(lwm2mLeshanBindingConstants.BRIDGE_TYPE, client.getEndpoint());
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withLabel(client.getAddress().getHostAddress()).build();
        thingDiscovered(discoveryResult);
    }

    @Override
    public void updated(ClientUpdate update, Client clientUpdated) {
    }

    @Override
    public void unregistered(Client client) {
        ThingUID thingUID = new ThingUID(lwm2mLeshanBindingConstants.BRIDGE_TYPE, client.getEndpoint());
        thingRemoved(thingUID);
    }
}