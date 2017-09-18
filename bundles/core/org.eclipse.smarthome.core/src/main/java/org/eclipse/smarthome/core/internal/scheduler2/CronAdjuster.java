/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.scheduler2;

import java.time.DayOfWeek;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class creates a Temporal Adjuster that takes a temporal and adjust it to
 * the next deadline based on a cron specification.
 *
 * @See http://www.cronmaker.com/
 * @See http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/
 *      crontrigger
 * @author Peter Kriens - initial contribution and API
 */

class CronAdjuster implements TemporalAdjuster {

    static Pattern WEEKDAY_P = Pattern.compile("(?<day>\\d+|MON|TUE|WED|THU|FRI|SAT|SUN)(#(?<nr>\\d+)|(?<l>L))?",
            Pattern.CASE_INSENSITIVE);

    /*
     * A function interface that we use to check a Temporal to see if it matches
     * a part of the specification. We combine these checkers in and and ors.
     */
    interface Checker {
        boolean matches(Temporal t);
    }

    Checker ALWAYS_FALSE = (t) -> false;
    Checker ALWAYS_TRUE = (t) -> true;

    /*
     * Maintains the type and the combined checker. It can verify if a specific
     * part of the temporal is ok, and if not, it will reset it to the next
     * higher temporal with the lower fields set to their minimum value.
     */
    static class Field {
        ChronoField type;
        Checker checker;

        Temporal isOk(Temporal t) {
            if (checker.matches(t)) {
                return null;
            }

            Temporal out = t.plus(1, type.getBaseUnit());

            switch (type) {
                case YEAR:
                    out = out.with(ChronoField.MONTH_OF_YEAR, 1);

                case MONTH_OF_YEAR:
                    out = out.with(ChronoField.DAY_OF_MONTH, 1);

                case DAY_OF_WEEK:
                case DAY_OF_MONTH:
                    out = out.with(ChronoField.HOUR_OF_DAY, 0);

                case HOUR_OF_DAY:
                    out = out.with(ChronoField.MINUTE_OF_HOUR, 0);

                case MINUTE_OF_HOUR:
                    out = out.with(ChronoField.SECOND_OF_MINUTE, 0);

                case SECOND_OF_MINUTE:
                    return out;

                default:
                    throw new IllegalArgumentException("Invalid field type " + type);
            }

        }
    }

    /*
     * We adjust the given temporal with this type before we check it to prevent
     * it from keep matching the same value.
     */
    TemporalField minIncrement = ChronoField.SECOND_OF_MINUTE;

    final Field seconds;
    final Field minutes;
    final Field hours;
    final Field dayOfMonth;
    final Field month;
    final Field dayOfWeek;
    final Field year;
    final Field fields[];
    final Map<String, String> map;
    final boolean reboot;

    /*
     * Constructor
     */
    public CronAdjuster(String specification) {

        String entries[] = specification.split("(\n|\r)+");
        map = doEnv(entries);

        String expression = entries[entries.length - 1].trim();

        reboot = expression.equals("@reboot");

        if (expression.startsWith("@")) {
            expression = preDeclared(expression);
        }

        String parts[] = expression.trim().toUpperCase().split("\\s+");

        if (parts.length < 6 || parts.length > 7) {
            throw new IllegalArgumentException(
                    "Invalid cron expression, too many fields. Only 6 or 7 (with year) allowed: " + expression);
        }

        seconds = parse(parts[0], ChronoField.SECOND_OF_MINUTE);
        minutes = parse(parts[1], ChronoField.MINUTE_OF_HOUR);
        hours = parse(parts[2], ChronoField.HOUR_OF_DAY);
        dayOfMonth = parse(parts[3], ChronoField.DAY_OF_MONTH);
        month = parse(parts[4], ChronoField.MONTH_OF_YEAR, "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG",
                "SEP", "OCT", "NOV", "DEC");
        dayOfWeek = parse(parts[5], ChronoField.DAY_OF_WEEK, "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");
        if (parts.length > 6) {
            year = parse(parts[6], ChronoField.YEAR);
        } else {
            year = null;
        }

        fields = new Field[] { year, month, dayOfWeek, dayOfMonth, hours, minutes, seconds };
    }

