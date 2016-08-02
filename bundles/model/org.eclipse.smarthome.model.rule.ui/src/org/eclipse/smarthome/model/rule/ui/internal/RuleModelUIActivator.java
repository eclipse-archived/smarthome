/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.rule.ui.internal;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class RuleModelUIActivator extends RulesActivator {

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
