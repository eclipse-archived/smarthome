/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.core.i18n.ConfigI18nLocalizationService;
import org.eclipse.smarthome.core.common.osgi.ServiceBinder.Bind;
import org.eclipse.smarthome.core.common.osgi.ServiceBinder.Unbind;
import org.osgi.framework.Bundle;

/**
 * The {@link XmlConfigDescriptionProvider} is a concrete implementation of the {@link ConfigDescriptionProvider}
 * service interface.
 * <p>
 * This implementation manages any {@link ConfigDescription} objects associated to specific modules. If a specific
 * module disappears, any registered {@link ConfigDescription} objects associated with that module are released.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Dennis Nobel - Added locale support
 * @author Alex Tugarev - Extended for pattern and options
 * @author Chris Jackson - Modify to use config parameter builder
 * @author Thomas Höfer - Extended for unit
 * @author Markus Rathgeb - Use ConfigI18nLocalizerService
 */
public class XmlConfigDescriptionProvider implements ConfigDescriptionProvider {

    private Map<Bundle, List<ConfigDescription>> bundleConfigDescriptionsMap;

    private ConfigI18nLocalizationService configI18nLocalizerService;

    public XmlConfigDescriptionProvider() {
        this.bundleConfigDescriptionsMap = new HashMap<>(10);
    }

    private List<ConfigDescription> acquireConfigDescriptions(Bundle bundle) {
        if (bundle != null) {
            List<ConfigDescription> configDescriptions = this.bundleConfigDescriptionsMap.get(bundle);

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
     * @param bundle
     *            the module to which the config description to be added
     * @param configDescription
     *            the config description to be added
     */
    public synchronized void addConfigDescription(Bundle bundle, ConfigDescription configDescription) {
        if (configDescription != null) {
            List<ConfigDescription> configDescriptionList = acquireConfigDescriptions(bundle);

            if (configDescriptionList != null) {
                configDescriptionList.add(configDescription);
            }
        }
    }

    /**
     * Adds a list of {@link ConfigDescription} objects to the internal list
     * associated with the specified module.
     * <p>
     * Any added {@link ConfigDescription} object leads to a separate event.
     * <p>
     * This method returns silently, if any of the parameters is {@code null} or empty.
     *
     * @param bundle
     *            the module to which the list of config descriptions to be
     *            added
     * @param configDescriptions
     *            the list of config descriptions to be added
     */
    public synchronized void addConfigDescriptions(Bundle bundle, List<ConfigDescription> configDescriptions) {

        if ((configDescriptions != null) && (configDescriptions.size() > 0)) {
            List<ConfigDescription> currentConfigDescriptionList = acquireConfigDescriptions(bundle);

            if (currentConfigDescriptionList != null) {
                for (ConfigDescription configDescription : configDescriptions) {
                    currentConfigDescriptionList.add(configDescription);
                }
            }
        }
    }

    /**
     * Removes all {@link ConfigDescription} objects from the internal list
     * associated with the specified module.
     * <p>
     * Any removed {@link ConfigDescription} object leads to a separate event.
     * <p>
     * This method returns silently if the module is {@code null}.
     *
     * @param bundle
     *            the module for which all associated config descriptions to be
     *            removed
     */
    public synchronized void removeAllConfigDescriptions(Bundle bundle) {
        if (bundle != null) {
            List<ConfigDescription> configDescriptions = this.bundleConfigDescriptionsMap.get(bundle);

            if (configDescriptions != null) {
                this.bundleConfigDescriptionsMap.remove(bundle);
            }
        }
    }

    @Override
    public synchronized Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
        List<ConfigDescription> allConfigDescriptions = new ArrayList<>(10);

        Collection<Entry<Bundle, List<ConfigDescription>>> configDescriptionsList = this.bundleConfigDescriptionsMap
                .entrySet();

        if (configDescriptionsList != null) {
            for (Entry<Bundle, List<ConfigDescription>> configDescriptions : configDescriptionsList) {
                for (ConfigDescription configDescription : configDescriptions.getValue()) {
                    allConfigDescriptions.add(tryLocalization(configDescriptions.getKey(), configDescription, locale));
                }
            }
        }

        return allConfigDescriptions;
    }

    @Override
    public synchronized ConfigDescription getConfigDescription(URI uri, Locale locale) {
        Collection<Entry<Bundle, List<ConfigDescription>>> configDescriptionsList = this.bundleConfigDescriptionsMap
                .entrySet();

        // Loop through the config description list looking for the one
        // associated with this URI
        if (configDescriptionsList != null) {
            for (Entry<Bundle, List<ConfigDescription>> configDescriptions : configDescriptionsList) {
                for (ConfigDescription configDescription : configDescriptions.getValue()) {
                    if (configDescription.getURI().equals(uri)) {
                        return tryLocalization(configDescriptions.getKey(), configDescription, locale);
                    }
                }
            }
        }

        return null;
    }

    @Bind
    public void setConfigI18nLocalizerService(ConfigI18nLocalizationService configI18nLocalizerService) {
        this.configI18nLocalizerService = configI18nLocalizerService;
    }

    @Unbind
    public void unsetConfigI18nLocalizerService(ConfigI18nLocalizationService configI18nLocalizerService) {
        this.configI18nLocalizerService = null;
    }

    private ConfigDescription tryLocalization(final Bundle bundle, final ConfigDescription configDescription,
            final Locale locale) {
        if (configI18nLocalizerService == null) {
            return configDescription;
        } else {
            return configI18nLocalizerService.getLocalizedConfigDescription(bundle, configDescription, locale);
        }
    }

}
