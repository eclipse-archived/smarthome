/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.designer.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class UIActivator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.smarthome.designer.ui"; //$NON-NLS-1$

    // The shared instance
    private static UIActivator plugin;

    public static ServiceTracker<ItemRegistry, ItemRegistry> itemRegistryTracker;

    /**
     * The constructor
     */
    public UIActivator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        itemRegistryTracker = new ServiceTracker<ItemRegistry, ItemRegistry>(context, ItemRegistry.class, null);
        itemRegistryTracker.open();
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        itemRegistryTracker.close();
        itemRegistryTracker = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static UIActivator getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
