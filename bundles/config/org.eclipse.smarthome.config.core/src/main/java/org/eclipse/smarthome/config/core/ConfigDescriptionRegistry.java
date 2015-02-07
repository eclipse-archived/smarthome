/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@link ConfigDescriptionRegistry} provides access to {@link ConfigDescription}s.
 * It tracks {@link ConfigDescriptionProvider} OSGi services to collect all {@link ConfigDescription}s.
 *
 * @see ConfigDescriptionProvider
 *
 * @author Dennis Nobel - Initial contribution, added locale support
 * @author Michael Grammling - Initial contribution
 */
public class ConfigDescriptionRegistry {

    private final List<ConfigDescriptionProvider> configDescriptionProviders = new CopyOnWriteArrayList<>();

    protected void addConfigDescriptionProvider(ConfigDescriptionProvider configDescriptionProvider) {

        if (configDescriptionProvider != null) {
            configDescriptionProviders.add(configDescriptionProvider);
        }
    }

    protected void removeConfigDescriptionProvider(ConfigDescriptionProvider configDescriptionProvider) {

        if (configDescriptionProvider != null) {
            configDescriptionProviders.remove(configDescriptionProvider);
        }
    }

    /**
     * Returns all config descriptions.
     *
     * @param locale
     *            locale
     * @return all config descriptions or an empty collection if no config
     *         description exists
     */
    public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
        Collection<ConfigDescription> configDescriptions = new ArrayList<>(10);
        for (ConfigDescriptionProvider configDescriptionProvider : this.configDescriptionProviders) {
            configDescriptions.addAll(configDescriptionProvider.getConfigDescriptions(locale));
        }
        return Collections.unmodifiableCollection(configDescriptions);
    }

    /**
     * Returns all config descriptions.
     *
     * @return all config descriptions or an empty collection if no config
     *         description exists
     */
    public Collection<ConfigDescription> getConfigDescriptions() {
        return getConfigDescriptions(null);
    }

    /**
     * Returns a config description for a given URI.
     *
     * @param uri
     *            the URI to which the config description to be returned (must
     *            not be null)
     * @param locale
     *            locale
     * @return config description or null if no config description exists for
     *         the given name
     */
    public ConfigDescription getConfigDescription(URI uri, Locale locale) {
        for (ConfigDescriptionProvider configDescriptionProvider : this.configDescriptionProviders) {
            ConfigDescription configDescription = configDescriptionProvider.getConfigDescription(uri, locale);

            if (configDescription != null) {
                return configDescription;
            }
        }
        return null;
    }

    /**
     * Returns a config description for a given URI.
     *
     * @param uri
     *            the URI to which the config description to be returned (must
     *            not be null)
     * @return config description or null if no config description exists for
     *         the given name
     */
    public ConfigDescription getConfigDescription(URI uri) {
        return getConfigDescription(uri, null);
    }

}
