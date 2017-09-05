/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.optimize.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;

import com.eclipsesource.jaxrs.publisher.ResourceFilter;

/**
 * Activator
 *
 * It registers a {@link ResourceFilter} in order to prevent the JAX-RS implementation to
 * enforce starting all services once they are registered.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class Activator implements BundleActivator {

    private ServiceRegistration<?> resourceFilterRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        registerResourceFilter(context);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        unregisterResourceFilter();
    }

    private void registerResourceFilter(BundleContext context) throws InvalidSyntaxException {
        resourceFilterRegistration = context.registerService(ResourceFilter.class.getName(), new ResourceFilterImpl(),
                null);
    }

    private void unregisterResourceFilter() {
        if (resourceFilterRegistration != null) {
            resourceFilterRegistration.unregister();
            resourceFilterRegistration = null;
        }
    }

}
