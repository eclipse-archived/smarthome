/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters;

import java.util.HashMap;

/**
 * The {@link SensorEnum} lists all available digitalSTROM sensor types.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 * @see http://developer.digitalstrom.org/Architecture/ds-basics.pdf Table 36: Output Mode Register, page 51
 */
public enum SensorEnum {
    /*
     * Table 40: Sensor Types from ds-basic.pdf (http://developer.digitalstrom.org/Architecture/ds-basics.pdf) from
     * 19.08.2015
     *
     * | Sensor Type | Description | Unit | Min | 12 Bit Max | 12 Bit Resolution |
     * -----------------------------------------------------------------------------------------------------------------
     * -------------------------------------------------------------
     * | 4 | Active power | Watts (W) | 0 | 4095 | 1 |
     * | 5 | Output current | Ampere (mA) | 0 | 4095 | 1 |
     * | 6 | Electric meter | Kilowatt hours (kWh) | 0 | 40,95 | 0,01 |
     * | 9 | Temperature indoors | Kelvin (K) | 230 | 332,375 | 0,25 |
     * | 10 | Temperature outdoors | Kelvin (K) | 230 | 332,375 | 0,25 |
     * | 11 | Brightness indoors | Lux (Lx) | 1 | 131446,795 | logarithmic: lx = 10 * (x/800), x = 800 * log(lx) |
     * | 12 | Brightness outdoors | Lux (Lx) | 1 | 131446,795 | logarithmic: lx = 10 * (x/800), x = 800 * log(lx) |
     * | 13 | Relative humidity indoors | Percent (%) | 0 | 102,375 | 0,025 |
     * | 14 | Relative humidity outdoors | Percent (%) | 0 | 102,375 | 0,025 |
     * | 15 | Air pressure | Pascal (hPa) | 200 | 1223,75 | 0,25 |
     * | 18 | Wind speed | Meters per second (m/s) | 0 | 102,375 | 0,025 |
     * | 19 | Wind direction | degrees | 0 | 511,875 | 0,54 |
     * | 20 | Precipitation | Millimeter per square meter (mm/m2) | 0 | 102,375 | 0,025 |
     * | 21 | Carbon Dioxide | Parts per million (ppm) | 1 | 131446,795 | logarithmic: ppm = 10 * (x/800), x = 800 * log
     * (ppm) |
     * | 25 | Sound pressure level | Decibel (dB) | 0 | 255,938 | 0,25/4 |
     * | 50 | Room temperature set point | Kelvin (K) | 230 | 332,375 | 0,025 |
     * | 51 | Room temperature control variable | Percent (%) | 0 | 102,375 | 0,025 |
     * | 64 | Output current (H) | Ampere (mA) | 0 | 16380 | 4 |
     * | 65 | Power consumption | Volt-Ampere (VA) | 0 | 4095 | 1 |
     */
    ACTIVE_POWER(4, "watt", "W"),
    OUTPUT_CURRENT(5, "ampere", "mA"),
    ELECTRIC_METER(6, "kilowatt hours", "kWh"),
    TEMPERATURE_INDOORS(9, "kelvin", "K"),
    TEMPERATURE_OUTDOORS(10, "kelvin", "K"),
    BRIGHTNESS_INDOORS(11, "lux", "Lx"),
    BRIGHTNESS_OUTDOORS(12, "lux", "Lx"),
    RELATIVE_HUMIDITY_INDOORS(13, "percent", "%"),
    RELATIVE_HUMIDITY_OUTDOORS(14, "percent", "%"),
    AIR_PRESSURE(15, "pascal", "hPa"),
    WIND_SPEED(18, "meters per second", "m/s"),
    PRECIPITATION(20, "millimeter per square meter", "mm/m2"),
    CARBONE_DIOXIDE(21, "parts per million", "ppm"),
    SOUND_PRESSURE_LEVEL(25, "decibel", "dB"),
    ROOM_TEMPERATION_SET_POINT(50, "kelvin", "K"),
    ROOM_TEMPERATION_CONTROL_VARIABLE(51, "kelvin", "K"),
    OUTPUT_CURRENT_H(64, "ampere", "mA"),
    POWER_CONSUMPTION(65, "volt-ampere", "VA");

    private final int sensorType;
    private final String unit;
    private final String unitShortcut;

    static final HashMap<Integer, SensorEnum> sensorEnums = new HashMap<Integer, SensorEnum>();

    static {
        for (SensorEnum sensor : SensorEnum.values()) {
            sensorEnums.put(sensor.getSensorType(), sensor);
        }
    }

    /**
     * Returns true, if the given typeIndex contains in digitalSTROM, otherwise false.
     *
     * @param typeIndex
     * @return true, if contains otherwise false
     */
    public static boolean containsSensor(Integer typeIndex) {
        return sensorEnums.keySet().contains(typeIndex);
    }

    /**
     * Returns the {@link SensorEnum} for the given typeIndex, otherwise null.
     *
     * @param typeIndex
     * @return SensorEnum or null
     */
    public static SensorEnum getSensor(Integer typeIndex) {
        return sensorEnums.get(typeIndex);
    }

    /**
     * Returns true, if the given sensor type index contains in digitalSTROM sensor types, otherwise false.
     *
     * @param sceneNumber
     * @return true, if contains otherwise false
     */
    public static boolean containsScene(Integer typeIndex) {
        return sensorEnums.keySet().contains(typeIndex);
    }

    SensorEnum(int sensorType, String unit, String unitShortcut) {
        this.sensorType = sensorType;
        this.unit = unit;
        this.unitShortcut = unitShortcut;
    }

    /**
     * Returns the typeIndex of this {@link OutputModeEnum} object.
     *
     * @return typeIndex
     */
    public int getSensorType() {
        return this.sensorType;
    }

    /**
     * Returns the unit of this {@link OutputModeEnum} object.
     *
     * @return unit
     */
    public String getUnit() {
        return this.unit;
    }

    /**
     * Returns the unit shortcut of this {@link OutputModeEnum} object.
     *
     * @return unit shortcut
     */
    public String getUnitShortcut() {
        return this.unitShortcut;
    }
}
