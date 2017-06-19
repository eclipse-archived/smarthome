/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.items;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.junit.Test;

/**
 *
 * @author Stefan Triller - Initial version
 *
 */
public class ContactItemTest {

    @Test
    public void testOpenCloseType() {
        ContactItem item = new ContactItem("test");
        item.setState(OpenClosedType.OPEN);
        assertEquals(OpenClosedType.OPEN, item.getState());

        item.setState(OpenClosedType.CLOSED);
        assertEquals(OpenClosedType.CLOSED, item.getState());
    }

    @Test
    public void testUndefType() {
        ContactItem item = new ContactItem("test");
        StateUtil.testUndefStates(item);
    }

    @Test
    public void testAcceptedStates() {
        ContactItem item = new ContactItem("test");
        StateUtil.testAcceptedStates(item);
    }
}
