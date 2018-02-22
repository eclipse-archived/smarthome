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
package org.eclipse.smarthome.core.types;

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.HECTO;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;

import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.junit.Test;

import tec.uom.se.quantity.Quantities;

/**
 * Test for the framework defined {@link SmartHomeUnits}.
 *
 * @author Henning Treu - initial contribution and API
 *
 */
public class SmartHomeUnitsTest {

    private static final double DEFAULT_ERROR = 0.0000000000000001d;

    @Test
    public void testinHg2PascalConversion() {
        Quantity<Pressure> inHg = Quantities.getQuantity(BigDecimal.ONE, ImperialUnits.INCH_OF_MERCURY);

        assertThat(inHg.to(SIUnits.PASCAL), is(Quantities.getQuantity(new BigDecimal("3386.388"), SIUnits.PASCAL)));
        assertThat(inHg.to(HECTO(SIUnits.PASCAL)),
                is(Quantities.getQuantity(new BigDecimal("33.86388"), HECTO(SIUnits.PASCAL))));
    }

    @Test
    public void test_inHg_UnitSymbol() {
        assertThat(ImperialUnits.INCH_OF_MERCURY.getSymbol(), is("inHg"));
        assertThat(ImperialUnits.INCH_OF_MERCURY.toString(), is("inHg"));
    }

    @Test
    public void testPascal2inHgConversion() {
        Quantity<Pressure> pascal = Quantities.getQuantity(new BigDecimal("3386.388"), SIUnits.PASCAL);

        assertThat(pascal.to(ImperialUnits.INCH_OF_MERCURY),
                is(Quantities.getQuantity(new BigDecimal("1.000"), ImperialUnits.INCH_OF_MERCURY)));
    }

    @Test
    public void testHectoPascal2Pascal() {
        Quantity<Pressure> pascal = Quantities.getQuantity(BigDecimal.valueOf(100), SIUnits.PASCAL);

        assertThat(pascal.to(HECTO(SIUnits.PASCAL)), is(Quantities.getQuantity(BigDecimal.ONE, HECTO(SIUnits.PASCAL))));
    }

    @Test
    public void test_hPa_UnitSymbol() {
        assertThat(HECTO(SIUnits.PASCAL).toString(), is("hPa"));
    }

    @Test
    public void testKelvin2Fahrenheit() {
        Quantity<Temperature> kelvin = Quantities.getQuantity(BigDecimal.ZERO, SmartHomeUnits.KELVIN);

        assertThat(kelvin.to(ImperialUnits.FAHRENHEIT),
                is(Quantities.getQuantity(new BigDecimal("-459.67"), ImperialUnits.FAHRENHEIT)));
    }

    @Test
    public void testKelvin2Fahrenheit2() {
        Quantity<Temperature> kelvin = Quantities.getQuantity(new BigDecimal(300), SmartHomeUnits.KELVIN);

        assertThat(kelvin.to(ImperialUnits.FAHRENHEIT),
                is(Quantities.getQuantity(new BigDecimal("80.33"), ImperialUnits.FAHRENHEIT)));
    }

    @Test
    public void testFahrenheit2Kelvin() {
        Quantity<Temperature> fahrenheit = Quantities.getQuantity(new BigDecimal(100), ImperialUnits.FAHRENHEIT);

        Quantity<Temperature> kelvin = fahrenheit.to(SmartHomeUnits.KELVIN);
        assertThat(kelvin.getUnit(), is(SmartHomeUnits.KELVIN));
        assertThat(kelvin.getValue().doubleValue(), is(closeTo(310.92777777777777778d, DEFAULT_ERROR)));
    }

    @Test
    public void testKmh2Mih() {
        Quantity<Speed> kmh = Quantities.getQuantity(new BigDecimal(10), SIUnits.KILOMETRE_PER_HOUR);

        Quantity<Speed> mph = kmh.to(ImperialUnits.MILES_PER_HOUR);
        assertThat(mph.getUnit(), is(ImperialUnits.MILES_PER_HOUR));
        assertThat(mph.getValue().doubleValue(), is(closeTo(6.21371192237333935d, DEFAULT_ERROR)));
    }

    @Test
    public void testCm2In() {
        Quantity<Length> cm = Quantities.getQuantity(new BigDecimal(10), MetricPrefix.CENTI(SIUnits.METRE));

        Quantity<Length> in = cm.to(ImperialUnits.INCH);
        assertThat(in.getUnit(), is(ImperialUnits.INCH));
        assertThat(in.getValue().doubleValue(), is(closeTo(3.93700787401574803d, DEFAULT_ERROR)));
    }

    @Test
    public void testM2Ml() {
        Quantity<Length> km = Quantities.getQuantity(new BigDecimal(10), MetricPrefix.KILO(SIUnits.METRE));

        Quantity<Length> mile = km.to(ImperialUnits.MILE);
        assertThat(mile.getUnit(), is(ImperialUnits.MILE));
        assertThat(mile.getValue().doubleValue(), is(closeTo(6.2137119223733395d, DEFAULT_ERROR)));
    }

    @Test
    public void test_fahrenheit_UnitSymbol() {
        assertThat(ImperialUnits.FAHRENHEIT.getSymbol(), is("°F"));
        assertThat(ImperialUnits.FAHRENHEIT.toString(), is("°F"));
    }

    @Test
    public void test_inch_UnitSymbol() {
        assertThat(ImperialUnits.INCH.getSymbol(), is("in"));
        assertThat(ImperialUnits.INCH.toString(), is("in"));
    }

    @Test
    public void test_mile_UnitSymbol() {
        assertThat(ImperialUnits.MILE.getSymbol(), is("mi"));
        assertThat(ImperialUnits.MILE.toString(), is("mi"));
    }

    @Test
    public void test_one_UnitSymbol() {
        assertThat(SmartHomeUnits.ONE.getSymbol(), is(""));

        Quantity<Dimensionless> one1 = Quantities.getQuantity(new BigDecimal(1), SmartHomeUnits.ONE);
        Quantity<Dimensionless> one2 = Quantities.getQuantity(new BigDecimal(1), SmartHomeUnits.ONE);

        assertThat(one1.add(one2).toString(), is("2 one"));
    }

}
