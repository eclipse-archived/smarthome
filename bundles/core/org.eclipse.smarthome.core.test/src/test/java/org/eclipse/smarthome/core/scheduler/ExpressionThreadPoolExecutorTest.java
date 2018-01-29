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
package org.eclipse.smarthome.core.scheduler;

import static org.junit.Assert.*;

import java.text.ParseException;

import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager.ExpressionThreadPoolExecutor;
import org.junit.Test;

public class ExpressionThreadPoolExecutorTest {

    private ExpressionThreadPoolExecutor scheduler = ExpressionThreadPoolManager.getExpressionScheduledPool("test");
    protected boolean success;

    @Test
    public void testScheduleAndRemove() throws ParseException, InterruptedException {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                success = true;
            }
        };

        success = false;
        CronExpression expression = new CronExpression("0/1 * * * * ?");
        scheduler.schedule(runnable, expression);
        Thread.sleep(1500);
        assertTrue(success);

        boolean removed = scheduler.remove(expression);

        assertTrue(removed);

        success = false;
        Thread.sleep(1500);
        assertFalse(success);

        assertEquals(0, scheduler.getQueue().size());
    }

    @Test
    public void testScheduleAndRemoveRunnable() throws ParseException, InterruptedException {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                success = true;
            }
        };

        success = false;
        CronExpression expression = new CronExpression("0/1 * * * * ?");
        scheduler.schedule(runnable, expression);
        Thread.sleep(1500);
        assertTrue(success);

        boolean removed = scheduler.remove(runnable);

        assertTrue(removed);

        success = false;
        Thread.sleep(1500);
        assertFalse(success);

        assertEquals(0, scheduler.getQueue().size());
    }

}
