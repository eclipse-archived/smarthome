/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Gaël L'hopital
 * @author Kai Kreuzer - added tests for valueOf and toFullString
 */
public class StringListTypeTest {
    @Test
    public void testEquals() {
        final int DEST_IDX = 0;
        final int ORIG_IDX = 1;

        StringListType call1 = new StringListType("0179999998", "0699222222");
        StringListType call2 = new StringListType("0699222222,0179999998");

        assertEquals(call1.getValue(ORIG_IDX), call2.getValue(DEST_IDX));
        assertEquals(call2.toString(), "0699222222,0179999998");

        String serialized = new String("value1,value2,value=with=foo,value\\,with\\,foo,,\\,\\,foo");
        StringListType call4 = new StringListType(serialized);
        assertTrue(call4.getValue(1).toString().equals("value2"));
        assertTrue(call4.getValue(4).toString().isEmpty());
        assertTrue(call4.getValue(2).toString().equals("value=with=foo"));
        assertTrue(call4.getValue(3).toString().equals("value,with,foo"));
        assertTrue(call4.getValue(5).toString().equals(",,foo"));
        assertTrue(call4.toString().equals(serialized));
    }

    @Test
    public void testError() {
        StringListType gct = new StringListType("foo=bar", "electric", "chair");
        try {

            // Index is between 0 and number of elements -1
            @SuppressWarnings("unused")
            String value = gct.getValue(-1);
            fail();
        } catch (Exception e) {
            try {

                @SuppressWarnings("unused")
                String value = gct.getValue(3);
                fail();
            } catch (Exception e2) {
                // That's what we expect.
            }

        }

    }

    @Test
    public void testToFullString() {
        StringListType abc = new StringListType("a", "b", "c");
        String fullString = abc.toFullString();
        assertEquals("a,b,c", fullString);
    }

    @Test
    public void testValueOf() {
        StringListType abc = StringListType.valueOf("a,b,c");
        assertEquals("a", abc.getValue(0));
        assertEquals("b", abc.getValue(1));
        assertEquals("c", abc.getValue(2));

        StringListType abC = StringListType.valueOf("a\\,b,c");
        assertEquals("a,b", abC.getValue(0));
        assertEquals("c", abC.getValue(1));
    }
}