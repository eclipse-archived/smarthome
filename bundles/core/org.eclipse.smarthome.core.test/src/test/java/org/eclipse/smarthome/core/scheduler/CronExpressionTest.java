package org.eclipse.smarthome.core.scheduler;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.smarthome.core.scheduler.CronExpression;
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
        Date startDate = cal.getTime();

        // Fire at 10:15am on the third Friday of every month
        CronExpression expr = new CronExpression("0 15 10 ? * 6#3", startDate);

        Date nextDate = expr.getTimeAfter(startDate);

        cal.set(2016, 0, 15, 10, 15, 0);
        Date checkDate = cal.getTime();

        assertEquals(checkDate, nextDate);
    }

    @Test
    public void getFinalTimeCheck() throws ParseException {

        Calendar cal = Calendar.getInstance();
        cal.set(2016, 0, 1, 0, 0, 0); // set to Jan 1st 2016, 00:00
        Date startDate = cal.getTime();

        // Fire at 10:15am on every last friday of every month during the years 2016 to 2020
        CronExpression expr = new CronExpression("0 15 10 ? * 6L 2016-2020", startDate);

        Date nextDate = expr.getFinalFireTime();

        cal.set(2020, 11, 25, 10, 15, 0);
        Date checkDate = cal.getTime();

        assertEquals(checkDate, nextDate);
    }
}