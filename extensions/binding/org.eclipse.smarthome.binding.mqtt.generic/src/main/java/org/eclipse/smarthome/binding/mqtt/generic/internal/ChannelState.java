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
package org.eclipse.smarthome.binding.mqtt.generic.internal;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.values.Value;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttMessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This object consists of an {@link Value}, which is updated on the respective MQTT topic change.
 * Updates to the value are propagated via the {@link ChannelStateUpdateListener}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ChannelState implements MqttMessageSubscriber {
    private final Logger logger = LoggerFactory.getLogger(ChannelState.class);

    protected @Nullable MqttBrokerConnection connection;
    private final String stateTopic;
    private final String regexStateTopic;
    private final String commandTopic;
    private boolean retained;
    private boolean readOnly = false;
    protected final ChannelUID channelUID;
    protected final Value value;

    private @Nullable ChannelStateUpdateListener channelStateUpdateListener;
    protected boolean hasSubscribed = false;
    private @Nullable ScheduledFuture<?> scheduledFuture;
    private CompletableFuture<@Nullable Void> future = new CompletableFuture<>();

    /**
     * Creates a new channel state.
     *
     * @param stateTopic The state topic. Might be null for a no-state channel
     * @param commandTopic The command topic, Might be null for a read-only channel
     * @param channelUID The channelUID is used for the {@link ChannelStateUpdateListener} to notify about value changes
     * @param value The channel state value.
     */
    public ChannelState(@Nullable String stateTopic, @Nullable String commandTopic, ChannelUID channelUID, Value value,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener) {
        this.channelStateUpdateListener = channelStateUpdateListener;
        this.stateTopic = stateTopic == null ? "" : stateTopic;
        this.regexStateTopic = StringUtils
                .replace(StringUtils.replace(Matcher.quoteReplacement(this.stateTopic), "+", "[^/]*"), "#", ".*");
        this.commandTopic = commandTopic == null ? "" : commandTopic;
        this.channelUID = channelUID;
        this.value = value;
        this.retained = StringUtils.isNotBlank(stateTopic);
    }

    /**
     * Override the automatically determined retained flag.
     * Usually a channel publishes retained if a stateTopic is given.
     *
     * @param retained MQTT retained
     */
    public void setRetained(boolean retained) {
        this.retained = retained;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    /**
     * Returns the value state object of this message subscriber.
     */
    public Value getValue() {
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
     * @param topic The topic. Is the same as the field stateTopic.
     * @param payload The byte payload. Must be UTF8 encoded text.
     *            Some clients may decide to encode their own binary number or struct types.
     *            We do not and cannot support those here though.
     */
    @Override
    public void processMessage(String topic, byte[] payload) {
        final ChannelStateUpdateListener channelStateUpdateListener = this.getChannelStateUpdateListener();
        if (channelStateUpdateListener == null) {
            logger.warn("MQTT message received, but MessageSubscriber object hasn't been started!", topic);
            return;
        }
        if (!topic.matches(regexStateTopic)) {
            logger.warn("Received MQTT data on {}. This does not match the computed regex: {}", topic, regexStateTopic);
            return;
        }

        if (readOnly) {
            logger.warn("Received MQTT data on {}. This channel is read only!", topic);
            return;
        }

        try {
            final String str = postProcessIncomingValue(new String(payload, StandardCharsets.UTF_8));
            final State updatedState = value.update(str);
            channelStateUpdateListener.updateChannelState(channelUID, updatedState);
        } catch (TransformationException e) {
            logger.warn("Error executing the transformation", e);
        } catch (IllegalArgumentException e) {
            logger.warn("Incoming payload '{}' not supported by type '{}'", new String(payload, StandardCharsets.UTF_8),
                    value.getClass().getSimpleName());
        }

        receivedOrTimeout();
    }

    /**
     * Returns the state topic. Might be an empty string if this is a stateless channel (TRIGGER kind channel).
     */
    public String getStateTopic() {
        return stateTopic;
    }

    /**
     * Return the command topic. Might be an empty string, if this is a read-only channel.
     */
    public String getCommandTopic() {
        return commandTopic;
    }

    /**
     * Returns the channelType ID which also happens to be an item-type
     */
    public String getItemType() {
        return value.channelTypeID();
    }

    /**
     * Returns true if this is a stateful channel.
     */
    public boolean isStateful() {
        return retained;
    }

    /**
     * Removes the subscription to the state topic and resets the channelStateUpdateListener.
     *
     * @return A future that completes with true if unsubscribing from the state topic succeeded.
     *         It completes with false if no connection is established and completes exceptionally otherwise.
     */
    public CompletableFuture<@Nullable Void> stop() {
        final MqttBrokerConnection connection = this.connection;
        if (connection != null && StringUtils.isNotBlank(stateTopic)) {
            return connection.unsubscribe(stateTopic, this).thenRun(this::internalStop);
        } else {
            internalStop();
            return CompletableFuture.completedFuture(null);
        }
    }

    private void internalStop() {
        this.connection = null;
        this.channelStateUpdateListener = null;
        hasSubscribed = false;
        value.resetState();
    }

    private void receivedOrTimeout() {
        final ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
        if (scheduledFuture != null) { // Cancel timeout
            scheduledFuture.cancel(false);
            this.scheduledFuture = null;
        }
        future.complete(null);
    }

    private @Nullable Void subscribeFail(Throwable e) {
        final ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
        if (scheduledFuture != null) { // Cancel timeout
            scheduledFuture.cancel(false);
            this.scheduledFuture = null;
        }
        future.completeExceptionally(e);
        return null;
    }

    /**
     * Subscribes to the state topic on the given connection and informs about updates on the given listener.
     *
     * @param connection A broker connection
     * @param scheduler A scheduler to realize the timeout
     * @param timeout A timeout in milliseconds. Can be 0 to disable the timeout and let the future return earlier.
     * @param channelStateUpdateListener An update listener
     * @return A future that completes with true if the subscribing worked, with false if the stateTopic is not set
     *         and exceptionally otherwise.
     */
    public CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection, ScheduledExecutorService scheduler,
            int timeout) {
        if (hasSubscribed || StringUtils.isBlank(stateTopic)) {
            return CompletableFuture.completedFuture(null);
        }

        this.connection = connection;
        this.future = new CompletableFuture<>();
        connection.subscribe(stateTopic, this).thenRun(() -> {
            hasSubscribed = true;
            if (timeout > 0 && !future.isDone()) {
                this.scheduledFuture = scheduler.schedule(this::receivedOrTimeout, timeout, TimeUnit.MILLISECONDS);
            } else {
                receivedOrTimeout();
            }
        }).exceptionally(this::subscribeFail);
        return future;
    }

    /**
     * Return true if this channel has subscribed to its MQTT topics.
     * You need to call {@link #start(MqttBrokerConnection, ScheduledExecutorService, int)} and
     * have a stateTopic set, to subscribe this channel.
     */
    public boolean hasSubscribed() {
        return this.hasSubscribed;
    }

    /**
     * Publishes a value on MQTT. A command topic needs to be set in the configuration.
     *
     * @param command The command to send
     * @return A future that completes with true if the publishing worked and false and/or exceptionally otherwise.
     */
    public CompletableFuture<@Nullable Void> setValue(Command command) {
        final String mqttCommandValue = getValue().update(command);

        final MqttBrokerConnection connection = this.connection;
        if (!commandTopic.isEmpty() && connection != null) {
            // Send retained messages if this is a stateful channel
            return connection.publish(commandTopic, mqttCommandValue.getBytes(), 1, retained).thenRun(() -> {
            });
        } else {
            CompletableFuture<@Nullable Void> f = new CompletableFuture<>();
            f.completeExceptionally(new IllegalStateException("No connection or no command topic!"));
            return f;
        }
    }

    /**
     * @return The channelStateUpdateListener
     */
    public @Nullable ChannelStateUpdateListener getChannelStateUpdateListener() {
        return channelStateUpdateListener;
    }

    /**
     * @param channelStateUpdateListener The channelStateUpdateListener to set
     */
    public void setChannelStateUpdateListener(ChannelStateUpdateListener channelStateUpdateListener) {
        this.channelStateUpdateListener = channelStateUpdateListener;
    }

}
