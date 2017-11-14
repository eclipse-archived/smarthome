/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
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
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The {@link BundleActivator} for the config.core bundle.
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class Activator implements BundleActivator {

    private static Bundle bundle;
    private static BundleContext bundleContext;
    private static ConfigDescriptionRegistry configDescriptionRegistry;
    private static TranslationProvider i18nProvider;

    private ServiceBinder configDescriptionRegistryServiceBinder;
    private ServiceBinder i18nProviderServiceBinder;

    @Override
    public void start(BundleContext context) throws Exception {
        Activator.bundleContext = context;
        Activator.bundle = context.getBundle();

        configDescriptionRegistryServiceBinder = new ServiceBinder(context, new ConfigDescriptionRegistryBinder());
        configDescriptionRegistryServiceBinder.open();

        i18nProviderServiceBinder = new ServiceBinder(context, new TranslationProviderBinder());
        i18nProviderServiceBinder.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        Activator.bundleContext = null;
        Activator.bundle = null;

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
     * @return the bundle
     *
     * @throws IllegalStateException if bundle is not available
     */
    public static final Bundle getBundle() {
        if (bundle == null) {
            throw new IllegalStateException("There is no bundle");
        }
        return bundle;
    }

    /**
     * @return the config description registry or null if config description registry is not available
     **/
    public static final ConfigDescriptionRegistry getConfigDescriptionRegistry() {
        return configDescriptionRegistry;
    }

    /**
     * @return the i18nProvider or null if i18nProvider is not available
     **/
    public static final TranslationProvider getTranslationProvider() {
        return i18nProvider;
    }

    public static final class ConfigDescriptionRegistryBinder {
        @Bind
        @Unbind
        public void bindConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
            Activator.configDescriptionRegistry = configDescriptionRegistry;
        }
    }

    public static final class TranslationProviderBinder {
        @Bind
        @Unbind
        public void bindTranslationProvider(TranslationProvider i18nProvider) {
            Activator.i18nProvider = i18nProvider;
        }
    }
}
