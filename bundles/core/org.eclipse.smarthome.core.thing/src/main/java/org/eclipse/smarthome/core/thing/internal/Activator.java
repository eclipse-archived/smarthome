/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

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
