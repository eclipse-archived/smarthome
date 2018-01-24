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

import static org.eclipse.smarthome.binding.mqttgeneric.MqttBrokerBindingConstants.PARAM_BRIDGE_name;

import java.math.BigDecimal;
import java.util.function.Consumer;

import org.eclipse.smarthome.binding.mqttgeneric.MqttBrokerBindingConstants;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokersObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;

/**
 * This bridge broker connection listens to changes of the {@link MqttBrokerConnection}
 * and puts the Thing on or offline. It also handles adding/removing notifications of the
 * {@link MqttService}.
 *
 * @author David Graeff - Initial contribution
 */
public class MqttBrokerConnectionHandler extends BaseBridgeHandler
        implements MqttConnectionObserver, MqttBrokersObserver {
    protected final MqttService service;
    protected String brokerID;
    private MqttBrokerConnection connection;

    public MqttBrokerConnectionHandler(Bridge thing, MqttService service) {
        super(thing);
        if (service == null) {
            throw new IllegalArgumentException("No MqttService provided!");
        }
        this.service = service;
        this.brokerID = thing.getUID().getId();
    }

    /**
     * Do nothing in the base implementation.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands to handle
    }

    /**
     * Helper method to read an integer configuration value and provide it to the consumer if it is not null.
     *
     * @param configKey The configuration key
     * @param consumer The consumer
     */
    protected void assignBigDecimal(String configKey, Consumer<Integer> consumer) {
        BigDecimal v = (BigDecimal) getConfig().get(configKey);
        if (v != null) {
            consumer.accept(v.intValue());
        }
    }

    /**
     * The base implementation will set the connection variable to the given broker
     * if it matches the brokerID and will start to connect to the broker if there
     * is no connection established yet.
     */
    @Override
    public void brokerAdded(MqttBrokerConnection broker) {
        if (!broker.getName().equals(brokerID) || connection == broker) {
            return;
        }

        connection = broker;
        connection.addConnectionObserver(this);
        connectionStateChanged(
                connection.isConnected() ? MqttConnectionState.CONNECTED : MqttConnectionState.DISCONNECTED, null);
    }

    @Override
    public void brokerRemoved(MqttBrokerConnection broker) {
        if (broker == connection) {
            connection.removeConnectionObserver(this);
            connection = null;
            updateProperty(MqttBrokerBindingConstants.PROPERTY_internal_status, "");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.sharedremoved");
            return;
        }
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, Throwable error) {
        if (state == MqttConnectionState.CONNECTED) {
            updateProperty(MqttBrokerBindingConstants.PROPERTY_internal_status, "");
            updateStatus(ThingStatus.ONLINE);
        } else {
            if (error == null) {
                updateProperty(MqttBrokerBindingConstants.PROPERTY_internal_status, "Offline - Reason unknown");
            } else {
                updateProperty(MqttBrokerBindingConstants.PROPERTY_internal_status, error.getMessage());
            }
            updateStatus(ThingStatus.OFFLINE);
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
        brokerID = (String) getConfig().get(PARAM_BRIDGE_name);
        if (brokerID == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "A url is required for a broker connection");
            return;
        }

        service.addBrokersListener(this);

        connection = service.getBrokerConnection(brokerID);
        if (connection == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The broker connection with the name " + brokerID + " not found");
            return;
        }

        connection.addConnectionObserver(this);
        connectionStateChanged(
                connection.isConnected() ? MqttConnectionState.CONNECTED : MqttConnectionState.DISCONNECTED, null);
    }

    public MqttBrokerConnection getConnection() {
        return connection;
    }
}
