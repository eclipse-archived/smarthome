/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.weatherunderground.internal.json;

import java.util.List;

/**
 * The {@link WeatherUndergroundJsonSimpleForecast} is the Java class used
 * to map the entry "forecast.simpleforecast" from the JSON response
 * to a Weather Underground request.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundJsonSimpleForecast {

    private List<WeatherUndergroundJsonForecastDay> forecastday;

    public WeatherUndergroundJsonSimpleForecast() {
    }

    /**
     * Get the {@link WeatherUndergroundJsonForecastDay} object for a given day
     *
     * @return the {@link WeatherUndergroundJsonForecastDay} object for the day
     */
    public WeatherUndergroundJsonForecastDay getForecastDay(int day) {
        for (WeatherUndergroundJsonForecastDay forecast : forecastday) {
            if (forecast.getPeriod().intValue() == day) {
                return forecast;
            }
        }
        return null;
    }
}
