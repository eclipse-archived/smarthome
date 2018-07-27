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
package org.eclipse.smarthome.binding.hue.internal;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

/**
 * Detailed sensor information
 *
 * @author Samuel Leisering - Initial sensor support
 *
 */
public class FullSensor extends FullHueObject {

    public static final String CONFIG_REACHABLE = "reachable";
    public static final String CONFIG_BATTERY = "battery";
    public static final String CONFIG_ALERT = "alert";
    public static final String CONFIG_ON = "on";
    public static final String CONFIG_PRESENCE_SENSITIVITY = "sensitivity";
    public static final String CONFIG_PRESENCE_SENSITIVITY_MAX = "sensitivitymax";

    public static final String CONFIG_THOLD_DARK = "tholddark";
    public static final String CONFIG_THOLD_OFFSET = "tholdoffset";

    public static final Type GSON_TYPE = new TypeToken<Map<String, FullSensor>>() {
    }.getType();

    private Map<String, Object> state;
    private Map<String, Object> config;

    public Map<String, Object> getState() {
        return state;
    }

    public Map<String, Object> getConfig() {
        return config;
    }
}
