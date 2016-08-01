/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.items;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;

/**
 * @author Chris Jackson - Initial contribution
 * @author Markus Rathgeb - Add more tests
 */
public class DimmerItemTest {

    private static DimmerItem createDimmerItem(final State state) {
        final DimmerItem item = new DimmerItem("Test");
        item.setState(state);
        return item;
    }

    private static BigDecimal getState(final Item item, Class<? extends State> typeClass) {
        final State state = item.getStateAs(typeClass);
        final String str = state.toString();
        final BigDecimal result = new BigDecimal(str);
        return result;
    }

    @Test
    public void getAsPercentFromPercent() {
        final BigDecimal origin = new BigDecimal(25);
        final DimmerItem item = createDimmerItem(new PercentType(origin));
        final BigDecimal result = getState(item, PercentType.class);
        assertEquals(origin.compareTo(result), 0);
    }

    @Test
    public void getAsDecimalFromDecimal() {
        final BigDecimal origin = new BigDecimal(0.25);
        final DimmerItem item = createDimmerItem(new DecimalType(origin));
        final BigDecimal result = getState(item, DecimalType.class);
        assertEquals(origin.compareTo(result), 0);
    }

    @Test
    public void getAsPercentFromDecimal() {
        final BigDecimal origin = new BigDecimal(0.25);
        final DimmerItem item = createDimmerItem(new DecimalType(origin));
        final BigDecimal result = getState(item, PercentType.class);
        assertEquals(origin.multiply(new BigDecimal(100)).compareTo(result), 0);
    }

    @Test
    public void getAsDecimalFromPercent() {
        final BigDecimal origin = new BigDecimal(25);
        final DimmerItem item = createDimmerItem(new PercentType(origin));
        final BigDecimal result = getState(item, DecimalType.class);
        assertEquals(origin.divide(new BigDecimal(100)).compareTo(result), 0);
    }

}
