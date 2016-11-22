/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HueBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Jochen Hiller - Added OSRAM Classic A60 RGBW
 * @author Markus Mazurczak - Added OSRAM PAR16 50
 * @author Andre Fuechsel - changed to generic thing types
 *
 */
public class HueBindingConstants {

    public static final String BINDING_ID = "hue";

    // List all Thing Type UIDs, related to the Hue Binding

    // bridge
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // generic thing types
    public final static ThingTypeUID THING_TYPE_ON_OFF_LIGHT = new ThingTypeUID(BINDING_ID, "0000");
    public final static ThingTypeUID THING_TYPE_ON_OFF_PLUG = new ThingTypeUID(BINDING_ID, "0010");
    public final static ThingTypeUID THING_TYPE_COLOR_LIGHT = new ThingTypeUID(BINDING_ID, "0200");
    public final static ThingTypeUID THING_TYPE_COLOR_TEMPERATURE_LIGHT = new ThingTypeUID(BINDING_ID, "0220");
    public final static ThingTypeUID THING_TYPE_EXTENDED_COLOR_LIGHT = new ThingTypeUID(BINDING_ID, "0210");
    public final static ThingTypeUID THING_TYPE_DIMMABLE_LIGHT = new ThingTypeUID(BINDING_ID, "0100");
    public final static ThingTypeUID THING_TYPE_DIMMABLE_PLUG = new ThingTypeUID(BINDING_ID, "0110");

    // List all channels
    public static final String CHANNEL_COLORTEMPERATURE = "color_temperature";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_ALERT = "alert";
    public static final String CHANNEL_EFFECT = "effect";
    public static final String CHANNEL_SWITCH = "switch";

    // Bridge config properties
    public static final String HOST = "ipAddress";
    public static final String USER_NAME = "userName";
    public static final String SERIAL_NUMBER = "serialNumber";
    public static final String POLLING_INTERVAL = "pollingInterval";

    // Light config properties
    public static final String LIGHT_ID = "lightId";

}
