package org.eclipse.smarthome.core.scheduler;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.smarthome.core.scheduler.DateExpression;
import org.junit.Test;

public class DateExpressionTest {

    @Test(expected = ParseException.class)
    public void garbageString() throws ParseException {
        new DateExpression("blahblahblah");
    }

    @Test
    public void getTimeAfterCheck() throws ParseException {

        Calendar cal = Calendar.getInstance();
        cal.set(2016, 0, 1, 0, 0, 0); // set to Jan 1st 2016, 00:00
        cal.setTimeZone(TimeZone.getTimeZone("GMT+00"));
        Date startDate = cal.getTime();

        DateExpression expr = new DateExpression("2016-01-31T00:00:00+00:00");
        expr.setStartDate(startDate);
        expr.setTimeZone(TimeZone.getTimeZone("GMT+00"));

        Date nextDate = expr.getTimeAfter(startDate);

        cal.set(2016, 0, 31, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.setTimeZone(TimeZone.getTimeZone("GMT+00"));
        Date checkDate = cal.getTime();

        assertEquals(checkDate, nextDate);
    }

    @Test
    public void getFinalTimeCheck() throws ParseException {

        Calendar cal = Calendar.getInstance();
        cal.set(2016, 0, 1, 0, 0, 0); // set to Jan 1st 2016, 00:00
        cal.setTimeZone(TimeZone.getTimeZone("GMT+00"));
        Date startDate = cal.getTime();

        DateExpression expr = new DateExpression("2016-01-31T00:00:00+00:00");
        expr.setStartDate(startDate);
        expr.setTimeZone(TimeZone.getTimeZone("GMT+00"));

        Date nextDate = expr.getFinalFireTime();

        cal.set(2016, 0, 31, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.setTimeZone(TimeZone.getTimeZone("GMT+00"));
        Date checkDate = cal.getTime();

        assertEquals(checkDate, nextDate);
    }
}