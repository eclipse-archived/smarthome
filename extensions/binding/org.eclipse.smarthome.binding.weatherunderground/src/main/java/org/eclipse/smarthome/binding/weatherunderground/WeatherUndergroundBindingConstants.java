/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.weatherunderground;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link WeatherUndergroundBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundBindingConstants {

    public static final String BINDING_ID = "weatherunderground";

    public static final String LOCAL = "local";

    // List all Thing Type UIDs, related to the WeatherUnderground Binding
    public final static ThingTypeUID THING_TYPE_WEATHER = new ThingTypeUID(BINDING_ID, "weather");

    // Channel configuration Properties
    public final static String PROPERTY_SOURCE_UNIT = "SourceUnit";
}
