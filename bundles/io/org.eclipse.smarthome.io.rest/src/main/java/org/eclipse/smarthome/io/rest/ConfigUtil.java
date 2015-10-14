/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * {@link ConfigUtil} provides helper method for working with configs in REST
 * resources.
 *
 * @author Kai Kreuzer - Initial contribution
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
        return value instanceof Double ? new BigDecimal((Double) value) : value;
    }

}
