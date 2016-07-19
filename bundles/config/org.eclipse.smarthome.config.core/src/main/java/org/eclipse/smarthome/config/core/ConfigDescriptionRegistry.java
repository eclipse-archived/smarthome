/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@link ConfigDescriptionRegistry} provides access to {@link ConfigDescription}s.
 * It tracks {@link ConfigDescriptionProvider} OSGi services to collect all {@link ConfigDescription}s.
 *
 * @see ConfigDescriptionProvider
 *
 * @author Dennis Nobel - Initial contribution, added locale support
 * @author Michael Grammling - Initial contribution
 * @author Chris Jackson - Added compatibility with multiple ConfigDescriptionProviders. Added Config OptionProvider.
 * @author Thomas Höfer - Added unit
 */
public class ConfigDescriptionRegistry {

    private final List<ConfigOptionProvider> configOptionProviders = new CopyOnWriteArrayList<>();
    private final List<ConfigDescriptionProvider> configDescriptionProviders = new CopyOnWriteArrayList<>();

    protected void addConfigOptionProvider(ConfigOptionProvider configOptionProvider) {
        if (configOptionProvider != null) {
            configOptionProviders.add(configOptionProvider);
        }
    }

    protected void removeConfigOptionProvider(ConfigOptionProvider configOptionProvider) {
        if (configOptionProvider != null) {
            configOptionProviders.remove(configOptionProvider);
        }
    }

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
     * <p>
     * If more than one {@link ConfigDescriptionProvider} is registered for a specific URI, then the returned
     * {@link ConfigDescription} collection will contain the data from all providers.
     * <p>
     * No checking is performed to ensure that multiple providers don't provide the same configuration data. It is up to
     * the binding to ensure that multiple sources (eg static XML and dynamic binding data) do not contain overlapping
     * information.
     *
     * @param locale
     *            locale
     * @return all config descriptions or an empty collection if no config
     *         description exists
     */
    public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
        Map<URI, ConfigDescription> configMap = new HashMap<URI, ConfigDescription>();

        // Loop over all providers
        for (ConfigDescriptionProvider configDescriptionProvider : this.configDescriptionProviders) {
            // And for each provider, loop over all their config descriptions
            for (ConfigDescription configDescription : configDescriptionProvider.getConfigDescriptions(locale)) {
                // See if there already exists a configuration for this URI in the map
                ConfigDescription configFromMap = configMap.get(configDescription.getURI());
                if (configFromMap != null) {
                    // Yes - Merge the groups and parameters
                    List<ConfigDescriptionParameter> parameters = new ArrayList<ConfigDescriptionParameter>();
                    parameters.addAll(configFromMap.getParameters());
                    parameters.addAll(configDescription.getParameters());

                    List<ConfigDescriptionParameterGroup> parameterGroups = new ArrayList<ConfigDescriptionParameterGroup>();
                    parameterGroups.addAll(configFromMap.getParameterGroups());
                    parameterGroups.addAll(configDescription.getParameterGroups());

                    // And add the combined configuration to the map
                    configMap.put(configDescription.getURI(),
                            new ConfigDescription(configDescription.getURI(), parameters, parameterGroups));
                } else {
                    // No - Just add the new configuration to the map
                    configMap.put(configDescription.getURI(), configDescription);
                }
            }
        }

        // Now convert the map into the collection
        Collection<ConfigDescription> configDescriptions = new ArrayList<ConfigDescription>(configMap.size());
        for (ConfigDescription configDescription : configMap.values()) {
            configDescriptions.add(configDescription);
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
     * <p>
     * If more than one {@link ConfigDescriptionProvider} is registered for the requested URI, then the returned
     * {@link ConfigDescription} will contain the data from all providers.
     * <p>
     * No checking is performed to ensure that multiple providers don't provide the same configuration data. It is up to
     * the binding to ensure that multiple sources (eg static XML and dynamic binding data) do not contain overlapping
     * information.
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
        List<ConfigDescriptionParameter> parameters = new ArrayList<ConfigDescriptionParameter>();
        List<ConfigDescriptionParameterGroup> parameterGroups = new ArrayList<ConfigDescriptionParameterGroup>();

        boolean found = false;
        for (ConfigDescriptionProvider configDescriptionProvider : this.configDescriptionProviders) {
            ConfigDescription config = configDescriptionProvider.getConfigDescription(uri, locale);

            if (config != null) {
                found = true;

                // Simply merge the groups and parameters
                parameters.addAll(config.getParameters());
                parameterGroups.addAll(config.getParameterGroups());
            }
        }

        if (found) {
            List<ConfigDescriptionParameter> parametersWithOptions = new ArrayList<ConfigDescriptionParameter>(
                    parameters.size());
            for (ConfigDescriptionParameter parameter : parameters) {
                parametersWithOptions.add(getConfigOptions(uri, parameter, locale));
            }

            // Return the new configuration description
            return new ConfigDescription(uri, parametersWithOptions, parameterGroups);
        } else {
            // Otherwise null
            return null;
        }
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

    /**
     * Updates the config parameter options for a given URI and parameter
     * <p>
     * If more than one {@link ConfigOptionProvider} is registered for the requested URI, then the returned
     * {@link ConfigDescriptionParameter} will contain the data from all providers.
     * <p>
     * No checking is performed to ensure that multiple providers don't provide the same options. It is up to
     * the binding to ensure that multiple sources (eg static XML and dynamic binding data) do not contain overlapping
     * information.
     *
     * @param uri
     *            the URI to which the options to be returned (must not be null)
     * @param parameter
     *            the parameter requiring options to be updated
     * @param locale
     *            locale
     * @return config description
     */
    private ConfigDescriptionParameter getConfigOptions(URI uri, ConfigDescriptionParameter parameter, Locale locale) {
        List<ParameterOption> options = new ArrayList<ParameterOption>();

        // Add all the existing options that may be provided by the initial config description provider
        options.addAll(parameter.getOptions());

        boolean found = false;
        for (ConfigOptionProvider configOptionProvider : this.configOptionProviders) {
            Collection<ParameterOption> newOptions = configOptionProvider.getParameterOptions(uri, parameter.getName(),
                    locale);

            if (newOptions != null) {
                found = true;

                // Simply merge the options
                options.addAll(newOptions);
            }
        }

        if (found) {
            // Return the new parameter
            return new ConfigDescriptionParameter(parameter.getName(), parameter.getType(), parameter.getMinimum(),
                    parameter.getMaximum(), parameter.getStepSize(), parameter.getPattern(), parameter.isRequired(),
                    parameter.isReadOnly(), parameter.isMultiple(), parameter.getContext(), parameter.getDefault(),
                    parameter.getLabel(), parameter.getDescription(), options, parameter.getFilterCriteria(),
                    parameter.getGroupName(), parameter.isAdvanced(), parameter.getLimitToOptions(),
                    parameter.getMultipleLimit(), parameter.getUnit(), parameter.getUnitLabel());
        } else {
            // Otherwise return the original parameter
            return parameter;
        }
    }
}
