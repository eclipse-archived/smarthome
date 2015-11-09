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
 */
public class HueBindingConstants {

    public static final String BINDING_ID = "hue";

    // List all Thing Type UIDs, related to the Hue Binding
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID THING_TYPE_LCT001 = new ThingTypeUID(BINDING_ID, "LCT001");
    public final static ThingTypeUID THING_TYPE_LCT002 = new ThingTypeUID(BINDING_ID, "LCT002");
    public final static ThingTypeUID THING_TYPE_LCT003 = new ThingTypeUID(BINDING_ID, "LCT003");
    public final static ThingTypeUID THING_TYPE_LCT007 = new ThingTypeUID(BINDING_ID, "LCT007");
    public final static ThingTypeUID THING_TYPE_LLC001 = new ThingTypeUID(BINDING_ID, "LLC001");
    public final static ThingTypeUID THING_TYPE_LLC006 = new ThingTypeUID(BINDING_ID, "LLC006");
    public final static ThingTypeUID THING_TYPE_LLC007 = new ThingTypeUID(BINDING_ID, "LLC007");
    public final static ThingTypeUID THING_TYPE_LLC010 = new ThingTypeUID(BINDING_ID, "LLC010");
    public final static ThingTypeUID THING_TYPE_LLC011 = new ThingTypeUID(BINDING_ID, "LLC011");
    public final static ThingTypeUID THING_TYPE_LLC012 = new ThingTypeUID(BINDING_ID, "LLC012");
    public final static ThingTypeUID THING_TYPE_LLC013 = new ThingTypeUID(BINDING_ID, "LLC013");
    public final static ThingTypeUID THING_TYPE_LLC020 = new ThingTypeUID(BINDING_ID, "LLC020");
    public final static ThingTypeUID THING_TYPE_LST001 = new ThingTypeUID(BINDING_ID, "LST001");
    public final static ThingTypeUID THING_TYPE_LST002 = new ThingTypeUID(BINDING_ID, "LST002");
    public final static ThingTypeUID THING_TYPE_LWB004 = new ThingTypeUID(BINDING_ID, "LWB004");
    public final static ThingTypeUID THING_TYPE_LWB006 = new ThingTypeUID(BINDING_ID, "LWB006");
    public final static ThingTypeUID THING_TYPE_LWB007 = new ThingTypeUID(BINDING_ID, "LWB007");
    public final static ThingTypeUID THING_TYPE_LWL001 = new ThingTypeUID(BINDING_ID, "LWL001");
    public final static ThingTypeUID THING_TYPE_CLASSIC_A60_RGBW = new ThingTypeUID(BINDING_ID, "Classic_A60_RGBW");
    public final static ThingTypeUID THING_TYPE_SURFACE_LIGHT_TW = new ThingTypeUID(BINDING_ID, "Surface_Light_TW");
    public final static ThingTypeUID THING_TYPE_ZLL_LIGHT = new ThingTypeUID(BINDING_ID, "ZLL_Light");

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

}
