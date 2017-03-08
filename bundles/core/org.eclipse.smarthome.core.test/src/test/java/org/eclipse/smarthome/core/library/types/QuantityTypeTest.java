/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import static org.junit.Assert.*;

import org.junit.Test;

import tec.uom.se.quantity.QuantityDimension;
import tec.uom.se.unit.Units;

/**
 * @author Gaël L'hopital
 */

public class QuantityTypeTest {

    @Test
    public void testDimensionless() {
        QuantityType dt0 = new QuantityType("12");
        assertTrue(dt0.getUnit().getDimension() == QuantityDimension.NONE);
    }

    @Test
    public void testKnownInvalidConstructors() {
        // These fails but shall not in principle, they are specific
        // cases of dimensionless values

        try {
            @SuppressWarnings("unused")
            QuantityType dt5 = new QuantityType("2°");
            fail();
        } catch (Exception e) {
        }

        try {
            @SuppressWarnings("unused")
            QuantityType dt0 = new QuantityType("52°31′27″");
            fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void testUnits() {

        // Check some splits
        @SuppressWarnings("unused")
        QuantityType dt00 = new QuantityType("2°C");
        QuantityType dt01 = new QuantityType("3 µs");
        QuantityType dt02 = new QuantityType("3km/h");
        QuantityType dt2 = QuantityType.valueOf("2m");

        // Check that the unit has correctly been identified
        assertEquals(dt2.getDimension(), QuantityDimension.LENGTH);
        assertEquals(dt2.getUnit(), Units.METRE);
        assertEquals("2 m", dt2.toFullString());

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

}
