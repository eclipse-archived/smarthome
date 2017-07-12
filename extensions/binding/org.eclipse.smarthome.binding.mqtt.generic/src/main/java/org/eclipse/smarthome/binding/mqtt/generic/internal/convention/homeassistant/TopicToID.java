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
package org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homeassistant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * HomeAssistant MQTT components use a specific MQTT topic layout,
 * starting with a base prefix (usually "homeassistant"),
 * followed by the component id, an optional node id and the object id.
 *
 * This helper class can split up an MQTT topic into such parts.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class TopicToID {
    final public String component;
    final public String nodeID;
    final public String objectID;

    /**
     * Creates a TopicStringParts object for a given HomeAssistant MQTT topic.
     *
     * @param mqttTopic A topic like "homeassistant/binary_sensor/garden/config" or
     *            "homeassistant/binary_sensor/0/garden/config"
     */
    public TopicToID(String mqttTopic) {
        String[] strings = mqttTopic.split("/");
        if (strings.length > 4) {
            component = strings[1];
            nodeID = strings[2];
            objectID = strings[3];
        } else {
            component = strings[1];
            nodeID = "";
            objectID = strings[2];
        }
    }

    /**
     * The HomeAssistant MQTT topic tree does not match how we assemble a generic MQTT Thing.
     * The closest mapping for a unique Thing ID is to combine object_id and node_id.
     */
    public String getThingID() {
        return objectID + "-" + nodeID;
    }
}