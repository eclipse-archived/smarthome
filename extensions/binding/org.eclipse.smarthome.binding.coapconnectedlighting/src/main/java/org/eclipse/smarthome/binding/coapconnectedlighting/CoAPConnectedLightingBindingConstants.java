/**
 * Copyright (c) 2016 Microchip Technology Inc. and its subsidiaries and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.binding.coapconnectedlighting;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link CoAPConnectedLightingBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Microchip Technology - Initial contribution
 */
public class CoAPConnectedLightingBindingConstants {

    public static final String BINDING_ID = "coap";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "smarthome");

    // List of all Channel ids
    public final static String CHANNEL_DimmableLight = "Dimmable_Light";
    public final static String CHANNEL_LCD = "LCD";
    public final static String CHANNEL_Temperature = "Temperature";
    public final static String CHANNEL_Humidity = "Humidity";
    public final static String CHANNEL_Pressure = "Pressure";
    public final static String CHANNEL_RGB = "RGB_Sensor";
    public final static String CHANNEL_WallSwitch = "Wall_Switch";

    // Custom Properties
    public final static String PROPERTY_COAP_SERVER = "ipAddress";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SAMPLE);

}
