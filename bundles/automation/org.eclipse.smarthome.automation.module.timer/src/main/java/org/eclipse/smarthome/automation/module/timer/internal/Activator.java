/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        this.moduleHandlerFactory = new TimerModuleHandlerFactory();
        this.factoryRegistration = bundleContext.registerService(ModuleHandlerFactory.class.getName(),
                this.moduleHandlerFactory, null);
        logger.debug("started bundle timer.module");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        this.moduleHandlerFactory.deactivate();
        if (this.factoryRegistration != null) {
            this.factoryRegistration.unregister();
        }
        this.moduleHandlerFactory = null;

    }

}
