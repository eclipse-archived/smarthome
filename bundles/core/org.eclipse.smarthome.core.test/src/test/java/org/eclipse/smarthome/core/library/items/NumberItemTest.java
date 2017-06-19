/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.items;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;

/**
 *
 * @author Stefan Triller - Initial version
 *
 */
public class NumberItemTest {

    @Test
    public void setDecimalType() {
        NumberItem item = new NumberItem("test");
        State decimal = new DecimalType("23");
        item.setState(decimal);
        assertEquals(decimal, item.getState());
    }

    @Test
    public void setPercentType() {
        NumberItem item = new NumberItem("test");
        State percent = new PercentType(50);
        item.setState(percent);
        assertEquals(percent, item.getState());
    }

    @Test
    public void setHSBType() {
        NumberItem item = new NumberItem("test");
        State hsb = new HSBType("5,23,42");
        item.setState(hsb);
        assertEquals(hsb, item.getState());
    }

    @Test
    public void testUndefType() {
        NumberItem item = new NumberItem("test");
        StateUtil.testUndefStates(item);
    }

    @Test
    public void testAcceptedStates() {
        NumberItem item = new NumberItem("test");
        StateUtil.testAcceptedStates(item);
    }

}
