/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.validation;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.internal.Activator;
import org.eclipse.smarthome.config.core.validation.internal.ConfigDescriptionParameterValidator;
import org.eclipse.smarthome.config.core.validation.internal.ConfigDescriptionParameterValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * The {@link ConfigDescriptionValidator} validates a given set of {@link Configuration} parameters against a
 * given {@link ConfigDescription} URI. So it can be used as a static pre-validation to avoid that the configuration of
 * an entity is updated with parameters which do not match with the declarations in the configuration description.
 * If the validator detects one or more mismatches then a {@link ConfigValidationException} is thrown.
 *
 * @author Thomas Höfer - Initial contribution
 * @author Chris Jackson - Handle checks on multiple selection parameters
 */
public final class ConfigDescriptionValidator {

    private static final Logger logger = LoggerFactory.getLogger(ConfigDescriptionValidator.class);

    private static final List<ConfigDescriptionParameterValidator> validators = new ImmutableList.Builder<ConfigDescriptionParameterValidator>()
            .add(ConfigDescriptionParameterValidatorFactory.createRequiredValidator())
            .add(ConfigDescriptionParameterValidatorFactory.createTypeValidator())
            .add(ConfigDescriptionParameterValidatorFactory.createMinMaxValidator())
            .add(ConfigDescriptionParameterValidatorFactory.createPatternValidator()).build();

    private ConfigDescriptionValidator() {
        super();
    }

    /**
     * Validates the given configuration parameters against the given configuration description having the given URI.
     *
     * @param configurationParameters the configuration parameters to be validated
     * @param configDescriptionURI the URI of the configuration description against which the configuration parameters
     *            are to be validated
     *
     * @throws ConfigValidationException if one or more configuration parameters do not match with the configuration
     *             description having the given URI
     * @throws NullPointerException if given config description URI or configuration parameters are null
     */
    @SuppressWarnings("unchecked")
    public static void validate(Map<String, Object> configurationParameters, URI configDescriptionURI) {
        Preconditions.checkNotNull(configurationParameters, "Configuration parameters must not be null");
        Preconditions.checkNotNull(configDescriptionURI, "Config description URI must not be null");

        ConfigDescription configDescription = getConfigDescription(configDescriptionURI);

        if (configDescription == null) {
            logger.warn("Skipping config description validation because no config description found for URI '{}'",
                    configDescriptionURI);
            return;
        }

        Map<String, ConfigDescriptionParameter> map = configDescription.toParametersMap();

        Collection<ConfigValidationMessage> configDescriptionValidationMessages = new ArrayList<>();

        for (String key : configurationParameters.keySet()) {
            ConfigDescriptionParameter configDescriptionParameter = map.get(key);
            if (configDescriptionParameter != null) {
                // If the parameter supports multiple selection, then it may be provided as an array
                if (configDescriptionParameter.isMultiple() && configurationParameters.get(key) instanceof List) {
                    // Perform validation on each value in the list separately
                    for (Object value : (List<Object>) configurationParameters.get(key)) {
                        ConfigValidationMessage message = validateParameter(configDescriptionParameter, value);
                        if (message != null) {
                            configDescriptionValidationMessages.add(message);
                        }
                    }
                } else {
                    ConfigValidationMessage message = validateParameter(configDescriptionParameter,
                            configurationParameters.get(key));
                    if (message != null) {
                        configDescriptionValidationMessages.add(message);
                    }
                }
            }
        }

        if (!configDescriptionValidationMessages.isEmpty()) {
            throw new ConfigValidationException(Activator.getBundleContext().getBundle(),
                    configDescriptionValidationMessages);
        }
    }

    /**
     * Validates the given value against the given config description parameter.
     *
     * @param configDescriptionParameter the corresponding config description parameter
     * @param value the actual value
     *
     * @return the {@link ConfigValidationMessage} if the given value is not valid for the config description parameter,
     *         otherwise null
     */
    private static ConfigValidationMessage validateParameter(ConfigDescriptionParameter configDescriptionParameter,
            Object value) {
        for (ConfigDescriptionParameterValidator validator : validators) {
            ConfigValidationMessage message = validator.validate(configDescriptionParameter, value);
            if (message != null) {
                return message;
            }
        }
        return null;
    }

    /**
     * Retrieves the {@link ConfigDescription} for the given URI.
     *
     * @param configDescriptionURI the URI of the configuration description to be retrieved
     *
     * @return the requested config description or null if config description could not be found (either because of
     *         config description registry is not available or because of config description could not be found for
     *         given URI)
     */
    private static ConfigDescription getConfigDescription(URI configDescriptionURI) {
        ConfigDescriptionRegistry configDescriptionRegistry = Activator.getConfigDescriptionRegistry();
        if (configDescriptionRegistry == null) {
            logger.warn("No config description registry available.");
            return null;
        }
        ConfigDescription configDescription = configDescriptionRegistry.getConfigDescription(configDescriptionURI);
        if (configDescription == null) {
            logger.warn("No config description found for URI '{}'", configDescriptionURI);
        }
        return configDescription;
    }
}