/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
