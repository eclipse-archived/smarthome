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
package org.eclipse.smarthome.binding.meteoblue.internal.json;

import com.google.gson.annotations.SerializedName;

/**
 * {@link JsonUnits} models the 'units' portion of the JSON
 * response to a weather request.
 *
 * @author Chris Carman - Initial contribution
 */
public class JsonUnits {
    private String time;
    private String predictability;

    @SerializedName("precipitation_probability")
    private String precipitationProbability;

    private String pressure;

    @SerializedName("relativehumidity")
    private String relativeHumidity;

    private String temperature;

    @SerializedName("winddirection")
    private String windDirection;

    private String precipitation;

    @SerializedName("windspeed")
    private String windSpeed;

    public JsonUnits() {
    }

    public String getPredictability() {
        return predictability;
    }

    public String getPrecipitationProbability() {
        return precipitationProbability;
    }

    public String getPressure() {
        return pressure;
    }

    public String getRelativeHumidity() {
        return relativeHumidity;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getPrecipitation() {
        return precipitation;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getTime() {
        return time;
    }
}
