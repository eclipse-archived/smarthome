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
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MapToField;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.TopicPrefix;

/**
 * Homie 3.x Property attributes
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
@TopicPrefix
public class PropertyAttributes {
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

    public String name = "";
    public boolean settable = false;
    public String unit = "";
    public @MapToField(suffix = "_") DataTypeEnum datatype = DataTypeEnum.unknown;
    public String format = "";

}
