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

/**
 *
 * The {@link MQTTTopicDiscoveryService} service is responsible for subscribing to a topic on
 * all currently available broker connections as well as later on appearing broker connections.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface MQTTTopicDiscoveryService {
    /**
     * Subscribe to the given topic and get notified of messages on that topic via the listener.
     * Subscribing happens on a best-effort strategy. Any errors on any connections are suppressed.
     *
     * @param listener A listener. Need to be a strong reference.
     * @param topic The topic. Can contain wildcards.
     */
    void subscribe(MQTTTopicDiscoveryParticipant listener, String topic);

    /**
     * Unsubscribe the given listener.
     *
     * @param listener A listener that has subscribed before.
     */
    void unsubscribe(MQTTTopicDiscoveryParticipant listener);
}
