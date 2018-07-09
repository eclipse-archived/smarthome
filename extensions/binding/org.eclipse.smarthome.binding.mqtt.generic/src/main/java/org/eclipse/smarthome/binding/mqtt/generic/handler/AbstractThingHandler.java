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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.MqttChannelTypeProvider;
import org.eclipse.smarthome.binding.mqtt.generic.internal.TransformationServiceProvider;
import org.eclipse.smarthome.binding.mqtt.handler.AbstractBrokerHandler;
import org.eclipse.smarthome.core.thing.Bridge;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base handler
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractThingHandler extends BaseThingHandler implements ChannelStateUpdateListener {
    private final Logger logger = LoggerFactory.getLogger(AbstractThingHandler.class);
    protected final @Nullable TransformationServiceProvider transformationServiceProvider;
    protected final MqttChannelTypeProvider provider;

    protected @Nullable MqttBrokerConnection connection;

    public AbstractThingHandler(Thing thing, MqttChannelTypeProvider provider,
            @Nullable TransformationServiceProvider transformationServiceProvider) {
        super(thing);
        this.provider = provider;
        this.transformationServiceProvider = transformationServiceProvider;
    }

    /**
     * Return the channel state for the given channelUID.
     *
     * @param channelUID The channelUID
     * @return A channel state. May be null.
     */
    abstract protected @Nullable ChannelState getChannelState(ChannelUID channelUID);

    /**
     * Start the topic discovery and subscribe to all channel state topics on all {@link ChannelState}s.
     * Put the thing ONLINE or OFFLINE.
     *
     * @param connection A started broker connection
     */
    abstract protected void start(MqttBrokerConnection connection) throws MqttException;

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (connection == null) {
            return;
        }

        final @Nullable ChannelState data = getChannelState(channelUID);

        if (data == null) {
            logger.warn("Channel {} not supported", channelUID.getId());
            return;
        }

        if (command instanceof RefreshType || data.isReadOnly()) {
            updateState(channelUID.getId(), data.getValue().getValue());
            return;
        }

        data.setValue(command)
                .thenRun(() -> logger.debug("Successfully published value {} to topic {}", command, data.getTopic()))
                .exceptionally(e -> {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
                    return null;
                });
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            connection = null;
            return;
        }
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            return;
        }

        AbstractBrokerHandler h = getBridgeHandler();
        if (h == null) {
            return;
        }
        connection = h.getConnection();
        MqttBrokerConnection connection = this.connection;
        if (connection != null) {
            try {
                start(connection);
            } catch (MqttException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            }
        }
    }

    /**
     * Return the bride handler. The bridge is from the "mqtt" bundle.
     */
    protected @Nullable AbstractBrokerHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        return (AbstractBrokerHandler) bridge.getHandler();
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
        bridgeStatusChanged(getBridgeStatus());
    }

    @Override
    public void updateChannelState(ChannelUID channelUID, State value) {
        super.updateState(channelUID, value);
    }

}
