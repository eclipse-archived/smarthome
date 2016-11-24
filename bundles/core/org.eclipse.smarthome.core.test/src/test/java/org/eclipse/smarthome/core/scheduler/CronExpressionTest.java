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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class CronExpressionTest {

    @Test(expected = ParseException.class)
    public void garbageString() throws ParseException {
        new CronExpression("blahblahblah");
    }

    @Test(expected = IllegalArgumentException.class)
    public void dayOfWeekAndMonth() throws ParseException {
        new CronExpression("* * * 1 * 1");
    }

    @Test
    public void getTimeAfterCheck() throws ParseException {

        Calendar cal = Calendar.getInstance();
        cal.set(2016, 0, 1, 0, 0, 0); // set to Jan 1st 2016, 00:00
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();

        // Fire at 10:15am on the third Friday of every month
        CronExpression expr = new CronExpression("0 15 10 ? * 6#3", startDate);

        Date nextDate = expr.getTimeAfter(startDate);

        cal.set(2016, 0, 15, 10, 15, 0);
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

        // Fire at 10:15am on every last friday of every month during the years 2016 to 2020
        CronExpression expr = new CronExpression("0 15 10 ? * 6L 2016-2020", startDate);

        Date nextDate = expr.getFinalFireTime();

        cal.set(2020, 11, 25, 10, 15, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date checkDate = cal.getTime();

        assertEquals(checkDate, nextDate);
    }

    @Test
    public void runForever() throws ParseException, InterruptedException {

        final CronExpression expression;
        expression = new CronExpression("* * * * * ?");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        Date nextDate = expression.getTimeAfter(Calendar.getInstance().getTime());
        int counter = 1;

        while (nextDate != null && counter <= 150) {
            System.out.println("value " + counter + " is " + sdf.format(nextDate));
            Calendar cal = Calendar.getInstance();
            cal.setTime(nextDate);
            cal.add(Calendar.MILLISECOND, 1);
            nextDate = cal.getTime();
            nextDate = expression.getTimeAfter(nextDate);
            counter++;
        }

    }

}