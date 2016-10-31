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
public class OpenClosedTypeTest {

    @Test
    public void testConversionToPercentType() {
        assertEquals(PercentType.ZERO, OpenClosedType.CLOSED.as(PercentType.class));
        assertEquals(PercentType.HUNDRED, OpenClosedType.OPEN.as(PercentType.class));
    }

    @Test
    public void testConversionToDecimalType() {
        assertEquals(DecimalType.ZERO, OpenClosedType.CLOSED.as(DecimalType.class));
        assertEquals(new DecimalType(1), OpenClosedType.OPEN.as(DecimalType.class));
    }
}
