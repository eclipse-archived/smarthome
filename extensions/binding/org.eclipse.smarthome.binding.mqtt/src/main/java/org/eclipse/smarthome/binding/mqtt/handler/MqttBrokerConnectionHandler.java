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

import static org.eclipse.smarthome.binding.mqtt.MqttBrokerBindingConstants.PARAM_BRIDGE_name;

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
import org.eclipse.smarthome.io.transport.mqtt.MqttServiceObserver;

/**
 * This bridge broker connection listens to changes of the {@link MqttBrokerConnection}
 * and puts the Thing on or offline. It also handles adding/removing notifications of the
 * {@link MqttService}.
 *
 * @author David Graeff - Initial contribution
 */
public class MqttBrokerConnectionHandler extends BaseBridgeHandler
        implements MqttConnectionObserver, MqttServiceObserver {

    private final MqttService service;
    private String brokerName;
    private MqttBrokerConnection connection;

    public MqttBrokerConnectionHandler(Bridge thing, MqttService service) {
        super(thing);
        if (service == null) {
            throw new IllegalArgumentException("No MqttService provided!");
        }
        this.service = service;
    }

    /**
     * Do nothing in the base implementation.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands to handle
    }

    /**
     * The base implementation will set the connection variable to the given broker
     * if it matches the brokerID and will start to connect to the broker if there
     * is no connection established yet.
     */
    @Override
    public void brokerAdded(String brokerID, MqttBrokerConnection broker) {
        if (!brokerID.equals(brokerName) || connection == broker) {
            return;
        }

        if (connection != null) {
            connection.removeConnectionObserver(this);
        }

        connection = broker;
        connection.addConnectionObserver(this);
        connectionStateChanged(connection.connectionState(), null);
    }

    @Override
    public void brokerRemoved(String brokerID, MqttBrokerConnection broker) {
        if (broker == connection) {
            connection.removeConnectionObserver(this);
            connection = null;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.sharedremoved");
            return;
        }
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, Throwable error) {
        if (state == MqttConnectionState.CONNECTED) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            String message = error == null ? "Offline - Reason unknown" : error.getLocalizedMessage();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        }
    }

    /**
     * The base class will remove listeners to the {@link MqttBrokerConnection}
     * and the {@link MqttService}.
     */
    @Override
    public void dispose() {
        if (connection != null) {
            connection.removeConnectionObserver(this);
            connection = null;
        }
        if (service != null) {
            service.removeBrokersListener(this);
        }
    }

    @Override
    public void initialize() {
        brokerName = (String) getConfig().get(PARAM_BRIDGE_name);
        if (brokerName == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "The broker name is required.");
            return;
        }

        service.addBrokersListener(this);

        connection = service.getBrokerConnection(brokerName);
        if (connection == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The broker connection with the name '" + brokerName + "' could not be found.");
            return;
        }

        connection.addConnectionObserver(this);
        connectionStateChanged(connection.connectionState(), null);
    }

    public MqttBrokerConnection getConnection() {
        return connection;
    }
}
