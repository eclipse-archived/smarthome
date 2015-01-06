/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.GenericThingBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.BridgeType;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * {@link ThingFactory} helps to create thing based on a given {@link ThingType}
 * .
 * 
 * @author Dennis Nobel - Initial contribution, added support for channel groups
 * @author Benedikt Niehues - fix for Bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=445137 considering default values
 */
public class ThingFactory {

    /**
     * Creates a thing based on a given thing type.
     * 
     * @param thingType
     *            thing type (should not be null)
     * @param thingUID
     *            thindUID (should not be null)
     * @param configuration
     *            (should not be null)
     * @param bridge
     *            (can be null)
     * @return thing
     */
    public static Thing createThing(ThingType thingType, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        return createThing(thingType, thingUID, configuration, bridgeUID, null);
    }
    
    /**
     * Creates a thing based on a given thing type. It also creates the
     * default-configuration given in the configDescriptions if the
     * configDescriptionRegistry is not null
     * 
     * @param thingType
     *            (should not be null)
     * @param thingUID
     *            (should not be null)
     * @param configuration
     *            (should not be null)
     * @param bridgeUID
     *            (can be null)
     * @param configDescriptionRegistry
     *            (can be null)
     * @return thing
     */
    public static Thing createThing(ThingType thingType, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID, ConfigDescriptionRegistry configDescriptionRegistry) {
        if (thingType == null) {
            throw new IllegalArgumentException("The thingType should not be null.");
        }
        if (thingUID == null) {
            throw new IllegalArgumentException("The thingUID should not be null.");
        }

        if (configDescriptionRegistry != null) {
            // Set default values to thing-configuration
            ConfigDescription thingConfigDescription = configDescriptionRegistry.getConfigDescription(thingType
                    .getConfigDescriptionURI());
            if (thingConfigDescription != null) {
                for (ConfigDescriptionParameter parameter : thingConfigDescription.getParameters()) {
                    String defaultValue = parameter.getDefault();
                    if (defaultValue != null && configuration.get(parameter.getName()) == null) {
                        Object value = getDefaultValueAsCorrectType(parameter.getType(), defaultValue);
                        if (value != null) {
                            configuration.put(parameter.getName(), value);
                        }
                    }
                }
            }
        }

        List<Channel> channels = createChannels(thingType, thingUID, configDescriptionRegistry);

        return createThingBuilder(thingType, thingUID).withConfiguration(configuration).withChannels(channels)
                .withBridge(bridgeUID).build();

    }

    /**
     * 
     * Creates a thing based on given thing type.
     * 
     * @param thingType
     *            thing type (should not be null)
     * @param thingUID
     *            thindUID (should not be null)
     * @param configuration
     *            (should not be null)
     * @return thing
     */
    public static Thing createThing(ThingType thingType, ThingUID thingUID,
            Configuration configuration) {

        return createThing(thingType, thingUID, configuration, null);
    }

    private static GenericThingBuilder<?> createThingBuilder(ThingType thingType, ThingUID thingUID) {
        if (thingType instanceof BridgeType) {
            return BridgeBuilder.create(thingUID);
        } else {
            return ThingBuilder.create(thingUID);
        }
    }

    private static List<Channel> createChannels(ThingType thingType, ThingUID thingUID,
            ConfigDescriptionRegistry configDescriptionRegistry) {
        List<Channel> channels = Lists.newArrayList();
        List<ChannelDefinition> channelDefinitions = thingType.getChannelDefinitions();
        for (ChannelDefinition channelDefinition : channelDefinitions) {
            channels.add(createChannel(channelDefinition, thingUID, null, configDescriptionRegistry));
        }
        List<ChannelGroupDefinition> channelGroupDefinitions = thingType.getChannelGroupDefinitions();
        for (ChannelGroupDefinition channelGroupDefinition : channelGroupDefinitions) {
            ChannelGroupType channelGroupType = channelGroupDefinition.getType();
            List<ChannelDefinition> channelGroupChannelDefinitions = channelGroupType.getChannelDefinitions();
            for (ChannelDefinition channelDefinition : channelGroupChannelDefinitions) {
                channels.add(createChannel(channelDefinition, thingUID, channelGroupDefinition.getId(),
                        configDescriptionRegistry));
            }
        }
        return channels;
    }

    private static Channel createChannel(ChannelDefinition channelDefinition, ThingUID thingUID, String groupId,
            ConfigDescriptionRegistry configDescriptionRegistry) {
        ChannelType type = channelDefinition.getType();

        ChannelBuilder channelBuilder = ChannelBuilder.create(
                new ChannelUID(thingUID, groupId, channelDefinition.getId()), type.getItemType()).withDefaultTags(
                type.getTags());

        // initializing channels with default-values
        if (configDescriptionRegistry != null) {
            ConfigDescription cd = configDescriptionRegistry.getConfigDescription(type.getConfigDescriptionURI());
            if (cd != null) {
                Configuration config = new Configuration();
                for (ConfigDescriptionParameter param : cd.getParameters()) {
                    String defaultValue = param.getDefault();
                    if (defaultValue != null) {
                        Object value = getDefaultValueAsCorrectType(param.getType(), defaultValue);
                        if(value != null) {
                            config.put(param.getName(), value);
                        }
                    }
                }
                channelBuilder = channelBuilder.withConfiguration(config);
            }
        }

        Channel channel = channelBuilder.build();
        return channel;
    }

    private static Object getDefaultValueAsCorrectType(Type parameterType, String defaultValue) {
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
        	LoggerFactory.getLogger(ThingFactory.class).warn(
                    "Could not parse default value '" + defaultValue + "' as type '" + parameterType + "': "
                            + ex.getMessage(), ex);
            return null;
        }
    }

}
