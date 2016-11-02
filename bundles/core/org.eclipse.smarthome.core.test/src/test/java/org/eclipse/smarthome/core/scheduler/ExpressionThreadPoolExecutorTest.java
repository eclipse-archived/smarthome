/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
