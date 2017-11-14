/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.internal;

import java.util.Locale;

import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of the default OSGi bundle activator
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Markus Rathgeb - Add locale provider support
 * @author Thomas Höfer - Changed service tracker constructor usage for locale provider tracker
 */
public class RESTActivator implements BundleActivator {

    private final Logger logger = LoggerFactory.getLogger(RESTActivator.class);

    private static BundleContext context;

    private static LocaleProvider localeProvider;
    private ServiceTracker<LocaleProvider, LocaleProvider> localeProviderTracker;

    private class LocaleProviderServiceTrackerCustomizer
            implements ServiceTrackerCustomizer<LocaleProvider, LocaleProvider> {

        private final BundleContext context;

        public LocaleProviderServiceTrackerCustomizer(final BundleContext context) {
            this.context = context;
        }

        @Override
        public LocaleProvider addingService(ServiceReference<LocaleProvider> reference) {
            localeProvider = context.getService(reference);
            return localeProvider;
        }

        @Override
        public void modifiedService(ServiceReference<LocaleProvider> reference, LocaleProvider service) {
        }

        @Override
        public void removedService(ServiceReference<LocaleProvider> reference, LocaleProvider service) {
            localeProvider = null;
        }

    }

    /**
     * Called whenever the OSGi framework starts our bundle
     */
    @Override
    public void start(BundleContext bc) throws Exception {
        context = bc;
        localeProviderTracker = new ServiceTracker<>(context, LocaleProvider.class.getName(),
                new LocaleProviderServiceTrackerCustomizer(context));
        localeProviderTracker.open();
        logger.debug("REST API has been started.");
    }

    /**
     * Called whenever the OSGi framework stops our bundle
     */
    @Override
    public void stop(BundleContext bc) throws Exception {
        context = null;
        localeProviderTracker.close();
        logger.debug("REST API has been stopped.");
    }

    /**
     * Returns the bundle context of this bundle
     *
     * @return the bundle context
     */
    public static BundleContext getContext() {
        return context;
    }

    /**
     * Returns the default locale.
     *
     * @return the default locale
     */
    public static Locale getLocale() {
        final LocaleProvider provider = localeProvider;
        if (provider != null) {
            return provider.getLocale();
        } else {
            LoggerFactory.getLogger(RESTActivator.class)
                    .error("There should ALWAYS a local provider available, as it is provided by the core.");
            return Locale.US;
        }
    }
}
