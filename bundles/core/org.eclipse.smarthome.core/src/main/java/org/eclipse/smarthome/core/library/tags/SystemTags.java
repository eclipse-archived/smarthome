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
package org.eclipse.smarthome.core.library.tags;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * System-wide semantic tags
 *
 * @author Stefan Triller - Initial Contribution
 *
 */
@NonNullByDefault
public class SystemTags {

    public static final String TAG_KIND_DELIMITER = ":";

    private static enum Kind {
        Object,
        Location,
        Property,
        Purpose,
        Capability;
    }

    public static final String LOC_HOME = Kind.Location + TAG_KIND_DELIMITER + "Home";
    public static final String LOC_WORK = Kind.Location + TAG_KIND_DELIMITER + "Work";

    public static final String PROP_TEMPERATURE = Kind.Property + TAG_KIND_DELIMITER + "Temperature";
    public static final String PROP_LIGHT = Kind.Property + TAG_KIND_DELIMITER + "Light";
    public static final String PROP_HUMIDITY = Kind.Property + TAG_KIND_DELIMITER + "Humidity";
    public static final String PROP_PRESSURE = Kind.Property + TAG_KIND_DELIMITER + "Pressure";
    public static final String PROP_SMOKE = Kind.Property + TAG_KIND_DELIMITER + "Smoke";
    public static final String PROP_NOISE = Kind.Property + TAG_KIND_DELIMITER + "Noise";
    public static final String PROP_RAIN = Kind.Property + TAG_KIND_DELIMITER + "Rain";
    public static final String PROP_WIND = Kind.Property + TAG_KIND_DELIMITER + "Wind";
    public static final String PROP_WATER = Kind.Property + TAG_KIND_DELIMITER + "Water";
    public static final String PROP_CO2 = Kind.Property + TAG_KIND_DELIMITER + "CO2";
    public static final String PROP_CO = Kind.Property + TAG_KIND_DELIMITER + "CO";
    public static final String PROP_ENERGY = Kind.Property + TAG_KIND_DELIMITER + "Energy";
    public static final String PROP_POWER = Kind.Property + TAG_KIND_DELIMITER + "Power";
    public static final String PROP_VOLTAGE = Kind.Property + TAG_KIND_DELIMITER + "Voltage";
    public static final String PROP_CURRENT = Kind.Property + TAG_KIND_DELIMITER + "Current";
    public static final String PROP_FREQUENCY = Kind.Property + TAG_KIND_DELIMITER + "Frequency";
    public static final String PROP_GAS = Kind.Property + TAG_KIND_DELIMITER + "Gas";
    public static final String PROP_OIL = Kind.Property + TAG_KIND_DELIMITER + "Oil";

    public static final String PUR_ALARM = Kind.Purpose + TAG_KIND_DELIMITER + "Alarm";
    public static final String PUR_ALARM_FIRE = PUR_ALARM + TAG_KIND_DELIMITER + "Fire";
    public static final String PUR_ALARM_LEAKAGE = PUR_ALARM + TAG_KIND_DELIMITER + "Leakage";
    public static final String PUR_ALARM_INTRUSION = PUR_ALARM + TAG_KIND_DELIMITER + "Intrusion";
    public static final String PUR_ALARM_CO = PUR_ALARM + TAG_KIND_DELIMITER + "CO";
    public static final String PUR_LIGHTING = Kind.Purpose + TAG_KIND_DELIMITER + "Lighting";
    public static final String PUR_LIGHTING_ILLUMINATION = PUR_LIGHTING + TAG_KIND_DELIMITER + "Illumination";
    public static final String PUR_LIGHTING_AMBIENCE = PUR_LIGHTING + TAG_KIND_DELIMITER + "Ambience";
    public static final String PUR_LIGHTING_NIGHT = PUR_LIGHTING + TAG_KIND_DELIMITER + "Night";
    public static final String PUR_HEATING = Kind.Purpose + TAG_KIND_DELIMITER + "Heating";
    public static final String PUR_COOLING = Kind.Purpose + TAG_KIND_DELIMITER + "Cooling";

    public static final String CAP_MEASUREMENT = Kind.Capability + TAG_KIND_DELIMITER + "Measurement";
    public static final String CAP_CONTROL = Kind.Capability + TAG_KIND_DELIMITER + "Control";
    public static final String CAP_SWITCHABLE = Kind.Capability + TAG_KIND_DELIMITER + "Switchable";
    public static final String CAP_DIMMABLE = Kind.Capability + TAG_KIND_DELIMITER + "Dimmable";
    public static final String CAP_COLOR = Kind.Capability + TAG_KIND_DELIMITER + "Color";
    public static final String CAP_COLOR_TEMPERATURE = Kind.Capability + TAG_KIND_DELIMITER + "ColorTemperature";
    public static final String CAP_LOW_BATTERY = Kind.Capability + TAG_KIND_DELIMITER + "LowBattery";
    public static final String CAP_OPENSTATE = Kind.Capability + TAG_KIND_DELIMITER + "OpenState";
    public static final String CAP_TAMPERED = Kind.Capability + TAG_KIND_DELIMITER + "Tampered";
    public static final String CAP_OPEN_LEVEL = Kind.Capability + TAG_KIND_DELIMITER + "OpenLevel";
    public static final String CAP_TILT = Kind.Capability + TAG_KIND_DELIMITER + "Tilt";
    public static final String CAP_VOLUME = Kind.Capability + TAG_KIND_DELIMITER + "Volume";
}
