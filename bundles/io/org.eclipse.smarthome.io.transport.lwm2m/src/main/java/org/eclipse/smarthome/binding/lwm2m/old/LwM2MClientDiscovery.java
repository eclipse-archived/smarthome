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
import java.util.Iterator;
import java.util.Map;

import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;
import org.eclipse.leshan.server.registration.RegistrationUpdate;
import org.osgi.framework.BundleContext;

public class LwM2MClientDiscovery implements RegistrationListener {
    public LwM2MClientDiscovery() {

    }

    public void start(BundleContext bundleContext, Iterator<Registration> clients) {
        while (clients.hasNext()) {
            registered(clients.next());
        }
    }

    @Override
    public void registered(Registration registration) {
        Map<String, Object> properties = new HashMap<>(3);
        // ThingUID uid = new ThingUID(lwm2mLeshanBindingConstants.BRIDGE_TYPE, registration.getEndpoint());
        // DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withProperties(properties)
        // .withLabel(client.getAddress().getHostAddress()).build();
        // thingDiscovered(discoveryResult);
    }

    @Override
    public void updated(RegistrationUpdate update, Registration updatedRegistration,
            Registration previousRegistration) {
        // TODO Auto-generated method stub

    }

    @Override
    public void unregistered(Registration registration, Collection<Observation> observations, boolean expired) {
        // ThingUID thingUID = new ThingUID(lwm2mLeshanBindingConstants.BRIDGE_TYPE, registration.getEndpoint());
        // thingRemoved(thingUID);
    }
}