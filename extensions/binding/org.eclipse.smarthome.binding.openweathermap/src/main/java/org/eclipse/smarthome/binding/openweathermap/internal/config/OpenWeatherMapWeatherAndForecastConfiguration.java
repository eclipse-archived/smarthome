/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.openweathermap.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.openweathermap.internal.handler.OpenWeatherMapWeatherAndForecastHandler;

/**
 * The {@link OpenWeatherMapWeatherAndForecastConfiguration} is the class used to match the
 * {@link OpenWeatherMapWeatherAndForecastHandler}s configuration.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapWeatherAndForecastConfiguration extends OpenWeatherMapLocationConfiguration {

    private int forecastHours;
    private int forecastDays;

    public int getForecastHours() {
        return forecastHours;
    }

    public void setForecastHours(int forecastHours) {
        this.forecastHours = forecastHours;
    }

    public int getForecastDays() {
        return forecastDays;
    }

    public void setForecastDays(int forecastDays) {
        this.forecastDays = forecastDays;
    }
}
