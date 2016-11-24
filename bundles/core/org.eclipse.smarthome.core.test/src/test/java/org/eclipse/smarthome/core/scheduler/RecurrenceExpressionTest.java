/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.scheduler;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class RecurrenceExpressionTest {

    @Test(expected = IllegalArgumentException.class)
    public void garbageString() throws ParseException {
        new RecurrenceExpression("blahblahblah");
    }

    @Test
    public void getTimeAfterCheck() throws ParseException {

        Calendar cal = Calendar.getInstance();
        cal.set(2016, 0, 1, 0, 0, 0); // set to Jan 1st 2016, 00:00
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();

        // This rule describes an event that takes place on every weekday (BYDAY) for the next 15 weekdays (COUNT).
        RecurrenceExpression expr = new RecurrenceExpression("FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR;COUNT=15", startDate);

        Date nextDate = expr.getTimeAfter(startDate);

        cal.set(2016, 0, 4, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date checkDate = cal.getTime();

        assertEquals(checkDate, nextDate);
    }

    @Test
    public void IntervalCheck() throws ParseException {

        Calendar cal = Calendar.getInstance();
        cal.set(2016, 0, 1, 0, 0, 0); // set to Jan 1st 2016, 00:00
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();

        // US election day. Every fourth year (INTERVAL) on the first Tuesday (BYDAY) after a Monday (BYMONTHDAY ensures
        // that) in November (BYMONTH).
        RecurrenceExpression expr = new RecurrenceExpression(
                "FREQ=YEARLY;INTERVAL=4;BYMONTH=11;BYDAY=TU;BYMONTHDAY=2,3,4,5,6,7,8", startDate);

        Date nextDate = expr.getTimeAfter(startDate);
        nextDate = expr.getTimeAfter(nextDate);

        cal.set(2020, 10, 3, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date checkDate = cal.getTime();

        assertEquals(checkDate, nextDate);
    }

    @Test
    public void getFinalTimeCheck() throws ParseException {

        Calendar cal = Calendar.getInstance();
        cal.set(2016, 0, 1, 0, 0, 0); // set to Jan 1st 2016, 00:00
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();

        // This rule describes an event that takes place on every weekday (BYDAY) for the next 15 weekdays (COUNT).
        RecurrenceExpression expr = new RecurrenceExpression("FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR;COUNT=15", startDate);

        Date nextDate = expr.getFinalFireTime();

        cal.set(2016, 0, 21, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date checkDate = cal.getTime();

        assertEquals(checkDate, nextDate);
    }
}