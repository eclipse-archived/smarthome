/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.module;

import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.module.factory.BasicModuleHandlerFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activator class for item based automation modules
 * 
 * @author Benedikt Niehues
 *
 */
public class Activator implements BundleActivator {

    private final Logger logger = LoggerFactory.getLogger(Activator.class);
    private BundleContext context;
    private BasicModuleHandlerFactory moduleHandlerFactory;
    private ServiceRegistration factoryRegistration;

    public BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
     * BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception {
        this.context = bundleContext;
        this.moduleHandlerFactory = new BasicModuleHandlerFactory(context);
        this.factoryRegistration = bundleContext.registerService(ModuleHandlerFactory.class.getName(),
                this.moduleHandlerFactory, null);
        logger.debug("started bundle automation.module");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        this.context = null;
        this.moduleHandlerFactory.dispose();
        if (this.factoryRegistration != null) {
            this.factoryRegistration.unregister();
        }
        this.moduleHandlerFactory = null;

    }

}
