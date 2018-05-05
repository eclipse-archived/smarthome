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
package org.eclipse.smarthome.binding.mqtt.handler;

import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;

/**
 * This base implementation handles connection changes of the {@link MqttBrokerConnection}
 * and puts the Thing on or offline. It also handles adding/removing notifications of the
 * {@link MqttService} and provides a basic dispose() implementation.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractBrokerHandler extends BaseBridgeHandler implements MqttConnectionObserver {
    public static int TIMEOUT_DEFAULT = 1200; /* timeout in milliseconds */
    protected final String brokerID;
    protected @Nullable MqttBrokerConnection connection;

    public AbstractBrokerHandler(Bridge thing) {
        super(thing);
        this.brokerID = thing.getUID().getId();
    }

    /**
     * Returns the underlying {@link MqttBrokerConnection}.
     */
    public @Nullable MqttBrokerConnection getConnection() {
        return connection;
    }

    /**
     * Does nothing in the base implementation.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands to handle
    }

    /**
     * Registers a connection status listener and attempts a connection. This should be called only once per connection
     * object. A connection usually tries to reconnect to the server via the configured reconnect strategy.
     *
     */
    @Override
    public void initialize() {
        MqttBrokerConnection c = connection;
        if (c == null) {
            return;
        }
        c.addConnectionObserver(this);

        c.start().exceptionally(e -> {
            connectionStateChanged(MqttConnectionState.DISCONNECTED, e);
            return false;
        }).thenAccept(v -> {
            if (!v) {
                connectionStateChanged(MqttConnectionState.DISCONNECTED, new TimeoutException("Timeout"));
            }
        });
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
        if (state == MqttConnectionState.CONNECTED) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            if (error == null) {
                updateStatus(ThingStatus.OFFLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error.getMessage());
            }
        }
    }

    /**
     * Removes listeners to the {@link MqttBrokerConnection}.
     */
    @Override
    public void dispose() {
        if (connection != null) {
            connection.removeConnectionObserver(this);
            connection = null;
        }
        super.dispose();
    }
}
