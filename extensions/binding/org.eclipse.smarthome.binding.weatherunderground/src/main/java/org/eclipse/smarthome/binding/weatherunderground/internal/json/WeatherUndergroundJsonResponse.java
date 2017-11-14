/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.weatherunderground.internal.json;

/**
 * The {@link WeatherUndergroundJsonResponse} is the Java class used
 * to map the entry "response" from the JSON response to a Weather Underground
 * request.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundJsonResponse {

    // Commented members indicate properties returned by the API not used by the binding

    // private String version;
    // private String termsofService;
    // private Object features;
    private WeatherUndergroundJsonError error;

    public WeatherUndergroundJsonResponse() {
    }

    /**
     * Get the error type returned by the Weather Underground service
     *
     * @return the error type or null if no error
     */
    public String getErrorType() {
        return (error == null) ? null : error.getType();
    }

    /**
     * Get the error description returned by the Weather Underground service
     *
     * @return the error description or null if no error
     */
    public String getErrorDescription() {
        return (error == null) ? null : error.getDescription();
    }
}
