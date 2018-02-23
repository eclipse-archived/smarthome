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
package org.eclipse.smarthome.core.library.types;

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.CENTI;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Before;
import org.junit.Test;

import tec.uom.se.quantity.QuantityDimension;

/**
 * @author Gaël L'hopital - initial contribution
 */
public class QuantityTypeTest {

    @Before
    public void setup() {
        @SuppressWarnings("unused")
        Unit<Temperature> fahrenheit = ImperialUnits.FAHRENHEIT;
    }

    @Test
    public void testDimensionless() {
        // Dimensionless value that works
        new QuantityType<Dimensionless>("57%");

        QuantityType<Dimensionless> dt0 = new QuantityType<>("12");
        assertTrue(dt0.getUnit().getDimension() == QuantityDimension.NONE);
        dt0 = new QuantityType<>("2rad");
        assertTrue(dt0.getUnit().getDimension() == QuantityDimension.NONE);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testKnownInvalidConstructors() throws Exception {
        new QuantityType<>("123 Hello World");
    }

    @Test
    public void testValidConstructors() throws Exception {
        // Testing various quantities in order to ensure split and parsing is working
        // as expected
        new QuantityType<>("2°");
        new QuantityType<>("2°C");
        new QuantityType<>("3 µs");
        new QuantityType<>("3km/h");
        new QuantityType<>("1084 hPa");
        QuantityType.valueOf("2m");
    }

    @Test
    public void testUnits() {
        QuantityType<Length> dt2 = new QuantityType<>("2 m");
        // Check that the unit has correctly been identified
        assertEquals(dt2.getDimension(), QuantityDimension.LENGTH);
        assertEquals(dt2.getUnit(), SIUnits.METRE);
        assertEquals("2 m", dt2.toString());

        QuantityType<Length> dt1 = new QuantityType<>("2.1cm");
        // Check that the unit has correctly been identified
        assertEquals(dt1.getDimension(), QuantityDimension.LENGTH);
        assertEquals(dt1.getUnit(), CENTI(SIUnits.METRE));
        assertEquals("2.1 cm", dt1.toString());

        assertEquals(dt1.intValue(), dt2.intValue());

        QuantityType<Length> dt3 = new QuantityType<Length>("200cm");
        assertEquals(dt3.compareTo(dt2), 0);
        assertTrue(dt3.equals(dt2));

        QuantityType dt4 = new QuantityType<>("2kg");
        assertEquals("2 kg", dt4.toString());
        // check that beside the fact that we've got the same value, we don't have the same unit
        assertFalse(dt2.equals(dt4));
        try {
            dt2.compareTo(dt4);
            fail();
        } catch (Exception e) {
            // That's what we expect.
        }
    }

    @Test
    public void testConverters() {
        QuantityType<?> dt2 = QuantityType.valueOf("2 m");
        QuantityType<?> dt3 = new QuantityType<>("200 cm");

        assertEquals(dt2.toUnit(SIUnits.METRE), dt3.toUnit(SIUnits.METRE));
        assertEquals(dt2.toUnit(MetricPrefix.CENTI(SIUnits.METRE)), dt3.toUnit(MetricPrefix.CENTI(SIUnits.METRE)));

        dt3 = dt2.toUnit("cm");
        assertTrue(dt2.equals(dt3));

        QuantityType<?> tempInC = new QuantityType<>("22 °C");
        QuantityType<?> tempInK = tempInC.toUnit(SmartHomeUnits.KELVIN);
        assertTrue(tempInC.equals(tempInK));
        tempInK = tempInC.toUnit("K");
        assertTrue(tempInC.equals(tempInK));
    }

    @Test
    public void testConvertionOnSameUnit() {
        QuantityType<?> dt2 = QuantityType.valueOf("2 m");
        QuantityType<?> dt3 = dt2.toUnit("m");
        assertTrue(dt3.getUnit().toString().equalsIgnoreCase("m"));
    }

    @Test
    public void testConvertionFromDimensionless() {
        QuantityType<?> dt2 = QuantityType.valueOf("2");
        QuantityType<?> dt3 = dt2.toUnit("m");
        // Inconvertible units
        assertTrue(dt3 == null);
    }

    @Test
    public void testConversionToOnOffType() {
        assertEquals(OnOffType.ON, new QuantityType<>("1").as(OnOffType.class));
        assertEquals(OnOffType.OFF, new QuantityType<>("0").as(OnOffType.class));

        assertEquals(OnOffType.ON, new QuantityType<>("1 %").as(OnOffType.class));
        assertEquals(OnOffType.ON, new QuantityType<>("100 %").as(OnOffType.class));
        assertEquals(OnOffType.OFF, new QuantityType<>("0 %").as(OnOffType.class));
    }

    @Test
    public void testConversionToOpenCloseType() {
        assertEquals(OpenClosedType.OPEN, new QuantityType<>("1.0").as(OpenClosedType.class));
        assertEquals(OpenClosedType.CLOSED, new QuantityType<>("0.0").as(OpenClosedType.class));
        assertEquals(UnDefType.UNDEF, new QuantityType<>("0.5").as(OpenClosedType.class));
    }

    @Test
    public void testConversionToUpDownType() {
        assertEquals(UpDownType.UP, new QuantityType<>("0.0").as(UpDownType.class));
        assertEquals(UpDownType.DOWN, new QuantityType<>("1.0").as(UpDownType.class));
        assertEquals(UnDefType.UNDEF, new QuantityType<>("0.5").as(OpenClosedType.class));
    }

    @Test
    public void testConversionToHSBType() {
        assertEquals(new HSBType("0,0,0"), new QuantityType<>("0.0").as(HSBType.class));
        assertEquals(new HSBType("0,0,100"), new QuantityType<>("1.0").as(HSBType.class));
        assertEquals(new HSBType("0,0,50"), new QuantityType<>("0.5").as(HSBType.class));
    }

    @Test
    public void testConverionToPercentType() {
        assertEquals(PercentType.HUNDRED, new QuantityType<>("100 %").as(PercentType.class));
        assertEquals(PercentType.ZERO, new QuantityType<>("0 %").as(PercentType.class));
    }

    @Test
    public void toFullStringShouldParseToEqualState() {
        QuantityType<Temperature> temp = new QuantityType<>("20 °C");

        assertThat(temp.toFullString(), is("20 ℃"));
        assertThat(QuantityType.valueOf(temp.toFullString()), is(temp));
    }

    public void testAdd() {
        QuantityType<?> result = new QuantityType<>("20 m").add(new QuantityType<>("20cm"));
        assertThat(result, is(new QuantityType<>("20.20 m")));
    }

    @Test
    public void testNegate() {
        assertThat(new QuantityType<>("20 °C").negate(), is(new QuantityType<>("-20 °C")));
    }

    @Test
    public void testSubtract() {
        QuantityType<?> result = new QuantityType<>("20 m").subtract(new QuantityType<>("20cm"));
        assertThat(result, is(new QuantityType<>("19.80 m")));
    }

    @Test
    public void testMultiply_Number() {
        assertThat(new QuantityType<>("2 m").multiply(BigDecimal.valueOf(2)), is(new QuantityType<>("4 m")));
    }

    @Test
    public void testMultiply_QuantityType() {
        assertThat(new QuantityType<>("2 m").multiply(new QuantityType<>("4 cm")), is(new QuantityType<>("8 m·cm")));
    }

    @Test
    public void testDivide_Number() {
        assertThat(new QuantityType<>("4 m").divide(BigDecimal.valueOf(2)), is(new QuantityType<>("2 m")));
    }

    @Test
    public void testDivide_QuantityType() {
        assertThat(new QuantityType<>("4 m").divide(new QuantityType<>("2 cm")), is(new QuantityType<>("2 m/cm")));
    }

    @Test(expected = ArithmeticException.class)
    public void testDivide_Zero() {
        new QuantityType<>("4 m").divide(QuantityType.ZERO);
    }

}
