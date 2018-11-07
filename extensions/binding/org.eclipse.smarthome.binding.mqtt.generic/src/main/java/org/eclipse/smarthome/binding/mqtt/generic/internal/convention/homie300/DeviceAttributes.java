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
package org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.AbstractMqttAttributeClass;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MQTTvalueTransform;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MandatoryField;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.TopicPrefix;

/**
 * Homie 3.x Device attributes
 *
 * @author David Graeff - Initial contribution
 */
@TopicPrefix
public class DeviceAttributes extends AbstractMqttAttributeClass {
    // Lower-case enum value names required. Those are identifiers for the MQTT/homie protocol.
    public enum ReadyState {
        unknown,
        init,
        ready,
        disconnected,
        sleeping,
        lost,
        alert
    }

    public @MandatoryField String homie;
    public @MandatoryField String name;
    public @MandatoryField ReadyState state = ReadyState.unknown;
    public @MandatoryField @MQTTvalueTransform(splitCharacter = ",") String[] nodes;

    @Override
    public @NonNull Object getFieldsOf() {
        return this;
    }
}
