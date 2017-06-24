/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.weatherunderground.internal.json;

import java.net.URL;

/**
 * The {@link WeatherUndergroundJsonLocation} is the Java class used
 * to map the entry "location" from the JSON response to a Weather
 * Underground request.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundJsonLocation {

    // Commented members indicate properties returned by the API not used by the binding

    private String type;
    private String country;
    private String country_iso3166;
    private String country_name;
    private String state;
    private String city;
    private String tz_short;
    private String tz_long;
    private String lat;
    private String lon;
    private String zip;
    private String magic;
    private String wmo;
    private String l;
    private String requesturl;
    private String wuiurl;
    // private Object nearby_weather_stations;

    public WeatherUndergroundJsonLocation() {
    }

    public String getType() {
        return type;
    }

    public String getCountry() {
        return country;
    }

    public String getCountry_iso3166() {
        return country_iso3166;
    }

    public String getCountry_name() {
        return country_name;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }

    public String getTz_short() {
        return tz_short;
    }

    public String getTz_long() {
        return tz_long;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public String getZip() {
        return zip;
    }

    public String getMagic() {
        return magic;
    }

    public String getWmo() {
        return wmo;
    }

    public String getL() {
        return l;
    }

    public URL getRequesturl() {
        return WeatherUndergroundJsonUtils.getValidUrl(requesturl);
    }

    public URL getWuiurl() {
        return WeatherUndergroundJsonUtils.getValidUrl(wuiurl);
    }
}
