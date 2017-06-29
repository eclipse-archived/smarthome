/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.type;

import javax.measure.Unit;

import org.eclipse.smarthome.core.i18n.UnitProvider;

/**
 * Dimension of the channel.
 *
 * @author Gaël L'hopital - Initial contribution .
 */
public enum ChannelDimension {
    ACCELERATION,
    AMOUNT_OF_SUBSTANCE,
    ANGLE,
    AREA,
    CATALIYTIC_ACTIVITY,
    DIMENSIONLESS,
    ELECTRIC_CAPACITANCE,
    ELECTRIC_CHARGE,
    ELECTRIC_CONDUCTANCE,
    ELECTRIC_CURRENT,
    ELECTRIC_INDUCTANCE,
    ELECTRIC_POTENTIAL,
    ELECTRIC_RESISTANCE,
    ENERGY,
    FORCE,
    FREQUENCY,
    ILLUMINANCE,
    LENGTH,
    LUMINOUS_FLUX,
    LUMINOUS_INTENSITY,
    MAGNETIC_FLUX,
    MAGNETIC_FLUX_DENSITY,
    MASS,
    POWER,
    PRESSURE,
    RADIATION_DOSE_ABSORBED,
    RADIATION_DOSE_EFFECTIVE,
    RADIO_ACTVITY,
    SOLID_ANGLE,
    SPEED,
    TEMPERATURE,
    TIME,
    VOLUME;

    /**
     * Parses the input string into a {@link ChannelDimension}.
     *
     * @param input the input string
     * @return the parsed ChannelDimension.
     * @throws IllegalArgumentException if the input couldn't be parsed.
     */
    public static ChannelDimension parse(String input) {

        if (input == null) {
            return null;
        }

        for (ChannelDimension value : values()) {
            if (value.name().equalsIgnoreCase(input)) {
                return value;
            }
        }

        throw new IllegalArgumentException("Unknown channel dimension: '" + input + "'");
    }

    /**
     * Parses the input string into a {@link ChannelDimension}.
     *
     * @param input the input string
     * @return the parsed ChannelDimension.
     * @throws IllegalArgumentException if the input couldn't be parsed.
     */

    public static ChannelDimension fromUnit(Unit<?> input) {

        if (input == null) {
            return null;
        }
        
        if (input.isCompatible(UnitProvider.PASCAL)) return PRESSURE;
        if (input.isCompatible(UnitProvider.CELSIUS)) return TEMPERATURE;      

        throw new IllegalArgumentException("Unknown channel dimension: '" + input.getClass().getName() + "'");
    }

    public static Unit<?> getDefaultUnit(ChannelDimension dimension) {
        switch (dimension) {
            case TEMPERATURE:
                return UnitProvider.CELSIUS;
            case PRESSURE:
                return UnitProvider.PASCAL;
            default:
                return null;
        }
    }

}
