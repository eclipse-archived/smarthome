/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.model.rule.ui.internal;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class RuleModelUIActivator extends RuleActivator {

    public static ServiceTracker<ItemRegistry, ItemRegistry> itemRegistryTracker;

    /**
     * Called whenever the OSGi framework starts our bundle
     */
    @Override
    public void start(BundleContext bc) throws Exception {
        super.start(bc);
        itemRegistryTracker = new ServiceTracker<ItemRegistry, ItemRegistry>(bc, ItemRegistry.class, null);
        itemRegistryTracker.open();
    }

    /**
     * Called whenever the OSGi framework stops our bundle
     */
    @Override
    public void stop(BundleContext bc) throws Exception {
        super.stop(bc);
        itemRegistryTracker.close();
    }

}
