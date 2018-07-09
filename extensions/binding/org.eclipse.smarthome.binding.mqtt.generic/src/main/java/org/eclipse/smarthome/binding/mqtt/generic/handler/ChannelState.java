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

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.AbstractMqttThingValue;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.eclipse.smarthome.io.transport.mqtt.MqttMessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This object consists of an {@link AbstractMqttThingValue}, which is updated on the respective MQTT topic change.
 * Updates to the value are propagated via the {@link ChannelStateUpdateListener}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ChannelState implements MqttMessageSubscriber {
    private final Logger logger = LoggerFactory.getLogger(ChannelState.class);

    protected @Nullable MqttBrokerConnection connection;
    private final String stateTopic;
    private final String commandTopic;
    protected final ChannelUID channelUID;
    protected final AbstractMqttThingValue value;
    protected @Nullable ChannelStateUpdateListener channelStateUpdateListener;
    protected boolean hasSubscribed = false;

    /**
     * Creates a new channel state.
     *
     * @param config The channel configuration
     * @param channelUID The channelUID is used for the {@link ChannelStateUpdateListener} to notify about value changes
     * @param value The channel state value.
     */
    public ChannelState(String stateTopic, String commandTopic, ChannelUID channelUID, AbstractMqttThingValue value) {
        this.stateTopic = stateTopic;
        this.commandTopic = commandTopic;
        this.channelUID = channelUID;
        this.value = value;
    }

    /**
     * Returns the value state object of this message subscriber.
     */
    public AbstractMqttThingValue getValue() {
        return value;
    }

    /**
     * Return the channelUID
     */
    public ChannelUID channelUID() {
        return channelUID;
    }

    /**
     * Overwrite to process the incoming value. The base implementation just passes the value.
     *
     * @throws TransformationException
     */
    protected String postProcessIncomingValue(String value) throws TransformationException {
        return value;
    }

    /**
     * Incoming message from the MqttBrokerConnection
     *
     * @param topic The topic. Should be the same as the field stateTopic.
     * @param payload The byte payload. Must be UTF8 encoded text.
     *            Some clients may decide to encode their own binary number or struct types.
     *            We do not and cannot support those here though.
     */
    @Override
    public void processMessage(String topic, byte[] payload) {
        ChannelStateUpdateListener channelStateUpdateListener = this.channelStateUpdateListener;
        if (channelStateUpdateListener == null) {
            logger.trace("MQTT message received, but MessageSubscriber object hasn't been started!", topic);
            return;
        }
        if (!topic.equals(stateTopic)) {
            logger.trace("Received MQTT data on {}. Does not match configured thing {}", topic, stateTopic);
            return;
        }

        try {
            String str = postProcessIncomingValue(new String(payload, StandardCharsets.UTF_8));
            channelStateUpdateListener.updateChannelState(channelUID, getValue().update(str));
        } catch (TransformationException e) {
            logger.error("Error executing the transformation", e);
        } catch (IllegalArgumentException e) {
            logger.warn("Incoming payload '{}' not supported by type '{}'", new String(payload, StandardCharsets.UTF_8),
                    getValue().getClass().getSimpleName());
        }
    }

    /**
     * Returns the state topic. Might be an empty string if this is a stateless channel (TRIGGER kind channel).
     *
     * We register the whole object as a message consumer. The getTopic() method tells
     * the MqttBrokerConnection object in which topic we are interested in.
     */
    public String getTopic() {
        return stateTopic;
    }

    /**
     * Returns the channelType ID which also happens to be an item-type
     */
    public String getItemType() {
        return value.channelTypeID();
    }

    /**
     * Removes the subscription to the state topic.
     */
    @SuppressWarnings("null")
    public void stop() {
        if (connection != null) {
            if (StringUtils.isNotBlank(stateTopic)) {
                connection.unsubscribe(stateTopic, this);
            }
            connection = null;
            channelStateUpdateListener = null;
            hasSubscribed = false;
        }
    }

    /**
     * Subscribes to the state topic on the given connection and informs about updates on the given listener.
     *
     * @param connection A broker connection
     * @param channelStateUpdateListener An update listener
     * @throws MqttException Subscribing on the connection may cause a MqttException
     */
    public CompletableFuture<Boolean> start(MqttBrokerConnection connection,
            ChannelStateUpdateListener channelStateUpdateListener) throws MqttException {
        this.connection = connection;
        this.channelStateUpdateListener = channelStateUpdateListener;
        if (!hasSubscribed && StringUtils.isNotBlank(stateTopic)) {
            connection.subscribe(stateTopic, this);
            hasSubscribed = true;
            return CompletableFuture.completedFuture(true);
        }
        return CompletableFuture.completedFuture(false);
    }

    /**
     * Publishes a value on MQTT. A command topic needs to be set in the configuration.
     *
     * @param command The command to send
     * @param listener This is an async method. Use this callback to get notified of the result.
     * @return A future that completes with true if the publishing worked and false and/or exceptionally otherwise.
     */
    public CompletableFuture<Boolean> setValue(Command command) {
        String mqttCommandValue = getValue().update(command);
        String commandTopic = this.commandTopic;

        if (StringUtils.isNotBlank(commandTopic) && connection != null) {
            return connection.publish(commandTopic, mqttCommandValue.getBytes());
        } else {
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Return the command topic. Might be an empty string, if this is a read-only channel.
     */
    public String getCommandTopic() {
        return commandTopic;
    }

    /**
     * Returns true if this is a read-only state value.
     */
    public boolean isReadOnly() {
        return commandTopic.isEmpty();
    }
}