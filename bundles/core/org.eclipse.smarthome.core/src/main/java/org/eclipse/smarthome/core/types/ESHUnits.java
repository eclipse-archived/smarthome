/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.types;

import static tec.uom.se.unit.MetricPrefix.HECTO;

import java.math.BigInteger;

import javax.measure.Unit;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import tec.uom.se.format.SimpleUnitFormat;
import tec.uom.se.function.AddConverter;
import tec.uom.se.function.RationalConverter;
import tec.uom.se.unit.TransformedUnit;
import tec.uom.se.unit.Units;

/**
 * Unit Provider.
 *
 * @author Gaël L'hopital - Initial contribution .
 */
public class ESHUnits extends Units {

    public static final Unit<Pressure> INCH_OF_MERCURY = new TransformedUnit<>("inHg", PASCAL,
            new RationalConverter(BigInteger.valueOf(3386388), BigInteger.valueOf(1000)));

    public static final Unit<Pressure> HECTO_PASCAL = HECTO(PASCAL);

    public static final Unit<Temperature> FAHRENHEIT = new TransformedUnit<>("°F", KELVIN,
            new RationalConverter(BigInteger.valueOf(5), BigInteger.valueOf(9)).concatenate(new AddConverter(459.67)));

    static {
        SimpleUnitFormat.getInstance().label(INCH_OF_MERCURY, "inHg");
        SimpleUnitFormat.getInstance().label(FAHRENHEIT, "°F");
    }

}
