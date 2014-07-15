/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.yahooweather;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link YahooWeatherBinding} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author Kai Kreuzer - Initial contribution
 */
public class YahooWeatherBindingConstants {

    public static final String BINDING_ID = "yahooweather";
    
    // List all Thing Type UIDs, related to the YahooWeather Binding
    public final static ThingTypeUID THING_TYPE_WEATHER = new ThingTypeUID(BINDING_ID, "weather");

    // List all channels
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_PRESSURE = "pressure";

}
