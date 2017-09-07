/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
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
import org.eclipse.jdt.annotation.Nullable;
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

    private final Map<String, Object> properties;

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

    public static Map<String, Object> readFromConfigurationClassInstance(Object o) throws IllegalAccessException {
        Map<String, Object> v = new HashMap<String, Object>();
        List<Field> fields = getAllFields(o.getClass());
        for (Field field : fields) {
            // Don't read from final fields
            if (Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            final String fieldName = field.getName();
            final Class<?> type = field.getType();

            if (type.equals(BigDecimal.class) || type.equals(String.class) || type.equals(Boolean.class)
                    || type.equals(Integer.class) || type.equals(Long.class) || type.equals(Double.class)
                    || type.equals(Float.class) || field.getType().isPrimitive()) {
                Object value = FieldUtils.readField(field, o, true);
                if (value != null) {
                    v.put(fieldName, value);
                }
            } else if (Collection.class.isAssignableFrom(type)) {
                List<Map<String, Object>> collect = new ArrayList<>();
                for (Object subObject : (Collection<?>) FieldUtils.readField(field, o, true)) {
                    if (subObject != null) {
                        collect.add(readFromConfigurationClassInstance(subObject));
                    }
                }
                v.put(fieldName, collect);

            } else if (Array.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException("Arrays are not supported");

            } else { // Read complex objects
                v.put(fieldName, readFromConfigurationClassInstance(FieldUtils.readField(field, o, true)));
            }
        }
        return v;
    }

    /**
     * Creates an instance of the given configuration class (a default constructor is required) and
     * tries to find a matching property value for each non-final field.
     * Nested classes are supported.
     * List fields are supported as long as the property value is of type Collection.
     *
     * @param configurationClass
     * @return
     */
    @Nullable
    public <T> T as(Class<T> configurationClass) {
        synchronized (this) {
            try {
                return getAs(logger, configurationClass, properties);
            } catch (IllegalAccessException e) {
                // This should never happen, because we ignore final fields and ignore modifiers
                logger.error("Field in configuration class not accessible", e);
                return null;
            } catch (InstantiationException e) {
                logger.error("Could not create configuration instance", e);
                return null;
            }
        }
    }

    @Nullable
    private static <T> T getAs(Logger logger, Class<T> configurationClass, Map<?, ?> properties)
            throws IllegalAccessException, InstantiationException {
        T configuration = configurationClass.newInstance();

        List<Field> fields = getAllFields(configurationClass);
        for (Field field : fields) {
            // Don't try to write to final fields
            if (Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            final String fieldName = field.getName();
            final Class<?> type = field.getType();
            Object value = properties.get(fieldName);

            // Case: value is null.
            if (value == null) {
                // A warning if the field is a primitive type
                if (field.getType().isPrimitive()) {
                    logger.warn("Skipping field '{}', because it's primitive data type and value is not set",
                            fieldName);
                } else if (properties.containsKey(fieldName)) {
                    // Write null to the field (which may contain another default value)
                    FieldUtils.writeField(field, configuration, null, true);
                }
                continue;
            }

            if (value instanceof BigDecimal) {
                // Case value is BigDecimal, but field is of another numeric type
                if (!field.getType().equals(BigDecimal.class)) {
                    final String typeName = type.getSimpleName();
                    BigDecimal bdValue = (BigDecimal) value;
                    if (type.equals(Float.class) || typeName.equals("float")) {
                        value = bdValue.floatValue();
                    } else if (type.equals(Double.class) || typeName.equals("double")) {
                        value = bdValue.doubleValue();
                    } else if (type.equals(Long.class) || typeName.equals("long")) {
                        value = bdValue.longValue();
                    } else if (type.equals(Integer.class) || typeName.equals("int")) {
                        value = bdValue.intValue();
                    } else {
                        logger.warn("Skipping field '{}'. The number value could not be converted to the field type",
                                fieldName);
                        continue;
                    }
                }
            } else if (value instanceof Collection) {
                if (!(Collection.class.isAssignableFrom(type))) {
                    logger.warn("Skipping field '{}'. The list value could not be converted to the list type {}",
                            fieldName, type.getSimpleName());
                    continue;
                }
                // Get the generic type of the collection
                Class<?> componentType = (Class<?>) ((ParameterizedType) field.getGenericType())
                        .getActualTypeArguments()[0];
                Collection<?> listOfValues = (Collection<?>) value;
                // Create a new list and add all unmarshalled objects.
                // getAs is called recursively and exceptions are passed to the caller.
                List<Object> newList = new ArrayList<>();
                for (Object listValue : listOfValues) {
                    newList.add(getAs(logger, componentType, (Map<?, ?>) listValue));
                }
                value = newList;
            } else if (value instanceof Map) {
                value = getAs(logger, type, (Map<?, ?>) value);
            }

            FieldUtils.writeField(configuration, fieldName, value, true);
        }

        return configuration;
    }

    private static List<Field> getAllFields(Class<?> clazz) {
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
            return properties.put(key, ConfigUtil.normalizeType(value));
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
            this.put(entrySet.getKey(), entrySet.getValue());
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Configuration[");
        boolean first = true;
        for (final Map.Entry<String, Object> prop : properties.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            Object value = prop.getValue();
            sb.append(String.format("{key=%s; type=%s; value=%s}", prop.getKey(),
                    value != null ? value.getClass().getSimpleName() : "?", value));
        }
        sb.append("]");
        return sb.toString();
    }

}
