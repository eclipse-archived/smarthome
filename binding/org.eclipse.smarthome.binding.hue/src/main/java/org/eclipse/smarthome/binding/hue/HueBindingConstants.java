/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
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
 */
public class HueBindingConstants {

    public static final String BINDING_ID = "hue";
    
    // light model ids
    public static final String MODEL_LCT001 = "LCT001";
    
    // List all Thing Type UIDs, related to the Hue Binding
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID THING_TYPE_LCT001 = new ThingTypeUID(BINDING_ID, MODEL_LCT001);

    // List all channels
    public static final String CHANNEL_COLORTEMPERATURE = "color_temperature";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_BRIGHTNESS = "brightness";

}
