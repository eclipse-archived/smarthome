/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterGroup;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.config.core.i18n.ConfigDescriptionGroupI18nUtil;
import org.eclipse.smarthome.config.core.i18n.ConfigDescriptionI18nUtil;
import org.eclipse.smarthome.core.common.osgi.ServiceBinder.Bind;
import org.eclipse.smarthome.core.common.osgi.ServiceBinder.Unbind;
import org.eclipse.smarthome.core.i18n.I18nProvider;
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
 */
public class XmlConfigDescriptionProvider implements ConfigDescriptionProvider {

    private Map<Bundle, List<ConfigDescription>> bundleConfigDescriptionsMap;

    private ConfigDescriptionI18nUtil configDescriptionParamI18nUtil;
    private ConfigDescriptionGroupI18nUtil configDescriptionGroupI18nUtil;

    public XmlConfigDescriptionProvider() {
        this.bundleConfigDescriptionsMap = new HashMap<>(10);
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
                    ConfigDescription localizedConfigDescription = getLocalizedConfigDescription(
                            configDescriptions.getKey(), configDescription, locale);
                    allConfigDescriptions.add(localizedConfigDescription);
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
                        return getLocalizedConfigDescription(configDescriptions.getKey(), configDescription, locale);
                    }
                }
            }
        }

        return null;
    }

    @Bind
    public void seI18nProvider(I18nProvider i18nProvider) {
        this.configDescriptionParamI18nUtil = new ConfigDescriptionI18nUtil(i18nProvider);
        this.configDescriptionGroupI18nUtil = new ConfigDescriptionGroupI18nUtil(i18nProvider);
    }

    @Unbind
    public void unsetI18nProvider(I18nProvider i18nProvider) {
        this.configDescriptionParamI18nUtil = null;
        this.configDescriptionGroupI18nUtil = null;
    }

    private ConfigDescription getLocalizedConfigDescription(Bundle bundle, ConfigDescription configDescription,
            Locale locale) {

        // We can only localise if we have both converters (for parameters and groups)
        if (this.configDescriptionParamI18nUtil != null && this.configDescriptionGroupI18nUtil != null) {
            List<ConfigDescriptionParameter> localizedConfigDescriptionParameters = new ArrayList<>(
                    configDescription.getParameters().size());

            // Loop through all the configuration parameters and localize them
            for (ConfigDescriptionParameter configDescriptionParameter : configDescription.getParameters()) {
                ConfigDescriptionParameter localizedConfigDescriptionParameter = getLocalizedConfigDescriptionParameter(
                        bundle, configDescription, configDescriptionParameter, locale);
                localizedConfigDescriptionParameters.add(localizedConfigDescriptionParameter);
            }

            List<ConfigDescriptionParameterGroup> localizedConfigDescriptionGroups = new ArrayList<>(
                    configDescription.getParameterGroups().size());

            // Loop through all the configuration groups and localize them
            for (ConfigDescriptionParameterGroup configDescriptionParameterGroup : configDescription
                    .getParameterGroups()) {
                ConfigDescriptionParameterGroup localizedConfigDescriptionGroup = getLocalizedConfigDescriptionGroup(
                        bundle, configDescription, configDescriptionParameterGroup, locale);
                localizedConfigDescriptionGroups.add(localizedConfigDescriptionGroup);
            }
            return new ConfigDescription(configDescription.getURI(), localizedConfigDescriptionParameters,
                    localizedConfigDescriptionGroups);
        } else {
            return configDescription;
        }
    }

    private ConfigDescriptionParameter getLocalizedConfigDescriptionParameter(Bundle bundle,
            ConfigDescription configDescription, ConfigDescriptionParameter parameter, Locale locale) {

        URI configDescriptionURI = configDescription.getURI();
        String parameterName = parameter.getName();

        String label = this.configDescriptionParamI18nUtil.getParameterLabel(bundle, configDescriptionURI,
                parameterName, parameter.getLabel(), locale);

        String description = this.configDescriptionParamI18nUtil.getParameterDescription(bundle, configDescriptionURI,
                parameterName, parameter.getDescription(), locale);

        String pattern = this.configDescriptionParamI18nUtil.getParameterPattern(bundle, configDescriptionURI,
                parameterName, parameter.getPattern(), locale);

        String unitLabel = this.configDescriptionParamI18nUtil.getParameterUnitLabel(bundle, configDescriptionURI,
                parameterName, parameter.getUnit(), parameter.getUnitLabel(), locale);

        List<ParameterOption> options = getLocalizedOptions(parameter.getOptions(), bundle, configDescriptionURI,
                parameterName, locale);

        ConfigDescriptionParameter localizedParameter = ConfigDescriptionParameterBuilder
                .create(parameterName, parameter.getType()).withMinimum(parameter.getMinimum())
                .withMaximum(parameter.getMaximum()).withStepSize(parameter.getStepSize()).withPattern(pattern)
                .withRequired(parameter.isRequired()).withReadOnly(parameter.isReadOnly())
                .withMultiple(parameter.isMultiple()).withContext(parameter.getContext())
                .withDefault(parameter.getDefault()).withLabel(label).withDescription(description).withOptions(options)
                .withFilterCriteria(parameter.getFilterCriteria()).withGroupName(parameter.getGroupName())
                .withAdvanced(parameter.isAdvanced()).withLimitToOptions(parameter.getLimitToOptions())
                .withMultipleLimit(parameter.getMultipleLimit()).withUnit(parameter.getUnit()).withUnitLabel(unitLabel)
                .build();

        return localizedParameter;
    }

    private ConfigDescriptionParameterGroup getLocalizedConfigDescriptionGroup(Bundle bundle,
            ConfigDescription configDescription, ConfigDescriptionParameterGroup group, Locale locale) {

        URI configDescriptionURI = configDescription.getURI();
        String name = group.getName();

        String label = this.configDescriptionGroupI18nUtil.getGroupLabel(bundle, configDescriptionURI, name,
                group.getLabel(), locale);

        String description = this.configDescriptionGroupI18nUtil.getGroupDescription(bundle, configDescriptionURI, name,
                group.getDescription(), locale);

        ConfigDescriptionParameterGroup localizedGroup = new ConfigDescriptionParameterGroup(name, group.getContext(),
                group.isAdvanced(), label, description);

        return localizedGroup;
    }

    private List<ParameterOption> getLocalizedOptions(List<ParameterOption> originalOptions, Bundle bundle,
            URI configDescriptionURI, String parameterName, Locale locale) {
        if (originalOptions == null || originalOptions.isEmpty()) {
            return originalOptions;
        }

        List<ParameterOption> localizedOptions = new ArrayList<ParameterOption>();
        for (ParameterOption option : originalOptions) {

            String localizedLabel = this.configDescriptionParamI18nUtil.getParameterOptionLabel(bundle,
                    configDescriptionURI, parameterName, /* key */option.getValue(), /* fallback */option.getLabel(),
                    locale);
            ParameterOption localizedOption = new ParameterOption(option.getValue(), localizedLabel);
            localizedOptions.add(localizedOption);
        }
        return localizedOptions;
    }

}
