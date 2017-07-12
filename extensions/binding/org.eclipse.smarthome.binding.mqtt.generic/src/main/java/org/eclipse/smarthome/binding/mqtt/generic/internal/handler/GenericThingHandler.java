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
package org.eclipse.smarthome.binding.mqtt.generic.internal.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.ChannelState;
import org.eclipse.smarthome.binding.mqtt.generic.internal.ChannelStateUpdateListener;
import org.eclipse.smarthome.binding.mqtt.generic.internal.ChannelStateWithTransformation;
import org.eclipse.smarthome.binding.mqtt.generic.internal.MqttChannelTypeProvider;
import org.eclipse.smarthome.binding.mqtt.generic.internal.TransformationServiceProvider;
import org.eclipse.smarthome.binding.mqtt.generic.internal.values.AbstractMqttThingValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.values.ValueFactory;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler manages manual created Things with manually added channels to link to MQTT topics.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class GenericThingHandler extends AbstractMQTTThingHandler implements ChannelStateUpdateListener {
    private final Logger logger = LoggerFactory.getLogger(GenericThingHandler.class);
    final Map<ChannelUID, ChannelState> channelStateByChannelUID = new HashMap<>();

    public GenericThingHandler(Thing thing, MqttChannelTypeProvider provider,
            @Nullable TransformationServiceProvider transformationServiceProvider, int subscribeTimeout) {
        super(thing, provider, transformationServiceProvider, subscribeTimeout);
    }

    @Override
    protected @Nullable ChannelState getChannelState(ChannelUID channelUID) {
        return channelStateByChannelUID.get(channelUID);
    }

    /**
     * Subscribe on all channel static topics on all {@link ChannelState}s.
     * If subscribing on all channels worked, the thing is put ONLINE, else OFFLINE.
     *
     * @param connection A started broker connection
     */
    @Override
    protected CompletableFuture<Void> start(MqttBrokerConnection connection) {
        List<CompletableFuture<Boolean>> futures = channelStateByChannelUID.values().stream()
                .map(c -> c.start(connection, this)).collect(Collectors.toList());
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).thenRun(() -> {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        });
    }

    @Override
    public void dispose() {
        if (connection != null) {
            channelStateByChannelUID.values().forEach(ChannelState::stop);
            connection = null;
        }
        channelStateByChannelUID.clear();
        super.dispose();
    }

    /**
     * For every Thing channel there exists a corresponding {@link ChannelState}. It consists of the MQTT state and
     * MQTT command topic, the ChannelUID and a value state.
     *
     * @param channelConfig The channel configuration that contains MQTT state and command topic and multiple other
     *            configurations.
     * @param channelUID The channel UID
     * @param valueState The channel value state
     * @return
     */
    protected ChannelState createChannelState(GenericChannelConfig channelConfig, ChannelUID channelUID,
            AbstractMqttThingValue valueState) {
        TransformationServiceProvider transformationServiceProvider = this.transformationServiceProvider;
        if (transformationServiceProvider != null) {
            return new ChannelStateWithTransformation(channelConfig.stateTopic, channelConfig.commandTopic,
                    channelConfig.transformationPattern, channelUID, valueState, transformationServiceProvider);
        } else {
            return new ChannelState(channelConfig.stateTopic, channelConfig.commandTopic, channelUID, valueState);
        }
    }

    @Override
    public void initialize() {
        for (Channel channel : thing.getChannels()) {
            final ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
            if (channelTypeUID == null) {
                logger.warn("Channel {} has no type", channel.getLabel());
                continue;
            }
            final GenericChannelConfig channelConfig = channel.getConfiguration().as(GenericChannelConfig.class);
            ChannelState channelState = createChannelState(channelConfig, channel.getUID(),
                    ValueFactory.createValueState(channelConfig, channelTypeUID.getId()));
            channelStateByChannelUID.put(channel.getUID(), channelState);
        }

        super.initialize();
    }
}
