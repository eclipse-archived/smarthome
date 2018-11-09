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
package org.eclipse.smarthome.binding.mqtt.generic.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link MqttBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class MqttBindingConstants {

    public static final String BINDING_ID = "mqtt";

    // List of all Thing Type UIDs
    public static final ThingTypeUID GENERIC_MQTT_THING = new ThingTypeUID(BINDING_ID, "topic");
    public static final ThingTypeUID HOMIE300_MQTT_THING = new ThingTypeUID(BINDING_ID, "homie300");
    public static final ThingTypeUID HOMEASSISTANT_MQTT_THING = new ThingTypeUID(BINDING_ID, "homeassistant");

    // Generic thing channel types
    public static final String COLOR_RGB = "colorRGB";
    public static final String COLOR_HSB = "colorHSB";
    public static final String CONTACT = "contact";
    public static final String DATETIME = "dateTime";
    public static final String DIMMER = "dimmer";
    public static final String NUMBER = "number";
    public static final String STRING = "string";
    public static final String SWITCH = "switch";

    public static final String CONFIG_HA_CHANNEL = "mqtt:ha_channel";
    public static final String CONFIG_HOMIE_CHANNEL = "mqtt:homie_channel";

    public static final String HOMIE_PROPERTY_VERSION = "homieversion";
    public static final String HOMIE_PROPERTY_HEARTBEAT_INTERVAL = "heartbeat_interval";
}
