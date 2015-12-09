/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link WemoBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
public class WemoBindingConstants {

    public static final String BINDING_ID = "wemo";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID THING_TYPE_SOCKET = new ThingTypeUID(BINDING_ID, "socket");
    public final static ThingTypeUID THING_TYPE_INSIGHT = new ThingTypeUID(BINDING_ID, "insight");
    public final static ThingTypeUID THING_TYPE_LIGHTSWITCH = new ThingTypeUID(BINDING_ID, "lightswitch");
    public final static ThingTypeUID THING_TYPE_MOTION = new ThingTypeUID(BINDING_ID, "motion");
    public final static ThingTypeUID THING_TYPE_MZ100 = new ThingTypeUID(BINDING_ID, "MZ100");
    public final static ThingTypeUID THING_TYPE_SENSOR = new ThingTypeUID(BINDING_ID, "sensor");

    // List of all Channel ids
    public final static String CHANNEL_STATE = "state";
    public final static String CHANNEL_CURRENTPOWER = "currentPower";
    public final static String CHANNEL_LASTONFOR = "lastOnFor";
    public final static String CHANNEL_ONTODAY = "onToday";
    public final static String CHANNEL_ONTOTAL = "onTotal";
    public static final String CHANNEL_BRIGHTNESS = "brightness";

    // List of configuration properties
    public static final String UDN = "udn";
    public static final String ROOT_UDN = "rootUDN";
    public static final String DEVICE_ID = "deviceID";
    public static final String LOCATION = "location";

    public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES = ImmutableSet.of(THING_TYPE_BRIDGE);

    public final static Set<ThingTypeUID> SUPPORTED_LIGHT_THING_TYPES = ImmutableSet.of(THING_TYPE_MZ100);

    public final static Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES = ImmutableSet.of(THING_TYPE_SOCKET,
            THING_TYPE_INSIGHT, THING_TYPE_LIGHTSWITCH, THING_TYPE_MOTION, THING_TYPE_SENSOR);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = ImmutableSet.of(THING_TYPE_SOCKET, THING_TYPE_INSIGHT,
            THING_TYPE_LIGHTSWITCH, THING_TYPE_MOTION, THING_TYPE_BRIDGE, THING_TYPE_MZ100);

}
