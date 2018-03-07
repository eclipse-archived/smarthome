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
package org.eclipse.smarthome.core.library.unit;

import java.math.BigInteger;

import javax.measure.Unit;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;
import javax.measure.spi.SystemOfUnits;

import tec.uom.se.format.SimpleUnitFormat;
import tec.uom.se.function.AddConverter;
import tec.uom.se.function.RationalConverter;
import tec.uom.se.unit.TransformedUnit;
import tec.uom.se.unit.Units;

/**
 * Imperial units used for the United States and Liberia.
 *
 * @author Henning Treu - initial contribution
 *
 */
public class ImperialUnits extends SmartHomeUnits {

    private static ImperialUnits INSTANCE = new ImperialUnits();

    private ImperialUnits() {
        // avoid external instantiation
    }

    @Override
    public String getName() {
        return ImperialUnits.class.getSimpleName();
    }

    /**
     * Returns the unique instance of this class.
     *
     * @return the Units instance.
     */
    public static SystemOfUnits getInstance() {
        return INSTANCE;
    }

    /** Additionally defined units to be used in ESH **/

    public static final Unit<Pressure> INCH_OF_MERCURY = addUnit(new TransformedUnit<>("inHg", Units.PASCAL,
            new RationalConverter(BigInteger.valueOf(3386388), BigInteger.valueOf(1000))));

    public static final Unit<Temperature> FAHRENHEIT = addUnit(new TransformedUnit<>("°F", Units.KELVIN,
            new RationalConverter(BigInteger.valueOf(5), BigInteger.valueOf(9)).concatenate(new AddConverter(459.67))));

    public static final Unit<Speed> MILES_PER_HOUR = addUnit(
            new TransformedUnit<>("mph", Units.KILOMETRE_PER_HOUR, new RationalConverter(1609344l, 1000000l)));

    public static final Unit<Length> INCH = addUnit(
            new TransformedUnit<>("in", Units.METRE, new RationalConverter(254l, 10000l)));

    public static final Unit<Length> MILE = addUnit(
            new TransformedUnit<>("mi", Units.METRE, new RationalConverter(1609344l, 1000l)));

    /**
     * Add unit symbols for imperial units.
     */
    static {
        SimpleUnitFormat.getInstance().label(INCH_OF_MERCURY, "inHg");
        SimpleUnitFormat.getInstance().label(FAHRENHEIT, "°F");
        SimpleUnitFormat.getInstance().label(MILES_PER_HOUR, "mph");
        SimpleUnitFormat.getInstance().label(INCH, "in");
        SimpleUnitFormat.getInstance().label(MILE, "mi");
    }

    /**
     * Adds a new unit not mapped to any specified quantity type.
     *
     * @param unit the unit being added.
     * @return <code>unit</code>.
     */
    private static <U extends Unit<?>> U addUnit(U unit) {
        INSTANCE.units.add(unit);
        return unit;
    }

}
