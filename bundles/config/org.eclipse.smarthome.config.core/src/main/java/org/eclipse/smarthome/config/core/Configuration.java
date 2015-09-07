/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * This class is a wrapper for configuration settings of {@link Thing}s.
 *
 * @author Dennis Nobel - Initial API and contribution, Changed Logging
 * @author Kai Kreuzer - added constructors
 * @author Gerhard Riegler - added converting BigDecimal values to the type of the configuration class field
 */
public class Configuration {

    final private Map<String, Object> properties;

    private static transient final Logger logger = LoggerFactory.getLogger(Configuration.class);

    public Configuration() {
        this(new HashMap<String, Object>());
    }

    public Configuration(Map<String, Object> properties) {
        this.properties = properties;
    }

    public <T> T as(Class<T> configurationClass) {
        synchronized (this) {

            T configuration = null;

            try {
                configuration = configurationClass.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                logger.error("Could not create configuration instance: " + ex.getMessage(), ex);
                return null;
            }

            List<Field> fields = getAllFields(configurationClass);
            for (Field field : fields) {
                String fieldName = field.getName();
                String typeName = field.getType().getSimpleName();
                Object value = properties.get(fieldName);

                if (value == null && field.getType().isPrimitive()) {
                    logger.debug("Skipping field '{}', because it's primitive data type and value is not set",
                            fieldName);
                    continue;
                }

                try {
                    if (value != null && value instanceof BigDecimal && !typeName.equals("BigDecimal")) {
                        BigDecimal bdValue = (BigDecimal) value;
                        if (typeName.equalsIgnoreCase("Float")) {
                            value = bdValue.floatValue();
                        } else if (typeName.equalsIgnoreCase("Double")) {
                            value = bdValue.doubleValue();
                        } else if (typeName.equalsIgnoreCase("Long")) {
                            value = bdValue.longValue();
                        } else if (typeName.equalsIgnoreCase("Integer") || typeName.equalsIgnoreCase("int")) {
                            value = bdValue.intValue();
                        }
                    }

                    if (value != null) {
                        logger.debug("Setting value ({}) {} to field '{}' in configuration class {}", typeName, value,
                                fieldName, configurationClass.getName());
                        FieldUtils.writeField(configuration, fieldName, value, true);
                    }
                } catch (Exception ex) {
                    logger.warn("Could not set field value for field '" + fieldName + "': " + ex.getMessage(), ex);
                }
            }

            return configuration;
        }
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<Field>();

        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }

        return fields;
    }

    public Object get(Object key) {
        synchronized (this) {
            return properties.get(key);
        }
    }

    public Object put(String key, Object value) {
        synchronized (this) {
            return properties.put(key, value);
        }
    }

    public Object remove(Object key) {
        synchronized (this) {
            return properties.remove(key);
        }
    }

    public Set<String> keySet() {
        synchronized (this) {
            return ImmutableSet.copyOf(properties.keySet());
        }
    }

    public Collection<Object> values() {
        synchronized (this) {
            return ImmutableList.copyOf(properties.values());
        }
    }

    public Map<String, Object> getProperties() {
        synchronized (this) {
            return ImmutableMap.copyOf(properties);
        }
    }

    public void setProperties(Map<String, Object> properties) {
        for (Entry<String, Object> entrySet : properties.entrySet()) {
            this.put(entrySet.getKey(), entrySet.getValue());
        }
        for (String key : this.properties.keySet()) {
            if (!properties.containsKey(key)) {
                this.remove(key);
            }
        }
    }

    @Override
    public int hashCode() {
        synchronized (this) {
            return properties.hashCode();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Configuration))
            return false;
        return this.hashCode() == obj.hashCode();
    }
}
