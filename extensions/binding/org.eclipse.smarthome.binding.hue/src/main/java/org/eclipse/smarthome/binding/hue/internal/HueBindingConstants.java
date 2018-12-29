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
package org.eclipse.smarthome.binding.hue.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HueBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Jochen Hiller - Added OSRAM Classic A60 RGBW
 * @author Markus Mazurczak - Added OSRAM PAR16 50
 * @author Andre Fuechsel - changed to generic thing types
 * @author Samuel Leisering - Added support for sensor API
 * @author Christoph Weitkamp - Added support for sensor API
 */
public class HueBindingConstants {

    public static final String BINDING_ID = "hue";

    // List all Thing Type UIDs, related to the Hue Binding

    // bridge
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // generic thing types
    public static final ThingTypeUID THING_TYPE_ON_OFF_LIGHT = new ThingTypeUID(BINDING_ID, "0000");
    public static final ThingTypeUID THING_TYPE_ON_OFF_PLUG = new ThingTypeUID(BINDING_ID, "0010");
    public static final ThingTypeUID THING_TYPE_COLOR_LIGHT = new ThingTypeUID(BINDING_ID, "0200");
    public static final ThingTypeUID THING_TYPE_COLOR_TEMPERATURE_LIGHT = new ThingTypeUID(BINDING_ID, "0220");
    public static final ThingTypeUID THING_TYPE_EXTENDED_COLOR_LIGHT = new ThingTypeUID(BINDING_ID, "0210");
    public static final ThingTypeUID THING_TYPE_DIMMABLE_LIGHT = new ThingTypeUID(BINDING_ID, "0100");
    public static final ThingTypeUID THING_TYPE_DIMMABLE_PLUG = new ThingTypeUID(BINDING_ID, "0110");

    public static final ThingTypeUID THING_TYPE_DIMMER_SWITCH = new ThingTypeUID(BINDING_ID, "0820");
    public static final ThingTypeUID THING_TYPE_TAP_SWITCH = new ThingTypeUID(BINDING_ID, "0830");
    public static final ThingTypeUID THING_TYPE_PRESENCE_SENSOR = new ThingTypeUID(BINDING_ID, "0107");
    public static final ThingTypeUID THING_TYPE_TEMPERATURE_SENSOR = new ThingTypeUID(BINDING_ID, "0302");
    public static final ThingTypeUID THING_TYPE_LIGHT_LEVEL_SENSOR = new ThingTypeUID(BINDING_ID, "0106");

    // List all channels
    public static final String CHANNEL_COLORTEMPERATURE = "color_temperature";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_ALERT = "alert";
    public static final String CHANNEL_EFFECT = "effect";
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_DIMMER_SWITCH = "dimmer_switch";
    public static final String CHANNEL_TAP_SWITCH = "tap_switch";
    public static final String CHANNEL_PRESENCE = "presence";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_LAST_UPDATED = "last_updated";
    public static final String CHANNEL_BATTERY_LEVEL = "battery_level";
    public static final String CHANNEL_BATTERY_LOW = "battery_low";
    public static final String CHANNEL_ILLUMINANCE = "illuminance";
    public static final String CHANNEL_LIGHT_LEVEL = "light_level";
    public static final String CHANNEL_DARK = "dark";
    public static final String CHANNEL_DAYLIGHT = "daylight";

    // List all triggers
    public static final String EVENT_DIMMER_SWITCH = "dimmer_switch_event";
    public static final String EVENT_TAP_SWITCH = "tap_switch_event";

    // Bridge config properties
    public static final String HOST = "ipAddress";
    public static final String USER_NAME = "userName";

    // Light config properties
    public static final String LIGHT_ID = "lightId";
    public static final String SENSOR_ID = "sensorId";
    public static final String PRODUCT_NAME = "productName";
    public static final String UNIQUE_ID = "uniqueId";
}
