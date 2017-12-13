/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.io.rest.core.config;

import java.io.IOException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.config.core.Configuration;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ConfigurationService} manages configurations in the {@link ConfigurationAdmin}. The config id is the
 * equivalent to the {@link Constants#SERVICE_PID}.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
@Component(service = ConfigurationService.class)
public class ConfigurationService {

    private ConfigurationAdmin configurationAdmin;

    private final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

    /**
     * Returns a configuration for a config id.
     *
     * @param configId config id
     * @return config or null if no config with the given config id exists
     * @throws IOException if configuration can not be read
     */
    // public Configuration get(String configId, Map<String, Object> props) throws IOException {
    public Configuration get(String configId) throws IOException {
        Dictionary<String, Object> properties = null;
        // if (props.containsKey(ConfigConstants.SERVICE_CONTEXT)) {
        // String context = (String) props.get(ConfigConstants.SERVICE_CONTEXT);
        // String pidWithContext = configId + ConfigConstants.SERVICE_CONTEXT_MARKER + context;
        // try {
        // org.osgi.service.cm.Configuration configuration = getConfigurationWithContext(pidWithContext);
        // properties = configuration.getProperties();
        // } catch (InvalidSyntaxException e) {
        // logger.error("Failed to lookup config for PID '{}' with context '{}'", configId, context);
        // }
        // } else {
        org.osgi.service.cm.Configuration configuration = configurationAdmin.getConfiguration(configId, null);
        properties = configuration.getProperties();
        // }
        return toConfiguration(properties);
    }

    /**
     * Creates or updates a configuration for a config id.
     *
     * @param configId config id
     * @param newConfiguration the configuration
     * @return old config or null if no old config existed
     * @throws IOException if configuration can not be stored
     */
    public Configuration update(String configId, Configuration newConfiguration) throws IOException {
        return update(configId, newConfiguration, false);
    }

    /**
     * Creates or updates a configuration for a config id.
     *
     * @param configId config id
     * @param newConfiguration the configuration
     * @param override if true, it overrides the old config completely. means it deletes all parameters even if they are
     *            not defined in the given configuration.
     * @return old config or null if no old config existed
     * @throws IOException if configuration can not be stored
     */
    public Configuration update(String configId, Configuration newConfiguration, boolean override) throws IOException {

        org.osgi.service.cm.Configuration configuration = null;
        if (newConfiguration.containsKey(ConfigConstants.SERVICE_CONTEXT)) {
            String context = (String) newConfiguration.get(ConfigConstants.SERVICE_CONTEXT);
            String pidWithContext = configId + ConfigConstants.SERVICE_CONTEXT_MARKER + context;

            try {
                configuration = getConfigurationWithContext(pidWithContext);
            } catch (InvalidSyntaxException e) {
                logger.error("Failed to lookup config for PID '{}' with context '{}'", configId, context);
            }
            if (configuration == null) {
                configuration = configurationAdmin.createFactoryConfiguration(configId, null);
            }
        } else {
            configuration = configurationAdmin.getConfiguration(configId, null);
        }

        Configuration oldConfiguration = toConfiguration(configuration.getProperties());
        Dictionary<String, Object> properties = getProperties(configuration);
        Set<Entry<String, Object>> configurationParameters = newConfiguration.getProperties().entrySet();
        if (override) {
            Set<String> keySet = oldConfiguration.keySet();
            for (String key : keySet) {
                properties.remove(key);
            }
        }
        for (Entry<String, Object> configurationParameter : configurationParameters) {
            Object value = configurationParameter.getValue();
            if (value == null) {
                properties.remove(configurationParameter.getKey());
            } else if (value instanceof String || value instanceof Integer || value instanceof Boolean
                    || value instanceof Object[] || value instanceof Collection) {
                properties.put(configurationParameter.getKey(), value);
            } else {
                // the config admin does not support complex object types, so let's store the string representation
                properties.put(configurationParameter.getKey(), value.toString());
            }
        }
        configuration.update(properties);
        return oldConfiguration;
    }

    // TODO: move
    private org.osgi.service.cm.Configuration getConfigurationWithContext(String pidWithContext)
            throws IOException, InvalidSyntaxException {

        if (!pidWithContext.contains(ConfigConstants.SERVICE_CONTEXT_MARKER)) {
            throw new IllegalArgumentException("Given PID should be followed by a context");
        }
        String pid = pidWithContext.split(ConfigConstants.SERVICE_CONTEXT_MARKER)[0];
        String context = pidWithContext.split(ConfigConstants.SERVICE_CONTEXT_MARKER)[1];

        org.osgi.service.cm.Configuration[] configs = configurationAdmin.listConfigurations(
                "(&(service.factoryPid=" + pid + ")(" + ConfigConstants.SERVICE_CONTEXT + "=" + context + "))");

        if (configs == null) {
            return null;
        }
        if (configs.length > 1) {
            throw new IllegalStateException("More than one configuration with PID " + pidWithContext + " exists");
        }

        return configs[0];
    }

    /**
     * Deletes a configuration for a config id.
     *
     * @param configId config id
     * @return old config or null if no old config existed
     * @throws IOException if configuration can not be removed
     */
    public Configuration delete(String configId) throws IOException {
        org.osgi.service.cm.Configuration serviceConfiguration = configurationAdmin.getConfiguration(configId, null);
        Configuration oldConfiguration = toConfiguration(serviceConfiguration.getProperties());
        serviceConfiguration.delete();
        return oldConfiguration;
    }

    private Configuration toConfiguration(Dictionary<String, Object> dictionary) {
        if (dictionary == null) {
            return null;
        }
        Map<String, Object> properties = new HashMap<>(dictionary.size());
        Enumeration<String> keys = dictionary.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (!key.equals(Constants.SERVICE_PID)) {
                properties.put(key, dictionary.get(key));
            }
        }
        return new Configuration(properties);
    }

    private Dictionary<String, Object> getProperties(org.osgi.service.cm.Configuration configuration) {
        Dictionary<String, Object> properties = configuration.getProperties();
        return properties != null ? properties : new Hashtable<String, Object>();
    }

    @Reference
    protected void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }

    protected void unsetConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }
}
