/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.scheduler;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.scheduler.AbstractExpressionPart.BoundedIntegerSet;
import org.eclipse.smarthome.core.scheduler.CronExpression.CronExpressionPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>CronExpression</code> is an implementation of {@link Expression} that provides a parser and evaluator for for
 * unix-like cron expressions that are compatible wit the Quartz (https://quartz-scheduler.org/, see
 * http://www.quartz-scheduler.org/api/2.2.1/org/quartz/CronExpression.html) framework. Cron
 * expressions provide the ability to specify complex time combinations such as
 * &quot;At 8:00am every Monday through Friday&quot; or &quot;At 1:30am every
 * last Friday of the month&quot;.
 *
 * @author Karel Goderis - Initial contribution
 *
 */
public final class CronExpression extends AbstractExpression<CronExpressionPart> {

    private final Logger logger = LoggerFactory.getLogger(CronExpression.class);

    public enum Month {
        JANUARY("JAN", Calendar.JANUARY, 31),
        FEBRUARY("FEB", Calendar.FEBRUARY, 28) {
            @Override
            public int getNumberOfDays(int year) {
                if (((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0))) {
                    return 29;
                } else {
                    return 28;
                }
            };
        },
        MARCH("MAR", Calendar.MARCH, 31),
        APRIL("APR", Calendar.APRIL, 30),
        MAY("MAY", Calendar.MAY, 31),
        JUNE("JUN", Calendar.JUNE, 30),
        JULY("JUL", Calendar.JULY, 31),
        AUGUST("AUG", Calendar.AUGUST, 31),
        SEPTEMBER("SEP", Calendar.SEPTEMBER, 30),
        OCTOBER("OCT", Calendar.OCTOBER, 31),
        NOVEMBER("NOV", Calendar.NOVEMBER, 30),
        DECEMBER("DEC", Calendar.DECEMBER, 31);

        private final String identifier;
        private final int calendarMonth;
        private final int numberOfDays;

        public static Month getMonth(final int calendar) {
            return Month.values()[calendar];
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

    public enum WeekDay {
        SUNDAY("SUN", Calendar.SUNDAY),
        MONDAY("MON", Calendar.MONDAY),
        TUESDAY("TUE", Calendar.TUESDAY),
        WEDNESDAY("WED", Calendar.WEDNESDAY),
        THURSDAY("THU", Calendar.THURSDAY),
        FRIDAY("FRI", Calendar.FRIDAY),
        SATURDAY("SAT", Calendar.SATURDAY);

        private final String identifier;
        private final int calendarDay;

        public static WeekDay getWeekDay(final int calendar) {
            return WeekDay.values()[calendar];
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

    /**
     * Constructs a new <code>CronExpression</code> based on the specified
     * parameter.
     *
     * @param expression string representation of the cron expression the new object should represent.
     * @throws ParseException if the string expression cannot be parsed into a valid <code>CronExpression</code>.
     */
    public CronExpression(final String expression) throws ParseException {
        this(expression, Calendar.getInstance().getTime(), TimeZone.getDefault());
    }

    /**
     * Constructs a new <code>CronExpression</code> based on the specified
     * parameter.
     *
     * @param expression string representation of the cron expression the new object should represent.
     * @param startTime the start time to consider for the cron expression.
     * @throws ParseException if the string expression cannot be parsed into a valid <code>CronExpression</code>.
     */
    public CronExpression(final String expression, final Date startTime) throws ParseException {
        this(expression, startTime, TimeZone.getDefault());
    }

    /**
     * Constructs a new <code>CronExpression</code> based on the specified
     * parameter.
     *
     * @param expression string representation of the cron expression the new object should represent
     * @param startTime the start time to consider for the cron expression.
     * @param zone the timezone for which this expression will be resolved.
     * @throws ParseException if the string expression cannot be parsed into a valid <code>CronExpression</code>.
     */
    public CronExpression(final String expression, final Date startTime, final TimeZone zone) throws ParseException {
        super(expression, " \t", startTime, zone, 0, 1);
    }

    @Override
    public void setStartDate(Date startDate) throws IllegalArgumentException, ParseException {
        if (startDate == null) {
            throw new IllegalArgumentException("The start date of the rule can not be null");
        }

        // We set the real start date to the next second; milliseconds are not supported by cron expressions anyways
        Calendar calendar = Calendar.getInstance(getTimeZone());
        calendar.setTime(startDate);
        if (calendar.get(Calendar.MILLISECOND) != 0) {
            calendar.add(Calendar.SECOND, 1);
            calendar.set(Calendar.MILLISECOND, 0);
        }
        super.setStartDate(calendar.getTime());
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
     * @return a boolean indicating whether the given expression is a valid cron expression
     */
    public static boolean isValidExpression(String cronExpression) {

        try {
            new CronExpression(cronExpression);
        } catch (ParseException pe) {
            return false;
        }

        return true;
    }

    @Override
    protected void validateExpression() throws IllegalArgumentException {

        DayOfMonthExpressionPart domPart = (DayOfMonthExpressionPart) this
                .getExpressionPart(DayOfMonthExpressionPart.class);
        DayOfWeekExpressionPart dowPart = (DayOfWeekExpressionPart) this
                .getExpressionPart(DayOfWeekExpressionPart.class);

        if (!domPart.isNotSpecific() && !dowPart.isNotSpecific()) {
            throw new IllegalArgumentException(
                    "The DayOfMonth and DayOfWeek rule parts CAN NOT be not specific at the same time.");
        }

    }

    @Override
    protected void populateWithSeeds() {

        YearsExpressionPart thePart = null;

        for (ExpressionPart part : getExpressionParts()) {
            if (part instanceof YearsExpressionPart) {
                thePart = (YearsExpressionPart) part;
                break;
            }
        }

        YearsExpressionPart yep = null;
        try {
            yep = new YearsExpressionPart("");
        } catch (ParseException e) {
            logger.error("An exception occurred while creating an expression part : '{}'", e.getMessage());
            return;
        }

        if (thePart == null) {
            BoundedIntegerSet set = yep.getValueSet();
            Calendar cal = Calendar.getInstance(getTimeZone());
            cal.setTime(getStartDate());
            int currentYear = cal.get(Calendar.YEAR);

            for (int i = 0; i < 10; i++) {
                set.add(currentYear++);
            }

            yep.setValueSet(set);
            getExpressionParts().add(yep);

        } else {
            BoundedIntegerSet set = thePart.getValueSet();
            int maxYear = set.last();
            for (int i = 0; i < 10; i++) {
                if (maxYear < YearsExpressionPart.MAX_YEAR) {
                    set.add(maxYear++);
                }
            }
        }

    }

    @Override
    protected CronExpressionPart parseToken(String token, int position) throws ParseException {
        switch (position) {
            case 1:
                return new SecondsExpressionPart(token);
            case 2:
                return new MinutesExpressionPart(token);
            case 3:
                return new HoursExpressionPart(token);
            case 4:
                return new DayOfMonthExpressionPart(token);
            case 5:
                return new MonthsExpressionPart(token);
            case 6:
                return new DayOfWeekExpressionPart(token);
            case 7:
                return new YearsExpressionPart(token);
            default:
                return null;
        }
    }

    protected abstract class CronExpressionPart extends AbstractExpressionPart {

        public CronExpressionPart(String s) throws ParseException {
            super(s);
        }

        @Override
        public final void parse() throws ParseException {
            setValueSet(initializeValueSet());
            StringTokenizer valueTokenizer = new StringTokenizer(getPart(), ",");
            while (valueTokenizer.hasMoreTokens()) {
                String v = valueTokenizer.nextToken();
                parseToken(v);
            }
        }

        abstract String getSpecialToken(String token);

        abstract void parseToken(String v) throws ParseException;

    }

    protected class SecondsExpressionPart extends CronExpressionPart {

        protected static final int MIN_SECOND = 0;
        protected static final int MAX_SECOND = 59;

        public SecondsExpressionPart(String s) throws ParseException {
            super(s);
            parse();
        }

        @Override
        public int order() {
            return 7;
        }

        @Override
        String getSpecialToken(String token) {
            if (token.equals("*")) {
                return token;
            }
            if (token.contains("-")) {
                return "-";
            }
            if (token.contains("/")) {
                return "/";
            }
            return "";
        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return new BoundedIntegerSet(MIN_SECOND, MAX_SECOND, false, false);
        }

        @Override
        void parseToken(String v) throws ParseException {
            switch (getSpecialToken(v)) {
                case "-": {
                    String from = StringUtils.substringBefore(v, "-");
                    String to = StringUtils.substringAfter(v, "-");
                    getValueSet().add(Integer.parseInt(from), Integer.parseInt(to), 1);
                    break;
                }
                case "/": {
                    String from = StringUtils.substringBefore(v, "/");
                    String increment = StringUtils.substringAfter(v, "/");
                    try {
                        if (Integer.parseInt(increment) > MAX_SECOND) {
                            throw new ParseException("Increment is too large", 0);
                        }
                    } catch (Exception e) {
                        throw new ParseException("Increment '" + v + "' is not a valid value", 0);

                    }
                    int fromValue = from.equals("*") ? 0 : Integer.parseInt(from);
                    getValueSet().add(fromValue, MAX_SECOND, Integer.parseInt(increment));
                    break;
                }
                case "*": {
                    getValueSet().add(MIN_SECOND, MAX_SECOND, 1);
                    break;
                }
                default: {
                    try {
                        getValueSet().add(Integer.parseInt(v));
                    } catch (Exception e) {
                        throw new ParseException("'" + v + "' is not a valid token", 0);
                    }
                    break;
                }
            }
        }

        @Override
        public ArrayList<Date> apply(Date startDate, ArrayList<Date> candidates) {
            final Calendar cal = Calendar.getInstance(getTimeZone());

            List<Date> newCandidates = new ArrayList<Date>();
            List<Date> oldCandidates = new ArrayList<Date>();

            if (candidates.isEmpty()) {
                candidates.add(startDate);
            }

            oldCandidates.addAll(candidates);

            for (Date date : candidates) {
                for (Integer element : getValueSet()) {
                    cal.setTime(date);
                    cal.set(Calendar.SECOND, element);
                    newCandidates.add(cal.getTime());
                }
            }

            candidates.removeAll(oldCandidates);
            candidates.addAll(newCandidates);
            return candidates;
        }
    }

    protected class MinutesExpressionPart extends CronExpressionPart {

        protected static final int MIN_MINUTE = 0;
        protected static final int MAX_MINUTE = 59;

        public MinutesExpressionPart(String s) throws ParseException {
            super(s);
        }

        @Override
        public int order() {
            return 6;
        }

        @Override
        String getSpecialToken(String token) {
            if (token.equals("*")) {
                return token;
            }
            if (token.contains("-")) {
                return "-";
            }
            if (token.contains("/")) {
                return "/";
            }
            return "";
        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return new BoundedIntegerSet(MIN_MINUTE, MAX_MINUTE, false, false);
        }

        @Override
        void parseToken(String v) throws ParseException {
            switch (getSpecialToken(v)) {
                case "-": {
                    String from = StringUtils.substringBefore(v, "-");
                    String to = StringUtils.substringAfter(v, "-");
                    getValueSet().add(Integer.parseInt(from), Integer.parseInt(to), 1);
                    break;
                }
                case "/": {
                    String from = StringUtils.substringBefore(v, "/");
                    String increment = StringUtils.substringAfter(v, "/");
                    try {
                        if (Integer.parseInt(increment) > MAX_MINUTE) {
                            throw new ParseException("Increment is too large", 0);
                        }
                    } catch (Exception e) {
                        throw new ParseException("Increment '" + v + "' is not a valid value", 0);

                    }
                    int fromValue = from.equals("*") ? 0 : Integer.parseInt(from);
                    getValueSet().add(fromValue, MAX_MINUTE, Integer.parseInt(increment));
                    break;
                }
                case "*": {
                    getValueSet().add(MIN_MINUTE, MAX_MINUTE, 1);
                    break;
                }
                default: {
                    try {
                        getValueSet().add(Integer.parseInt(v));
                    } catch (Exception e) {
                        throw new ParseException("'" + v + "' is not a valid token", 0);
                    }
                    break;
                }
            }
        }

        @Override
        public ArrayList<Date> apply(Date startDate, ArrayList<Date> candidates) {
            final Calendar cal = Calendar.getInstance(getTimeZone());

            List<Date> newCandidates = new ArrayList<Date>();
            List<Date> oldCandidates = new ArrayList<Date>();

            if (candidates.isEmpty()) {
                candidates.add(startDate);
            }

            oldCandidates.addAll(candidates);

            for (Date date : candidates) {
                for (Integer element : getValueSet()) {
                    cal.setTime(date);
                    cal.set(Calendar.MINUTE, element);
                    newCandidates.add(cal.getTime());
                }
            }

            candidates.removeAll(oldCandidates);
            candidates.addAll(newCandidates);
            return candidates;
        }
    }

    protected class HoursExpressionPart extends CronExpressionPart {

        protected static final int MIN_HOUR = 0;
        protected static final int MAX_HOUR = 23;

        public HoursExpressionPart(String s) throws ParseException {
            super(s);
        }

        @Override
        public int order() {
            return 5;
        }

        @Override
        String getSpecialToken(String token) {
            if (token.equals("*")) {
                return token;
            }
            if (token.contains("-")) {
                return "-";
            }
            if (token.contains("/")) {
                return "/";
            }
            return "";
        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return new BoundedIntegerSet(MIN_HOUR, MAX_HOUR, false, false);
        }

        @Override
        void parseToken(String v) throws ParseException {
            switch (getSpecialToken(v)) {
                case "-": {
                    String from = StringUtils.substringBefore(v, "-");
                    String to = StringUtils.substringAfter(v, "-");
                    getValueSet().add(Integer.parseInt(from), Integer.parseInt(to), 1);
                    break;
                }
                case "/": {
                    String from = StringUtils.substringBefore(v, "/");
                    String increment = StringUtils.substringAfter(v, "/");
                    try {
                        if (Integer.parseInt(increment) > MAX_HOUR) {
                            throw new ParseException("Increment is too large", 0);
                        }
                    } catch (Exception e) {
                        throw new ParseException("Increment '" + v + "' is not a valid value", 0);

                    }
                    int fromValue = from.equals("*") ? 0 : Integer.parseInt(from);
                    getValueSet().add(fromValue, MAX_HOUR, Integer.parseInt(increment));
                    break;
                }
                case "*": {
                    getValueSet().add(MIN_HOUR, MAX_HOUR, 1);
                    break;
                }
                default: {
                    try {
                        getValueSet().add(Integer.parseInt(v));
                    } catch (Exception e) {
                        throw new ParseException("'" + v + "' is not a valid token", 0);
                    }

                    break;
                }
            }
        }

        @Override
        public ArrayList<Date> apply(Date startDate, ArrayList<Date> candidates) {
            final Calendar cal = Calendar.getInstance(getTimeZone());

            List<Date> newCandidates = new ArrayList<Date>();
            List<Date> oldCandidates = new ArrayList<Date>();

            if (candidates.isEmpty()) {
                candidates.add(startDate);
            }

            oldCandidates.addAll(candidates);

            for (Date date : candidates) {
                for (Integer element : getValueSet()) {
                    cal.setTime(date);
                    cal.set(Calendar.HOUR_OF_DAY, element);
                    newCandidates.add(cal.getTime());
                }
            }
            candidates.removeAll(oldCandidates);
            candidates.addAll(newCandidates);
            return candidates;
        }
    }

    protected class MonthsExpressionPart extends CronExpressionPart {

        protected static final int MIN_MONTH = 1;
        protected static final int MAX_MONTH = 12;

        public MonthsExpressionPart(String s) throws ParseException {
            super(s);
        }

        @Override
        public int order() {
            return 2;
        }

        @Override
        String getSpecialToken(String token) {
            if (token.equals("*")) {
                return token;
            }
            if (token.contains("-")) {
                return "-";
            }
            if (token.contains("/")) {
                return "/";
            }
            return "";
        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return new BoundedIntegerSet(MIN_MONTH, MAX_MONTH, false, true);
        }

        protected int monthAsInteger(String monthAsString) throws ParseException {
            try {
                return Month.getMonth(monthAsString).getCalendarMonth();
            } catch (IllegalArgumentException e) {
                try {
                    return Integer.parseInt(monthAsString);
                } catch (Exception f) {
                    throw new ParseException("Invalid Month value: '" + monthAsString + "'", 0);
                }
            }

        }

        @Override
        void parseToken(String v) throws ParseException {
            switch (getSpecialToken(v)) {
                case "-": {
                    String from = StringUtils.substringBefore(v, "-");
                    String to = StringUtils.substringAfter(v, "-");
                    getValueSet().add(monthAsInteger(from), monthAsInteger(to), 1);
                    break;
                }
                case "/": {
                    String from = StringUtils.substringBefore(v, "/");
                    String increment = StringUtils.substringAfter(v, "/");
                    try {
                        if (monthAsInteger(increment) > MAX_MONTH) {
                            throw new ParseException("Increment is too large", 0);
                        }
                    } catch (Exception e) {
                        throw new ParseException("Increment '" + v + "' is not a valid value", 0);

                    }
                    int fromValue = from.equals("*") ? 0 : monthAsInteger(from);
                    getValueSet().add(fromValue, MAX_MONTH, monthAsInteger(increment));
                    break;
                }
                case "*": {
                    getValueSet().add(MIN_MONTH, MAX_MONTH, 1);
                    break;
                }
                default: {
                    try {
                        getValueSet().add(monthAsInteger(v));
                    } catch (Exception e) {
                        throw new ParseException("'" + v + "' is not a valid token", 0);
                    }
                    break;
                }
            }
        }

        @Override
        public ArrayList<Date> apply(Date startDate, ArrayList<Date> candidates) {
            final Calendar cal = Calendar.getInstance(getTimeZone());

            List<Date> newCandidates = new ArrayList<Date>();
            List<Date> oldCandidates = new ArrayList<Date>();

            if (candidates.isEmpty()) {
                candidates.add(startDate);
            }

            oldCandidates.addAll(candidates);

            for (Date date : candidates) {
                for (Integer element : getValueSet()) {
                    cal.setTime(date);
                    cal.roll(Calendar.MONTH, (element - 1) - cal.get(Calendar.MONTH));
                    newCandidates.add(cal.getTime());
                }
            }
            candidates.removeAll(oldCandidates);
            candidates.addAll(newCandidates);
            return candidates;
        }
    }

    protected class DayOfMonthExpressionPart extends CronExpressionPart {

        protected static final int MIN_MONTHDAY = 1;
        protected static final int MAX_MONTHDAY = 31;

        protected boolean isLastDayOfMonth;
        protected boolean isLastWeekDayOfMonth;
        protected boolean isNearestWeekDay;
        protected boolean isNotSpecific;
        protected int weekDay;
        protected int monthOffset;

        public boolean isLastDayOfMonth() {
            return isLastDayOfMonth;
        }

        public boolean isLastWeekDayOfMonth() {
            return isLastWeekDayOfMonth;
        }

        public boolean isNearestWeekDay() {
            return isNearestWeekDay;
        }

        public boolean isNotSpecific() {
            return isNotSpecific;
        }

        public DayOfMonthExpressionPart(String s) throws ParseException {
            super(s);
        }

        @Override
        public int order() {
            return 3;
        }

        @Override
        String getSpecialToken(String token) {
            if (token.equals("*") || token.equals("?") || token.equals("LW")) {
                return token;
            }
            if (token.contains("-") && !token.contains("L")) {
                return "-";
            }
            if (token.contains("L") && !token.equals("LW")) {
                return "L";
            }
            if (token.contains("W") && !token.equals("LW")) {
                return "W";
            }
            if (token.contains("/")) {
                return "/";
            }
            return "";
        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return new BoundedIntegerSet(MIN_MONTHDAY, MAX_MONTHDAY, false, true);
        }

        @Override
        void parseToken(String v) throws ParseException {
            switch (getSpecialToken(v)) {
                case "-": {
                    String from = StringUtils.substringBefore(v, "-");
                    String to = StringUtils.substringAfter(v, "-");
                    getValueSet().add(Integer.parseInt(from), Integer.parseInt(to), 1);
                    break;
                }
                case "/": {
                    String from = StringUtils.substringBefore(v, "/");
                    String increment = StringUtils.substringAfter(v, "/");
                    try {
                        if (Integer.parseInt(increment) > MAX_MONTHDAY) {
                            throw new ParseException("Increment is too large", 0);
                        }
                    } catch (Exception e) {
                        throw new ParseException("Increment '" + v + "' is not a valid value", 0);
                    }
                    int fromValue = from.equals("*") ? 0 : Integer.parseInt(from);
                    getValueSet().add(fromValue, MAX_MONTHDAY, Integer.parseInt(increment));
                    break;
                }
                case "*": {
                    getValueSet().add(MIN_MONTHDAY,
                            Calendar.getInstance(getTimeZone()).getActualMaximum(Calendar.DAY_OF_MONTH), 1);
                    break;
                }
                case "?": {
                    isNotSpecific = true;
                    break;
                }
                case "L": {
                    isLastDayOfMonth = true;
                    monthOffset = StringUtils.substringAfter(v, "L-").equals("") ? 0
                            : Integer.parseInt(StringUtils.substringAfter(v, "L-"));
                    if (monthOffset > 30) {
                        throw new ParseException("Offset from last day must be <= 30", 0);
                    }
                    break;
                }
                case "W": {
                    if (StringUtils.substringBefore(v, "W").equals("")) {
                        throw new ParseException("'W' option need to specify a number", 0);
                    } else {
                        isNearestWeekDay = true;
                        weekDay = Integer.parseInt(StringUtils.substringBefore(v, "W"));
                        if (weekDay > 31) {
                            throw new ParseException("'W' option can not be larger than 31", 0);
                        }
                    }
                    break;
                }
                case "LW": {
                    isLastWeekDayOfMonth = true;
                    break;
                }
                default: {
                    try {
                        getValueSet().add(Integer.parseInt(v));
                    } catch (Exception e) {
                        throw new ParseException("'" + v + "' is not a valid token", 0);
                    }
                    break;
                }
            }
        }

        @Override
        public ArrayList<Date> apply(Date startDate, ArrayList<Date> candidates) {

            if (!isNotSpecific) {
                final Calendar cal = Calendar.getInstance(getTimeZone());

                List<Date> newCandidates = new ArrayList<Date>();
                List<Date> oldCandidates = new ArrayList<Date>();

                if (candidates.isEmpty()) {
                    candidates.add(startDate);
                }

                oldCandidates.addAll(candidates);

                for (Date date : candidates) {
                    cal.setTime(date);

                    if (isLastDayOfMonth) {
                        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                    } else

                    if (isLastWeekDayOfMonth) {
                        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                        cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, -1);
                    } else

                    if (isNearestWeekDay) {
                        cal.set(Calendar.DAY_OF_MONTH, weekDay);
                        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                            if (weekDay == 1) {
                                cal.add(Calendar.DATE, 2);
                            } else {
                                cal.add(Calendar.DATE, -1);
                            }
                        } else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                            if (weekDay == cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                                cal.add(Calendar.DATE, -1);
                            } else {
                                cal.add(Calendar.DATE, 1);
                            }
                        }
                    } else {
                        for (Integer element : getValueSet()) {
                            cal.setTime(date);
                            if (element <= cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                                cal.set(Calendar.DAY_OF_MONTH, element);
                                newCandidates.add(cal.getTime());
                            }
                        }
                    }
                }
                candidates.removeAll(oldCandidates);
                candidates.addAll(newCandidates);
            }

            return candidates;
        }
    }

    protected class DayOfWeekExpressionPart extends CronExpressionPart {

        protected static final int MIN_DAYWEEK = 1;
        protected static final int MAX_DAYWEEK = 7;

        protected boolean isLastDayOfMonth;
        protected boolean isLastDayOfWeek;
        protected boolean isNotSpecific;
        protected boolean isInstanceOfWeekday;
        protected int weekDay;
        protected int instanceOfMonth;
        protected int monthOffset;

        public boolean isLastDayOfMonth() {
            return isLastDayOfMonth;
        }

        public boolean isLastDayOfWeek() {
            return isLastDayOfWeek;
        }

        public boolean isInstanceOfWeekday() {
            return isInstanceOfWeekday;
        }

        public boolean isNotSpecific() {
            return isNotSpecific;
        }

        public DayOfWeekExpressionPart(String s) throws ParseException {
            super(s);

        }

        @Override
        public int order() {
            return 4;
        }

        @Override
        String getSpecialToken(String token) {
            if (token.equals("*") || token.equals("?")) {
                return token;
            }
            if (token.contains("#")) {
                return "#";
            }
            if (token.contains("-") && !token.contains("L")) {
                return "-";
            }
            if (token.contains("L")) {
                return "L";
            }
            if (token.contains("/")) {
                return "/";
            }
            return "";
        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return new BoundedIntegerSet(MIN_DAYWEEK, MAX_DAYWEEK, false, true);
        }

        protected int dayAsInteger(String dayAsString) throws ParseException {
            try {
                return WeekDay.getWeekDay(dayAsString).getCalendarDay();
            } catch (IllegalArgumentException e) {
                try {
                    return Integer.parseInt(dayAsString);
                } catch (Exception f) {
                    throw new ParseException("Invalid Day of Week value: '" + dayAsString + "'", 0);
                }
            }
        }

        @Override
        void parseToken(String v) throws ParseException {
            switch (getSpecialToken(v)) {
                case "-": {
                    String from = StringUtils.substringBefore(v, "-");
                    String to = StringUtils.substringAfter(v, "-");
                    getValueSet().add(dayAsInteger(from), dayAsInteger(to), 1);
                    break;
                }
                case "/": {
                    String from = StringUtils.substringBefore(v, "/");
                    String increment = StringUtils.substringAfter(v, "/");
                    try {
                        if (dayAsInteger(increment) > MAX_DAYWEEK) {
                            throw new ParseException("Increment is too large", 0);
                        }
                    } catch (Exception e) {
                        throw new ParseException("Increment '" + v + "' is not a valid value", 0);
                    }
                    int fromValue = from.equals("*") ? 0 : dayAsInteger(from);
                    getValueSet().add(fromValue, MAX_DAYWEEK, dayAsInteger(increment));
                    break;
                }
                case "*": {
                    getValueSet().add(MIN_DAYWEEK, MAX_DAYWEEK, 1);
                    break;
                }
                case "?": {
                    isNotSpecific = true;
                    break;
                }
                case "L": {
                    monthOffset = StringUtils.substringBefore(v, "L").equals("") ? 0
                            : Integer.parseInt(StringUtils.substringBefore(v, "L"));
                    if (monthOffset == 0) {
                        isLastDayOfWeek = true;
                    } else {
                        isLastDayOfMonth = true;
                    }
                    break;
                }
                case "#": {
                    this.weekDay = dayAsInteger(StringUtils.substringBefore(v, "#"));
                    this.instanceOfMonth = Integer.parseInt(StringUtils.substringAfter(v, "#"));
                    if (instanceOfMonth < 1 || instanceOfMonth > 5) {
                        throw new ParseException("A numeric value between 1 and 5 must follow the '#' option", 0);
                    } else {
                        isInstanceOfWeekday = true;
                    }
                    break;
                }
                default: {
                    try {
                        getValueSet().add(dayAsInteger(v));
                    } catch (Exception e) {
                        throw new ParseException("'" + v + "' is not a valid token", 0);
                    }
                    break;
                }
            }
        }

        @Override
        public ArrayList<Date> apply(Date startDate, ArrayList<Date> candidates) {

            if (!isNotSpecific) {
                final Calendar cal = Calendar.getInstance(getTimeZone());
                List<Date> oldCandidates = new ArrayList<Date>();

                List<Date> newCandidates = new ArrayList<Date>();

                if (candidates.isEmpty()) {
                    candidates.add(startDate);
                }

                oldCandidates.addAll(candidates);

                for (Date date : candidates) {
                    cal.setTime(date);
                    if (isLastDayOfMonth) {
                        cal.set(Calendar.DAY_OF_WEEK, monthOffset);
                        cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, -1);
                        newCandidates.add(cal.getTime());
                    } else

                    if (isLastDayOfWeek) {
                        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                        newCandidates.add(cal.getTime());
                    } else if (isInstanceOfWeekday) {
                        cal.set(Calendar.DAY_OF_WEEK, weekDay);
                        cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, instanceOfMonth);
                        newCandidates.add(cal.getTime());
                    } else {
                        for (Integer element : getValueSet()) {
                            cal.setTime(date);

                            cal.set(Calendar.DAY_OF_WEEK, element);
                            for (int i = 1; i <= 5; i++) {
                                cal.set(Calendar.WEEK_OF_MONTH, i);
                                newCandidates.add(cal.getTime());
                            }

                        }
                    }
                }
                candidates.removeAll(oldCandidates);

                candidates.addAll(newCandidates);
            }
            return candidates;
        }
    }

    protected class YearsExpressionPart extends CronExpressionPart {

        protected static final int MIN_YEAR = 1970;
        protected static final int MAX_YEAR = 2100;

        public YearsExpressionPart(String s) throws ParseException {
            super(s);
        }

        @Override
        public int order() {
            return 1;
        }

        @Override
        String getSpecialToken(String token) {
            if (token.equals("*")) {
                return token;
            }
            if (token.contains("-")) {
                return "-";
            }
            if (token.contains("/")) {
                return "/";
            }
            return "";
        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return new BoundedIntegerSet(MIN_YEAR, MAX_YEAR, false, false);
        }

        @Override
        void parseToken(String v) throws ParseException {
            switch (getSpecialToken(v)) {
                case "-": {
                    String from = StringUtils.substringBefore(v, "-");
                    String to = StringUtils.substringAfter(v, "-");
                    getValueSet().add(Integer.parseInt(from), Integer.parseInt(to), 1);
                    break;
                }
                case "/": {
                    String from = StringUtils.substringBefore(v, "/");
                    String increment = StringUtils.substringAfter(v, "/");
                    try {
                        if (Integer.parseInt(increment) > MAX_YEAR) {
                            throw new ParseException("Increment is too large", 0);
                        }
                    } catch (Exception e) {
                        throw new ParseException("Increment '" + v + "' is not a valid value", 0);
                    }
                    int fromValue = from.equals("*") ? 0 : Integer.parseInt(from);
                    getValueSet().add(fromValue, MAX_YEAR, Integer.parseInt(increment));
                    break;
                }
                case "*": {
                    getValueSet().add(MIN_YEAR, MAX_YEAR, 1);
                    break;
                }
                default: {
                    try {
                        getValueSet().add(Integer.parseInt(v));
                    } catch (Exception e) {
                        throw new ParseException("'" + v + "' is not a valid token", 0);
                    }
                    break;
                }
            }
        }

        @Override
        public ArrayList<Date> apply(Date startDate, ArrayList<Date> candidates) {
            final Calendar cal = Calendar.getInstance(getTimeZone());

            List<Date> newCandidates = new ArrayList<Date>();
            List<Date> oldCandidates = new ArrayList<Date>();

            if (candidates.isEmpty()) {
                candidates.add(startDate);
            }

            oldCandidates.addAll(candidates);

            for (Date date : candidates) {
                for (Integer element : getValueSet()) {
                    cal.setTime(date);
                    cal.set(Calendar.YEAR, element);
                    newCandidates.add(cal.getTime());
                }
            }
            candidates.removeAll(oldCandidates);

            candidates.addAll(newCandidates);
            return candidates;
        }
    }

    @Override
    public boolean hasFloatingStartDate() {
        return true;
    }
}
