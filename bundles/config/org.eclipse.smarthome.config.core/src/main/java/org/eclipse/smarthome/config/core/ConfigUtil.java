/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
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
     * We do not want to handle or try to normalize OSGi provided configuration parameters
     *
     * @param name The configuration parameter name
     */
    private static boolean isOSGiConfigParameter(String name) {
        return name.equals("objectClass") || name.equals("component.name") || name.equals("component.id");
    }

    /**
     * Normalizes the types to the ones allowed for configurations.
     *
     * @param configuration the configuration that needs to be normalzed
     * @return normalized configuration
     */
    public static Map<String, Object> normalizeTypes(Map<String, Object> configuration) {
        Map<String, Object> convertedConfiguration = new HashMap<>(configuration.size());
        for (Entry<String, Object> parameter : configuration.entrySet()) {
            String name = parameter.getKey();
            Object value = parameter.getValue();
            if (!isOSGiConfigParameter(name)) {
                convertedConfiguration.put(name, normalizeType(value, null));
            }
        }
        return convertedConfiguration;
    }

    /**
     * Normalizes the type of the parameter to the one allowed for configurations.
     *
     * @param value the value to return as normalized type
     * @return corresponding value as a valid type
     * @deprecated
     */
    @Deprecated
    public static Object normalizeType(Object value) {
        return normalizeType(value, null);
    }

    /**
     * Normalizes the type of the parameter to the one allowed for configurations.
     *
     * @param value the value to return as normalized type
     * @param configDescriptionParameter the parameter that needs to be normalized
     * @return corresponding value as a valid type
     * @throws IllegalArgumentException if a invalid type has been given
     */
    public static Object normalizeType(Object value, ConfigDescriptionParameter configDescriptionParameter) {
        if (configDescriptionParameter != null) {
            Normalizer normalizer = NormalizerFactory.getNormalizer(configDescriptionParameter);
            return normalizer.normalize(value);
        } else if (value == null || value instanceof Boolean || value instanceof String
                || value instanceof BigDecimal) {
            return value;
        } else if (value instanceof Number) {
            return new BigDecimal(value.toString());
        } else if (value instanceof Collection) {
            return ((Collection<?>) value).stream().map(c -> normalizeType(c, null)).collect(Collectors.toList());
        }
        throw new IllegalArgumentException(
                "Invalid type '{" + value.getClass().getCanonicalName() + "}' of configuration value!");
    }

    /**
     * Normalizes the given configuration according to the given config descriptions.
     *
     * By doing so, it tries to convert types on a best-effort basis. The result will contain
     * BigDecimals, Strings and Booleans wherever a conversion of similar types was possible.
     *
     * However, it does not check for general correctness of types. This can be done using the
     * {@link ConfigDescriptionValidator}.
     *
     * If multiple config descriptions are given and a parameter is described several times, then the first one (lower
     * index in the list) wins.
     *
     * @param configuration the configuration to be normalized (can be null)
     * @param configDescriptions the configuration descriptions that should be applied (must not be null or empty).
     * @return the normalized configuration or null if given configuration was null
     * @throws IllegalArgumentException if given config description is empty
     */
    public static Map<String, Object> normalizeTypes(@Nullable Map<String, Object> configuration,
            @NonNull List<ConfigDescription> configDescriptions) {
        if (configDescriptions.isEmpty()) {
            throw new IllegalArgumentException("Config description must not be empty.");
        }

        if (configuration == null) {
            return null;
        }

        Map<String, Object> convertedConfiguration = new HashMap<>();

        Map<String, ConfigDescriptionParameter> configParams = new HashMap<>();
        for (int i = configDescriptions.size() - 1; i >= 0; i--) {
            configParams.putAll(configDescriptions.get(i).toParametersMap());
        }
        configuration.forEach((name, value) -> {
            if (!isOSGiConfigParameter(name)) {
                ConfigDescriptionParameter configDescriptionParameter = configParams.get(name);
                convertedConfiguration.put(name, normalizeType(value, configDescriptionParameter));
            }
        });
        return convertedConfiguration;
    }

}
