/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.config.core.normalization.Normalizer;
import org.eclipse.smarthome.config.core.normalization.NormalizerFactory;
import org.eclipse.smarthome.config.core.validation.ConfigDescriptionValidator;

/**
 * This class provides some useful static methods for handling configurations
 *
 * @author Kai Kreuzer - Initial API and implementation
 * @author Thomas Höfer - Minor changes for type normalization based on config description
 */
public class ConfigUtil {

    /**
     * normalizes the types to the ones allowed for configurations
     *
     * @param configuration
     * @return normalized configuration
     */
    public static Map<String, Object> normalizeTypes(Map<String, Object> configuration) {
        Map<String, Object> convertedConfiguration = new HashMap<String, Object>(configuration.size());
        for (Entry<String, Object> parameter : configuration.entrySet()) {
            String name = parameter.getKey();
            Object value = parameter.getValue();
            convertedConfiguration.put(name, normalizeType(value));
        }
        return convertedConfiguration;
    }

    /**
     * normalizes the type of the parameter to the one allowed for configurations
     *
     * @param value the value to return as normalized type
     *
     * @return corresponding value as a valid type
     */
    public static Object normalizeType(Object value) {
        return normalizeType(value, null);
    }

    /**
     * normalizes the type of the parameter to the one allowed for configurations
     *
     * @param value the value to return as normalized type
     *
     * @return corresponding value as a valid type
     */
    public static Object normalizeType(Object value, ConfigDescriptionParameter configDescriptionParameter) {
        if (configDescriptionParameter != null) {
            Normalizer normalizer = NormalizerFactory.getNormalizer(configDescriptionParameter);
            return normalizer.normalize(value);
        } else {
            return value instanceof Double ? BigDecimal.valueOf((Double) value) : value;
        }
    }

    /**
     * Normalizes the given configuration according to their config description.
     *
     * By doing so, it tries to convert types on a best-effort basis. The result will contain
     * BigDecimals, Strings and Booleans wherever a conversion of similar types was possible.
     *
     * However, it does not check for general correctness of types. This can be done using the
     * {@link ConfigDescriptionValidator}.
     *
     * @param configuration the configuration to be normalized (can be null)
     * @param configDescription the configuration description (must not be null)
     * @return the normalized configuration or null if given configuration was null
     * @throws NullPointerException if given config description is null
     */
    public static Map<String, Object> normalizeTypes(Map<String, Object> configuration,
            ConfigDescription configDescription) {
        if (configDescription == null) {
            throw new NullPointerException("Config description must not be null.");
        }

        if (configuration == null) {
            return null;
        }

        Map<String, Object> convertedConfiguration = new HashMap<String, Object>(configuration.size());
        Map<String, ConfigDescriptionParameter> configParams = configDescription.toParametersMap();
        for (Entry<String, ?> parameter : configuration.entrySet()) {
            String name = parameter.getKey();
            Object value = parameter.getValue();
            ConfigDescriptionParameter configDescriptionParameter = configParams.get(name);
            convertedConfiguration.put(name, normalizeType(value, configDescriptionParameter));
        }
        return convertedConfiguration;
    }

}
