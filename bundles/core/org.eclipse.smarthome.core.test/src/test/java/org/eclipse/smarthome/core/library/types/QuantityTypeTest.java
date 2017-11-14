/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Test;

import tec.uom.se.quantity.QuantityDimension;
import tec.uom.se.unit.Units;

/**
 * @author Gaël L'hopital
 */

public class QuantityTypeTest {

    @Test
    public void testDimensionless() {
        // Dimensionless value that works
        new QuantityType("57%");

        QuantityType dt0 = new QuantityType("12");
        assertTrue(dt0.getUnit().getDimension() == QuantityDimension.NONE);
        dt0 = new QuantityType("2rad");
        assertTrue(dt0.getUnit().getDimension() == QuantityDimension.NONE);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testKnownInvalidConstructors1() throws Exception {
        new QuantityType("2°");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKnownInvalidConstructors2() throws Exception {
        new QuantityType("57° 17' 44\"");
    }

    @Test
    public void testValidConstructors() throws Exception {
        // Testing various quantities in order to ensure split and parsing is working
        // as expected
        new QuantityType("2°C");
        new QuantityType("3 µs");
        new QuantityType("3km/h");
        new QuantityType("1084 hPa");
        QuantityType.valueOf("2m");
    }

    @Test
    public void testUnits() {

        QuantityType dt2 = QuantityType.valueOf("2m");
        // Check that the unit has correctly been identified
        assertEquals(dt2.getDimension(), QuantityDimension.LENGTH);
        assertEquals(dt2.getUnit(), Units.METRE);
        assertEquals("2 m", dt2.toString());

        QuantityType dt1 = new QuantityType("2.1cm");
        // Check that the unit has correctly been identified
        assertEquals(dt1.getDimension(), QuantityDimension.LENGTH);
        assertEquals(dt1.getUnit(), tec.uom.se.unit.MetricPrefix.CENTI(Units.METRE));
        assertEquals("2.1 cm", dt1.toString());

        assertEquals(dt1.intValue(), dt2.intValue());

        QuantityType dt3 = new QuantityType("200cm");
        assertEquals(dt3.compareTo(dt2), 0);
        assertTrue(dt3.equals(dt2));

        QuantityType dt4 = new QuantityType("2kg");
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
        QuantityType dt2 = QuantityType.valueOf("2 m");
        QuantityType dt3 = new QuantityType("200 cm");

        assertEquals(dt2.toUnit(Units.METRE), dt3.toUnit(Units.METRE));
        assertEquals(dt2.toUnit(tec.uom.se.unit.MetricPrefix.CENTI(Units.METRE)),
                dt3.toUnit(tec.uom.se.unit.MetricPrefix.CENTI(Units.METRE)));

        dt3 = dt2.toUnit("cm");
        assertTrue(dt2.equals(dt3));

        QuantityType tempInC = new QuantityType("22 °C");
        QuantityType tempInK = tempInC.toUnit(Units.KELVIN);
        assertTrue(tempInC.equals(tempInK));
        tempInK = tempInC.toUnit("K");
        assertTrue(tempInC.equals(tempInK));
    }

    @Test
    public void testConvertionOnSameUnit() {
        QuantityType dt2 = QuantityType.valueOf("2 m");
        QuantityType dt3 = dt2.toUnit("m");
        assertTrue(dt3.getUnit().toString().equalsIgnoreCase("m"));
    }

    @Test
    public void testConvertionFromDimensionless() {
        QuantityType dt2 = QuantityType.valueOf("2");
        QuantityType dt3 = dt2.toUnit("m");
        // Inconvertible units
        assertTrue(dt3 == null);
    }

    @Test
    public void testConversionToOnOffType() {

        assertEquals(OnOffType.ON, new QuantityType("1").as(OnOffType.class));
        assertEquals(OnOffType.OFF, new QuantityType("0").as(OnOffType.class));
    }

    @Test
    public void testConversionToOpenCloseType() {
        assertEquals(OpenClosedType.OPEN, new QuantityType("1.0").as(OpenClosedType.class));
        assertEquals(OpenClosedType.CLOSED, new QuantityType("0.0").as(OpenClosedType.class));
        assertEquals(UnDefType.UNDEF, new QuantityType("0.5").as(OpenClosedType.class));
    }

    @Test
    public void testConversionToUpDownType() {
        assertEquals(UpDownType.UP, new QuantityType("0.0").as(UpDownType.class));
        assertEquals(UpDownType.DOWN, new QuantityType("1.0").as(UpDownType.class));
        assertEquals(UnDefType.UNDEF, new QuantityType("0.5").as(OpenClosedType.class));
    }

    @Test
    public void testConversionToHSBType() {
        assertEquals(new HSBType("0,0,0"), new QuantityType("0.0").as(HSBType.class));
        assertEquals(new HSBType("0,0,100"), new QuantityType("1.0").as(HSBType.class));
        assertEquals(new HSBType("0,0,50"), new QuantityType("0.5").as(HSBType.class));
    }

    @Test
    public void toFullStringShouldOnlyGiveScalarValue() {
        assertThat(new QuantityType("20 °C").toFullString(), is("20"));
    }

}
