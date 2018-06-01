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
package org.eclipse.smarthome.io.mqttembeddedbroker.internal;

import java.util.Collection;

import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptAcknowledgedMessage;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptConnectionLostMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.moquette.server.Server;

/**
 * Informs the given listener about connected clients and maybe other
 * server metrics in the future. You need to set the server with {@link #setServer(Server)}.
 *
 * Right now this is an adapter interface for Moquettes InterceptHandler.
 *
 * @author David Graeff - Initial contribution
 */
public class MqttEmbeddedBrokerMetrics implements InterceptHandler {
    /**
     * Metric listener interface. Implement this to get notified of currently connected clients.
     */
    public interface BrokerMetricsListener {
        void connectedClientIDs(Collection<String> clientIDs);
    }

    private final BrokerMetricsListener listener;
    private Server server;

    public MqttEmbeddedBrokerMetrics(BrokerMetricsListener listener) {
        this.listener = listener;
    }

    /**
     * Set the Moquette server.
     *
     * @param server Moquette server
     */
    public void setServer(Server server) {
        if (this.server != null) {
            this.server.removeInterceptHandler(this);
        }
        this.server = server;
        if (server != null) {
            server.addInterceptHandler(this);
        }
    }

    @Override
    public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
    }

    @Override
    public void onSubscribe(InterceptSubscribeMessage msg) {
    }

    @Override
    public void onPublish(InterceptPublishMessage msg) {
    }

    @Override
    public void onMessageAcknowledged(InterceptAcknowledgedMessage msg) {
    }

    @Override
    public void onDisconnect(InterceptDisconnectMessage msg) {
        listener.connectedClientIDs(server.getConnectionsManager().getConnectedClientIds());
    }

    @Override
    public void onConnectionLost(InterceptConnectionLostMessage msg) {
        listener.connectedClientIDs(server.getConnectionsManager().getConnectedClientIds());
    }

    @Override
    public void onConnect(InterceptConnectMessage msg) {
        listener.connectedClientIDs(server.getConnectionsManager().getConnectedClientIds());
    }

    @Override
    public Class<?>[] getInterceptedMessageTypes() {
        return new Class<?>[] { InterceptConnectMessage.class, InterceptConnectionLostMessage.class,
                InterceptDisconnectMessage.class };
    }

    @Override
    public String getID() {
        return "collectmetrics";
    }
}
