/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.model.script.actions;

import static org.hamcrest.CoreMatchers.*;
import static org.joda.time.Instant.now;
import static org.junit.Assert.assertThat;

import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.junit.Before;
import org.junit.Test;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Tests for {@link ScriptExecution}
 *
 * @author Jon Evans - initial contribution
 *
 */
public class ScriptExecutionTest {

    /**
     * Make sure that the Quartz scheduler is running
     *
     * @throws SchedulerException
     */
    @Before
    public void startScheduler() throws SchedulerException {
        StdSchedulerFactory.getDefaultScheduler().start();
    }

    /**
     * Test that a Timer can be rescheduled from within its closure
     *
     * @throws InterruptedException
     */
    @Test
    public void testRescheduleTimerDuringExecution() throws InterruptedException {
        MockClosure0 closure = new MockClosure0(1);

        // Create a Timer to run after 10ms
        Timer t = ScriptExecution.createTimer(now().plus(10), closure);
        closure.setTimer(t);

        assertThat(t.isRunning(), is(equalTo(false)));
        assertThat(t.hasTerminated(), is(equalTo(false)));
        assertThat(closure.getApplyCount(), is(equalTo(0)));

        // Wait enough time for the Timer to run twice
        Thread.sleep(30);

        // Check that the Timer ran
        assertThat(closure.getApplyCount(), is(equalTo(2)));
        assertThat(t.isRunning(), is(equalTo(false)));
        assertThat(t.hasTerminated(), is(equalTo(true)));
    }

    /**
     * Tests that a Timer can be rescheduled after it has terminated
     *
     * @throws InterruptedException
     */
    @Test
    public void testRescheduleTimerAfterExecution() throws InterruptedException {
        MockClosure0 closure = new MockClosure0();

        // Create a Timer to run after 10ms
        Timer t = ScriptExecution.createTimer(now().plus(10), closure);
        closure.setTimer(t);

        assertThat(t.isRunning(), is(equalTo(false)));
        assertThat(t.hasTerminated(), is(equalTo(false)));
        assertThat(closure.getApplyCount(), is(equalTo(0)));

        // Wait enough time for the Timer to run
        Thread.sleep(30);

        // Check that the Timer ran
        assertThat(closure.getApplyCount(), is(equalTo(1)));
        assertThat(t.isRunning(), is(equalTo(false)));
        assertThat(t.hasTerminated(), is(equalTo(true)));

        // Now try to reschedule the Timer to run again after 10ms
        t.reschedule(now().plus(10));
        assertThat(t.hasTerminated(), is(equalTo(false)));

        // Wait enough time for the Timer to run
        Thread.sleep(30);

        // Check that the Timer ran again
        assertThat(closure.getApplyCount(), is(equalTo(2)));
        assertThat(t.isRunning(), is(equalTo(false)));
        assertThat(t.hasTerminated(), is(equalTo(true)));
    }

    /**
     * A mock Closure class that we can use to verify how many times apply() has been called,
     * and optionally schedule timer restarts from within the apply() method.
     *
     */
    class MockClosure0 implements Procedure0 {
        private int rescheduleCount;
        private int applyCount;
        private Timer timer;

        public MockClosure0() {
            this(0);
        }

        public MockClosure0(int rescheduleCount) {
            this.rescheduleCount = rescheduleCount;
        }

        @Override
        public void apply() {
            this.applyCount++;
            // Might as well also test Timer#isRunning()
            assertThat(timer.isRunning(), is(equalTo(true)));

            if (this.rescheduleCount > 0) {
                this.rescheduleCount--;
                this.timer.reschedule(now().plus(10));
            }
        }

        public void setTimer(Timer timer) {
            this.timer = timer;
        }

        public int getApplyCount() {
            return applyCount;
        }
    }
}
