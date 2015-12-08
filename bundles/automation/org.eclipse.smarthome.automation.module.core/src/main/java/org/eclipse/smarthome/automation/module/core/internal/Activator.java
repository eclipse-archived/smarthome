/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.core.internal;

import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.module.core.factory.BasicModuleHandlerFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activator class for item based automation modules
 *
 * @author Benedikt Niehues - Initial contribution and API
 *
 */
public class Activator implements BundleActivator {

    private final Logger logger = LoggerFactory.getLogger(Activator.class);
    private BundleContext context;
    private BasicModuleHandlerFactory moduleHandlerFactory;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration factoryRegistration;

    public BundleContext getContext() {
        return context;
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        this.context = bundleContext;
        this.moduleHandlerFactory = new BasicModuleHandlerFactory(context);
        this.factoryRegistration = bundleContext.registerService(ModuleHandlerFactory.class.getName(),
                this.moduleHandlerFactory, null);
        logger.debug("started bundle automation.module");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        this.context = null;
        this.moduleHandlerFactory.dispose();
        if (this.factoryRegistration != null) {
            this.factoryRegistration.unregister();
        }
        this.moduleHandlerFactory = null;

    }

}
