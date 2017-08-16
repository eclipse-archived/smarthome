/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.weatherunderground.internal.config;

/**
 * The {@link WeatherUndergroundConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundConfiguration {

    public static final String LOCATION = "location";

    public String apikey;
    public String location;
    public String language;
    public Integer refresh;

}
