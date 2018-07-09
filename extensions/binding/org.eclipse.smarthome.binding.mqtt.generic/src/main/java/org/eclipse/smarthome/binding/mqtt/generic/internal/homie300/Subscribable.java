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
package org.eclipse.smarthome.binding.mqtt.generic.internal.homie300;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MqttTopicClassMapper;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;

/**
 * Homie 3.x Device, Node, Properties all have attributes. To automatically subscribe to all
 * fields of an attribute class, the {@link MqttTopicClassMapper} is used. The mentioned
 * classes implement this interface for a simpler handling.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface Subscribable {
    /**
     * Subscribe to all fields of a class via the topicMapper.
     *
     * @param topicMapper A MQTT topic<-->field mapper
     * @param timeout Timeout in milliseconds. The returned future completes after this time even if no message has been
     *            received for a single MQTT topic.
     * @return A promise should be returned that completes as soon as values for all fields have been received.
     * @throws MqttException A MQTT IO exception may cause this {@link MqttException}
     */
    CompletableFuture<@Nullable Void> subscribe(MqttTopicClassMapper topicMapper, int timeout);

    /**
     * Unsubscribe from previously subscribed topics.
     *
     * @param topicMapper A MQTT topic<-->field mapper. It needs to be the same that was used in
     *            {@link #subscribe(MqttTopicClassMapper)}.
     */
    void unsubscribe(MqttTopicClassMapper topicMapper);
}