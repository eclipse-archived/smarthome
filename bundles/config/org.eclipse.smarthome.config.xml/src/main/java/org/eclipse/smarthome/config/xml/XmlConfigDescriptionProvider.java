/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.core.ConfigDescriptionsChangeListener;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The {@link XmlConfigDescriptionProvider} is a concrete implementation of the
 * {@link ConfigDescriptionProvider} service interface.
 * <p>
 * This implementation manages any {@link ConfigDescription} objects associated to specific
 * modules. If a specific module disappears, any registered {@link ConfigDescription} objects
 * associated with that module are released.
 * 
 * @author Michael Grammling - Initial Contribution
 */
public class XmlConfigDescriptionProvider implements ConfigDescriptionProvider {

    private Logger logger = LoggerFactory.getLogger(XmlConfigDescriptionProvider.class);

    private Map<Bundle, List<ConfigDescription>> bundleConfigDescriptionsMap;
    private List<ConfigDescriptionsChangeListener> configDescriptionListeners;


    public XmlConfigDescriptionProvider() {
        this.bundleConfigDescriptionsMap = new HashMap<>(10);
        this.configDescriptionListeners = new CopyOnWriteArrayList<>();
    }

    private List<ConfigDescription> acquireConfigDescriptions(Bundle bundle) {
        if (bundle != null) {
            List<ConfigDescription> configDescriptions =
                    this.bundleConfigDescriptionsMap.get(bundle);

            if (configDescriptions == null) {
                configDescriptions = new ArrayList<ConfigDescription>(10);

                this.bundleConfigDescriptionsMap.put(bundle, configDescriptions);
            }

            return configDescriptions;
        }

        return null;
    }

    /**
     * Adds a {@link ConfigDescription} object to the internal list associated
     * with the specified module.
     * <p>
     * The added {@link ConfigDescription} object leads to an event.
     * <p>
     * This method returns silently, if any of the parameters is {@code null}.
     * 
     * @param bundle the module to which the config description to be added
     * @param configDescription the config description to be added
     */
    public synchronized void addConfigDescription(
            Bundle bundle, ConfigDescription configDescription) {

        if (configDescription != null) {
            List<ConfigDescription> configDescriptionList = acquireConfigDescriptions(bundle);
    
            if (configDescriptionList != null) {
                sendConfigDescriptionEvent(configDescription, true);
                configDescriptionList.add(configDescription);
            }
        }
    }

    /**
     * Adds a list of {@link ConfigDescription} objects to the internal list associated
     * with the specified module.
     * <p>
     * Any added {@link ConfigDescription} object leads to a separate event.
     * <p>
     * This method returns silently, if any of the parameters is {@code null} or empty. 
     * 
     * @param bundle the module to which the list of config descriptions to be added
     * @param configDescriptions the list of config descriptions to be added
     */
    public synchronized void addConfigDescriptions(
            Bundle bundle, List<ConfigDescription> configDescriptions) {

        if ((configDescriptions != null) && (configDescriptions.size() > 0)) {
            List<ConfigDescription> currentConfigDescriptionList = acquireConfigDescriptions(bundle);
    
            if (currentConfigDescriptionList != null) {
                for (ConfigDescription configDescription : configDescriptions) {
                    sendConfigDescriptionEvent(configDescription, true);
                    currentConfigDescriptionList.add(configDescription);
                }
            }
        }
    }

    /**
     * Removes all {@link ConfigDescription} objects from the internal list associated
     * with the specified module.
     * <p>
     * Any removed {@link ConfigDescription} object leads to a separate event.
     * <p>
     * This method returns silently if the module is {@code null}.
     * 
     * @param bundle the module for which all associated config descriptions to be removed
     */
    public synchronized void removeAllConfigDescriptions(Bundle bundle) {
        if (bundle != null) {
            List<ConfigDescription> configDescriptions =
                    this.bundleConfigDescriptionsMap.get(bundle);

            if (configDescriptions != null) {
                for (ConfigDescription configDescription : configDescriptions) {
                    sendConfigDescriptionEvent(configDescription, false);
                }

                this.bundleConfigDescriptionsMap.remove(bundle);
            }
        }
    }

    @Override
    public synchronized void addConfigDescriptionsChangeListener(ConfigDescriptionsChangeListener listener) {
        if ((listener != null) && (!this.configDescriptionListeners.contains(listener))) {
            this.configDescriptionListeners.add(listener);
        }
    }

    @Override
    public synchronized void removeConfigDescriptionsChangeListener(ConfigDescriptionsChangeListener listener) {
        if (listener != null) {
            this.configDescriptionListeners.remove(listener);
        }
    }

    @Override
    public synchronized Collection<ConfigDescription> getConfigDescriptions() {
        List<ConfigDescription> allConfigDescriptions = new ArrayList<>();

        Collection<List<ConfigDescription>> configDescriptions =
                this.bundleConfigDescriptionsMap.values();

        if (configDescriptions != null) {
            for (List<ConfigDescription> configDescription : configDescriptions) {
                allConfigDescriptions.addAll(configDescription);
            }
        }

        return allConfigDescriptions;
    }

    private void sendConfigDescriptionEvent(ConfigDescription configDescription, boolean added) {
        for (ConfigDescriptionsChangeListener listener : this.configDescriptionListeners) {
            try {
                if (added) {
                    listener.configDescriptionAdded(this, configDescription);
                } else {
                    listener.configDescriptionRemoved(this, configDescription);
                }
            } catch (Exception ex) {
                this.logger.error("Could not send an " + ((added) ? "added" : "removed")
                        + " ConfigDescription event to the listener '" + listener + "'!", ex);
            }
        }
    }

}
