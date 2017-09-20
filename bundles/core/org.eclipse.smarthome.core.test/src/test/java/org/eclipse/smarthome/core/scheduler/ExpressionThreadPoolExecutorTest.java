/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.scheduler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager.ExpressionThreadPoolExecutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpressionThreadPoolExecutorTest {

    private ExpressionThreadPoolExecutor scheduler;
    protected boolean success;

    private final Logger logger = LoggerFactory.getLogger(ExpressionThreadPoolExecutorTest.class);

    protected boolean fakeOn = false;

    @Mock
    DateWrapper dw;

    @Before
    public void setup() {
        initMocks(this);
        scheduler = ExpressionThreadPoolManager.getExpressionScheduledPool("test");
    }

    @After
    public void after() {
        scheduler = null;
    }

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
    public void testScheduleAndRemoveRunstaticnable() throws ParseException, InterruptedException {

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

    private Date createDate(int offset) {
        if (fakeOn) {
            return DateUtils.addMilliseconds(new Date(), offset);
        } else {
            return new Date();
        }
    }

    @Test
    public void testTimeJumpBackwards() throws ParseException, InterruptedException {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                success = true;
            }
        };

        when(dw.getDate()).thenAnswer(new Answer<Date>() {
            @Override
            public Date answer(InvocationOnMock invocation) throws Throwable {
                return createDate(-2500);
            }
        });

        success = false;
        scheduler.setDateWrapper(dw);
        scheduler.setMonitorSleep(500);
        scheduler.setMonitorAllowedDrift(100);

        Date date = new Date();
        Date futureDate = DateUtils.addSeconds(date, 1);

        // adding future job
        Calendar futureCalender = DateUtils.toCalendar(futureDate);
        CronExpression expression = new CronExpression(futureCalender.get(Calendar.SECOND) + " * * * * ?");
        scheduler.schedule(runnable, expression);

        Thread.sleep(1500);
        assertTrue(success);

        // reset the time into the past
        fakeOn = true;
        success = false;

        Thread.sleep(2000);
        assertTrue(success);
    }

    @Test
    public void testTimeJumpForward() throws ParseException, InterruptedException {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                success = true;
            }
        };

        when(dw.getDate()).thenAnswer(new Answer<Date>() {
            @Override
            public Date answer(InvocationOnMock invocation) throws Throwable {
                return createDate(57000);
            }
        });

        success = false;
        scheduler.setDateWrapper(dw);
        scheduler.setMonitorSleep(500);
        scheduler.setMonitorAllowedDrift(100);

        Date date = new Date();
        Date futureDate = DateUtils.addSeconds(date, 1);

        // adding future job
        Calendar futureCalender = DateUtils.toCalendar(futureDate);
        CronExpression expression = new CronExpression(futureCalender.get(Calendar.SECOND) + " * * * * ?");
        scheduler.schedule(runnable, expression);

        Thread.sleep(1500);
        assertTrue(success);

        // reset the time into the future
        fakeOn = true;
        success = false;

        Thread.sleep(2500);
        assertTrue(success);
    }

}
