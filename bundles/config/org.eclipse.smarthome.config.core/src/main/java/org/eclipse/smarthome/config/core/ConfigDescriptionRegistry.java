/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.service.component.ComponentContext;

/**
 * {@link ConfigDescriptionRegistry} provides access to
 * {@link ConfigDescription}s. It tracks {@link ConfigDescriptionProvider} OSGi
 * services to collect all {@link ConfigDescription}s.
 * 
 * @see ConfigDescriptionProvider
 * @author Dennis Nobel - Initial contribution
 * 
 */
public class ConfigDescriptionRegistry {

    private final Map<String, ConfigDescription> configDescriptions = new ConcurrentHashMap<>();

    private final List<ConfigDescriptionProvider> configDescriptionProviders = new CopyOnWriteArrayList<>();

    private final ConfigDescriptionListener configDescriptionListener = new ConfigDescriptionListener() {
        @Override
        public void configDescriptionAdded(ConfigDescription configDescription) {
            configDescriptions.put(configDescription.getURI(), configDescription);
        }

        @Override
        public void configDescriptionRemoved(ConfigDescription configDescription) {
            configDescriptions.remove(configDescription.getURI());
        }
    };

    protected void addConfigDescriptionProvider(ConfigDescriptionProvider configDescriptionProvider) {
        configDescriptionProviders.add(configDescriptionProvider);
        configDescriptionProvider.addConfigDescriptionListener(configDescriptionListener);
    }

    protected void removeConfigDescriptionProvider(
            ConfigDescriptionProvider configDescriptionProvider) {
        configDescriptionProvider.removeConfigDescriptionListener(configDescriptionListener);
        configDescriptionProviders.remove(configDescriptionProvider);
    }

    protected void deactivate(ComponentContext componentContext) {
        for (ConfigDescriptionProvider configDescriptionProvider : configDescriptionProviders) {
            configDescriptionProvider.removeConfigDescriptionListener(configDescriptionListener);
        }
        configDescriptionProviders.clear();
    }



    /**
     * Returns all config descriptions.
     * 
     * @return all config descriptions or an empty collection if config
     *         description exists
     */
    public Collection<ConfigDescription> getConfigDescriptions() {
        return configDescriptions.values();
    }

    /**
     * Returns a config description for a given name.
     * 
     * @param name
     *            name, which must not be null
     * @return config description or null if no config description exists for
     *         the given name
     */
    public ConfigDescription getConfigDescription(String name) {
        return configDescriptions.get(name);
    }

}
