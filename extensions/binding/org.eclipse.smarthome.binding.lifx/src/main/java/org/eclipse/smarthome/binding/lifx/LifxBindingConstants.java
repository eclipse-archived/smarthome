/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link LifxBinding} class defines common constants, which are used across
 * the whole binding.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Wouter Born - Added packet interval, power on brightness constants
 */
public class LifxBindingConstants {

    public static final String BINDING_ID = "lifx";
    public static final String THREADPOOL_NAME = "lifx";

    // The LIFX LAN Protocol Specification states that bulbs can process up to 20 messages per second, not more.
    public final static long PACKET_INTERVAL = 50;

    // List of all Channel IDs
    public final static String CHANNEL_COLOR = "color";
    public final static String CHANNEL_TEMPERATURE = "temperature";
    public final static String CHANNEL_BRIGHTNESS = "brightness";

    // config property for the LIFX device id
    public static final String CONFIG_PROPERTY_DEVICE_ID = "deviceId";
    public static final String CONFIG_PROPERTY_FADETIME = "fadetime";

    // config property for the interface to listen for broadcast UDP traffic
    public static final String CONFIG_PROPERTY_INTERFACE_ID = "interface";

    // config property for channel configuration
    public static final String CONFIG_PROPERTY_POWER_ON_BRIGHTNESS = "powerOnBrightness";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_COLORLIGHT = new ThingTypeUID(BINDING_ID, "colorlight");
    public final static ThingTypeUID THING_TYPE_WHITELIGHT = new ThingTypeUID(BINDING_ID, "whitelight");

}
