/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a wrapper for configuration settings of {@link Thing}s.
 *
 * @author Dennis Nobel - Initial API and contribution, Changed Logging
 * @author Kai Kreuzer - added constructors and normalization
 * @author Gerhard Riegler - added converting BigDecimal values to the type of the configuration class field
 * @author Chris Jackson - fix concurrent modification exception when removing properties
 */
public class Configuration {

    final private Map<String, Object> properties;

    private transient final Logger logger = LoggerFactory.getLogger(Configuration.class);

    public Configuration() {
        this(null);
    }

    /**
     * Create a new configuration.
     *
     * @param properties the properties the configuration should be filled. If null, an empty configuration is created.
     */
    public Configuration(Map<String, Object> properties) {
        this.properties = properties == null ? new HashMap<String, Object>() : ConfigUtil.normalizeTypes(properties);
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

    /**
     * Check if the given key is present in the configuration.
     *
     * @param key the key that existence should be checked
     * @return true if the key is part of the configuration, false if not
     */
    public boolean containsKey(String key) {
        synchronized (this) {
            return properties.containsKey(key);
        }
    }

    /**
     * @deprecated Use {@link #get(String)} instead.
     */
    @Deprecated
    public Object get(Object key) {
        return this.get((String) key);
    }

    public Object get(String key) {
        synchronized (this) {
            return properties.get(key);
        }
    }

    public Object put(String key, Object value) {
        synchronized (this) {
            return properties.put(key, value);
        }
    }

    /**
     * @deprecated Use {@link #remove(String)} instead.
     */
    @Deprecated
    public Object remove(Object key) {
        return remove((String) key);
    }

    public Object remove(String key) {
        synchronized (this) {
            return properties.remove(key);
        }
    }

    public Set<String> keySet() {
        synchronized (this) {
            return Collections.unmodifiableSet(new HashSet<>(properties.keySet()));
        }
    }

    public Collection<Object> values() {
        synchronized (this) {
            return Collections.unmodifiableCollection(new ArrayList<>(properties.values()));
        }
    }

    public Map<String, Object> getProperties() {
        synchronized (this) {
            return Collections.unmodifiableMap(new HashMap<>(properties));
        }
    }

    public void setProperties(Map<String, Object> properties) {
        for (Entry<String, Object> entrySet : properties.entrySet()) {
            this.put(entrySet.getKey(), ConfigUtil.normalizeType(entrySet.getValue()));
        }
        for (Iterator<String> it = this.properties.keySet().iterator(); it.hasNext();) {
            String entry = it.next();
            if (!properties.containsKey(entry)) {
                it.remove();
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
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Configuration)) {
            return false;
        }
        return this.hashCode() == obj.hashCode();
    }

}
