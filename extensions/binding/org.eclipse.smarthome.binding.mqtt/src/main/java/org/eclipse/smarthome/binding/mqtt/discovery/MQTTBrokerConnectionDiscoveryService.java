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
package org.eclipse.smarthome.binding.mqtt.discovery;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;

/**
 *
 * The {@link MQTTBrokerConnectionDiscoveryService} service offers to get notified of new/removed broker connections.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface MQTTBrokerConnectionDiscoveryService {
    /**
     * Add a listener to get notified
     *
     * @param listener A listener. Need to be a strong reference.
     */
    void addBrokersListener(MQTTBrokerConnectionDiscoveryParticipant listener);

    /**
     * Remove the given listener.
     *
     * @param listener A listener (that has been added before).
     */
    void removeBrokersListener(MQTTBrokerConnectionDiscoveryParticipant listener);

    /**
     * Return all broker connections.
     */
    Map<String, MqttBrokerConnection> getAllBrokerConnections();
}
