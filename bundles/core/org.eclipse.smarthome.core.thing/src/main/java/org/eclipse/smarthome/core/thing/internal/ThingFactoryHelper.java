/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingFactory;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Utility methods for creation of Things.
 *
 * It is supposed to contain methods that are commonly shared between {@link ThingManager} and {@link ThingFactory}.
 *
 * @author Simon Kaufmann - Initial contribution and API
 * @author Kai Kreuzer - Changed creation of channels to not require a thing type
 *
 */
public class ThingFactoryHelper {

    private static Logger logger = LoggerFactory.getLogger(ThingFactoryHelper.class);

    /**
     * Create {@link Channel} instances for the given Thing.
     *
     * @param thingType the type of the Thing (must not be null)
     * @param thingUID the Thing's UID (must not be null)
     * @param configDescriptionRegistry {@link ConfigDescriptionRegistry} that will be used to initialize the
     *            {@link Channel}s with their corresponding default values, if given.
     * @return a list of {@link Channel}s
     */
    public static List<Channel> createChannels(ThingType thingType, ThingUID thingUID,
            ConfigDescriptionRegistry configDescriptionRegistry) {
        List<Channel> channels = Lists.newArrayList();
        List<ChannelDefinition> channelDefinitions = thingType.getChannelDefinitions();
        for (ChannelDefinition channelDefinition : channelDefinitions) {
            Channel channel = createChannel(channelDefinition, thingUID, null, configDescriptionRegistry);
            if (channel != null) {
                channels.add(channel);
            }
        }
        List<ChannelGroupDefinition> channelGroupDefinitions = thingType.getChannelGroupDefinitions();
        for (ChannelGroupDefinition channelGroupDefinition : channelGroupDefinitions) {
            ChannelGroupType channelGroupType = TypeResolver.resolve(channelGroupDefinition.getTypeUID());
            if (channelGroupType != null) {
                List<ChannelDefinition> channelGroupChannelDefinitions = channelGroupType.getChannelDefinitions();
                for (ChannelDefinition channelDefinition : channelGroupChannelDefinitions) {
                    Channel channel = createChannel(channelDefinition, thingUID, channelGroupDefinition.getId(),
                            configDescriptionRegistry);
                    if (channel != null) {
                        channels.add(channel);
                    }
                }
            } else {
                logger.warn(
                        "Could not create channels for channel group '{}' for thing type '{}', because channel group type '{}' could not be found.",
                        channelGroupDefinition.getId(), thingUID, channelGroupDefinition.getTypeUID());
            }
        }
        return channels;
    }

    private static Channel createChannel(ChannelDefinition channelDefinition, ThingUID thingUID, String groupId,
            ConfigDescriptionRegistry configDescriptionRegistry) {
        ChannelType type = TypeResolver.resolve(channelDefinition.getChannelTypeUID());
        if (type == null) {
            logger.warn(
                    "Could not create channel '{}' for thing type '{}', because channel type '{}' could not be found.",
                    channelDefinition.getId(), thingUID, channelDefinition.getChannelTypeUID());
            return null;
        }

        ChannelBuilder channelBuilder = ChannelBuilder
                .create(new ChannelUID(thingUID, groupId, channelDefinition.getId()), type.getItemType())
                .withType(type.getUID()).withDefaultTags(type.getTags()).withKind(type.getKind());

        // If we want to override the label, add it...
        if (channelDefinition.getLabel() != null) {
            channelBuilder = channelBuilder.withLabel(channelDefinition.getLabel());
        }

        // If we want to override the description, add it...
        if (channelDefinition.getDescription() != null) {
            channelBuilder = channelBuilder.withDescription(channelDefinition.getDescription());
        }

        // Initialize channel configuration with default-values
        URI channelConfigDescriptionURI = type.getConfigDescriptionURI();
        if (configDescriptionRegistry != null && channelConfigDescriptionURI != null) {
            ConfigDescription cd = configDescriptionRegistry.getConfigDescription(channelConfigDescriptionURI);
            if (cd != null) {
                Configuration config = new Configuration();
                for (ConfigDescriptionParameter param : cd.getParameters()) {
                    String defaultValue = param.getDefault();
                    if (defaultValue != null) {
                        Object value = getDefaultValueAsCorrectType(param.getType(), defaultValue);
                        if (value != null) {
                            config.put(param.getName(), value);
                        }
                    }
                }
                channelBuilder = channelBuilder.withConfiguration(config);
            }
        }

        channelBuilder = channelBuilder.withProperties(channelDefinition.getProperties());

        Channel channel = channelBuilder.build();
        return channel;
    }

    /**
     * Map the provided (default) value of the given {@link Type} to the corresponding Java type.
     *
     * In case the provided value is supposed to be a number and cannot be converted into the target type correctly,
     * this method will return <code>null</code> while logging a warning.
     *
     * @param parameterType the {@link Type} of the value
     * @param defaultValue the value that should be converted
     * @return the given value as the corresponding Java type or <code>null</code> if the value could not be converted
     */
    public static Object getDefaultValueAsCorrectType(Type parameterType, String defaultValue) {
        try {
            switch (parameterType) {
                case TEXT:
                    return defaultValue;
                case BOOLEAN:
                    return Boolean.parseBoolean(defaultValue);
                case INTEGER:
                    return new BigDecimal(defaultValue);
                case DECIMAL:
                    return new BigDecimal(defaultValue);
                default:
                    return null;
            }
        } catch (NumberFormatException ex) {
            LoggerFactory.getLogger(ThingFactory.class).warn("Could not parse default value '{}' as type '{}': {}",
                    defaultValue, parameterType, ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Apply the {@link ThingType}'s default values to the given {@link Configuration}.
     *
     * @param configuration the {@link Configuration} where the default values should be added (may be null, but method
     *            won't have any effect then)
     * @param thingType the {@link ThingType} where to look for the default values (must not be null)
     * @param configDescriptionRegistry the {@link ConfigDescriptionRegistry} to use (may be null, but method won't have
     *            any
     *            effect then)
     */
    public static void applyDefaultConfiguration(Configuration configuration, ThingType thingType,
            ConfigDescriptionRegistry configDescriptionRegistry) {
        if (configDescriptionRegistry != null && configuration != null) {
            // Set default values to thing-configuration
            if (thingType.getConfigDescriptionURI() != null) {
                ConfigDescription thingConfigDescription = configDescriptionRegistry
                        .getConfigDescription(thingType.getConfigDescriptionURI());
                if (thingConfigDescription != null) {
                    for (ConfigDescriptionParameter parameter : thingConfigDescription.getParameters()) {
                        String defaultValue = parameter.getDefault();
                        if (defaultValue != null && configuration.get(parameter.getName()) == null) {
                            Object value = ThingFactoryHelper.getDefaultValueAsCorrectType(parameter.getType(),
                                    defaultValue);
                            if (value != null) {
                                configuration.put(parameter.getName(), value);
                            }
                        }
                    }
                }
            }
        }
    }

}
