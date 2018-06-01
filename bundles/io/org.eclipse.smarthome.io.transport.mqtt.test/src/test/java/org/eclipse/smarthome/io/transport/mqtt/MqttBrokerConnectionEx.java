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
package org.eclipse.smarthome.io.transport.mqtt;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.osgi.service.cm.ConfigurationException;

/**
 * We need an extended MqttBrokerConnection to overwrite the protected `connectionCallbacks` with
 * an instance that takes the mocked version of `MqttBrokerConnection`.
 */
@NonNullByDefault
public class MqttBrokerConnectionEx extends MqttBrokerConnection {
    public MqttConnectionState connectionStateOverwrite = MqttConnectionState.DISCONNECTED;

    public MqttBrokerConnectionEx(String host, @Nullable Integer port, boolean secure, String clientId) {
        super(host, port, secure, clientId);
    }

    void setConnectionCallback(MqttBrokerConnectionEx o, IMqttToken t) {
        connectionCallbacks = new ConnectionCallbacks(o);
        connectionToken = t;
    }

    /**
     * Do not connect to any server. Just return a MqttAsyncClient. We will wait for the timeout in some tests
     * and use {@link #connectionState()} for pretending to be connected.
     */
    @Override
    protected @NonNull MqttAsyncClient createAndConnectClient() throws MqttException, ConfigurationException {
        StringBuilder serverURI = new StringBuilder();
        serverURI.append((secure ? "ssl://" : "tcp://"));
        serverURI.append(host);
        serverURI.append(":");
        serverURI.append(port);
        try {
            return new MqttAsyncClient(serverURI.toString(), clientId);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttException(e);
        }
    }

    @Override
    public @NonNull MqttConnectionState connectionState() {
        return connectionStateOverwrite;
    }
}
