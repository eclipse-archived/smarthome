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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.AbstractMqttAttributeClass;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MQTTvalueTransform;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MandatoryField;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.TopicPrefix;

/**
 * Homie 3.x Property attributes
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
@TopicPrefix
public class PropertyAttributes extends AbstractMqttAttributeClass {
    // Lower-case enum value names required. Those are identifiers for the MQTT/homie protocol.
    public enum DataTypeEnum {
        unknown,
        integer_,
        float_,
        boolean_,
        string_,
        enum_,
        color_
    }

    public @MandatoryField String name = "";

    /**
     * stateful + non-settable: The node publishes a property state (temperature sensor)
     * stateful + settable: The node publishes a property state, and can receive commands for the property (by
     * controller or other party) (lamp power)
     * stateless + non-settable: The node publishes momentary events (door bell pressed)
     * stateless + settable: The node publishes momentary events, and can receive commands for the property (by
     * controller or other party) (brew coffee)
     */
    public boolean settable = false;
    public boolean retained = true;
    public String unit = "";
    public @MandatoryField @MQTTvalueTransform(suffix = "_") DataTypeEnum datatype = DataTypeEnum.unknown;
    public String format = "";

    @Override
    public Object getFieldsOf() {
        return this;
    }
}
