/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * <code>CronExpression</code> is an implementation of {@link Expression} that provides a parser and evaluator for for
 * unix-like cron expressions that are compatible wit the Quartz (https://quartz-scheduler.org/) framework. Cron
 * expressions provide the ability to specify complex time combinations such as
 * &quot;At 8:00am every Monday through Friday&quot; or &quot;At 1:30am every
 * last Friday of the month&quot;.
 * <P>
 * Cron expressions are comprised of 6 required fields and one optional field
 * separated by white space. The fields respectively are described as follows:
 *
 * <table cellspacing="8">
 * <tr>
 * <th align="left">Field Name</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Values</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Special Characters</th>
 * </tr>
 * <tr>
 * <td align="left"><code>Seconds</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Minutes</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Hours</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-23</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-month</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>1-31</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * ? / L W</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Month</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-11 or JAN-DEC</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-Week</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>1-7 or SUN-SAT</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * ? / L #</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Year (Optional)</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>empty, 1970-2199</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * </table>
 * <P>
 * The '*' character is used to specify all values. For example, &quot;*&quot;
 * in the minute field means &quot;every minute&quot;.
 * <P>
 * The '?' character is allowed for the day-of-month and day-of-week fields. It
 * is used to specify 'no specific value'. This is useful when you need to
 * specify something in one of the two fields, but not the other.
 * <P>
 * The '-' character is used to specify ranges For example &quot;10-12&quot; in
 * the hour field means &quot;the hours 10, 11 and 12&quot;.
 * <P>
 * The ',' character is used to specify additional values. For example
 * &quot;MON,WED,FRI&quot; in the day-of-week field means &quot;the days Monday,
 * Wednesday, and Friday&quot;.
 * <P>
 * The '/' character is used to specify increments. For example &quot;0/15&quot;
 * in the seconds field means &quot;the seconds 0, 15, 30, and 45&quot;. And
 * &quot;5/15&quot; in the seconds field means &quot;the seconds 5, 20, 35, and
 * 50&quot;. Specifying '*' before the '/' is equivalent to specifying 0 is
 * the value to start with. Essentially, for each field in the expression, there
 * is a set of numbers that can be turned on or off. For seconds and minutes,
 * the numbers range from 0 to 59. For hours 0 to 23, for days of the month 0 to
 * 31, and for months 0 to 11 (JAN to DEC). The &quot;/&quot; character simply helps you turn
 * on every &quot;nth&quot; value in the given set. Thus &quot;7/6&quot; in the
 * month field only turns on month &quot;7&quot;, it does NOT mean every 6th
 * month, please note that subtlety.
 * <P>
 * The 'L' character is allowed for the day-of-month and day-of-week fields.
 * This character is short-hand for &quot;last&quot;, but it has different
 * meaning in each of the two fields. For example, the value &quot;L&quot; in
 * the day-of-month field means &quot;the last day of the month&quot; - day 31
 * for January, day 28 for February on non-leap parsedExpression.get(ExpressionPart.YEARS). If used in the
 * day-of-week field by itself, it simply means &quot;7&quot; or
 * &quot;SAT&quot;. But if used in the day-of-week field after another value, it
 * means &quot;the last xxx day of the month&quot; - for example &quot;6L&quot;
 * means &quot;the last friday of the month&quot;. You can also specify an offset
 * from the last day of the month, such as "L-3" which would mean the third-to-last
 * day of the calendar month. <i>When using the 'L' option, it is important not to
 * specify lists, or ranges of values, as you'll get confusing/unexpected results.</i>
 * <P>
 * The 'W' character is allowed for the day-of-month field. This character
 * is used to specify the weekday (Monday-Friday) nearest the given day. As an
 * example, if you were to specify &quot;15W&quot; as the value for the
 * day-of-month field, the meaning is: &quot;the nearest weekday to the 15th of
 * the month&quot;. So if the 15th is a Saturday, the trigger will fire on
 * Friday the 14th. If the 15th is a Sunday, the trigger will fire on Monday the
 * 16th. If the 15th is a Tuesday, then it will fire on Tuesday the 15th.
 * However if you specify &quot;1W&quot; as the value for day-of-month, and the
 * 1st is a Saturday, the trigger will fire on Monday the 3rd, as it will not
 * 'jump' over the boundary of a month's days. The 'W' character can only be
 * specified when the day-of-month is a single day, not a range or list of days.
 * <P>
 * The 'L' and 'W' characters can also be combined for the day-of-month
 * expression to yield 'LW', which translates to &quot;last weekday of the
 * month&quot;.
 * <P>
 * The '#' character is allowed for the day-of-week field. This character is
 * used to specify &quot;the nth&quot; XXX day of the month. For example, the
 * value of &quot;6#3&quot; in the day-of-week field means the third Friday of
 * the month (day 6 = Friday and &quot;#3&quot; = the 3rd one in the month).
 * Other examples: &quot;2#1&quot; = the first Monday of the month and
 * &quot;4#5&quot; = the fifth Wednesday of the month. Note that if you specify
 * &quot;#5&quot; and there is not 5 of the given day-of-week in the month, then
 * no firing will occur that month. If the '#' character is used, there can
 * only be one expression in the day-of-week field (&quot;3#1,6#3&quot; is
 * not valid, since there are two expressions).
 * <P>
 * <!--The 'C' character is allowed for the day-of-month and day-of-week fields.
 * This character is short-hand for "calendar". This means values are
 * calculated against the associated calendar, if any. If no calendar is
 * associated, then it is equivalent to having an all-inclusive calendar. A
 * value of "5C" in the day-of-month field means "the first day included by the
 * calendar on or after the 5th". A value of "1C" in the day-of-week field
 * means "the first day included by the calendar on or after Sunday".-->
 * <P>
 * The legal characters and the names of months and days of the week are not
 * case sensitive.
 *
 * <p>
 * <b>NOTES:</b>
 * <ul>
 * <li>Support for specifying both a day-of-week and a day-of-month value is
 * not complete (you'll need to use the '?' character in one of these fields).
 * </li>
 * <li>Overflowing ranges is supported - that is, having a larger number on
 * the left hand side than the right. You might do 22-2 to catch 10 o'clock
 * at night until 2 o'clock in the morning, or you might have NOV-FEB. It is
 * very important to note that overuse of overflowing ranges creates ranges
 * that don't make sense and no effort has been made to determine which
 * interpretation CronExpression chooses. An example would be
 * "0 0 14-6 ? * FRI-MON".</li>
 * </ul>
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 *
 */
public final class CronExpression extends AbstractExpression {

    protected static final int MIN_DAYOFWEEK = 0;
    protected static final int MAX_DAYOFWEEK = 6;
    protected static final int MIN_YEAR = 1970;
    protected static final int MAX_YEAR = Calendar.getInstance().get(Calendar.YEAR) + 100;

    protected HashMap<ExpressionPart, FlaggedBoundedIntegerList> parsedExpression;

    protected boolean lastdayOfWeek = false;
    protected int nthdayOfWeek = 0;
    protected boolean lastdayOfMonth = false;
    protected boolean nearestWeekday = false;
    protected int lastdayOffset = 0;
    protected boolean expressionParsed = false;

    enum Month {
        january("JAN", Calendar.JANUARY, 31),
        february("FEB", Calendar.FEBRUARY, 28) {
            @Override
            public int getNumberOfDays(int year) {
                if (((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0))) {
                    return 29;
                } else {
                    return 28;
                }
            };
        },
        march("MAR", Calendar.MARCH, 31),
        april("APR", Calendar.APRIL, 30),
        may("MAY", Calendar.MAY, 31),
        june("JUN", Calendar.JUNE, 30),
        july("JUL", Calendar.JULY, 31),
        august("AUG", Calendar.AUGUST, 31),
        september("SEP", Calendar.SEPTEMBER, 30),
        october("OCT", Calendar.OCTOBER, 31),
        november("NOV", Calendar.NOVEMBER, 30),
        december("DEC", Calendar.DECEMBER, 31);

        private final String identifier;
        private final int calendarMonth;
        private final int numberOfDays;

        public static Month getMonth(final int calendar) {
            switch (calendar) {
                case Calendar.JANUARY:
                    return january;
                case Calendar.FEBRUARY:
                    return february;
                case Calendar.MARCH:
                    return march;
                case Calendar.APRIL:
                    return april;
                case Calendar.MAY:
                    return may;
                case Calendar.JUNE:
                    return june;
                case Calendar.JULY:
                    return july;
                case Calendar.AUGUST:
                    return august;
                case Calendar.SEPTEMBER:
                    return september;
                case Calendar.OCTOBER:
                    return october;
                case Calendar.NOVEMBER:
                    return november;
                case Calendar.DECEMBER:
                    return december;
                default:
                    throw new IllegalArgumentException("invalid calendar value " + calendar);
            }
        }

        public static Month getMonth(final String id) {
            for (Month aMonth : Month.values()) {
                if (aMonth.toString().equals(id)) {
                    return aMonth;
                }
            }
            throw new IllegalArgumentException("invalid calendar value " + id);
        }

        private Month(final String code, final int month, final int numberOfDays) {
            this.identifier = code;
            this.calendarMonth = month;
            this.numberOfDays = numberOfDays;
        }

        public int getNumberOfDays(int year) {
            return numberOfDays;
        }

        public int getCalendarMonth() {
            return calendarMonth;
        }

        @Override
        public String toString() {
            return identifier;
        }
    };

    enum WeekDay {
        sunday("SUN", Calendar.SUNDAY),
        monday("MON", Calendar.MONDAY),
        tuesday("TUE", Calendar.TUESDAY),
        wednesday("WED", Calendar.WEDNESDAY),
        thursday("THU", Calendar.THURSDAY),
        friday("FRI", Calendar.FRIDAY),
        saturday("SAT", Calendar.SATURDAY);

        private final String identifier;
        private final int calendarDay;

        public static WeekDay getWeekDay(final int calendar) {
            switch (calendar) {
                case Calendar.SUNDAY:
                    return sunday;
                case Calendar.MONDAY:
                    return monday;
                case Calendar.TUESDAY:
                    return tuesday;
                case Calendar.WEDNESDAY:
                    return wednesday;
                case Calendar.THURSDAY:
                    return thursday;
                case Calendar.FRIDAY:
                    return friday;
                case Calendar.SATURDAY:
                    return saturday;
                default:
                    throw new IllegalArgumentException("Invalid calendar value " + calendar);
            }
        }

        public static WeekDay getWeekDay(final String id) {
            for (WeekDay aDay : WeekDay.values()) {
                if (aDay.toString().equals(id)) {
                    return aDay;
                }
            }
            throw new IllegalArgumentException("Invalid calendar value " + id);
        }

        private WeekDay(final String code, final int day) {
            this.identifier = code;
            this.calendarDay = day;
        }

        public int getCalendarDay() {
            return calendarDay;
        }

        @Override
        public String toString() {
            return identifier;
        }
    };

    enum ExpressionPart {
        SECONDS,
        MINUTES,
        HOURS,
        DAYSOFMONTH,
        MONTHS,
        DAYSOFWEEK,
        YEARS {
            @Override
            public ExpressionPart next() {
                return null;
            };
        };

        public ExpressionPart next() {
            return values()[ordinal() + 1];
        }
    };

    protected class FlaggedBoundedIntegerList extends BoundedIntegerList {

        private static final long serialVersionUID = -7838773351281106717L;
        protected boolean allValues;
        protected boolean noSpecificValue;

        FlaggedBoundedIntegerList(int absMin, int absMax, boolean negativeValuesAllowed, boolean is1indexed) {
            super(absMin, absMax, negativeValuesAllowed, is1indexed);
            allValues = false;
            noSpecificValue = false;
        }

        public void setAllValues() {
            allValues = true;
            add(min, max, 1);
        }

        public void setNotSpecificValue() {
            noSpecificValue = true;
        }

        public boolean isAllValues() {
            return allValues;
        }

        public boolean isNotSpecificValue() {
            return noSpecificValue;
        }
    }

    class ValueSet {
        public int value;
        public int pos;
    }

    /**
     * Constructs a new <code>CronExpression</code> based on the specified
     * parameter.
     *
     * @param expression
     *            String representation of the cron expression the new
     *            object should represent.
     * @throws java.text.ParseException
     *             Thrown if the string expression cannot be parsed into a valid
     *             <code>CronExpression</code>.
     */
    public CronExpression(final String expression) throws ParseException {
        this(expression, Calendar.getInstance().getTime(), TimeZone.getDefault());
    }

    /**
     * Constructs a new <code>CronExpression</code> based on the specified
     * parameter.
     *
     * @param expression
     *            String representation of the cron expression the new
     *            object should represent.
     * @param startTime
     *            The start time to consider for the cron expression.
     * @throws java.text.ParseException
     *             Thrown if the string expression cannot be parsed into a valid
     *             <code>CronExpression</code>.
     */
    public CronExpression(final String expression, final Date startTime) throws ParseException {
        this(expression, startTime, TimeZone.getDefault());
    }

    /**
     * Constructs a new <code>CronExpression</code> based on the specified
     * parameter.
     *
     * @param expression String representation of the cron expression the
     *            new object should represent
     * @param startTime
     *            The start time to consider for the cron expression.
     * @param zone
     *            The timezone for which this expression will be resolved.
     * @throws java.text.ParseException
     *             Thrown if the string expression cannot be parsed into a valid
     *             <code>CronExpression</code>.
     */
    public CronExpression(final String expression, final Date startTime, final TimeZone zone) throws ParseException {
        if (expression == null) {
            throw new IllegalArgumentException("The expression cannot be null");
        }

        this.expression = expression.toUpperCase(Locale.US);

        setStartDate(startTime, false);
        setTimeZone(zone);
        buildExpression(expression);
    }

    /**
     * Constructs a new {@code CronExpression} as a copy of an existing
     * instance.
     *
     * @param expression
     *            The existing cron expression to be copied
     */
    public CronExpression(Expression expression) {
        this.expression = expression.getExpression();
        try {
            buildExpression(this.expression);
        } catch (ParseException ex) {
            throw new AssertionError();
        }
        if (expression.getTimeZone() != null) {
            setTimeZone((TimeZone) expression.getTimeZone().clone());
        }
    }

    @Override
    public boolean isSatisfiedBy(Date date) {
        Calendar testDateCal = Calendar.getInstance(getTimeZone());
        testDateCal.setTime(date);
        testDateCal.set(Calendar.MILLISECOND, 0);
        Date originalDate = testDateCal.getTime();

        testDateCal.add(Calendar.SECOND, -1);

        Date timeAfter = getTimeAfter(testDateCal.getTime());

        return ((timeAfter != null) && (timeAfter.equals(originalDate)));
    }

    /**
     * Indicates whether the specified expression can be parsed into a
     * valid expression
     *
     * @param expression the expression to evaluate
     * @return a boolean indicating whether the given expression is a valid cron
     *         expression
     */
    public static boolean isValidExpression(String cronExpression) {

        try {
            new CronExpression(cronExpression);
        } catch (ParseException pe) {
            return false;
        }

        return true;
    }

    public static void validateExpression(String cronExpression) throws ParseException {
        new CronExpression(cronExpression);
    }

    @Override
    public void validateStartDate(Date startTime) {
        // Since we not really need a formal start time for a cron rule, we just skip this
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
    }

    @Override
    public Date getTimeAfter(Date afterTime) {

        if (afterTime == null) {
            throw new IllegalArgumentException("After time cannot be null");
        }

        if (!afterTime.after(getStartDate())) {
            return null;
        }

        // Computation is based on Gregorian year only.
        Calendar calendar = new java.util.GregorianCalendar(getTimeZone());

        // move ahead one second, since we're computing the time *after* the
        // given time
        afterTime = new Date(afterTime.getTime() + 1000);
        // we do not deal with milliseconds
        calendar.setTime(afterTime);
        calendar.set(Calendar.MILLISECOND, 0);

        boolean done = false;
        // loop until we've computed the next time, or we've past the endTime
        while (!done) {

            if (calendar.get(Calendar.YEAR) > 2999) { // prevent endless loop...
                return null;
            }

            SortedSet<Integer> st = null;
            int t = 0;

            int sec = calendar.get(Calendar.SECOND);
            int min = calendar.get(Calendar.MINUTE);

            st = parsedExpression.get(ExpressionPart.SECONDS).tailSet(sec);
            if (st != null && st.size() != 0) {
                sec = st.first();
            } else {
                sec = parsedExpression.get(ExpressionPart.SECONDS).first();
                min++;
                calendar.set(Calendar.MINUTE, min);
            }
            calendar.set(Calendar.SECOND, sec);

            min = calendar.get(Calendar.MINUTE);
            int hr = calendar.get(Calendar.HOUR_OF_DAY);
            t = -1;

            st = parsedExpression.get(ExpressionPart.MINUTES).tailSet(min);
            if (st != null && st.size() != 0) {
                t = min;
                min = st.first();
            } else {
                min = parsedExpression.get(ExpressionPart.MINUTES).first();
                hr++;
            }
            if (min != t) {
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, min);
                setCalendarHour(calendar, hr);
                continue;
            }
            calendar.set(Calendar.MINUTE, min);

            hr = calendar.get(Calendar.HOUR_OF_DAY);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            t = -1;

            st = parsedExpression.get(ExpressionPart.HOURS).tailSet(hr);
            if (st != null && st.size() != 0) {
                t = hr;
                hr = st.first();
            } else {
                hr = parsedExpression.get(ExpressionPart.HOURS).first();
                day++;
            }
            if (hr != t) {
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                setCalendarHour(calendar, hr);
                continue;
            }
            calendar.set(Calendar.HOUR_OF_DAY, hr);

            day = calendar.get(Calendar.DAY_OF_MONTH);
            int mon = calendar.get(Calendar.MONTH) + 1;
            // '+ 1' because calendar is 0-based for this field, and we are
            // 1-based
            t = -1;
            int tmon = mon;

            boolean dayOfMSpec = !parsedExpression.get(ExpressionPart.DAYSOFMONTH).isNotSpecificValue();
            boolean dayOfWSpec = !parsedExpression.get(ExpressionPart.DAYSOFWEEK).isNotSpecificValue();
            if (dayOfMSpec && !dayOfWSpec) { // get day by day of month rule
                st = parsedExpression.get(ExpressionPart.DAYSOFMONTH).tailSet(day);
                if (lastdayOfMonth) {
                    if (!nearestWeekday) {
                        t = day;
                        day = Month.getMonth(mon).getNumberOfDays(calendar.get(Calendar.YEAR));
                        day -= lastdayOffset;
                        if (t > day) {
                            mon++;
                            if (mon > 12) {
                                mon = 1;
                                tmon = 3333; // ensure test of mon != tmon further below fails
                                calendar.add(Calendar.YEAR, 1);
                            }
                            day = 1;
                        }
                    } else {
                        t = day;
                        day = Month.getMonth(mon).getNumberOfDays(calendar.get(Calendar.YEAR));
                        day -= lastdayOffset;

                        java.util.Calendar tcal = java.util.Calendar.getInstance(getTimeZone());
                        tcal.set(Calendar.SECOND, 0);
                        tcal.set(Calendar.MINUTE, 0);
                        tcal.set(Calendar.HOUR_OF_DAY, 0);
                        tcal.set(Calendar.DAY_OF_MONTH, day);
                        tcal.set(Calendar.MONTH, mon - 1);
                        tcal.set(Calendar.YEAR, calendar.get(Calendar.YEAR));

                        int ldom = Month.getMonth(mon).getNumberOfDays(calendar.get(Calendar.YEAR));
                        int dow = tcal.get(Calendar.DAY_OF_WEEK);

                        if (dow == Calendar.SATURDAY && day == 1) {
                            day += 2;
                        } else if (dow == Calendar.SATURDAY) {
                            day -= 1;
                        } else if (dow == Calendar.SUNDAY && day == ldom) {
                            day -= 2;
                        } else if (dow == Calendar.SUNDAY) {
                            day += 1;
                        }

                        tcal.set(Calendar.SECOND, sec);
                        tcal.set(Calendar.MINUTE, min);
                        tcal.set(Calendar.HOUR_OF_DAY, hr);
                        tcal.set(Calendar.DAY_OF_MONTH, day);
                        tcal.set(Calendar.MONTH, mon - 1);
                        Date nTime = tcal.getTime();
                        if (nTime.before(afterTime)) {
                            day = 1;
                            mon++;
                        }
                    }
                } else if (nearestWeekday) {
                    t = day;
                    day = parsedExpression.get(ExpressionPart.DAYSOFMONTH).first();

                    java.util.Calendar tcal = java.util.Calendar.getInstance(getTimeZone());
                    tcal.set(Calendar.SECOND, 0);
                    tcal.set(Calendar.MINUTE, 0);
                    tcal.set(Calendar.HOUR_OF_DAY, 0);
                    tcal.set(Calendar.DAY_OF_MONTH, day);
                    tcal.set(Calendar.MONTH, mon - 1);
                    tcal.set(Calendar.YEAR, calendar.get(Calendar.YEAR));

                    int ldom = Month.getMonth(mon).getNumberOfDays(calendar.get(Calendar.YEAR));
                    int dow = tcal.get(Calendar.DAY_OF_WEEK);

                    if (dow == Calendar.SATURDAY && day == 1) {
                        day += 2;
                    } else if (dow == Calendar.SATURDAY) {
                        day -= 1;
                    } else if (dow == Calendar.SUNDAY && day == ldom) {
                        day -= 2;
                    } else if (dow == Calendar.SUNDAY) {
                        day += 1;
                    }

                    tcal.set(Calendar.SECOND, sec);
                    tcal.set(Calendar.MINUTE, min);
                    tcal.set(Calendar.HOUR_OF_DAY, hr);
                    tcal.set(Calendar.DAY_OF_MONTH, day);
                    tcal.set(Calendar.MONTH, mon - 1);
                    Date nTime = tcal.getTime();
                    if (nTime.before(afterTime)) {
                        day = parsedExpression.get(ExpressionPart.DAYSOFMONTH).first();
                        mon++;
                    }
                } else if (st != null && st.size() != 0) {
                    t = day;
                    day = st.first();
                    // make sure we don't over-run a short month, such as february
                    int lastDay = Month.getMonth(mon).getNumberOfDays(calendar.get(Calendar.YEAR));
                    if (day > lastDay) {
                        day = parsedExpression.get(ExpressionPart.DAYSOFMONTH).first();
                        mon++;
                    }
                } else {
                    day = parsedExpression.get(ExpressionPart.DAYSOFMONTH).first();
                    mon++;
                }

                if (day != t || mon != tmon) {
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    calendar.set(Calendar.MONTH, mon - 1);
                    // '- 1' because calendar is 0-based for this field, and we
                    // are 1-based
                    continue;
                }
            } else if (dayOfWSpec && !dayOfMSpec) { // get day by day of week rule
                if (lastdayOfWeek) { // are we looking for the last XXX day of
                    // the month?
                    int dow = parsedExpression.get(ExpressionPart.DAYSOFWEEK).first(); // desired
                    // d-o-w
                    int cDow = calendar.get(Calendar.DAY_OF_WEEK); // current d-o-w
                    int daysToAdd = 0;
                    if (cDow < dow) {
                        daysToAdd = dow - cDow;
                    }
                    if (cDow > dow) {
                        daysToAdd = dow + (7 - cDow);
                    }

                    int lDay = Month.getMonth(mon).getNumberOfDays(calendar.get(Calendar.YEAR));

                    if (day + daysToAdd > lDay) { // did we already miss the
                        // last one?
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        calendar.set(Calendar.MONTH, mon);
                        // no '- 1' here because we are promoting the month
                        continue;
                    }

                    // find date of last occurrence of this day in this month...
                    while ((day + daysToAdd + 7) <= lDay) {
                        daysToAdd += 7;
                    }

                    day += daysToAdd;

                    if (daysToAdd > 0) {
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.DAY_OF_MONTH, day);
                        calendar.set(Calendar.MONTH, mon - 1);
                        // '- 1' here because we are not promoting the month
                        continue;
                    }

                } else if (nthdayOfWeek != 0) {
                    // are we looking for the Nth XXX day in the month?
                    int dow = parsedExpression.get(ExpressionPart.DAYSOFWEEK).first(); // desired
                    // d-o-w
                    int cDow = calendar.get(Calendar.DAY_OF_WEEK); // current d-o-w
                    int daysToAdd = 0;
                    if (cDow < dow) {
                        daysToAdd = dow - cDow;
                    } else if (cDow > dow) {
                        daysToAdd = dow + (7 - cDow);
                    }

                    boolean dayShifted = false;
                    if (daysToAdd > 0) {
                        dayShifted = true;
                    }

                    day += daysToAdd;
                    int weekOfMonth = day / 7;
                    if (day % 7 > 0) {
                        weekOfMonth++;
                    }

                    daysToAdd = (nthdayOfWeek - weekOfMonth) * 7;
                    day += daysToAdd;
                    if (daysToAdd < 0 || day > Month.getMonth(mon).getNumberOfDays(calendar.get(Calendar.YEAR))) {
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        calendar.set(Calendar.MONTH, mon);
                        // no '- 1' here because we are promoting the month
                        continue;
                    } else if (daysToAdd > 0 || dayShifted) {
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.DAY_OF_MONTH, day);
                        calendar.set(Calendar.MONTH, mon - 1);
                        // '- 1' here because we are NOT promoting the month
                        continue;
                    }
                } else {
                    int cDow = calendar.get(Calendar.DAY_OF_WEEK); // current d-o-w
                    int dow = parsedExpression.get(ExpressionPart.DAYSOFWEEK).first(); // desired
                    // d-o-w
                    st = parsedExpression.get(ExpressionPart.DAYSOFWEEK).tailSet(cDow);
                    if (st != null && st.size() > 0) {
                        dow = st.first();
                    }

                    int daysToAdd = 0;
                    if (cDow < dow) {
                        daysToAdd = dow - cDow;
                    }
                    if (cDow > dow) {
                        daysToAdd = dow + (7 - cDow);
                    }

                    int lDay = Month.getMonth(mon).getNumberOfDays(calendar.get(Calendar.YEAR));

                    if (day + daysToAdd > lDay) { // will we pass the end of
                        // the month?
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        calendar.set(Calendar.MONTH, mon);
                        // no '- 1' here because we are promoting the month
                        continue;
                    } else if (daysToAdd > 0) { // are we swithing days?
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.DAY_OF_MONTH, day + daysToAdd);
                        calendar.set(Calendar.MONTH, mon - 1);
                        // '- 1' because calendar is 0-based for this field,
                        // and we are 1-based
                        continue;
                    }
                }
            } else { // dayOfWSpec && !dayOfMSpec
                throw new UnsupportedOperationException(
                        "Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.");
            }
            calendar.set(Calendar.DAY_OF_MONTH, day);

            mon = calendar.get(Calendar.MONTH) + 1;
            // '+ 1' because calendar is 0-based for this field, and we are
            // 1-based
            int year = calendar.get(Calendar.YEAR);
            t = -1;

            if (year > MAX_YEAR) {
                return null;
            }

            st = parsedExpression.get(ExpressionPart.MONTHS).tailSet(mon);
            if (st != null && st.size() != 0) {
                t = mon;
                mon = st.first();
            } else {
                mon = parsedExpression.get(ExpressionPart.MONTHS).first();
                year++;
            }
            if (mon != t) {
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.MONTH, mon - 1);
                // '- 1' because calendar is 0-based for this field, and we are
                // 1-based
                calendar.set(Calendar.YEAR, year);
                continue;
            }
            calendar.set(Calendar.MONTH, mon - 1);
            // '- 1' because calendar is 0-based for this field, and we are
            // 1-based

            year = calendar.get(Calendar.YEAR);
            t = -1;

            st = parsedExpression.get(ExpressionPart.YEARS).tailSet(year);
            if (st != null && st.size() != 0) {
                t = year;
                year = st.first();
            } else {
                return null;
            }

            if (year != t) {
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.MONTH, 0);
                // '- 1' because calendar is 0-based for this field, and we are
                // 1-based
                calendar.set(Calendar.YEAR, year);
                continue;
            }
            calendar.set(Calendar.YEAR, year);

            done = true;
        }

        return calendar.getTime();
    }

    @Override
    public Date getFinalFireTime() {
        return null;
    }

    protected void buildExpression(String expression) throws ParseException {
        parsedExpression = new HashMap<ExpressionPart, FlaggedBoundedIntegerList>();
        parsedExpression.put(ExpressionPart.SECONDS,
                new FlaggedBoundedIntegerList(MIN_SECOND, MAX_SECOND, false, false));
        parsedExpression.put(ExpressionPart.MINUTES,
                new FlaggedBoundedIntegerList(MIN_MINUTE, MAX_MINUTE, false, false));
        parsedExpression.put(ExpressionPart.HOURS, new FlaggedBoundedIntegerList(MIN_HOUR, MAX_HOUR, false, false));
        parsedExpression.put(ExpressionPart.DAYSOFMONTH,
                new FlaggedBoundedIntegerList(MIN_MONTHDAY, MAX_MONTHDAY, false, true));
        parsedExpression.put(ExpressionPart.MONTHS, new FlaggedBoundedIntegerList(MIN_MONTH, MAX_MONTH, false, true));
        parsedExpression.put(ExpressionPart.DAYSOFWEEK,
                new FlaggedBoundedIntegerList(MIN_DAYOFWEEK, MAX_DAYOFWEEK, false, true));
        parsedExpression.put(ExpressionPart.YEARS, new FlaggedBoundedIntegerList(MIN_YEAR, MAX_YEAR, false, false));

        expressionParsed = true;

        try {

            ExpressionPart currentPart = ExpressionPart.SECONDS;

            StringTokenizer expressionTokenizer = new StringTokenizer(expression, " \t", false);

            while (expressionTokenizer.hasMoreTokens() && currentPart != null
                    && currentPart.ordinal() <= ExpressionPart.YEARS.ordinal()) {
                String token = expressionTokenizer.nextToken().trim();

                if (currentPart == ExpressionPart.DAYSOFMONTH && token.indexOf('L') != -1 && token.length() > 1
                        && token.contains(",")) {
                    throw new ParseException(
                            "Support for specifying 'L' and 'LW' with other days of the month is not implemented", -1);
                }
                if (currentPart == ExpressionPart.DAYSOFWEEK && token.indexOf('L') != -1 && token.length() > 1
                        && token.contains(",")) {
                    throw new ParseException(
                            "Support for specifying 'L' with other days of the week is not implemented", -1);
                }
                if (currentPart == ExpressionPart.DAYSOFWEEK && token.indexOf('#') != -1
                        && token.indexOf('#', token.indexOf('#') + 1) != -1) {
                    throw new ParseException("Support for specifying multiple \"nth\" days is not implemented.", -1);
                }

                StringTokenizer valueTokenizer = new StringTokenizer(token, ",");
                while (valueTokenizer.hasMoreTokens()) {
                    String v = valueTokenizer.nextToken();
                    storeExpressionVals(0, v, currentPart);
                }

                currentPart = currentPart.next();
            }

            if (currentPart != null && currentPart.ordinal() <= ExpressionPart.DAYSOFWEEK.ordinal()) {
                throw new ParseException("Unexpected end of expression.", expression.length());
            }

            if (currentPart != null && currentPart.ordinal() <= ExpressionPart.YEARS.ordinal()) {
                storeExpressionVals(0, "*", ExpressionPart.YEARS);
            }

            FlaggedBoundedIntegerList dow = parsedExpression.get(ExpressionPart.DAYSOFWEEK);
            FlaggedBoundedIntegerList dom = parsedExpression.get(ExpressionPart.DAYSOFMONTH);

            boolean dayOfMSpec = !dom.isNotSpecificValue();
            boolean dayOfWSpec = !dow.isNotSpecificValue();

            if (!dayOfMSpec || dayOfWSpec) {
                if (!dayOfWSpec || dayOfMSpec) {
                    throw new ParseException(
                            "Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.",
                            0);
                }
            }
        } catch (ParseException pe) {
            throw pe;
        } catch (Exception e) {
            throw new ParseException("Illegal cron expression format (" + e.toString() + ")", 0);
        }
    }

    protected int storeExpressionVals(int position, String string, ExpressionPart expressionPart)
            throws ParseException {
        int increment = 0;
        int i = skipWhiteSpace(position, string);
        if (i >= string.length()) {
            return i;
        }
        char c = string.charAt(i);
        if ((c >= 'A') && (c <= 'Z') && (!string.equals("L")) && (!string.equals("LW"))
                && (!string.matches("^L-[0-9]*[W]?"))) {
            String sub = string.substring(i, i + 3);
            int sval = -1;
            int eval = -1;
            if (expressionPart == ExpressionPart.MONTHS) {
                try {
                    sval = Month.getMonth(sub).getCalendarMonth() + 1;
                } catch (Exception e) {
                    throw new ParseException("Invalid Month value: '" + sub + "'", i);
                }

                if (string.length() > i + 3) {
                    c = string.charAt(i + 3);
                    if (c == '-') {
                        i += 4;
                        sub = string.substring(i, i + 3);
                        try {
                            eval = Month.getMonth(sub).getCalendarMonth() + 1;
                        } catch (Exception e) {
                            throw new ParseException("Invalid Month value: '" + sub + "'", i);
                        }
                    }
                }
            } else if (expressionPart == ExpressionPart.DAYSOFWEEK) {
                try {
                    sval = WeekDay.getWeekDay(sub).getCalendarDay();
                } catch (Exception e) {
                    throw new ParseException("Invalid Day-of-Week value: '" + sub + "'", i);
                }

                if (string.length() > i + 3) {
                    c = string.charAt(i + 3);
                    if (c == '-') {
                        i += 4;
                        sub = string.substring(i, i + 3);
                        try {
                            eval = WeekDay.getWeekDay(sub).getCalendarDay();
                        } catch (Exception e) {
                            throw new ParseException("Invalid Day-of-Week value: '" + sub + "'", i);

                        }
                    } else if (c == '#') {
                        try {
                            i += 4;
                            nthdayOfWeek = Integer.parseInt(string.substring(i));
                            if (nthdayOfWeek < 1 || nthdayOfWeek > 5) {
                                throw new Exception();
                            }
                        } catch (Exception e) {
                            throw new ParseException("A numeric value between 1 and 5 must follow the '#' option", i);
                        }
                    } else if (c == 'L') {
                        lastdayOfWeek = true;
                        i++;
                    }
                }
            } else {
                throw new ParseException("Illegal characters for this position: '" + sub + "'", i);
            }
            if (eval != -1) {
                increment = 1;
            }
            parsedExpression.get(expressionPart).add(sval, eval, increment);
            return (i + 3);
        }

        if (c == '?') {
            i++;
            if ((i + 1) < string.length() && (string.charAt(i) != ' ' && string.charAt(i + 1) != '\t')) {
                throw new ParseException("Illegal character after '?': " + string.charAt(i), i);
            }
            if (expressionPart != ExpressionPart.DAYSOFWEEK && expressionPart != ExpressionPart.DAYSOFMONTH) {
                throw new ParseException("'?' can only be specfied for Day-of-Month or Day-of-Week.", i);
            }
            if (expressionPart == ExpressionPart.DAYSOFWEEK && !lastdayOfMonth) {
                if (parsedExpression.get(ExpressionPart.DAYSOFMONTH).isNotSpecificValue()) {
                    throw new ParseException("'?' can only be specfied for Day-of-Month -OR- Day-of-Week.", i);
                }
            }

            parsedExpression.get(expressionPart).setNotSpecificValue();
            return i;
        }

        if (c == '*' || c == '/') {
            if (c == '*' && (i + 1) >= string.length()) {
                parsedExpression.get(expressionPart).setAllValues();
                return i + 1;
            } else if (c == '/'
                    && ((i + 1) >= string.length() || string.charAt(i + 1) == ' ' || string.charAt(i + 1) == '\t')) {
                throw new ParseException("'/' must be followed by an integer.", i);
            } else if (c == '*') {
                i++;
            }
            c = string.charAt(i);
            if (c == '/') {
                i++;
                if (i >= string.length()) {
                    throw new ParseException("Unexpected end of string.", i);
                }

                increment = getNumericValue(string, i);

                i++;
                if (increment > 10) {
                    i++;
                }
                if (increment > 59
                        && (expressionPart == ExpressionPart.SECONDS || expressionPart == ExpressionPart.MINUTES)) {
                    throw new ParseException("Increment > 60 : " + increment, i);
                } else if (increment > 23 && (expressionPart == ExpressionPart.HOURS)) {
                    throw new ParseException("Increment > 24 : " + increment, i);
                } else if (increment > 31 && (expressionPart == ExpressionPart.DAYSOFMONTH)) {
                    throw new ParseException("Increment > 31 : " + increment, i);
                } else if (increment > 7 && (expressionPart == ExpressionPart.DAYSOFWEEK)) {
                    throw new ParseException("Increment > 7 : " + increment, i);
                } else if (increment > 12 && (expressionPart == ExpressionPart.MONTHS)) {
                    throw new ParseException("Increment > 12 : " + increment, i);
                }
            } else {
                increment = 1;
            }

            parsedExpression.get(expressionPart).setAllValues();
            return i;
        } else if (c == 'L') {
            i++;
            if (expressionPart == ExpressionPart.DAYSOFMONTH) {
                lastdayOfMonth = true;
            }
            if (expressionPart == ExpressionPart.DAYSOFWEEK) {
                parsedExpression.get(expressionPart).add(7, 7, 0);

            }
            if (expressionPart == ExpressionPart.DAYSOFMONTH && string.length() > i) {
                c = string.charAt(i);
                if (c == '-') {
                    ValueSet vs = getValue(0, string, i + 1);
                    lastdayOffset = vs.value;
                    if (lastdayOffset > 30) {
                        throw new ParseException("Offset from last day must be <= 30", i + 1);
                    }
                    i = vs.pos;
                }
                if (string.length() > i) {
                    c = string.charAt(i);
                    if (c == 'W') {
                        nearestWeekday = true;
                        i++;
                    }
                }
            }
            return i;
        } else if (c >= '0' && c <= '9') {
            int val = Integer.parseInt(String.valueOf(c));
            i++;
            if (i >= string.length()) {
                parsedExpression.get(expressionPart).add(val);
            } else {
                c = string.charAt(i);
                if (c >= '0' && c <= '9') {
                    ValueSet vs = getValue(val, string, i);
                    val = vs.value;
                    i = vs.pos;
                }
                i = checkNext(i, string, val, expressionPart);
                return i;
            }
        } else {
            throw new ParseException("Unexpected character: " + c, i);
        }

        return i;
    }

    protected int checkNext(int position, String string, int value, ExpressionPart type) throws ParseException {
        int end = -1;
        int i = position;

        if (i >= string.length()) {
            parsedExpression.get(type).add(value);
            return i;
        }

        char c = string.charAt(position);

        if (c == 'L') {
            if (type == ExpressionPart.DAYSOFWEEK) {
                if (value < 1 || value > 7) {
                    throw new ParseException("Day-of-Week values must be between 1 and 7", -1);
                }
                lastdayOfWeek = true;
            } else {
                throw new ParseException("'L' option is not valid here. (pos=" + i + ")", i);
            }
            FlaggedBoundedIntegerList set = parsedExpression.get(type);
            set.add(value);
            i++;
            return i;
        }

        if (c == 'W') {
            if (type == ExpressionPart.DAYSOFMONTH) {
                nearestWeekday = true;
            } else {
                throw new ParseException("'W' option is not valid here. (pos=" + i + ")", i);
            }
            if (value > 31) {
                throw new ParseException(
                        "The 'W' option does not make sense with values larger than 31 (max number of days in a month)",
                        i);
            }
            FlaggedBoundedIntegerList set = parsedExpression.get(type);
            set.add(value);
            i++;
            return i;
        }

        if (c == '#') {
            if (type != ExpressionPart.DAYSOFWEEK) {
                throw new ParseException("'#' option is not valid here. (pos=" + i + ")", i);
            }
            i++;
            try {
                nthdayOfWeek = Integer.parseInt(string.substring(i));
                if (nthdayOfWeek < 1 || nthdayOfWeek > 5) {
                    throw new Exception();
                }
            } catch (Exception e) {
                throw new ParseException("A numeric value between 1 and 5 must follow the '#' option", i);
            }

            FlaggedBoundedIntegerList set = parsedExpression.get(type);
            set.add(value);
            i++;
            return i;
        }

        if (c == '-') {
            i++;
            c = string.charAt(i);
            int v = Integer.parseInt(String.valueOf(c));
            end = v;
            i++;
            if (i >= string.length()) {
                parsedExpression.get(type).add(value, end, 1);

                return i;
            }
            c = string.charAt(i);
            if (c >= '0' && c <= '9') {
                ValueSet vs = getValue(v, string, i);
                end = vs.value;
                i = vs.pos;
            }
            if (i < string.length() && ((c = string.charAt(i)) == '/')) {
                i++;
                c = string.charAt(i);
                int v2 = Integer.parseInt(String.valueOf(c));
                i++;
                if (i >= string.length()) {
                    parsedExpression.get(type).add(value, end, v2);

                    return i;
                }
                c = string.charAt(i);
                if (c >= '0' && c <= '9') {
                    ValueSet vs = getValue(v2, string, i);
                    int v3 = vs.value;
                    parsedExpression.get(type).add(value, end, v3);

                    i = vs.pos;
                    return i;
                } else {
                    parsedExpression.get(type).add(value, end, v2);

                    return i;
                }
            } else {
                parsedExpression.get(type).add(value, end, 1);

                return i;
            }
        }

        if (c == '/') {
            i++;
            c = string.charAt(i);
            int v2 = Integer.parseInt(String.valueOf(c));
            i++;
            if (i >= string.length()) {
                parsedExpression.get(type).add(value, end, v2);

                return i;
            }
            c = string.charAt(i);
            if (c >= '0' && c <= '9') {
                ValueSet vs = getValue(v2, string, i);
                int v3 = vs.value;
                parsedExpression.get(type).add(value, end, v3);

                i = vs.pos;
                return i;
            } else {
                throw new ParseException("Unexpected character '" + c + "' after '/'", i);
            }
        }

        parsedExpression.get(type).add(value, end, 0);

        i++;
        return i;
    }

    protected int skipWhiteSpace(int i, String s) {
        for (; i < s.length() && (s.charAt(i) == ' ' || s.charAt(i) == '\t'); i++) {
            ;
        }
        return i;
    }

    protected int findNextWhiteSpace(int i, String s) {
        for (; i < s.length() && (s.charAt(i) != ' ' || s.charAt(i) != '\t'); i++) {
            ;
        }
        return i;
    }

    protected ValueSet getValue(int value, String string, int position) {
        char c = string.charAt(position);
        StringBuilder s1 = new StringBuilder(String.valueOf(value));
        while (c >= '0' && c <= '9') {
            s1.append(c);
            position++;
            if (position >= string.length()) {
                break;
            }
            c = string.charAt(position);
        }
        ValueSet val = new ValueSet();

        val.pos = (position < string.length()) ? position : position + 1;
        val.value = Integer.parseInt(s1.toString());
        return val;
    }

    protected int getNumericValue(String string, int index) {
        int endOfVal = findNextWhiteSpace(index, string);
        String val = string.substring(index, endOfVal);
        return Integer.parseInt(val);
    }

    /**
     * Advance the calendar to the particular hour paying particular attention
     * to daylight saving problems.
     *
     * @param cal the calendar to operate on
     * @param hour the hour to set
     */
    protected void setCalendarHour(Calendar cal, int hour) {
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
        if (cal.get(java.util.Calendar.HOUR_OF_DAY) != hour && hour != 24) {
            cal.set(java.util.Calendar.HOUR_OF_DAY, hour + 1);
        }
    }

}
