package org.eclipse.smarthome.core.thing.binding;

import java.util.List;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.BridgeType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelDefinition;
import org.eclipse.smarthome.core.thing.ChannelType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingType;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.GenericThingBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;

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
     *            thing type (can not be null)
     * @param thingUID
     *            thindUID (can not be null)
     * @param configuration
     *            (can not be null)
     * @param bridge
     *            (can be null)
     * @return thing
     */
    public static Thing createThing(ThingType thingType, ThingUID thingUID,
            Configuration configuration, Bridge bridge) {

        List<Channel> channels = createChannels(thingType, thingUID);

        return createThingBuilder(thingType, thingUID).withConfiguration(configuration)
                .withChannels(channels).withBridge(bridge).build();
    }

    /**
     * 
     * Creates a thing for based on given thing type.
     * 
     * @param thingType
     *            thing type (can not be null)
     * @param thingUID
     *            thindUID (can not be null)
     * @param configuration
     *            (can not be null)
     * @return thing
     */
    public static Thing createThing(ThingType thingType, ThingUID thingUID,
            Configuration configuration) {

        List<Channel> channels = createChannels(thingType, thingUID);

        return createThingBuilder(thingType, thingUID).withConfiguration(configuration)
                .withChannels(channels).build();
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
