/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.scheduler2;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

import junit.framework.TestCase;

/**
 *
 * @author Peter Kriens - initial contribution and API
 * @author Simon Kaufmann - adapted to Java 8
 *
 */
public class CronAdjusterTest extends TestCase {

    public void testSimple() {

        assertTrue(new CronAdjuster("@reboot").isReboot());
        assertFalse(new CronAdjuster("@daily").isReboot());

        assertCron("2015-01-01T00:00:00", "@reboot", "2200-01-01T00:00");
        assertCron("2015-01-01T00:00:00", "@hourly", "2015-01-01T00:00:04", "2015-01-01T01:00:04",
                "2015-01-01T02:00:04", "2015-01-01T03:00:04");
        assertCron("2015-01-01T00:00:00", "@daily", "2015-01-01T00:00:03", "2015-01-02T00:00:03", "2015-01-03T00:00:03",
                "2015-01-04T00:00:03");
        assertCron("2015-01-01T00:00:00", "@weekly", "2015-01-05T00:00:02", "2015-01-12T00:00:02",
                "2015-01-19T00:00:02", "2015-01-26T00:00:02");
        assertCron("2015-01-01T00:00:00", "@monthly", "2015-01-01T00:00:01", "2015-02-01T00:00:01",
                "2015-03-01T00:00:01", "2015-04-01T00:00:01");
        assertCron("2015-01-01T00:00:00", "@annually", "2016-01-01T00:00", "2017-01-01T00:00", "2018-01-01T00:00");
        assertCron("2015-01-01T00:00:00", "@yearly", "2016-01-01T00:00", "2017-01-01T00:00", "2018-01-01T00:00");

        // Last weekday
        assertCron("2015-01-01T00:00:00", "0 15 10 LW * ?", "2015-01-30T10:15", "2015-02-27T10:15", "2015-03-31T10:15",
                "2015-04-30T10:15", "2015-05-29T10:15");

        // Fire at 10.15 every 10 days every month, starting on the first day of the month.
        assertCron("2015-01-01T00:00:00", "0 15 10 1/10 * ?", "2015-01-01T10:15", "2015-01-11T10:15",
                "2015-01-21T10:15", "2015-01-31T10:15", "2015-02-01T10:15");

        // Fire at 10:15am on the second Friday of every month
        assertCron("2015-01-01T00:00:00", "0 15 10 ? * FRI#2", "2015-01-09T10:15", "2015-02-13T10:15",
                "2015-03-13T10:15");

        // Fire at 10:15am on the last Friday of every month
        assertCron("2015-01-01T00:00:00", "0 15 10 ? * 5L", "2015-01-30T10:15", "2015-02-27T10:15", "2015-03-27T10:15");

        // Fire at 10:15am on the last day of every month
        assertCron("2015-01-01T00:00:00", "0 15 10 L * ?", "2015-01-31T10:15", "2015-02-28T10:15", "2015-03-31T10:15");

        // Fire at 10:15am on the 15th day of every month
        assertCron("2015-01-01T00:00:00", "0 15 10 15 * ?", "2015-01-15T10:15", "2015-02-15T10:15", "2015-03-15T10:15");

        // Fire at 10:15am every Monday, Tuesday, Wednesday
        assertCron("2015-01-01T00:00:00", "0 15 10 ? * MON-WED", "2015-01-05T10:15", "2015-01-06T10:15",
                "2015-01-07T10:15", "2015-01-12T10:15");

        // Fire at 2:10pm and at 2:44pm every Wednesday in the month of January.
        assertCron("2015-01-01T00:00:00", "0 10,44 14 ? 1 WED", "2015-01-07T14:10", "2015-01-07T14:44",
                "2015-01-14T14:10");

        // Fire every minute starting at 2pm and ending at 2:02pm, every day
        assertCron("2015-01-01T00:00:00", "0 0-2 14 * * ?", "2015-01-01T14:00", "2015-01-01T14:01", "2015-01-01T14:02",
                "2015-01-02T14:00", "2015-01-02T14:01", "2015-01-02T14:02");
        assertCron("2015-01-01T00:00:00", "0 0/15 14,18 * * ?", "2015-01-01T14:00", "2015-01-01T14:15",
                "2015-01-01T14:30", "2015-01-01T14:45", "2015-01-01T18:00", "2015-01-01T18:15");

        assertCron("2015-01-01T00:00:00", "0 0 12 * * ?", "2015-01-01T12:00");
        assertCron("2015-01-01T00:00:00", "0 15 10 ? * *", "2015-01-01T10:15", "2015-01-02T10:15");
        assertCron("2015-01-01T00:00:00", "0 15 10 * * ?", "2015-01-01T10:15", "2015-01-02T10:15");
        assertCron("2015-01-01T00:00:00", "0 15 10 * * ? *", "2015-01-01T10:15", "2015-01-02T10:15");
        assertCron("2015-01-01T00:00:00", "0 15 10 * * ? 2015", "2015-01-01T10:15", "2015-01-02T10:15");
        assertCron("2015-01-01T00:00:00", "0 * 14 * * ?", "2015-01-01T14:00", "2015-01-01T14:01", "2015-01-01T14:02");
        assertCron("2015-01-01T00:00:00", "0 0/5 14 * * ?", "2015-01-01T14:00", "2015-01-01T14:05", "2015-01-01T14:10");
        assertCron("2015-01-01T00:00:00", "0 0/30 14,18 * * ?", "2015-01-01T14:00", "2015-01-01T14:30",
                "2015-01-01T18:00");
        assertCron("2015-01-01T00:00:00", "0 0-2 14 * * ?", "2015-01-01T14:00", "2015-01-01T14:01", "2015-01-01T14:02",
                "2015-01-02T14:00");

        assertCron("2015-01-01T00:00:00", "0 0 0 ? * SAT", "2015-01-03T00:00");
        assertCron("2000-01-01T00:00:00", "10-14/2 * * * * *", "2000-01-01T00:00:10", "2000-01-01T00:00:12",
                "2000-01-01T00:00:14", "2000-01-01T00:01:10");
        assertCron("2000-01-01T00:00:00", "1/15,3/15 * * * * *", "2000-01-01T00:00:01", "2000-01-01T00:00:03",
                "2000-01-01T00:00:16", "2000-01-01T00:00:18");

        assertCron("2000-01-01T00:00:00", "0 0 0 1 FEB,APR,JUN ?", "2000-02-01T00:00", "2000-04-01T00:00",
                "2000-06-01T00:00");
        assertCron("2000-01-01T00:00:00", "0 0 0 1 FEB ?", "2000-02-01T00:00", "2001-02-01T00:00", "2002-02-01T00:00");
        assertCron("2000-01-01T00:00:00", "0 0 0 * FEB ?", "2000-02-01T00:00", "2000-02-02T00:00", "2000-02-03T00:00");

        assertCron("2000-01-01T00:00:00", "15 2 * * * * 2001", "2001-01-01T00:02:15");
        assertCron("2000-01-01T00:00:00", "*/5 * * * * *", "2000-01-01T00:00:05", "2000-01-01T00:00:10",
                "2000-01-01T00:00:15");
        assertCron("2000-01-01T00:00:00", "3/5 * * * * *", "2000-01-01T00:00:03", "2000-01-01T00:00:08",
                "2000-01-01T00:00:13", "2000-01-01T00:00:18");
        assertCron("2000-01-01T00:00:00", "3-13/5 * * * * *", "2000-01-01T00:00:03", "2000-01-01T00:00:08",
                "2000-01-01T00:00:13", "2000-01-01T00:01:03");
    }

    void assertCron(String in, String cron, String... outs) {

        CronAdjuster c = new CronAdjuster(cron);
        Temporal ldt = LocalDateTime.parse(in);

        for (String out : outs) {
            Temporal t = c.adjustInto(ldt);
            assertEquals(out, t.toString());

            ldt = t.plus(1, ChronoUnit.SECONDS);
        }

    }
}
