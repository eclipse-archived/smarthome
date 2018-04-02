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

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CronExpressionTest {

    private final Logger logger = LoggerFactory.getLogger(CronExpressionTest.class);

    @Test(expected = ParseException.class)
    public void garbageString() throws ParseException {
        new CronExpression("blahblahblah");
    }

    @Test(expected = IllegalArgumentException.class)
    public void dayOfWeekAndMonth() throws ParseException {
        new CronExpression("* * * ? * ?");
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
    public void findNext() throws ParseException {
        final List<String> expressions = Arrays.asList(new String[] { //
                "* * * * * ?", //
                "* * * ? * *", //
                "0 * * ? * *", //
                "0 0 * ? * *", //
                "0 0 0 ? * *", //
                "0 15 07 * * ?", //
                "0 15 07 ? * MON,TUE,WED,THU,FRI,SAT,SUN" //
        });

        for (final String expr : expressions) {
            Calendar cal = Calendar.getInstance();
            findNextWorker(expr, cal);
        }
    }

    @Test
    public void findNextSpecial() throws ParseException {
        // 2018-03-22 10:12:22.514 AM, Europe/Berlin
        // DST starts on 2018-03-25 at 02:00 (2 am) local time, when clocks are set ahead 1 hour to 03:00 (3 am).
        findNextWorker("0 0 * ? * *", getCalendarInstance("Europe/Berlin", 1521709942514L));
    }

    private void findNextWorker(final String expr, final Calendar cal) throws ParseException {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        final int TRIES = 150;
        boolean trace = false;

        logger.info("Test cron expression: {}", expr);
        final CronExpression cronExpression;
        try {
            cronExpression = new CronExpression(expr);
        } catch (final IllegalArgumentException | ParseException ex) {
            logger.error("Error creating a new ConExpression", ex);
            throw ex;
        }

        Date curDate = cal.getTime();

        for (int i = 0; i < TRIES; ++i) {
            if (trace) {
                logger.info("Try to get time after: {}", sdf.format(curDate));
            }
            final Date nextDate = cronExpression.getTimeAfter(curDate);
            if (nextDate == null) {
                final String msg = String.format("Cannot find a time after '%s' for expression '%s'",
                        sdf.format(curDate), cronExpression.getExpression());
                logger.error("{}", msg);
                Assert.fail(msg);
            } else {
                if (trace) {
                    logger.info("Got: {}", sdf.format(nextDate));
                }
            }

            cal.setTime(nextDate);
            // Add some offset
            cal.add(Calendar.MINUTE, 1);
            cal.add(Calendar.SECOND, 1);
            curDate = cal.getTime();
        }
    }

    private static Calendar getCalendarInstance(final String timeZone, final long ms) {
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        cal.setTimeInMillis(ms);
        return cal;
    }

}
