/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.dmx.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.smarthome.binding.dmx.internal.action.ActionState;
import org.eclipse.smarthome.binding.dmx.internal.action.FadeAction;
import org.eclipse.smarthome.binding.dmx.internal.multiverse.DmxChannel;
import org.junit.Test;

/**
 * Tests cases FadeAction
 *
 * @author Jan N. Klug - Initial contribution
 */
public class FadeActionTest {

    private static final int testValue = 200;
    private static final int testFadeTime = 1000;
    private static final int testHoldTime = 1000;

    @Test
    public void checkWithFadingWithoutHold() {
        FadeAction fadeAction = new FadeAction(testFadeTime, testValue, 0);
        DmxChannel testChannel = new DmxChannel(0, 1);
        testChannel.setValue(0);

        long startTime = System.currentTimeMillis();

        assertThat(fadeAction.getState(), is(ActionState.WAITING));
        assertThat(fadeAction.getNewValue(testChannel, startTime), is(0));
        assertThat(fadeAction.getState(), is(ActionState.RUNNING));
        assertThat(fadeAction.getNewValue(testChannel, startTime + testFadeTime / 2), is(256 * testValue / 2));
        assertThat(fadeAction.getNewValue(testChannel, startTime + 1000), is(256 * testValue));
        assertThat(fadeAction.getState(), is(ActionState.COMPLETED));

        fadeAction.reset();
        assertThat(fadeAction.getState(), is(ActionState.WAITING));
    }

    @Test
    public void checkWithFadingWithHold() {
        FadeAction fadeAction = new FadeAction(testFadeTime, testValue, testHoldTime);
        DmxChannel testChannel = new DmxChannel(0, 1);
        testChannel.setValue(0);

        long startTime = System.currentTimeMillis();

        assertThat(fadeAction.getState(), is(ActionState.WAITING));
        assertThat(fadeAction.getNewValue(testChannel, startTime), is(0));
        assertThat(fadeAction.getState(), is(ActionState.RUNNING));
        assertThat(fadeAction.getNewValue(testChannel, startTime + testFadeTime / 2), is(256 * testValue / 2));
        assertThat(fadeAction.getNewValue(testChannel, startTime + testFadeTime), is(256 * testValue));
        assertThat(fadeAction.getState(), is(ActionState.RUNNING));
        assertThat(fadeAction.getNewValue(testChannel, startTime + testFadeTime + testHoldTime / 2),
                is(256 * testValue));
        assertThat(fadeAction.getState(), is(ActionState.RUNNING));
        assertThat(fadeAction.getNewValue(testChannel, startTime + testFadeTime + testHoldTime), is(256 * testValue));
        assertThat(fadeAction.getState(), is(ActionState.COMPLETED));

        fadeAction.reset();
        assertThat(fadeAction.getState(), is(ActionState.WAITING));
    }

    @Test
    public void checkWithFadingWithInfiniteHold() {
        FadeAction fadeAction = new FadeAction(testFadeTime, testValue, -1);
        DmxChannel testChannel = new DmxChannel(0, 1);
        testChannel.setValue(0);

        long startTime = System.currentTimeMillis();

        assertThat(fadeAction.getState(), is(ActionState.WAITING));
        assertThat(fadeAction.getNewValue(testChannel, startTime), is(0));
        assertThat(fadeAction.getState(), is(ActionState.RUNNING));
        assertThat(fadeAction.getNewValue(testChannel, startTime + testFadeTime / 2), is(256 * testValue / 2));
        assertThat(fadeAction.getNewValue(testChannel, startTime + testFadeTime), is(256 * testValue));
        assertThat(fadeAction.getState(), is(ActionState.COMPLETEDFINAL));

        fadeAction.reset();
        assertThat(fadeAction.getState(), is(ActionState.WAITING));
    }

    @Test
    public void checkWithoutFadingWithHold() {
        FadeAction fadeAction = new FadeAction(0, testValue, testHoldTime);
        DmxChannel testChannel = new DmxChannel(0, 1);
        testChannel.setValue(0);

        long startTime = System.currentTimeMillis();

        assertThat(fadeAction.getState(), is(ActionState.WAITING));
        assertThat(fadeAction.getNewValue(testChannel, startTime), is(256 * testValue));
        assertThat(fadeAction.getState(), is(ActionState.RUNNING));
        assertThat(fadeAction.getNewValue(testChannel, startTime + testHoldTime / 2), is(256 * testValue));
        assertThat(fadeAction.getState(), is(ActionState.RUNNING));
        assertThat(fadeAction.getNewValue(testChannel, startTime + testHoldTime), is(256 * testValue));
        assertThat(fadeAction.getState(), is(ActionState.COMPLETED));

        fadeAction.reset();
        assertThat(fadeAction.getState(), is(ActionState.WAITING));
    }

    @Test
    public void checkWithoutFadingWithoutHold() {
        FadeAction fadeAction = new FadeAction(0, testValue, 0);
        DmxChannel testChannel = new DmxChannel(0, 1);
        testChannel.setValue(0);

        long startTime = System.currentTimeMillis();

        assertThat(fadeAction.getState(), is(ActionState.WAITING));
        assertThat(fadeAction.getNewValue(testChannel, startTime), is(256 * testValue));
        assertThat(fadeAction.getState(), is(ActionState.COMPLETED));

        fadeAction.reset();
        assertThat(fadeAction.getState(), is(ActionState.WAITING));
    }

    @Test
    public void checkWithoutFadingWithInfiniteHold() {
        FadeAction fadeAction = new FadeAction(0, testValue, -1);
        DmxChannel testChannel = new DmxChannel(0, 1);
        testChannel.setValue(0);

        long startTime = System.currentTimeMillis();

        assertThat(fadeAction.getState(), is(ActionState.WAITING));
        assertThat(fadeAction.getNewValue(testChannel, startTime), is(256 * testValue));
        assertThat(fadeAction.getState(), is(ActionState.COMPLETEDFINAL));

        fadeAction.reset();
        assertThat(fadeAction.getState(), is(ActionState.WAITING));
    }

}
