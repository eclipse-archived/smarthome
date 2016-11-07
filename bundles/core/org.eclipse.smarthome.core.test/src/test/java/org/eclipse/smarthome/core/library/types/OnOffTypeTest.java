/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Simon Kaufmann - Initial contribution and API
 */
public class OnOffTypeTest {

    @Test
    public void testConversionToPercentType() {
        assertEquals(PercentType.HUNDRED, OnOffType.ON.as(PercentType.class));
        assertEquals(PercentType.ZERO, OnOffType.OFF.as(PercentType.class));
    }

    @Test
    public void testConversionToDecimalType() {
        assertEquals(new DecimalType("1.0"), OnOffType.ON.as(DecimalType.class));
        assertEquals(DecimalType.ZERO, OnOffType.OFF.as(DecimalType.class));
    }

    @Test
    public void testConversionToHSBType() {
        assertEquals(HSBType.WHITE, OnOffType.ON.as(HSBType.class));
        assertEquals(HSBType.BLACK, OnOffType.OFF.as(HSBType.class));
    }
}
