/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.weatherunderground.internal.json;

/**
 * The {@link WeatherUndergroundJsonError} is the Java class used
 * to map the entry "response.error" from the JSON response to a Weather Underground
 * request.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundJsonError {

    private String type;
    private String description;

    public WeatherUndergroundJsonError() {
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
