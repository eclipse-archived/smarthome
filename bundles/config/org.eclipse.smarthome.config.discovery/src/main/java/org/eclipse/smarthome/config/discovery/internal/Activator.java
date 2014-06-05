/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.internal;

import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.config.discovery.inbox.Inbox;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;



//TODO: Initialize the code in an own thread since it takes some.
/**
 * @author Michael Grammling - Initial Contribution (Draft API)
 * @author Oliver Libutzki
 */
public class Activator implements BundleActivator {


    private DiscoveryServiceRegistryImpl discoveryServiceRegistry;
    private ServiceRegistration discoveryServiceRegistryReg;

    private PersistentInbox inbox;
    private ServiceRegistration inboxReg;


    @Override
    public synchronized void start(BundleContext context) throws Exception {

        // initialize DiscoveryServiceRegistry
        this.discoveryServiceRegistry = new DiscoveryServiceRegistryImpl(context);

        // initialize and register Inbox
        this.inbox = new PersistentInbox(this.discoveryServiceRegistry);
        this.inboxReg = context.registerService(Inbox.class.getName(), this.inbox, null);

        // register DiscoveryServiceRegistry
        this.discoveryServiceRegistry.open();

        this.discoveryServiceRegistryReg = context.registerService(
                DiscoveryServiceRegistry.class.getName(), this.discoveryServiceRegistry, null);

    }

    @Override
    public synchronized void stop(BundleContext context) throws Exception {
        // unregister DiscoveryServiceRegistry
        this.discoveryServiceRegistryReg.unregister();
        this.discoveryServiceRegistry.release();

        // unregister Inbox
        this.inboxReg.unregister();
        this.inbox.release();
    }


}
