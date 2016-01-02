/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.internal;

import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.module.script.ScriptEngineProvider;
import org.eclipse.smarthome.automation.module.script.ScriptExtensionProvider;
import org.eclipse.smarthome.automation.module.script.ScriptScopeProvider;
import org.eclipse.smarthome.automation.module.script.internal.factory.ScriptModuleHandlerFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ScriptModuleActivator class for script automation modules
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Simon Merschjohann - original code from openHAB 1
 */
public class ScriptModuleActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(ScriptModuleActivator.class);
    private BundleContext context;
    private ScriptModuleHandlerFactory moduleHandlerFactory;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration factoryRegistration;
    @SuppressWarnings("rawtypes")
    private ServiceTracker scriptScopeProviderServiceTracker;

    public BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
     * BundleContext)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        this.context = bundleContext;
        this.moduleHandlerFactory = new ScriptModuleHandlerFactory(context);
        moduleHandlerFactory.activate();
        this.factoryRegistration = bundleContext.registerService(ModuleHandlerFactory.class.getName(),
                this.moduleHandlerFactory, null);

        Filter filter = bundleContext.createFilter("(|(objectClass=" + ScriptScopeProvider.class.getName()
                + ")(objectClass=" + ScriptExtensionProvider.class.getName() + "))");

        scriptScopeProviderServiceTracker = new ServiceTracker(bundleContext, filter, new ServiceTrackerCustomizer() {

            @Override
            public Object addingService(ServiceReference reference) {
                Object service = bundleContext.getService(reference);
                if (service instanceof ScriptScopeProvider) {
                    ScriptScopeProvider provider = (ScriptScopeProvider) service;

                    ScriptEngineProvider.addScopeProvider(provider);
                    return service;
                } else if (service instanceof ScriptExtensionProvider) {
                    ScriptExtensionProvider provider = (ScriptExtensionProvider) service;

                    ScriptExtensionManager.addScriptExtensionProvider(provider);
                    return service;
                } else {
                    return null;
                }
            }

            @Override
            public void modifiedService(ServiceReference reference, Object service) {
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                if (service instanceof ScriptScopeProvider) {
                    ScriptScopeProvider provider = (ScriptScopeProvider) service;
                    ScriptEngineProvider.removeScopeProvider(provider);
                } else if (service instanceof ScriptExtensionProvider) {
                    ScriptExtensionProvider provider = (ScriptExtensionProvider) service;

                    ScriptExtensionManager.removeScriptExtensionProvider(provider);
                }
            }
        });

        scriptScopeProviderServiceTracker.open();

        logger.debug("Started script automation support");
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
        this.moduleHandlerFactory.dispose();
        if (this.factoryRegistration != null) {
            this.factoryRegistration.unregister();
        }
        this.moduleHandlerFactory = null;
        this.scriptScopeProviderServiceTracker.close();
        ScriptEngineProvider.clearProviders();
    }

}
