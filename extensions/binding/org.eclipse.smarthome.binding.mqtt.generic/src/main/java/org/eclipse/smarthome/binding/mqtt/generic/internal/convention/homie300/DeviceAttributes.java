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

import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MapToField;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.TopicPrefix;

/**
 * Homie 3.x Device attributes
 *
 * @author David Graeff - Initial contribution
 */
@TopicPrefix
public class DeviceAttributes {
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

    public String homie;
    public String name;
    public ReadyState state = ReadyState.unknown;
    public String localip;
    public String mac;
    public @MapToField(splitCharacter = ",") String[] nodes;
    public String implementation;
    public String stats;

    // TODO A later PR may implement the firmware/OTA part
    // @TopicPrefix("")
    // public static class Firmware {
    // public String name;
    // public String version;
    // };
    //
    // public Firmware fw;
}