    private Map<String, String> doEnv(String[] entries) {
        Map<String, String> map = new HashMap<String, String>();
        if (entries.length > 1) {
            for (int i = 0; i < entries.length - 1; i++) {

                if (entries[i].startsWith("#") || entries[i].isEmpty()) {
                    continue;
                }

                int n = entries[i].indexOf('=');
                if (n >= 0) {
                    String key = entries[i].substring(0, n).trim();
                    String value = entries[i].substring(n + 1).trim();
                    map.put(key, value);
                } else {
                    map.put(entries[i].trim(), Boolean.TRUE.toString());
                }
            }
            return map;
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * <pre>
     * &#64;yearly (or @annually)  Run once a year at midnight on the morning of January 1                      0 0 1 1 *
     * &#64;monthly                Run once a month at midnight on the morning of the first day of the month    0 0 1 * *
     * &#64;weekly                 Run once a week at midnight on Sunday morning                                0 0 * * 0
     * &#64;daily                  Run once a day at midnight                                                   0 0 * * *
     * &#64;hourly                 Run once an hour at the beginning of the hour                                0 * * * *
     * &#64;reboot                 Run at startup                                                               @reboot
     * </pre>
     */

    private String preDeclared(String expression) {
        switch (expression) {
            case "@annually":
            case "@yearly":
                return "0 0 0 1 1 *";

            case "@monthly":
                return "1 0 0 1 * *";

            case "@weekly":
                return "2 0 0 ? * MON";

            case "@daily":
                return "3 0 0 * * ?";

            case "@hourly":
                return "4 0 * * * ?";

            case "@reboot":
                return "0 0 0 1 1 ? 1900";

            default:
                return expression;
        }
    }

    /*
     * A cron part consists of a number of sub expressions separated by a comma.
     * We parse each sub expression and combine the results.
     */
    private Field parse(String expr, ChronoField cf, String... names) {

        //
        // Check wild card.
        //

        if ("*".equals(expr) || "?".equals(expr)) {
            return null;
        }

        Field field = new Field();
        field.type = cf;
        field.checker = ALWAYS_FALSE;

        //
        // Parse each sub expression
        //

        for (String sub : expr.split(",")) {
            Checker s = parseSub(sub, cf, names);
            field.checker = or(field.checker, s);
        }

        //
        // If this is the year check, we create a conjunction with a check
        // for the maximum year
        //

        if (cf == ChronoField.YEAR) {
            field.checker = or(CronAdjuster::checkMaxYear, field.checker);
        }

        //
        // If we have no checker, all dates are ok
        //

        if (field.checker == ALWAYS_TRUE) {
            return null;
        }

        return field;
    }

    /*
     * Parse a sub expression.
     */
    private Checker parseSub(String sub, ChronoField cf, String[] names) {

        //
        // Max and min for the current type
        //

        int min = (int) cf.range().getMinimum();
        int max = (int) cf.range().getMaximum();

        if (cf == ChronoField.DAY_OF_WEEK) {
            if ("L".equals(sub)) {
                return parseSub("SUN", cf, names);
            } else {
                Matcher m = WEEKDAY_P.matcher(sub);
                if (m.matches()) {
                    int day = parseInt(m.group("day"), min, max, names);
                    Checker c = (temporal) -> temporal.get(ChronoField.DAY_OF_WEEK) == day;

                    if (m.group("nr") != null) {
                        int n = Integer.parseInt(m.group("nr"));
                        return and(c, (temporal) -> isNthWeekDayInMonth(temporal, n));

                    } else if (m.group("l") != null) {
                        return and(c, CronAdjuster::isLastOfThisWeekDayInMonth);
                    }

                }
                // fall through, it is a normal expression
            }
        } else if (cf == ChronoField.DAY_OF_MONTH) {
            if ("L".equals(sub)) {
                return CronAdjuster::isLastDayInMonth;
            } else if ("LW".equals(sub) || "WL".equals(sub)) {
                return CronAdjuster::isLastWorkingDayInMonth;
            } else if (sub.endsWith("W")) {
                int n = Integer.parseInt(sub.substring(0, sub.length() - 1));
                return (temporal) -> isNearestWorkDay(temporal, n);
            }
            // fall through, it is a normal expression
        }

        //
        // All the shit out of the way ...
        // accept nr | range '/' nr
        //

        String[] increments = sub.split("/");

        int[] range = parseRange(increments[0], min, max, cf, names);

        if (increments.length == 2) {

            //
            // we had a / expression
            //

            int increment = Integer.parseInt(increments[1]);

            if (range[0] == range[1]) {
                range[1] = max;
            }

            return (temporal) -> {
                int n = temporal.get(cf);
                return n >= range[0] && n <= range[1] && ((n - range[0]) % increment) == 0;
            };

        }

        //
        // simple range/value check
        //

        return (temporal) -> {
            int n = temporal.get(cf);
            return n >= range[0] && n <= range[1];
        };
    }

    /*
     * This is the # syntax. We must check that the given weekday is the nth one
     * in the current month. So we take the day of the month and divide it by 7.
     */
    private static boolean isNthWeekDayInMonth(Temporal temporal, int n) {
        int day = temporal.get(ChronoField.DAY_OF_MONTH);
        int occurrences = 1 + (day - 1) / 7;

        return n == occurrences;
    }

    /*
     * Check if this is the last week day in this month. I.e. the last saturday.
     */
    private static boolean isLastOfThisWeekDayInMonth(Temporal temporal) {
        int day = temporal.get(ChronoField.DAY_OF_MONTH);
        int max = (int) ChronoField.DAY_OF_MONTH.rangeRefinedBy(temporal).getMaximum();
        return day + 7 > max;
    }

    /*
     * Check if this is the last day in the month
     */
    private static boolean isLastDayInMonth(Temporal temporal) {
        int day = temporal.get(ChronoField.DAY_OF_MONTH);
        int max = (int) ChronoField.DAY_OF_MONTH.rangeRefinedBy(temporal).getMaximum();
        return day == max;
    }

    /*
     * Check if this is the last working day in the month.
     */
    private static boolean isLastWorkingDayInMonth(Temporal temporal) {
        int day = temporal.get(ChronoField.DAY_OF_MONTH);
        DayOfWeek type = DayOfWeek.of(temporal.get(ChronoField.DAY_OF_WEEK));
        int max = (int) ChronoField.DAY_OF_MONTH.rangeRefinedBy(temporal).getMaximum();

        switch (type) {
            case MONDAY:
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
                return day == max;

            case FRIDAY:
                return day + 2 >= max;

            default:
            case SATURDAY:
            case SUNDAY:
                return false;
        }
    }

    /*
     * Check for the nearest working day. E.g. 15W is the nearest working day
     * around the 15th.
     */
    static boolean isNearestWorkDay(Temporal temporal, int target) {
        int day = temporal.get(ChronoField.DAY_OF_MONTH);
        DayOfWeek type = DayOfWeek.of(temporal.get(ChronoField.DAY_OF_WEEK));
        switch (type) {
            case MONDAY:
                return //
                day == target // the actual day
                        || day == target + 1 // target was on a sunday
                        || (day == target + 2 && day == 3) // target was
                                                           // Saturday 1
                ;

            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
                return day == target;

            case FRIDAY:
                return day == target || day + 1 == target;

            // not a work day
            default:
            case SATURDAY:
            case SUNDAY:
                return false;
        }
    }

    /*
     * A check that we do not go ballistic with the year
     */
    static boolean checkMaxYear(Temporal temporal) {
        return temporal.get(ChronoField.YEAR) >= 2200;
    }

    private int[] parseRange(String range, int min, int max, ChronoField cf, String[] names) {
        int[] r = new int[2];
        r[0] = 0;
        r[1] = max;
        if ("*".equals(range)) {
            return r;
        }

        String parts[] = range.split("-");
        r[0] = r[1] = parseInt(parts[0], min, max, names);
        if (parts.length == 2) {
            r[1] = parseInt(parts[1], min, max, names);
        }

        if (r[0] < min) {
            throw new IllegalArgumentException("Value too small: " + r[0] + " for " + cf.toString());
        }
        if (r[1] > max) {
            throw new IllegalArgumentException("Value too high: " + r[1] + " for " + cf.toString());
        }

        return r;
    }

    private int parseInt(String string, int min, int max, String[] names) {
        if (string.isEmpty()) {
            return 0;
        }

        for (int n = 0; n < names.length; n++) {
            if (names[n].equals(string)) {
                return n + min;
            }
        }

        return Integer.parseInt(string);
    }

    @Override
    public Temporal adjustInto(Temporal temporal) {

        //
        // Never match the actual time, so since our basic
        // unit is seconds, we add one second.
        //

        Temporal ret = temporal.plus(1, ChronoUnit.SECONDS);

        //
        // We loop through the fields until they all match. If
        // one of them does not match, its type is incremented
        // and all lower fields are reset to their minimum. And
        // we start over with this new time.
        //

        loop: while (true) {

            for (Field field : fields) {
                if (field != null) {
                    Temporal out = field.isOk(ret);
                    if (out != null) {
                        ret = out;
                        continue loop;
                    }
                }
            }

            //
            // All fields match!
            //

            return ret;
        }
    }

    /*
     * Helper to create an or expression of 2 checkers.
     */
    private Checker or(Checker a, Checker b) {
        if (a == ALWAYS_TRUE || b == ALWAYS_TRUE) {
            return ALWAYS_TRUE;
        }

        if (a == ALWAYS_TRUE) {
            return b;
        }

        if (b == ALWAYS_TRUE) {
            return a;
        }

        return (temporal) -> a.matches(temporal) || b.matches(temporal);
    }

    /*
     * Helper to create an and expression of 2 checkers.
     */
    private Checker and(Checker a, Checker b) {
        if (a == ALWAYS_FALSE || b == ALWAYS_FALSE) {
            return ALWAYS_FALSE;
        }

        if (a == ALWAYS_TRUE) {
            return b;
        }

        if (b == ALWAYS_TRUE) {
            return a;
        }

        return (temporal) -> a.matches(temporal) && b.matches(temporal);
    }

    public Map<String, String> getEnv() {
        return map;
    }

    public boolean isReboot() {
        return reboot;
    }

}
