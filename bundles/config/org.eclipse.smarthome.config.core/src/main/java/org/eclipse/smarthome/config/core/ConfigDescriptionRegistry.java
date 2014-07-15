/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ConfigDescriptionRegistry} provides access to {@link ConfigDescription}s.
 * It tracks {@link ConfigDescriptionProvider} OSGi services to collect all
 * {@link ConfigDescription}s.
 * 
 * @see ConfigDescriptionProvider
 * 
 * @author Dennis Nobel - Initial contribution
 * @author Michael Grammling - Initial contribution
 */
public class ConfigDescriptionRegistry {
    
    private Logger logger = LoggerFactory.getLogger(ConfigDescriptionRegistry.class.getName());

    private final Map<ConfigDescriptionProvider, Collection<ConfigDescription>> configDescriptionMap = new ConcurrentHashMap<>();

    private final ConfigDescriptionsChangeListener configDescriptionsChangeListener =
            new ConfigDescriptionsChangeListener() {
                    @Override
                    public void configDescriptionAdded(ConfigDescriptionProvider configDescriptionProvider, ConfigDescription configDescription) {
                        Collection<ConfigDescription> descriptions = configDescriptionMap.get(configDescriptionProvider);
                        if (descriptions != null) {
                            descriptions.add(configDescription);
                        }
                    }
            
                    @Override
                    public void configDescriptionRemoved(ConfigDescriptionProvider configDescriptionProvider, ConfigDescription configDescription) {
                        Collection<ConfigDescription> descriptions = configDescriptionMap.get(configDescriptionProvider);
                        if (descriptions != null) {
                            descriptions.remove(configDescription);
                        }
                    }
    };

    protected void addConfigDescriptionProvider(ConfigDescriptionProvider configDescriptionProvider) {
        if (!configDescriptionMap.containsKey(configDescriptionProvider)) {
            CopyOnWriteArrayList<ConfigDescription> descriptions = new CopyOnWriteArrayList<>(configDescriptionProvider.getConfigDescriptions());
            configDescriptionProvider.addConfigDescriptionsChangeListener(configDescriptionsChangeListener);
            configDescriptionMap.put(configDescriptionProvider, descriptions);
            logger.debug("Config description provider '{}' has been added.", configDescriptionProvider.getClass().getName());
        }
    }

    protected void removeConfigDescriptionProvider(ConfigDescriptionProvider configDescriptionProvider) {
        if(configDescriptionMap.remove(configDescriptionProvider) != null) {
            configDescriptionProvider.removeConfigDescriptionsChangeListener(configDescriptionsChangeListener);
            logger.debug("Config description provider '{}' has been removed.", configDescriptionProvider.getClass().getName());
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        for (ConfigDescriptionProvider configDescriptionProvider : configDescriptionMap.keySet()) {
            configDescriptionProvider.removeConfigDescriptionsChangeListener(configDescriptionsChangeListener);
        }
        configDescriptionMap.clear();
    }

    /**
     * Returns all config descriptions.
     * 
     * @return all config descriptions or an empty collection if no config description exists
     */
    public Collection<ConfigDescription> getConfigDescriptions() {
        Collection<ConfigDescription> result = new ArrayList<>();
        for (Collection<ConfigDescription> descriptions : configDescriptionMap.values()) {
            result.addAll(descriptions);
        }
        return result;
    }

    /**
     * Returns a config description for a given URI.
     * 
     * @param uri the URI to which the config description to be returned (must not be null)
     * @return config description or null if no config description exists for the given name
     */
    public ConfigDescription getConfigDescription(URI uri) {
        for (Collection<ConfigDescription> descriptions : configDescriptionMap.values()) {
            for (ConfigDescription description : descriptions) {
                if (description.getURI().equals(uri)) {
                    return description;
                }
            }
        }
        return null;
    }

}
