/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.items;

import static junit.framework.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.junit.Test;

/**
 * @author Chris Jackson - Initial contribution
 */
public class DimmerItemTest {

    @Test
    public void GetAsPercentFromPercent() {
        DimmerItem item = new DimmerItem("Test");
        item.setState(new PercentType("25"));
        String res = item.getStateAs(PercentType.class).toString();
        assertEquals("0.25000000", res);
    }

    @Test
    public void GetAsPercentFromOnOff() {
        DimmerItem item = new DimmerItem("Test");
        item.setState(OnOffType.ON);
        String res = item.getStateAs(PercentType.class).toString();
        assertEquals("100", res);
    }
}
