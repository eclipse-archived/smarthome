/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Test;

/**
 * @author Kai Kreuzer - Initial contribution and API
 */
public class PercentTypeTest {

    @Test(expected = IllegalArgumentException.class)
    public void negativeNumber() {
        new PercentType(-3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void MoreThan100() {
        new PercentType("100.2");
    }

    @Test
    public void DoubleValue() {
        PercentType pt = new PercentType("0.0001");
        assertEquals("0.0001", pt.toString());
    }

    @Test
    public void IntValue() {
        PercentType pt = new PercentType(100);
        assertEquals("100", pt.toString());
    }

    @Test
    public void testEquals() {
        PercentType pt1 = new PercentType(new Integer(100));
        PercentType pt2 = new PercentType("100.0");
        PercentType pt3 = new PercentType(0);
        PercentType pt4 = new PercentType(0);

        assertEquals(true, pt1.equals(pt2));
        assertEquals(true, pt3.equals(pt4));
        assertEquals(false, pt3.equals(pt1));
    }

    @Test
    public void testConversionToOnOffType() {
        assertEquals(OnOffType.ON, new PercentType("100.0").as(OnOffType.class));
        assertEquals(OnOffType.ON, new PercentType("1.0").as(OnOffType.class));
        assertEquals(OnOffType.OFF, new PercentType("0.0").as(OnOffType.class));
    }

    @Test
    public void testConversionToDecimalType() {
        assertEquals(new DecimalType("1.0"), new PercentType("100.0").as(DecimalType.class));
        assertEquals(new DecimalType("0.01"), new PercentType("1.0").as(DecimalType.class));
        assertEquals(DecimalType.ZERO, new PercentType("0.0").as(DecimalType.class));
    }

    @Test
    public void testConversionToOpenCloseType() {
        assertEquals(OpenClosedType.OPEN, new PercentType("100.0").as(OpenClosedType.class));
        assertEquals(OpenClosedType.CLOSED, new PercentType("0.0").as(OpenClosedType.class));
        assertEquals(UnDefType.UNDEF, new PercentType("50.0").as(OpenClosedType.class));
    }

    @Test
    public void testConversionToUpDownType() {
        assertEquals(UpDownType.UP, new PercentType("0.0").as(UpDownType.class));
        assertEquals(UpDownType.DOWN, new PercentType("100.0").as(UpDownType.class));
        assertEquals(UnDefType.UNDEF, new PercentType("50.0").as(OpenClosedType.class));
    }

    @Test
    public void testConversionToHSBType() {
        assertEquals(new HSBType("0,0,0"), new PercentType("0.0").as(HSBType.class));
        assertEquals(new HSBType("0,0,100"), new PercentType("100.0").as(HSBType.class));
        assertEquals(new HSBType("0,0,50"), new PercentType("50.0").as(HSBType.class));
    }

}
