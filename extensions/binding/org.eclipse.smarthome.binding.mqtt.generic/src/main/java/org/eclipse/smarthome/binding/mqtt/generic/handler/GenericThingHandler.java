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
package org.eclipse.smarthome.binding.mqtt.generic.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.AbstractMqttThingValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.ColorValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.MqttChannelTypeProvider;
import org.eclipse.smarthome.binding.mqtt.generic.internal.NumberValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.OnOffValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.TextValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.TransformationServiceProvider;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;

/**
 * This handler manages manual created Things with manually added channels to link to MQTT topics.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class GenericThingHandler extends AbstractThingHandler implements ChannelStateUpdateListener {
    final Map<ChannelUID, ChannelState> channelDataByChannelUID = new HashMap<>();

    public GenericThingHandler(Thing thing, MqttChannelTypeProvider provider,
            @Nullable TransformationServiceProvider transformationServiceProvider) {
        super(thing, provider, transformationServiceProvider);

    }

    @Override
    protected @Nullable ChannelState getChannelState(ChannelUID channelUID) {
        return channelDataByChannelUID.get(channelUID);
    }

    /**
     * Subscribe on all channel static topics on all {@link ChannelState}s.
     * If subscribing on all channels worked, the thing is put ONLINE, else OFFLINE.
     *
     * @param connection A started broker connection
     */
    @Override
    protected void start(MqttBrokerConnection connection) throws MqttException {
        for (ChannelState c : channelDataByChannelUID.values()) {
            c.start(connection, this);
        }
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Override
    public void dispose() {
        if (connection != null) {
            channelDataByChannelUID.values().forEach(c -> c.stop());
            connection = null;
        }
        channelDataByChannelUID.clear();
        super.dispose();
    }

    /**
     * Creates a new channel state value.
     *
     * @param config The channel configuration
     * @param channelTypeID The channel type, for instance TEXT_CHANNEL.
     */
    public static AbstractMqttThingValue createValueState(ChannelConfig config, String channelTypeID) {
        AbstractMqttThingValue value;
        switch (channelTypeID) {
            case CoreItemFactory.STRING:
                value = StringUtils.isBlank(config.allowedStates) ? new TextValue()
                        : new TextValue(config.allowedStates.split(","));
                break;
            case CoreItemFactory.NUMBER:
                value = new NumberValue(config.isFloat, config.min, config.max, config.step, false);
                break;
            case CoreItemFactory.DIMMER:
                value = new NumberValue(config.isFloat, config.min, config.max, config.step, true);
                break;
            case CoreItemFactory.COLOR:
                value = new ColorValue(config.isRGB);
                break;
            case CoreItemFactory.SWITCH:
                value = new OnOffValue(config.on, config.off, config.inverse, true);
                break;
            case CoreItemFactory.CONTACT:
                value = new OnOffValue(config.on, config.off, config.inverse, false);
                break;
            default:
                throw new IllegalArgumentException("ChannelTypeUID not recognised");
        }
        return value;
    }

    protected ChannelState createMessageSubscriber(ChannelConfig channelConfig, ChannelUID channelUID,
            String channelTypeID) {
        TransformationServiceProvider transformationServiceProvider = this.transformationServiceProvider;
        AbstractMqttThingValue valueState = createValueState(channelConfig, channelTypeID);
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
            ChannelState subscriber;
            try {
                ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
                if (channelTypeUID == null) {
                    throw new IllegalArgumentException("Channel has no type");
                }
                subscriber = createMessageSubscriber(channel.getConfiguration().as(ChannelConfig.class),
                        channel.getUID(), channelTypeUID.getId());
            } catch (IllegalArgumentException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                return;
            }

            channelDataByChannelUID.put(channel.getUID(), subscriber);
        }

        super.initialize();
    }
}
