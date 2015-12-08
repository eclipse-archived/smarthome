/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
 * @author Markus Mazurczak - Deleted bulb specific thing types, added generic thing types
 */
public class HueBindingConstants {

    public static final String BINDING_ID = "hue";

    // List all Thing Type UIDs, related to the Hue Binding
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID THING_TYPE_COLOR_LIGHT = new ThingTypeUID(BINDING_ID, "color_light");
    public final static ThingTypeUID THING_TYPE_COLOR_TEMPERATURE_LIGHT = new ThingTypeUID(BINDING_ID,
            "color_temperature_light");
    public final static ThingTypeUID THING_TYPE_DIMMABLE_LIGHT = new ThingTypeUID(BINDING_ID, "dimmable_light");
    public final static ThingTypeUID THING_TYPE_EXTENDED_COLOR_LIGHT = new ThingTypeUID(BINDING_ID,
            "extended_color_light");

    // List all channels
    public static final String CHANNEL_COLORTEMPERATURE = "color_temperature";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_BRIGHTNESS = "brightness";

    // Bridge config properties
    public static final String HOST = "ipAddress";
    public static final String USER_NAME = "userName";
    public static final String SERIAL_NUMBER = "serialNumber";

    // Light config properties
    public static final String LIGHT_ID = "lightId";

    // Model ID to identify OSRAM Par16 50 TW bulb
    public static final String OSRAM_PAR16_MODELID = "PAR16 50 TW";

}
