/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding;

import java.util.List;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
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
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ThingType;

import com.google.common.collect.Lists;

/**
 * {@link ThingFactory} helps to create thing based on a given {@link ThingType}
 * .
 * 
 * @author Dennis Nobel - Initial contribution
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
    public static Thing createThing(ThingType thingType, ThingUID thingUID,
            Configuration configuration, Bridge bridge) {
    	if (thingType == null) {
    		throw new IllegalArgumentException("The thingType should not be null.");
    	}
    	if (thingUID == null) {
    		throw new IllegalArgumentException("The thingUID should not be null.");
    	}
        List<Channel> channels = createChannels(thingType, thingUID);

        return createThingBuilder(thingType, thingUID).withConfiguration(configuration)
                .withChannels(channels).withBridge(bridge).build();
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
            return BridgeBuilder.create(thingType.getUID(), thingUID.getId());
        } else {
            return ThingBuilder.create(thingType.getUID(), thingUID.getId());
        }
    }

    private static List<Channel> createChannels(ThingType thingType, ThingUID thingUID) {
        List<Channel> channels = Lists.newArrayList();
        List<ChannelDefinition> channelDefinitions = thingType.getChannelDefinitions();
        for (ChannelDefinition channelDefinition : channelDefinitions) {
            channels.add(createChannel(channelDefinition, thingUID));
        }
        return channels;
    }

    private static Channel createChannel(ChannelDefinition channelDefinition, ThingUID thingUID) {
        ChannelType type = channelDefinition.getType();
        Channel channel = ChannelBuilder.create(
                new ChannelUID(thingUID, channelDefinition.getId()), type.getItemType()).build();
        return channel;
    }

}
