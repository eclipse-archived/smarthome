/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx;

import static org.eclipse.smarthome.binding.lifx.internal.LifxUtils.kelvinToPercentType;

import java.util.Set;

import org.eclipse.smarthome.binding.lifx.internal.fields.HSBK;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

import com.google.common.collect.Sets;

/**
 * The {@link LifxBinding} class defines common constants, which are used across
 * the whole binding.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Wouter Born - Added packet interval, power on brightness constants
 */
public class LifxBindingConstants {

    public static final String BINDING_ID = "lifx";

    // The LIFX LAN Protocol Specification states that lights can process up to 20 messages per second, not more.
    public static final long PACKET_INTERVAL = 50;

    // Minimum and maximum of MultiZone light indices
    public static final int MIN_ZONE_INDEX = 0;
    public static final int MAX_ZONE_INDEX = 255;

    // Fallback light state defaults
    public static final HSBK DEFAULT_COLOR = new HSBK(HSBType.WHITE, kelvinToPercentType(3000));
    public static final PercentType DEFAULT_BRIGHTNESS = PercentType.HUNDRED;

    // List of all Channel IDs
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_COLOR_ZONE = "colorzone";
    public static final String CHANNEL_INFRARED = "infrared";
    public static final String CHANNEL_SIGNAL_STRENGTH = "signalstrength";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_TEMPERATURE_ZONE = "temperaturezone";

    // List of all Channel Type UIDs
    public static final ChannelTypeUID CHANNEL_TYPE_BRIGHTNESS = new ChannelTypeUID(BINDING_ID, CHANNEL_BRIGHTNESS);
    public static final ChannelTypeUID CHANNEL_TYPE_COLOR = new ChannelTypeUID(BINDING_ID, CHANNEL_COLOR);
    public static final ChannelTypeUID CHANNEL_TYPE_COLOR_ZONE = new ChannelTypeUID(BINDING_ID, CHANNEL_COLOR_ZONE);
    public static final ChannelTypeUID CHANNEL_TYPE_INFRARED = new ChannelTypeUID(BINDING_ID, CHANNEL_INFRARED);
    public static final ChannelTypeUID CHANNEL_TYPE_TEMPERATURE = new ChannelTypeUID(BINDING_ID, CHANNEL_TEMPERATURE);
    public static final ChannelTypeUID CHANNEL_TYPE_TEMPERATURE_ZONE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TEMPERATURE_ZONE);

    // Config property for the LIFX device id
    public static final String CONFIG_PROPERTY_DEVICE_ID = "deviceId";
    public static final String CONFIG_PROPERTY_FADETIME = "fadetime";

    // Config property for channel configuration
    public static final String CONFIG_PROPERTY_POWER_ON_BRIGHTNESS = "powerOnBrightness";

    // Property keys
    public static final String PROPERTY_HOST_VERSION = "hostVersion";
    public static final String PROPERTY_MAC_ADDRESS = "macAddress";
    public static final String PROPERTY_PRODUCT_ID = "productId";
    public static final String PROPERTY_PRODUCT_NAME = "productName";
    public static final String PROPERTY_PRODUCT_VERSION = "productVersion";
    public static final String PROPERTY_VENDOR_ID = "vendorId";
    public static final String PROPERTY_VENDOR_NAME = "vendorName";
    public static final String PROPERTY_WIFI_VERSION = "wifiVersion";
    public static final String PROPERTY_ZONES = "zones";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_COLORLIGHT = new ThingTypeUID(BINDING_ID, "colorlight");
    public static final ThingTypeUID THING_TYPE_COLORIRLIGHT = new ThingTypeUID(BINDING_ID, "colorirlight");
    public static final ThingTypeUID THING_TYPE_COLORMZLIGHT = new ThingTypeUID(BINDING_ID, "colormzlight");
    public static final ThingTypeUID THING_TYPE_WHITELIGHT = new ThingTypeUID(BINDING_ID, "whitelight");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_COLORLIGHT,
            THING_TYPE_COLORIRLIGHT, THING_TYPE_COLORMZLIGHT, THING_TYPE_WHITELIGHT);

}
