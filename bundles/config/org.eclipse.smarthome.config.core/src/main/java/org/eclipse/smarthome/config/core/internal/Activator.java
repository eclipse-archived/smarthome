/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.internal;

import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.core.common.osgi.ServiceBinder;
import org.eclipse.smarthome.core.common.osgi.ServiceBinder.Bind;
import org.eclipse.smarthome.core.common.osgi.ServiceBinder.Unbind;
import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The {@link BundleActivator} for the config.core bundle.
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class Activator implements BundleActivator {

    private static BundleContext bundleContext;
    private static ConfigDescriptionRegistry configDescriptionRegistry;
    private static I18nProvider i18nProvider;

    private ServiceBinder configDescriptionRegistryServiceBinder;
    private ServiceBinder i18nProviderServiceBinder;

    /*
     * (non-Javadoc)
     *
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        Activator.bundleContext = context;

        configDescriptionRegistryServiceBinder = new ServiceBinder(context, new ConfigDescriptionRegistryBinder());
        configDescriptionRegistryServiceBinder.open();

        i18nProviderServiceBinder = new ServiceBinder(context, new I18nProviderBinder());
        i18nProviderServiceBinder.open();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        Activator.bundleContext = null;

        configDescriptionRegistryServiceBinder.close();
        configDescriptionRegistryServiceBinder = null;

        i18nProviderServiceBinder.close();
        i18nProviderServiceBinder = null;
    }

    /**
     * @return the bundle context
     *
     * @throws IllegalStateException if bundle context is not available
     */
    public static final BundleContext getBundleContext() {
        if (bundleContext == null) {
            throw new IllegalStateException("There is no bundle context");
        }
        return bundleContext;
    }

    /**
     * @return the config description registry
     *
     * @throws IllegalStateException if config description registry is not available
     **/
    public static final ConfigDescriptionRegistry getConfigDescriptionRegistry() {
        if (configDescriptionRegistry == null) {
            throw new IllegalStateException("There is no config description registry");
        }
        return configDescriptionRegistry;
    }

    /**
     * @return the i18nProvider
     *
     * @throws IllegalStateException if i18nProvider is not available
     **/
    public static final I18nProvider getI18nProvider() {
        if (i18nProvider == null) {
            throw new IllegalStateException("There is no I18nProivder");
        }
        return i18nProvider;
    }

    public static final class ConfigDescriptionRegistryBinder {
        @Bind
        @Unbind
        public void bindConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
            Activator.configDescriptionRegistry = configDescriptionRegistry;
        }
    }

    public static final class I18nProviderBinder {
        @Bind
        @Unbind
        public void bindI18nProvider(I18nProvider i18nProvider) {
            Activator.i18nProvider = i18nProvider;
        }
    }
}
