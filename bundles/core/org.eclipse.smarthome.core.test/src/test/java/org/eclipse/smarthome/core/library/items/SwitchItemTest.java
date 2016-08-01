/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.items;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.junit.Test;

/**
 * @author Chris Jackson - Initial contribution
 */
public class SwitchItemTest {

    @Test
    public void getAsPercentFromSwitch() {
        SwitchItem item = new SwitchItem("Test");
        item.setState(OnOffType.ON);
        assertEquals("100", item.getStateAs(PercentType.class).toString());
    }
}
