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
package org.eclipse.smarthome.binding.mqtt.generic.internal.mapping;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * MQTT does not directly support key-value configuration maps. We do need those for device discovery and thing
 * configuration though.
 * Different conventions came up with different solutions, and one is to have "attribute classes".
 *
 * An attribute class describes all keys and their types by modeling a java class with corresponding fields.
 * To automatically subscribe to all fields of an attribute class, the {@link MqttTopicClassMapper} is used.
 *
 * For example the Homie 3.x convention uses attribute classes for Device, Nodes and Properties.
 * The mentioned classes just need to implement {@link MqttAttributeClass} and instances need to be handed
 * to {@link MqttTopicClassMapper}. For every field a corresponding MQTT topic subscribe happens.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface MqttAttributeClass {
    /**
     * Subscribe to all fields of a class via the topicMapper.
     *
     * @param topicMapper A MQTT topic<-->field mapper
     * @param timeout Timeout in milliseconds. The returned future completes after this time even if no message has
     *            been
     *            received for a single MQTT topic.
     * @return A promise is returned that completes as soon as values for all fields have been received.
     */
    CompletableFuture<@Nullable Void> subscribe(MqttTopicClassMapper topicMapper, int timeout);

    /**
     * Unsubscribe from previously subscribed topics.
     *
     * @param topicMapper A MQTT topic<-->field mapper. It needs to be the same that was used in
     *            {@link #subscribe(MqttTopicClassMapper)}.
     * @return Returns a future that completes as soon as the unsubscribing for all MQTT topics related to this
     *         Subscribable have been performed.
     */
    CompletableFuture<Void> unsubscribe(MqttTopicClassMapper topicMapper);
}
