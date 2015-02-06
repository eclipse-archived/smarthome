/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console.internal;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Extension of the default OSGi bundle activator
 */
public final class ConsoleActivator implements BundleActivator {

    public static ServiceTracker<ItemRegistry, ItemRegistry> itemRegistryTracker;
    public static ServiceTracker<EventPublisher, EventPublisher> eventPublisherTracker;

    /**
     * Called whenever the OSGi framework starts our bundle
     */
    @Override
    public void start(BundleContext bc) throws Exception {
        itemRegistryTracker = new ServiceTracker<ItemRegistry, ItemRegistry>(bc, ItemRegistry.class.getName(), null);
        itemRegistryTracker.open();

        eventPublisherTracker = new ServiceTracker<EventPublisher, EventPublisher>(bc, EventPublisher.class.getName(),
                null);
        eventPublisherTracker.open();
    }

    /**
     * Called whenever the OSGi framework stops our bundle
     */
    @Override
    public void stop(BundleContext bc) throws Exception {
        itemRegistryTracker.close();
        eventPublisherTracker.close();
    }
}
