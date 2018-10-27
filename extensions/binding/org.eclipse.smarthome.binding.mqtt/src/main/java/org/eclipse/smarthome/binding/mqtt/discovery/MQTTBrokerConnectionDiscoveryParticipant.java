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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;

/**
 * Implement this interface to get notified of received values and vanished topics.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface MQTTBrokerConnectionDiscoveryParticipant {
    /**
     * Called as soon as a new broker connection appeared.
     *
     * @param connection The broker connection
     */
    void brokerAdded(String brokerID, MqttBrokerConnection broker);

    /**
     * A broker connection vanished.
     *
     * @param connection The broker connection
     */
    void brokerRemoved(String brokerID, MqttBrokerConnection connection);
}
