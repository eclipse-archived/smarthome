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
package org.eclipse.smarthome.io.mqttembeddedbroker;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;

/**
 * This service allows to start and stop the embedded MQTT broker and to request the {@link MqttBrokerConnection}.
 *
 * @author David Graeff - Initial contribution
 */
public interface EmbeddedBrokerService {
    /**
     * Starts the embedded broker.
     *
     * @param port The broker port.
     * @param secure Allow only secure connections if true or only plain connections otherwise.
     * @param username Broker authentication user name. May be null.
     * @param password Broker authentication password. May be null.
     * @param persistence_filename The filename were persistent data should be stored.
     * @throws IOException If any error happens, like the port is already in use, this exception is thrown.
     */
    void startEmbeddedServer(Integer portParam, boolean secure, String username, String password,
            @NonNull String persistenceFilenameParam) throws IOException;

    /**
     * Stops the embedded broker, if it is started.
     */
    void stopEmbeddedServer();

    /**
     * Returns the MQTT broker connection, connected to the embedded broker
     */
    public MqttBrokerConnection getConnection();
}
