/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
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
 */
public class LifxBindingConstants {

    public static final String BINDING_ID = "lifx";

    // List of all Channel IDs
    public final static String CHANNEL_COLOR = "color";

    // config property for the LIFX device id
    public static final String CONFIG_PROPERTY_DEVICE_ID = "deviceId";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "light");

}
