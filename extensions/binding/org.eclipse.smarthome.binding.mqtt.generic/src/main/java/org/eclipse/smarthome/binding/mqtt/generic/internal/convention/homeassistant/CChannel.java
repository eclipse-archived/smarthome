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
package org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homeassistant;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.ChannelState;
import org.eclipse.smarthome.binding.mqtt.generic.internal.MqttBindingConstants;
import org.eclipse.smarthome.binding.mqtt.generic.internal.values.AbstractMqttThingValue;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * An {@link AbstractComponent}s derived class consists of one or multiple channels.
 * Each component channel consists of the determined ESH channel type, channel type UID and the
 * ESH channel description itself as well as the the channels state.
 *
 * After the discovery process has completed and the tree of components and component channels
 * have been built up, the channel types are registered to a custom channel type provider
 * before adding the channel descriptions to the ESH Thing themselves.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class CChannel {
    public final String channelID; // Channel ID
    public final ChannelState channelState; // Channel state (value)
    public final Channel channel; // ESH Channel
    public final ChannelType type;
    public final ChannelTypeUID channelTypeUID;

    public CChannel(ThingUID thing, String componentID, String channelID, AbstractMqttThingValue valueState,
            @Nullable String state_topic, @Nullable String command_topic, String name, String unit) {
        this.channelID = channelID;
        final ChannelUID channelUID = new ChannelUID(thing, thing.getId() + "_" + componentID, channelID);
        channelTypeUID = new ChannelTypeUID(MqttBindingConstants.BINDING_ID,
                thing.getId() + "_" + componentID + "_" + channelID);

        channelState = new ChannelState(state_topic, command_topic, channelUID, valueState);

        if (StringUtils.isBlank(state_topic)) {
            type = ChannelTypeBuilder.trigger(channelTypeUID, name).build();
        } else {
            type = ChannelTypeBuilder.state(channelTypeUID, name, channelState.getItemType())
                    .withStateDescription(valueState.createStateDescription(unit, command_topic == null)).build();
        }

        channel = ChannelBuilder.create(channelUID, channelState.getItemType()).withType(channelTypeUID)
                .withKind(type.getKind()).withLabel(name).build();
    }
}
