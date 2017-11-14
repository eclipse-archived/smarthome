/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.smarthome.config.core.status.ConfigStatusMessage.Type;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

/**
 * The {@link ConfigStatusInfo} contains {@link ConfigStatusMessage}s to represent the current configuration status of
 * an entity. Furthermore it provides some convenience operations to filter for specific {@link ConfigStatusMessage}s.
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class ConfigStatusInfo {

    private final Collection<ConfigStatusMessage> configStatusMessages = new ArrayList<>();

    /**
     * Creates a new {@link ConfigStatusInfo}.
     */
    public ConfigStatusInfo() {
        super();
    }

    /**
     * Creates a new {@link ConfigStatusInfo} with the given {@link ConfigStatusMessages}.
     *
     * @param configStatusMessages the configuration status messages to be added
     */
    public ConfigStatusInfo(Collection<ConfigStatusMessage> configStatusMessages) {
        add(configStatusMessages);
    }

    /**
     * Retrieves all configuration status messages.
     *
     * @return an unmodifiable collection of available configuration status messages
     */
    public Collection<ConfigStatusMessage> getConfigStatusMessages() {
        return Collections.unmodifiableCollection(configStatusMessages);
    }

    /**
     * Retrieves all configuration status messages that have one of the given types.
     *
     * @param types the types to be filtered for; if empty then all messages are delivered
     *
     * @return an unmodifiable collection of the corresponding configuration status messages
     */
    public Collection<ConfigStatusMessage> getConfigStatusMessages(Type... types) {
        final Collection<Type> typesCollection = ImmutableList.copyOf(types);
        return filter(typesCollection, new Predicate<ConfigStatusMessage>() {
            @Override
            public boolean apply(ConfigStatusMessage configStatusMessage) {
                return typesCollection.contains(configStatusMessage.type);
            }
        });
    }

    /**
     * Retrieves all configuration status messages that have one of the given parameter names.
     *
     * @param parameterNames the parameter names to be filtered for; if empty then all messages are delivered
     *
     * @return an unmodifiable collection of the corresponding configuration status messages
     */
    public Collection<ConfigStatusMessage> getConfigStatusMessages(String... parameterNames) {
        final Collection<String> parameterNamesCollection = ImmutableList.copyOf(parameterNames);
        return filter(parameterNamesCollection, new Predicate<ConfigStatusMessage>() {
            @Override
            public boolean apply(ConfigStatusMessage configStatusMessage) {
                return parameterNamesCollection.contains(configStatusMessage.parameterName);
            }
        });
    }

    /**
     * Retrieves all configuration status messages that have one of the given parameter names or types.
     *
     * @param types the types to be filtered for (must not be null)
     * @param parameterNames the parameter names to be filtered for (must not be null)
     *
     * @return an unmodifiable collection of the corresponding configuration status messages
     *
     * @throws NullPointerException if one of types or parameter names collection is empty
     */
    public Collection<ConfigStatusMessage> getConfigStatusMessages(final Collection<Type> types,
            final Collection<String> parameterNames) {
        Preconditions.checkNotNull(types);
        Preconditions.checkNotNull(parameterNames);
        return filterConfigStatusMessages(getConfigStatusMessages(), new Predicate<ConfigStatusMessage>() {
            @Override
            public boolean apply(ConfigStatusMessage configStatusMessage) {
                return types.contains(configStatusMessage.type)
                        || parameterNames.contains(configStatusMessage.parameterName);
            }
        });
    }

    /**
     * Adds the given {@link ConfigStatusMessage}.
     *
     * @param configStatusMessage the configuration status message to be added
     *
     * @throws IllegalArgumentException if given configuration status message is null
     */
    public void add(ConfigStatusMessage configStatusMessage) {
        if (configStatusMessage == null) {
            throw new IllegalArgumentException("Config status message must not be null");
        }
        configStatusMessages.add(configStatusMessage);
    }

    /**
     * Adds the given given {@link ConfigStatusMessage}s.
     *
     * @param configStatusMessages the configuration status messages to be added
     *
     * @throws IllegalArgumentException if given collection is null
     */
    public void add(Collection<ConfigStatusMessage> configStatusMessages) {
        if (configStatusMessages == null) {
            throw new IllegalArgumentException("Config status messages must not be null");
        }
        for (ConfigStatusMessage configStatusMessage : configStatusMessages) {
            add(configStatusMessage);
        }
    }

    private Collection<ConfigStatusMessage> filter(Collection<?> filter, Predicate<ConfigStatusMessage> predicate) {
        if (filter.isEmpty()) {
            return getConfigStatusMessages();
        }
        return filterConfigStatusMessages(getConfigStatusMessages(), predicate);
    }

    private static Collection<ConfigStatusMessage> filterConfigStatusMessages(
            Collection<ConfigStatusMessage> configStatusMessages, Predicate<ConfigStatusMessage> predicate) {
        return Collections.unmodifiableCollection(Collections2.filter(configStatusMessages, predicate));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((configStatusMessages == null) ? 0 : configStatusMessages.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConfigStatusInfo other = (ConfigStatusInfo) obj;
        if (configStatusMessages == null) {
            if (other.configStatusMessages != null)
                return false;
        } else if (!configStatusMessages.equals(other.configStatusMessages))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ConfigStatusInfo [configStatusMessages=" + configStatusMessages + "]";
    }
}
