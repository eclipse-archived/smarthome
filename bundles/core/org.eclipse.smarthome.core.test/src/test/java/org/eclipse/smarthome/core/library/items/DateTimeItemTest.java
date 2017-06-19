/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.items;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.junit.Test;

/**
 *
 * @author Stefan Triller
 *
 */
public class DateTimeItemTest {

    @Test
    public void testDateTimeType() {
        DateTimeItem item = new DateTimeItem("test");
        DateTimeType state = new DateTimeType();
        item.setState(state);
        assertEquals(state, item.getState());
    }

    @Test
    public void testUndefType() {
        DateTimeItem item = new DateTimeItem("test");
        StateUtil.testUndefStates(item);
    }

    @Test
    public void testAcceptedStates() {
        DateTimeItem item = new DateTimeItem("test");
        StateUtil.testAcceptedStates(item);
    }

}
