/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.timer.internal;

import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.module.timer.factory.TimerModuleHandlerFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activator class for trigger based automation modules
 *
 * @author Christoph Knauf - initial contribution
 *
 */
public class Activator implements BundleActivator {

    private final Logger logger = LoggerFactory.getLogger(Activator.class);
    private TimerModuleHandlerFactory moduleHandlerFactory;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration factoryRegistration;

    /*
     * (non-Javadoc)
     *
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
     * BundleContext)
     */
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        this.moduleHandlerFactory = new TimerModuleHandlerFactory();
        this.moduleHandlerFactory.activate(bundleContext);
        this.factoryRegistration = bundleContext.registerService(ModuleHandlerFactory.class.getName(),
                this.moduleHandlerFactory, null);
        logger.debug("started bundle timer.module");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        this.moduleHandlerFactory.dispose();
        if (this.factoryRegistration != null) {
            this.factoryRegistration.unregister();
        }
        this.moduleHandlerFactory = null;

    }

}
