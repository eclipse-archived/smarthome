/**
 * Copyright (c) 2015-2016 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal;

import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.factory.ScriptedModuleHandlerFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ScriptModuleActivator class for script automation modules
 *
 * @author Simon Merschjohann - initial contribution
 */
public class RuleSupportActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(RuleSupportActivator.class);
    private BundleContext context;
    private static ScriptedModuleHandlerFactory moduleHandlerFactory;

    public static ScriptedModuleHandlerFactory getModuleHandlerFactory() {
        return moduleHandlerFactory;
    }

    @SuppressWarnings("rawtypes")
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
    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        this.context = bundleContext;
        RuleSupportActivator.moduleHandlerFactory = new ScriptedModuleHandlerFactory(context);
        this.factoryRegistration = bundleContext.registerService(ModuleHandlerFactory.class.getName(),
                RuleSupportActivator.moduleHandlerFactory, null);

        logger.debug("Started script extension: RuleSupport");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        this.context = null;
        RuleSupportActivator.moduleHandlerFactory.dispose();
        if (this.factoryRegistration != null) {
            this.factoryRegistration.unregister();
        }
        RuleSupportActivator.moduleHandlerFactory = null;
    }

}
