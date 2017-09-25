/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.test.java;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Testing the test. Test suite for the JavaTest base test class.
 *
 * @author Henning Treu - initial contribution.
 *
 */
public class JavaTestTest {

    private JavaTest javaTest;

    @Before
    public void setup() {
        javaTest = new JavaTest();
    }

    @Test
    public void waitForAssertShouldRunAfterLastCall_whenAssertionSucceeds() {
        Runnable allwaysSucceedAssertion = mock(Runnable.class);
        doAnswer(invocation -> {
            assertTrue(true);
            return null;
        }).when(allwaysSucceedAssertion).run();

        Runnable afterLastCall = mock(Runnable.class);

        javaTest.waitForAssert(allwaysSucceedAssertion, null, afterLastCall, 10000, 50);

        verify(afterLastCall, times(1)).run();
    }

    @Test
    public void waitForAssertShouldRunAfterLastCall_whenAssertionFails() {
        Runnable allwaysFailAssertion = mock(Runnable.class);
        doAnswer(invocation -> {
            throw new NullPointerException();
        }).when(allwaysFailAssertion).run();

        Runnable afterLastCall = mock(Runnable.class);

        try {
            javaTest.waitForAssert(allwaysFailAssertion, null, afterLastCall, 10, 5);
            fail("NPE from mock is expected during last execution.");
        } catch (NullPointerException e) {
            // expected
        }

        verify(afterLastCall, times(1)).run();
    }

}
