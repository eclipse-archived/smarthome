/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.items;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.StringListType;
import org.junit.Test;

public class CallItemTest {

    @Test
    public void testError() {

        StringListType callType1 = new StringListType("0699222222", "0179999998");
        CallItem callItem1 = new CallItem("testItem");

        callItem1.setState(callType1);
        assertEquals(callItem1.toString(),
                "testItem (Type=CallItem, State=0699222222,0179999998, Label=null, Category=null)");

        callType1 = new StringListType("0699222222,0179999998");
        callItem1.setState(callType1);
        assertEquals(callItem1.toString(),
                "testItem (Type=CallItem, State=0699222222,0179999998, Label=null, Category=null)");
    }
}