package org.eclipse.smarthome.notification.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

    private static BundleContext context;

    @Override
    public void start(BundleContext context) throws Exception {
        Activator.context = context;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        Activator.context = null;
    }

    /**
     * Returns the bundle context of this bundle
     *
     * @return the bundle context
     */
    public static BundleContext getContext() {
        return Activator.context;
    }
}
