/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.enocean;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link YahooWeatherBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class EnOceanBindingConstants {

    public static final String BINDING_ID = "enocean";

    // List all Thing Type UIDs, related to the YahooWeather Binding
    public final static ThingTypeUID THING_TYPE_ELTAKO_SMOKE_DETECTOR = new ThingTypeUID(BINDING_ID, "smoke_detector");
    public final static ThingTypeUID THING_TYPE_ON_OFF_PLUG = new ThingTypeUID(BINDING_ID, "on_off_plug");
    // List all channels
    public static final String CHANNEL_ELTAKO_SMOKE_DETECTOR = "eltako smoke detector";
    public static final String CHANNEL_ON_OFF = "onOff";

}
