/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.types;

import javax.measure.Unit;

import org.apache.commons.lang.StringUtils;

import tec.uom.se.unit.Units;

/**
 * Dimension of the channel.
 *
 * @author Gaël L'hopital - Initial contribution .
 */
public enum Dimension {
    // ACCELERATION,
    // AMOUNT_OF_SUBSTANCE,
    // ANGLE,
    // AREA,
    // CATALIYTIC_ACTIVITY,
    DIMENSIONLESS(null),
    // ELECTRIC_CAPACITANCE,
    // ELECTRIC_CHARGE,
    // ELECTRIC_CONDUCTANCE,
    // ELECTRIC_CURRENT,
    // ELECTRIC_INDUCTANCE,
    // ELECTRIC_POTENTIAL,
    // ELECTRIC_RESISTANCE,
    // ENERGY,
    // FORCE,
    // FREQUENCY,
    // ILLUMINANCE,
    LENGTH(Units.METRE),
    // LUMINOUS_FLUX,
    // LUMINOUS_INTENSITY,
    // MAGNETIC_FLUX,
    // MAGNETIC_FLUX_DENSITY,
    // MASS,
    // POWER,
    PRESSURE(Units.PASCAL),
    // RADIATION_DOSE_ABSORBED,
    // RADIATION_DOSE_EFFECTIVE,
    // RADIO_ACTVITY,
    // SOLID_ANGLE,
    SPEED(Units.KILOMETRE_PER_HOUR),
    TEMPERATURE(Units.KELVIN),
    // TIME,
    // VOLUME,
    INTENSITY(ESHUnits.IRRADIANCE);

    private final Unit<?> defaultUnit;

    private Dimension(Unit<?> defaultUnit) {
        this.defaultUnit = defaultUnit;
    }

    public Unit<?> getDefaultUnit() {
        return defaultUnit;
    }

    /**
     * Parses the input string into a {@link Dimension}.
     *
     * @param input the input string
     * @return the parsed ChannelDimension.
     * @throws IllegalArgumentException if the input couldn't be parsed.
     */
    public static Dimension parse(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        for (Dimension value : values()) {
            if (value.name().equalsIgnoreCase(input)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown channel dimension: '" + input + "'");
    }

}
