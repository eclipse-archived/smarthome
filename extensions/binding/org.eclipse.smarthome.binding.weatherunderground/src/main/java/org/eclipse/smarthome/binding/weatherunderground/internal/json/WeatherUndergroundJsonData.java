/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.weatherunderground.internal.json;

/**
 * The {@link WeatherUndergroundJsonData} is the Java class used to map the JSON
 * response to a Weather Underground request.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundJsonData {

    private WeatherUndergroundJsonResponse response;
    private WeatherUndergroundJsonCurrent current_observation;
    private WeatherUndergroundJsonForecast forecast;
    private WeatherUndergroundJsonLocation location;

    public WeatherUndergroundJsonData() {
    }

    /**
     * Get the {@link WeatherUndergroundJsonResponse} object
     *
     * @return the {@link WeatherUndergroundJsonResponse} object
     */
    public WeatherUndergroundJsonResponse getResponse() {
        return response;
    }

    /**
     * Get the {@link WeatherUndergroundJsonLocation} object
     *
     * @return the {@link WeatherUndergroundJsonLocation} object
     */
    public WeatherUndergroundJsonLocation getLocation() {
        return location;
    }

    /**
     * Get the {@link WeatherUndergroundJsonForecast} object
     *
     * @return the {@link WeatherUndergroundJsonForecast} object
     */
    public WeatherUndergroundJsonForecast getForecast() {
        return forecast;
    }

    /**
     * Get the {@link WeatherUndergroundJsonCurrent} object
     *
     * Used to update the channels current#xxx
     *
     * @return the {@link WeatherUndergroundJsonCurrent} object
     */
    public WeatherUndergroundJsonCurrent getCurrent() {
        return current_observation;
    }

    /**
     * Get the {@link WeatherUndergroundJsonForecastDay} object for today
     *
     * Used to update the channels forecastToday#xxx
     *
     * @return the {@link WeatherUndergroundJsonForecastDay} object for today
     */
    public WeatherUndergroundJsonForecastDay getForecastToday() {
        return getForecastDay(1);
    }

    /**
     * Get the {@link WeatherUndergroundJsonForecastDay} object for tomorrow
     *
     * Used to update the channels forecastTomorrow#xxx
     *
     * @return the {@link WeatherUndergroundJsonForecastDay} object for tomorrow
     */
    public WeatherUndergroundJsonForecastDay getForecastTomorrow() {
        return getForecastDay(2);
    }

    /**
     * Get the {@link WeatherUndergroundJsonForecastDay} object for the second day
     *
     * Used to update the channels forecastDay2#xxx
     *
     * @return the {@link WeatherUndergroundJsonForecastDay} object for the second day
     */
    public WeatherUndergroundJsonForecastDay getForecastDay2() {
        return getForecastDay(3);
    }

    /**
     * Get the {@link WeatherUndergroundJsonForecastDay} object for the third day
     *
     * Used to update the channels forecastDay3#xxx
     *
     * @return the {@link WeatherUndergroundJsonForecastDay} object for the third day
     */
    public WeatherUndergroundJsonForecastDay getForecastDay3() {
        return getForecastDay(4);
    }

    /**
     * Get the {@link WeatherUndergroundJsonForecastDay} object for the 4th day
     *
     * Used to update the channels forecastDay4#xxx
     *
     * @return the {@link WeatherUndergroundJsonForecastDay} object for the 4th day
     */
    public WeatherUndergroundJsonForecastDay getForecastDay4() {
        return getForecastDay(5);
    }

    /**
     * Get the {@link WeatherUndergroundJsonForecastDay} object for the 5th day
     *
     * Used to update the channels forecastDay5#xxx
     *
     * @return the {@link WeatherUndergroundJsonForecastDay} object for the 5th day
     */
    public WeatherUndergroundJsonForecastDay getForecastDay5() {
        return getForecastDay(6);
    }

    /**
     * Get the {@link WeatherUndergroundJsonForecastDay} object for the 6th day
     *
     * Used to update the channels forecastDay6#xxx
     *
     * @return the {@link WeatherUndergroundJsonForecastDay} object for the 6th day
     */
    public WeatherUndergroundJsonForecastDay getForecastDay6() {
        return getForecastDay(7);
    }

    /**
     * Get the {@link WeatherUndergroundJsonForecastDay} object for the 7th day
     *
     * Used to update the channels forecastDay7#xxx
     *
     * @return the {@link WeatherUndergroundJsonForecastDay} object for the 7th day
     */
    public WeatherUndergroundJsonForecastDay getForecastDay7() {
        return getForecastDay(8);
    }

    /**
     * Get the {@link WeatherUndergroundJsonForecastDay} object for the 8th day
     *
     * Used to update the channels forecastDay8#xxx
     *
     * @return the {@link WeatherUndergroundJsonForecastDay} object for the 8th day
     */
    public WeatherUndergroundJsonForecastDay getForecastDay8() {
        return getForecastDay(9);
    }

    /**
     * Get the {@link WeatherUndergroundJsonForecastDay} object for the 9th day
     *
     * Used to update the channels forecastDay9#xxx
     *
     * @return the {@link WeatherUndergroundJsonForecastDay} object for the 9th day
     */
    public WeatherUndergroundJsonForecastDay getForecastDay9() {
        return getForecastDay(10);
    }

    /**
     * Get the {@link WeatherUndergroundJsonForecastDay} object for a given day
     *
     * @return the {@link WeatherUndergroundJsonForecastDay} object for the day
     */
    private WeatherUndergroundJsonForecastDay getForecastDay(int day) {
        return (forecast == null) ? null : forecast.getSimpleForecast(day);
    }
}
