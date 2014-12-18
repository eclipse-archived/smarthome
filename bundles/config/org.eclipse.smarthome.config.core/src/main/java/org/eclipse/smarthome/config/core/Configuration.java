/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * This class is a wrapper for configuration settings of {@link Thing}s.
 * 
 * @author Dennis Nobel - Initial API and contribution
 * @author Kai Kreuzer - added constructors
 */
public class Configuration {

    final private Map<String, Object> properties;

    private transient final Logger logger = LoggerFactory.getLogger(Configuration.class);

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
            Field[] declaredFields = configurationClass.getDeclaredFields();
            for (Field field : declaredFields) {
                Object value = this.get(field.getName());
                if (value != null) {
                    try {
                        field.set(configuration, value);
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        logger.warn("Could not set field value for field '" + field.getName()
                                + "': " + ex.getMessage(), ex);
                    }
                }
            }
            return configuration;
        }
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

    public boolean isProperlyConfigured() {
        return true;
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
