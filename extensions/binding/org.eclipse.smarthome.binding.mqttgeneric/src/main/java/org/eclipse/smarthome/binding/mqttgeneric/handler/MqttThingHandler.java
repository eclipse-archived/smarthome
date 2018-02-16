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
package org.eclipse.smarthome.binding.mqttgeneric.handler;

import static org.eclipse.smarthome.binding.mqttgeneric.MqttBrokerBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.mqttgeneric.internal.NumberValue;
import org.eclipse.smarthome.binding.mqttgeneric.internal.OnOffValue;
import org.eclipse.smarthome.binding.mqttgeneric.internal.PercentValue;
import org.eclipse.smarthome.binding.mqttgeneric.internal.TextValue;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.eclipse.smarthome.io.transport.mqtt.MqttPublishCallback;
import org.eclipse.smarthome.io.transport.mqtt.MqttPublishResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler connects an Mqtt topic state (and command topic) to a Thing.
 *
 * @author David Graeff - Initial contribution
 */
public class MqttThingHandler extends BaseThingHandler implements ChannelStateUpdateListener {

    private final Logger logger = LoggerFactory.getLogger(MqttThingHandler.class);

    private final TransformationServiceProvider transformationServiceProvider;

    private final MqttPublishCallback listener = new MqttPublishCallback() {
        @Override
        public void onSuccess(MqttPublishResult result) {
            logger.debug("Successfully published value to topic {}. ID: {}", result.getTopic(), result.getMessageID());
        }

        @Override
        public void onFailure(MqttPublishResult result, Throwable error) {
            logger.warn("Could not publish value to broker on topic {} with message ID {}", result.getTopic(),
                    result.getMessageID(), error);
        }
    };

    // package private for tests
    final Map<ChannelUID, ChannelConfig> channelDataByChannelUID = new HashMap<>();
    // package private for tests
    MqttBrokerConnection connection;

    public MqttThingHandler(Thing thing, TransformationServiceProvider transformationServiceProvider) {
        super(thing);
        this.transformationServiceProvider = transformationServiceProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (connection == null) {
            return;
        }

        ChannelConfig data = channelDataByChannelUID.get(channelUID);

        if (data == null) {
            logger.warn("Channel {} not supported", channelUID.getId());
            return;
        }

        if (command instanceof RefreshType) {
            updateState(channelUID.getId(), data.value.getValue());
            return;
        }

        String mqttCommandValue = data.value.update(command);

        if (StringUtils.isNotBlank(data.commandTopic)) {
            try {
                connection.publish(data.commandTopic, mqttCommandValue.getBytes(), listener);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid percentage/number value {}", command.toString(), e);
            } catch (MqttException e) {
                logger.warn("Could not publish new value {} to broker", command.toString(), e);
            }
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            // If the bridge is online, no handler null check needs to be performed
            try {
                MqttBrokerConnectionHandler h = getBridgeHandler();
                connection = h.getConnection();
                for (ChannelConfig channelConfig : channelDataByChannelUID.values()) {
                    channelConfig.start(connection, this);
                }
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            } catch (MqttException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            }
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            connection = null;
        }
    }

    @Override
    public void dispose() {
        if (connection != null) {
            for (ChannelConfig channelConfig : channelDataByChannelUID.values()) {
                channelConfig.dispose();
            }
            connection = null;
        }
        channelDataByChannelUID.clear();
    }

    /**
     * Return the bride handler.
     * We make this an explicit function for test mocks.
     */
    protected MqttBrokerConnectionHandler getBridgeHandler() {
        return (MqttBrokerConnectionHandler) getBridge().getHandler();
    }

    /**
     * Return the bridge status.
     * We make this an explicit function for test mocks.
     */
    protected ThingStatusInfo getBridgeStatus() {
        Bridge b = getBridge();
        if (b != null) {
            return b.getStatusInfo();
        } else {
            return new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null);
        }
    }

    @Override
    public void initialize() {
        for (Channel channel : thing.getChannels()) {
            ChannelConfig config = channel.getConfiguration().as(ChannelConfig.class);
            config.channelUID = channel.getUID();
            config.transformationServiceProvider = transformationServiceProvider;

            if (StringUtils.isNotBlank(config.transformationPattern)) {
                int index = config.transformationPattern.indexOf(':');
                if (index == -1) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "The transformation pattern must consist of the type and the pattern separated by a colon");
                    return;
                }
                String type = config.transformationPattern.substring(0, index).toUpperCase();
                config.transformationPattern = config.transformationPattern.substring(index + 1);
                config.transformationServiceName = type;
            }

            switch (channel.getChannelTypeUID().getId()) {
                case TEXT_CHANNEL:
                    config.value = new TextValue();
                    break;
                case NUMBER_CHANNEL:
                    config.value = new NumberValue(config.isFloat, config.step);
                    break;
                case PERCENTAGE_CHANNEL:
                    config.value = new PercentValue(config.isFloat, config.min, config.max, config.step);
                    break;
                case ONOFF_CHANNEL:
                    config.value = new OnOffValue(config.on, config.off, config.inverse);
                    break;
                default:
                    throw new IllegalArgumentException("ThingTypeUID not recognised");
            }

            channelDataByChannelUID.put(channel.getUID(), config);
        }

        bridgeStatusChanged(getBridgeStatus());
    }

    @Override
    public void channelStateUpdated(ChannelUID channelUID, State value) {
        updateState(channelUID.getId(), value);
    }

}
