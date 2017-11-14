/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.weatherunderground.internal.json;

/**
 * The {@link WeatherUndergroundJsonForecast} is the Java class used
 * to map the entry "forecast" from the JSON response to a Weather Underground
 * request.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundJsonForecast {

    // Commented members indicate properties returned by the API not used by the binding

    // private Object txt_forecast;
    private WeatherUndergroundJsonSimpleForecast simpleforecast;

    public WeatherUndergroundJsonForecast() {
    }

    /**
     * Get the {@link WeatherUndergroundJsonForecastDay} object for a given day
     *
     * @return the {@link WeatherUndergroundJsonForecastDay} object for the day
     */
    public WeatherUndergroundJsonForecastDay getSimpleForecast(int day) {
        return (simpleforecast == null) ? null : simpleforecast.getForecastDay(day);
    }
}
