/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.scheduler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.scheduler.RecurrenceExpression.RecurrenceExpressionPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>RecurrenceExpression</code> is an implementation of {@link Expression} that provides a parser and evaluator for
 * iCalendar recurrence rule expressions as defined in the
 * <ahref="http://tools.ietf.org/html/rfc5545#section-3.3.10">RFC 5545</a>.
 * <p>
 * Recurrence rules provide the ability to specify complex time combinations
 * based on the standard format for defining calendar and scheduling information
 * (iCalendar). For instance : "every Sunday in January at 8:30 AM and 9:30 AM,
 * every other year". More examples can be found <a
 * href="http://http://tools.ietf.org/html/rfc5545#section-3.8.5.3">here</a>.
 * <p>
 * A recurrence rule is composed of 1 required part that defines the frequency
 * and 13 optional ones separated by a semi-colon.
 * <p>
 * The recur definition of the RFC 5545 is as follows:.
 *
 * <pre>
 * <code>
 *     recur           = recur-rule-part *( ";" recur-rule-part )
 *                     ;
 *                     ; The rule parts are not ordered in any
 *                     ; particular sequence.
 *                     ;
 *                     ; The FREQ rule part is REQUIRED,
 *                     ; but MUST NOT occur more than once.
 *                     ;
 *                     ; The UNTIL or COUNT rule parts are OPTIONAL,
 *                     ; but they MUST NOT occur in the same 'recur'.
 *                     ;
 *                     ; The other rule parts are OPTIONAL,
 *                     ; but MUST NOT occur more than once.
 *
 *     recur-rule-part = ( "FREQ" "=" freq )
 *                     / ( "UNTIL" "=" enddate )
 *                     / ( "COUNT" "=" 1*DIGIT )
 *                     / ( "INTERVAL" "=" 1*DIGIT )
 *                     / ( "BYSECOND" "=" byseclist )
 *                     / ( "BYMINUTE" "=" byminlist )
 *                     / ( "BYHOUR" "=" byhrlist )
 *                     / ( "BYDAY" "=" bywdaylist )
 *                     / ( "BYMONTHDAY" "=" bymodaylist )
 *                     / ( "BYYEARDAY" "=" byyrdaylist )
 *                     / ( "BYWEEKNO" "=" bywknolist )
 *                     / ( "BYMONTH" "=" bymolist )
 *                     / ( "BYSETPOS" "=" bysplist )
 *                     / ( "WKST" "=" weekday )
 *     freq        = "SECONDLY" / "MINUTELY" / "HOURLY" / "DAILY"
 *                 / "WEEKLY" / "MONTHLY" / "YEARLY"
 *     enddate     = date / date-time
 *     byseclist   = ( seconds *("," seconds) )
 *     seconds     = 1*2DIGIT       ;0 to 60
 *     byminlist   = ( minutes *("," minutes) )
 *     minutes     = 1*2DIGIT       ;0 to 59
 *     byhrlist    = ( hour *("," hour) )
 *     hour        = 1*2DIGIT       ;0 to 23
 *     bywdaylist  = ( weekdaynum *("," weekdaynum) )
 *     weekdaynum  = [[plus / minus] ordwk] weekday
 *     plus        = "+"
 *     minus       = "-"
 *     ordwk       = 1*2DIGIT       ;1 to 53
 *     weekday     = "SU" / "MO" / "TU" / "WE" / "TH" / "FR" / "SA"
 *                 ; Corresponding to SUNDAY, MONDAY, TUESDAY,
 *                 ; WEDNESDAY, THURSDAY, FRIDAY, and SATURDAY days of the week.
 *     bymodaylist = ( monthdaynum *("," monthdaynum) )
 *     monthdaynum = [plus / minus] ordmoday
 *     ordmoday    = 1*2DIGIT       ;1 to 31
 *     byyrdaylist = ( yeardaynum *("," yeardaynum) )
 *     yeardaynum  = [plus / minus] ordyrday
 *     ordyrday    = 1*3DIGIT      ;1 to 366
 *     bywknolist  = ( weeknum *("," weeknum) )
 *     weeknum     = [plus / minus] ordwk
 *     bymolist    = ( monthnum *("," monthnum) )
 *     monthnum    = 1*2DIGIT       ;1 to 12
 *     bysplist    = ( setposday *("," setposday) )
 *     setposday   = yeardaynum
 * </code>
 * </pre>
 *
 * Description: This value type is a structured value consisting of a
 * list of one or more recurrence grammar parts. Each rule part is
 * defined by a NAME=VALUE pair. The rule parts are separated from
 * each other by the SEMICOLON character. The rule parts are not
 * ordered in any particular sequence. Individual rule parts MUST
 * only be specified once. Compliant applications MUST accept rule
 * parts ordered in any sequence, but to ensure backward
 * compatibility with applications that pre-date this revision of
 * iCalendar the FREQ rule part MUST be the first rule part specified
 * in a RECUR value.
 *
 * The FREQ rule part identifies the type of recurrence rule. This
 * rule part MUST be specified in the recurrence rule. Valid values
 * include SECONDLY, to specify repeating events based on an interval
 * of a second or more; MINUTELY, to specify repeating events based
 * on an interval of a minute or more; HOURLY, to specify repeating
 * events based on an interval of an hour or more; DAILY, to specify
 * repeating events based on an interval of a day or more; WEEKLY, to
 * specify repeating events based on an interval of a week or more;
 * MONTHLY, to specify repeating events based on an interval of a
 * month or more; and YEARLY, to specify repeating events based on an
 * interval of a year or more.
 *
 * The INTERVAL rule part contains a positive integer representing at
 * which intervals the recurrence rule repeats. The default value is
 * "1", meaning every second for a SECONDLY rule, every minute for a
 * MINUTELY rule, every hour for an HOURLY rule, every day for a
 * DAILY rule, every week for a WEEKLY rule, every month for a
 * MONTHLY rule, and every year for a YEARLY rule. For example,
 * within a DAILY rule, a value of "8" means every eight days.
 *
 * The UNTIL rule part defines a DATE or DATE-TIME value that bounds
 * the recurrence rule in an inclusive manner. If the value
 * specified by UNTIL is synchronized with the specified recurrence,
 * this DATE or DATE-TIME becomes the last instance of the
 * recurrence. The value of the UNTIL rule part MUST have the same
 * value type as the "DTSTART" property. Furthermore, if the
 * "DTSTART" property is specified as a date with local time, then
 * the UNTIL rule part MUST also be specified as a date with local
 * time. If the "DTSTART" property is specified as a date with UTC
 * time or a date with local time and time zone reference, then the
 * UNTIL rule part MUST be specified as a date with UTC time. In the
 * case of the "STANDARD" and "DAYLIGHT" sub-components the UNTIL
 * rule part MUST always be specified as a date with UTC time. If
 * specified as a DATE-TIME value, then it MUST be specified in a UTC
 * time format. If not present, and the COUNT rule part is also not
 * present, the "RRULE" is considered to repeat forever.
 *
 * The COUNT rule part defines the number of occurrences at which to
 * range-bound the recurrence. The "DTSTART" property value always
 * counts as the first occurrence.
 *
 * The BYSECOND rule part specifies a COMMA-separated list of seconds
 * within a minute. Valid values are 0 to 60. The BYMINUTE rule
 * part specifies a COMMA-separated list of minutes within an hour.
 * Valid values are 0 to 59. The BYHOUR rule part specifies a COMMA-
 * separated list of hours of the day. Valid values are 0 to 23.
 * The BYSECOND, BYMINUTE and BYHOUR rule parts MUST NOT be specified
 * when the associated "DTSTART" property has a DATE value type.
 * These rule parts MUST be ignored in RECUR value that violate the
 * above requirement (e.g., generated by applications that pre-date
 * this revision of iCalendar).
 *
 * The BYDAY rule part specifies a COMMA-separated list of days of
 * the week; SU indicates Sunday; MO indicates Monday; TU indicates
 * Tuesday; WE indicates Wednesday; TH indicates Thursday; FR
 * indicates Friday; and SA indicates Saturday.
 *
 * Each BYDAY value can also be preceded by a positive (+n) or
 * negative (-n) integer. If present, this indicates the nth
 * occurrence of a specific day within the MONTHLY or YEARLY "RRULE".
 *
 * For example, within a MONTHLY rule, +1MO (or simply 1MO)
 * represents the first Monday within the month, whereas -1MO
 * represents the last Monday of the month. The numeric value in a
 * BYDAY rule part with the FREQ rule part set to YEARLY corresponds
 * to an offset within the month when the BYMONTH rule part is
 * present, and corresponds to an offset within the year when the
 * BYWEEKNO or BYMONTH rule parts are present. If an integer
 * modifier is not present, it means all days of this type within the
 * specified frequency. For example, within a MONTHLY rule, MO
 * represents all Mondays within the month. The BYDAY rule part MUST
 * NOT be specified with a numeric value when the FREQ rule part is
 * not set to MONTHLY or YEARLY. Furthermore, the BYDAY rule part
 * MUST NOT be specified with a numeric value with the FREQ rule part
 * set to YEARLY when the BYWEEKNO rule part is specified.
 *
 * The BYMONTHDAY rule part specifies a COMMA-separated list of days
 * of the month. Valid values are 1 to 31 or -31 to -1. For
 * example, -10 represents the tenth to the last day of the month.
 * The BYMONTHDAY rule part MUST NOT be specified when the FREQ rule
 * part is set to WEEKLY.
 *
 * The BYYEARDAY rule part specifies a COMMA-separated list of days
 * of the year. Valid values are 1 to 366 or -366 to -1. For
 * example, -1 represents the last day of the year (December 31st)
 * and -306 represents the 306th to the last day of the year (March
 * 1st). The BYYEARDAY rule part MUST NOT be specified when the FREQ
 * rule part is set to DAILY, WEEKLY, or MONTHLY.
 *
 * The BYWEEKNO rule part specifies a COMMA-separated list of
 * ordinals specifying weeks of the year. Valid values are 1 to 53
 * or -53 to -1. This corresponds to weeks according to week
 * numbering as defined in [ISO.8601.2004]. A week is defined as a
 * seven day period, starting on the day of the week defined to be
 * the week start (see WKST). Week number one of the calendar year
 * is the first week that contains at least four (4) days in that
 * calendar year. This rule part MUST NOT be used when the FREQ rule
 * part is set to anything other than YEARLY. For example, 3
 * represents the third week of the year.
 *
 * Note: Assuming a Monday week start, week 53 can only occur when
 * Thursday is January 1 or if it is a leap year and Wednesday is
 * January 1.
 *
 * The BYMONTH rule part specifies a COMMA-separated list of months
 * of the year. Valid values are 1 to 12.
 *
 * The WKST rule part specifies the day on which the workweek starts.
 * Valid values are MO, TU, WE, TH, FR, SA, and SU. This is
 * significant when a WEEKLY "RRULE" has an interval greater than 1,
 * and a BYDAY rule part is specified. This is also significant when
 * in a YEARLY "RRULE" when a BYWEEKNO rule part is specified. The
 * default value is MO.
 *
 * The BYSETPOS rule part specifies a COMMA-separated list of values
 * that corresponds to the nth occurrence within the set of
 * recurrence instances specified by the rule. BYSETPOS operates on
 * a set of recurrence instances in one interval of the recurrence
 * rule. For example, in a WEEKLY rule, the interval would be one
 * week A set of recurrence instances starts at the beginning of the
 * interval defined by the FREQ rule part. Valid values are 1 to 366
 * or -366 to -1. It MUST only be used in conjunction with another
 * BYxxx rule part. For example "the last work day of the month"
 * could be represented as:
 *
 * FREQ=MONTHLY;BYDAY=MO,TU,WE,TH,FR;BYSETPOS=-1
 *
 * Each BYSETPOS value can include a positive (+n) or negative (-n)
 * integer. If present, this indicates the nth occurrence of the
 * specific occurrence within the set of occurrences specified by the
 * rule.
 *
 * Recurrence rules may generate recurrence instances with an invalid
 * date (e.g., February 30) or nonexistent local time (e.g., 1:30 AM
 * on a day where the local time is moved forward by an hour at 1:00
 * AM). Such recurrence instances MUST be ignored and MUST NOT be
 * counted as part of the recurrence set.
 *
 * Information, not contained in the rule, necessary to determine the
 * various recurrence instance start time and dates are derived from
 * the Start Time ("DTSTART") component attribute. For example,
 * "FREQ=YEARLY;BYMONTH=1" doesn't specify a specific day within the
 * month or a time. This information would be the same as what is
 * specified for "DTSTART".
 *
 * BYxxx rule parts modify the recurrence in some manner. BYxxx rule
 * parts for a period of time that is the same or greater than the
 * frequency generally reduce or limit the number of occurrences of
 * the recurrence generated. For example, "FREQ=DAILY;BYMONTH=1"
 * reduces the number of recurrence instances from all days (if
 * BYMONTH rule part is not present) to all days in January. BYxxx
 * rule parts for a period of time less than the frequency generally
 * increase or expand the number of occurrences of the recurrence.
 * For example, "FREQ=YEARLY;BYMONTH=1,2" increases the number of
 * days within the yearly recurrence set from 1 (if BYMONTH rule part
 * is not present) to 2.
 *
 * If multiple BYxxx rule parts are specified, then after evaluating
 * the specified FREQ and INTERVAL rule parts, the BYxxx rule parts
 * are applied to the current set of evaluated occurrences in the
 * following order: BYMONTH, BYWEEKNO, BYYEARDAY, BYMONTHDAY, BYDAY,
 * BYHOUR, BYMINUTE, BYSECOND and BYSETPOS; then COUNT and UNTIL are
 * evaluated.
 *
 * The table below summarizes the dependency of BYxxx rule part
 * expand or limit behavior on the FREQ rule part value.
 *
 * The term "N/A" means that the corresponding BYxxx rule part MUST
 * NOT be used with the corresponding FREQ value.
 *
 * BYDAY has some special behavior depending on the FREQ value and
 * this is described in separate notes below the table.
 *
 * +----------+--------+--------+-------+-------+------+-------+------+
 * | |SECONDLY|MINUTELY|HOURLY |DAILY |WEEKLY|MONTHLY|YEARLY|
 * +----------+--------+--------+-------+-------+------+-------+------+
 * |BYMONTH |Limit |Limit |Limit |Limit |Limit |Limit |Expand|
 * +----------+--------+--------+-------+-------+------+-------+------+
 * |BYWEEKNO |N/A |N/A |N/A |N/A |N/A |N/A |Expand|
 * +----------+--------+--------+-------+-------+------+-------+------+
 * |BYYEARDAY |Limit |Limit |Limit |N/A |N/A |N/A |Expand|
 * +----------+--------+--------+-------+-------+------+-------+------+
 * |BYMONTHDAY|Limit |Limit |Limit |Limit |N/A |Expand |Expand|
 * +----------+--------+--------+-------+-------+------+-------+------+
 * |BYDAY |Limit |Limit |Limit |Limit |Expand|Note 1 |Note 2|
 * +----------+--------+--------+-------+-------+------+-------+------+
 * |BYHOUR |Limit |Limit |Limit |Expand |Expand|Expand |Expand|
 * +----------+--------+--------+-------+-------+------+-------+------+
 * |BYMINUTE |Limit |Limit |Expand |Expand |Expand|Expand |Expand|
 * +----------+--------+--------+-------+-------+------+-------+------+
 * |BYSECOND |Limit |Expand |Expand |Expand |Expand|Expand |Expand|
 * +----------+--------+--------+-------+-------+------+-------+------+
 * |BYSETPOS |Limit |Limit |Limit |Limit |Limit |Limit |Limit |
 * +----------+--------+--------+-------+-------+------+-------+------+
 *
 * Note 1: Limit if BYMONTHDAY is present; otherwise, special expand
 * for MONTHLY.
 *
 * Note 2: Limit if BYYEARDAY or BYMONTHDAY is present; otherwise,
 * special expand for WEEKLY if BYWEEKNO present; otherwise,
 * special expand for MONTHLY if BYMONTH present; otherwise,
 * special expand for YEARLY.
 *
 * Here is an example of evaluating multiple BYxxx rule parts.
 *
 * DTSTART;TZID=America/New_York:19970105T083000
 * RRULE:FREQ=YEARLY;INTERVAL=2;BYMONTH=1;BYDAY=SU;BYHOUR=8,9;
 * BYMINUTE=30
 *
 * First, the "INTERVAL=2" would be applied to "FREQ=YEARLY" to
 * arrive at "every other year". Then, "BYMONTH=1" would be applied
 * to arrive at "every January, every other year". Then, "BYDAY=SU"
 * would be applied to arrive at "every Sunday in January, every
 * other year". Then, "BYHOUR=8,9" would be applied to arrive at
 * "every Sunday in January at 8 AM and 9 AM, every other year".
 * Then, "BYMINUTE=30" would be applied to arrive at "every Sunday in
 * January at 8:30 AM and 9:30 AM, every other year". Then, lacking
 * information from "RRULE", the second is derived from "DTSTART", to
 * end up in "every Sunday in January at 8:30:00 AM and 9:30:00 AM,
 * every other year". Similarly, if the BYMINUTE, BYHOUR, BYDAY,
 * BYMONTHDAY, or BYMONTH rule part were missing, the appropriate
 * minute, hour, day, or month would have been retrieved from the
 * "DTSTART" property.
 *
 * If the computed local start time of a recurrence instance does not
 * exist, or occurs more than once, for the specified time zone, the
 * time of the recurrence instance is interpreted in the same manner
 * as an explicit DATE-TIME value describing that date and time, as
 * specified in Section 3.3.5.
 *
 * No additional content value encoding (i.e., BACKSLASH character
 * encoding, see Section 3.3.11) is defined for this value type.
 *
 * Example: The following is a rule that specifies 10 occurrences that
 * occur every other day:
 *
 * FREQ=DAILY;COUNT=10;INTERVAL=2
 *
 *
 * @author Karel Goderis - Initial contribution
 *
 */
public class RecurrenceExpression extends AbstractExpression<RecurrenceExpressionPart> {

    private static final String FREQ = "FREQ";
    private static final String UNTIL = "UNTIL";
    private static final String COUNT = "COUNT";
    private static final String INTERVAL = "INTERVAL";
    private static final String BYSECOND = "BYSECOND";
    private static final String BYMINUTE = "BYMINUTE";
    private static final String BYHOUR = "BYHOUR";
    private static final String BYDAY = "BYDAY";
    private static final String BYMONTHDAY = "BYMONTHDAY";
    private static final String BYYEARDAY = "BYYEARDAY";
    private static final String BYWEEKNO = "BYWEEKNO";
    private static final String BYMONTH = "BYMONTH";
    private static final String BYSETPOS = "BYSETPOS";
    private static final String WKST = "WKST";

    private final Logger logger = LoggerFactory.getLogger(RecurrenceExpression.class);

    public enum Frequency {
        SECONDLY("SECONDLY", Calendar.SECOND),
        MINUTELY("MINUTELY", Calendar.MINUTE),
        HOURLY("HOURLY", Calendar.HOUR_OF_DAY),
        DAILY("DAILY", Calendar.DAY_OF_YEAR),
        WEEKLY("WEEKLY", Calendar.WEEK_OF_YEAR),
        MONTHLY("MONTHLY", Calendar.MONTH),
        YEARLY("YEARLY", Calendar.YEAR);

        private final String identifier;
        private final int calendarField;

        private Frequency(final String id, final int field) {
            this.identifier = id;
            this.calendarField = field;
        }

        public static Frequency getFrequency(final String id) {
            for (Frequency aFrequency : Frequency.values()) {
                if (aFrequency.toString().equals(id)) {
                    return aFrequency;
                }
            }
            throw new IllegalArgumentException("Invalid frequency value " + id);
        }

        public int getCalendarField() {
            return calendarField;
        }

        @Override
        public String toString() {
            return identifier;
        }
    }

    public enum WeekDay {
        SUNDAY("SU", Calendar.SUNDAY),
        MONDAY("MO", Calendar.MONDAY),
        TUESDAY("TU", Calendar.TUESDAY),
        WEDNESDAY("WE", Calendar.WEDNESDAY),
        THURSDAY("TH", Calendar.THURSDAY),
        FRIDAY("FR", Calendar.FRIDAY),
        SATURDAY("SA", Calendar.SATURDAY);

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
     * Constructs a new <CODE>RecurrenceExpression</CODE> based on the specified
     * parameter.
     *
     * @param recurrenceRule string representation of the RFC 5545 recurrence rule the new object should represent.
     * @throws ParseException if the string expression cannot be parsed into a valid <code>RecurrenceExpression</code>
     *             .
     */
    public RecurrenceExpression(final String recurrenceRule) throws ParseException {
        this(recurrenceRule, Calendar.getInstance().getTime(), TimeZone.getDefault());
    }

    /**
     * Constructs a new <CODE>RecurrenceExpression</CODE> based on the specified
     * parameter.
     *
     * @param recurrenceRule string representation of the RFC 5545 recurrence rule the new object should represent.
     * @param startTime the start time to consider for the recurrence rule.
     * @throws ParseException if the string expression cannot be parsed into a valid <code>RecurrenceExpression</code>
     *             .
     */
    public RecurrenceExpression(final String recurrenceRule, final Date startTime) throws ParseException {
        this(recurrenceRule, startTime, TimeZone.getDefault());
    }

    /**
     * Constructs a new <CODE>RecurrenceExpression</CODE> based on the specified
     * parameter.
     *
     * @param recurrenceRule string representation of the RFC 5545 recurrence rule the new object should represent.
     * @param startTime the start time to consider for the recurrence rule.
     * @param zone the timezone for which this recurrence rule will be resolved.
     * @throws ParseException if the string expression cannot be parsed into a valid <code>RecurrenceExpression</code>
     *             .
     */
    public RecurrenceExpression(final String recurrenceRule, final Date startTime, final TimeZone zone)
            throws ParseException {
        super(recurrenceRule, ";", startTime, zone, 0, 366);
    }

    @Override
    public void setStartDate(Date startDate) throws IllegalArgumentException, ParseException {
        if (startDate == null) {
            throw new IllegalArgumentException("The start date of the rule can not be null");
        }

        UntilExpressionPart until = getExpressionPart(UntilExpressionPart.class);

        if (until != null && until.getUntil().before(startDate)) {
            throw new IllegalArgumentException("Start date cannot be after until");
        }

        // We set the real start date to the next second; milliseconds are not supported by Recurrence expressions
        // anyways
        Calendar calendar = Calendar.getInstance(getTimeZone());
        calendar.setTime(startDate);
        if (calendar.get(Calendar.MILLISECOND) != 0) {
            calendar.add(Calendar.SECOND, 1);
            calendar.set(Calendar.MILLISECOND, 0);
        }
        super.setStartDate(calendar.getTime());
    }

    @Override
    public boolean isSatisfiedBy(final Date test) {

        getTimeAfter(test);

        Collections.sort(getCandidates());

        for (Date aDate : getCandidates()) {
            if (aDate.after(test)) {
                return false;
            }
            if (aDate.equals(test)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Indicates whether the specified expression can be parsed into a
     * valid <code>RecurrencExpression</code>
     *
     * @param expression the expression to evaluate
     * @return a boolean indicating whether the given expression will yield a valid <code>RecurrencExpression</code>
     */
    public static boolean isValidExpression(String expression) {

        try {
            new RecurrenceExpression(expression);
        } catch (ParseException pe) {
            return false;
        }

        return true;
    }

    @Override
    public final Date getFinalFireTime() {

        boolean isUntil = getExpressionPart(UntilExpressionPart.class) != null ? true : false;
        boolean isCount = getExpressionPart(CountExpressionPart.class) != null ? true : false;

        if (!(isUntil || isCount)) {
            return null;
        } else {
            return super.getFinalFireTime();
        }
    }

    @Override
    protected void validateExpression() throws IllegalArgumentException {

        boolean isFrequency = getExpressionPart(FrequencyExpressionPart.class) != null ? true : false;
        boolean isUntil = getExpressionPart(UntilExpressionPart.class) != null ? true : false;
        boolean isCount = getExpressionPart(CountExpressionPart.class) != null ? true : false;
        boolean isByDay = getExpressionPart(DayExpressionPart.class) != null ? true : false;
        boolean isByWeekNumber = getExpressionPart(WeekNumberExpressionPart.class) != null ? true : false;
        boolean isByMonthDay = getExpressionPart(MonthDayExpressionPart.class) != null ? true : false;
        boolean isByYearDay = getExpressionPart(YearDayExpressionPart.class) != null ? true : false;
        boolean isByPosition = getExpressionPart(PositionExpressionPart.class) != null ? true : false;
        boolean isBySecond = getExpressionPart(SecondExpressionPart.class) != null ? true : false;
        boolean isByHour = getExpressionPart(HourExpressionPart.class) != null ? true : false;
        boolean isByMinute = getExpressionPart(MinuteExpressionPart.class) != null ? true : false;
        boolean isByMonth = getExpressionPart(MonthExpressionPart.class) != null ? true : false;

        if (!isFrequency) {
            throw new IllegalArgumentException("A recurrence rule MUST contain a FREQ rule part.");
        }

        if (isUntil && isCount) {
            throw new IllegalArgumentException(
                    "The UNTIL and COUNT rule parts MUST NOT occur in the same recurrence rule.");
        }

        if (isByDay && isFrequency) {
            Frequency frequency = getExpressionPart(FrequencyExpressionPart.class).getFrequency();

            if (getExpressionPart(DayExpressionPart.class).isNumeric()
                    && (frequency == Frequency.MONTHLY || frequency == Frequency.YEARLY)) {
                throw new IllegalArgumentException("The BYDAY rule part MUST NOT be specified with a numeric value "
                        + "when the FREQ rule part is not set to MONTHLY or YEARLY.");
            }
        }

        if (isByDay && isFrequency && isByWeekNumber) {
            Frequency frequency = getExpressionPart(FrequencyExpressionPart.class).getFrequency();

            if (getExpressionPart(DayExpressionPart.class).isNumeric() && frequency == Frequency.YEARLY) {
                throw new IllegalArgumentException(
                        "The BYDAY rule part MUST NOT be specified with a numeric value with the FREQ rule part set to YEARLY when the BYWEEKNO rule part is specified.");
            }
        }

        if (isByMonthDay && isFrequency) {
            Frequency frequency = getExpressionPart(FrequencyExpressionPart.class).getFrequency();

            if (frequency == Frequency.WEEKLY) {
                throw new IllegalArgumentException(
                        "The BYMONTHDAY rule part MUST NOT be specified when the FREQ rule part is set to WEEKLY.");
            }
        }

        if (isByYearDay && isFrequency) {
            Frequency frequency = getExpressionPart(FrequencyExpressionPart.class).getFrequency();

            if (frequency == Frequency.WEEKLY || frequency == Frequency.DAILY || frequency == Frequency.MONTHLY) {
                throw new IllegalArgumentException(
                        "The BYYEARDAY rule part MUST NOT be specified when the FREQ rule part is set to DAILY, WEEKLY, or MONTHLY.");
            }
        }

        if (isByWeekNumber && isFrequency) {
            Frequency frequency = getExpressionPart(FrequencyExpressionPart.class).getFrequency();

            if (frequency != Frequency.YEARLY) {
                throw new IllegalArgumentException(
                        "The BYWEEKNO rule part MUST NOT be used when the FREQ rule part is set to anything other than YEARLY.");
            }
        }

        if (isByPosition && !(isByDay || isByHour || isByMinute || isByMonth || isByMonthDay || isBySecond
                || isByWeekNumber || isByYearDay)) {
            throw new IllegalArgumentException(
                    "The BYSETPOS rule part MUST only be used in conjunction with another BYxxx rule part.");
        }

    }

    @Override
    protected void populateWithSeeds() {
        // Nothing to do here, as the mandatory FREQ part of the Recurrence Rule is a defacto source of seeds
    }

    @Override
    protected void pruneFarthest() {
        Collections.sort(getCandidates());

        List<Date> beforeDates = new ArrayList<>();

        for (Date candidate : getCandidates()) {
            if (candidate.before(getStartDate())) {
                beforeDates.add(candidate);
            }
        }

        getCandidates().removeAll(beforeDates);
    }

    @Override
    protected RecurrenceExpressionPart parseToken(String token, int position) throws ParseException {
        String key = StringUtils.substringBefore(token, "=");
        String value = StringUtils.substringAfter(token, "=");
        switch (key) {
            case FREQ: {
                return new FrequencyExpressionPart(value);
            }
            case INTERVAL: {
                return new IntervalExpressionPart(value);
            }
            case BYSECOND: {
                return new SecondExpressionPart(value);
            }
            case BYMINUTE: {
                return new MinuteExpressionPart(value);
            }
            case BYHOUR: {
                return new HourExpressionPart(value);
            }
            case BYDAY: {
                return new DayExpressionPart(value);
            }
            case BYMONTHDAY: {
                return new MonthDayExpressionPart(value);
            }
            case BYMONTH: {
                return new MonthExpressionPart(value);
            }
            case BYYEARDAY: {
                return new YearDayExpressionPart(value);
            }
            case BYWEEKNO: {
                return new WeekNumberExpressionPart(value);
            }
            case BYSETPOS: {
                return new PositionExpressionPart(value);
            }
            case WKST: {
                return new WeekStartExpressionPart(value);
            }
            case UNTIL: {
                return new UntilExpressionPart(value);
            }
            case COUNT: {
                return new CountExpressionPart(value);
            }
            default:
                throw new IllegalArgumentException("Unknown expression part");
        }

    }

    protected abstract class RecurrenceExpressionPart extends AbstractExpressionPart {

        public RecurrenceExpressionPart(String s) throws ParseException {
            super(s);
        }
    }

    protected abstract class IntegerListRecurrenceExpressionPart extends RecurrenceExpressionPart {

        public IntegerListRecurrenceExpressionPart(String s) throws ParseException {
            super(s);
        }

        @Override
        public void parse() throws ParseException {
            setValueSet(initializeValueSet());
            StringTokenizer valueTokenizer = new StringTokenizer(getPart(), ",");
            while (valueTokenizer.hasMoreTokens()) {
                String v = valueTokenizer.nextToken();
                try {
                    try {
                        getValueSet().add(Integer.parseInt(v));
                    } catch (NumberFormatException e) {
                        throw new ParseException("Invalid integer value : " + v, 0);
                    }
                    getValueSet().add(Integer.parseInt(v));
                } catch (NumberFormatException e) {
                    throw new ParseException("Invalid integer value : " + v, 0);
                }
            }
        }
    }

    protected abstract class DayListRecurrenceExpressionPart extends RecurrenceExpressionPart {

        protected HashMap<WeekDay, Integer> dayList;

        public DayListRecurrenceExpressionPart(String s) throws ParseException {
            super(s);
        }

        @Override
        public void parse() throws ParseException {
            dayList = new HashMap<WeekDay, Integer>();
            StringTokenizer valueTokenizer = new StringTokenizer(getPart(), ",");
            while (valueTokenizer.hasMoreTokens()) {
                String v = valueTokenizer.nextToken();
                try {
                    try {
                        WeekDay day = WeekDay.getWeekDay(v);
                        dayList.put(day, 0);
                    } catch (IllegalArgumentException e) {
                        String dayname = StringUtils.right(v, 2);
                        String occurrence = StringUtils.left(v, v.length() - 2);
                        if (occurrence.length() == 0) {
                            dayList.put(WeekDay.getWeekDay(dayname), 0);
                        } else {
                            dayList.put(WeekDay.getWeekDay(dayname), Integer.parseInt(occurrence));
                        }
                    }
                } catch (Exception f) {
                    throw new ParseException("Invalid day/occurence value : " + v, 0);
                }
            }
        }

        public boolean isNumeric() {

            if (dayList != null) {
                for (Integer number : dayList.values()) {
                    if (number != 0) {
                        return true;
                    }
                }
            }

            return false;

        }

    }

    public class FrequencyExpressionPart extends RecurrenceExpressionPart {

        protected static final int SEEDS = 100;

        public FrequencyExpressionPart(String s) throws ParseException {
            super(s);
        }

        protected Frequency frequency;

        public Frequency getFrequency() {
            return frequency;
        }

        @Override
        public void parse() throws ParseException {
            frequency = Frequency.getFrequency(getPart());
        }

        @Override
        public List<Date> apply(Date startDate, List<Date> candidates) {

            IntervalExpressionPart intervalPart = getExpressionPart(IntervalExpressionPart.class);

            Calendar cal = Calendar.getInstance(getTimeZone());
            cal.setLenient(false);
            cal.setTime(getStartDate());

            List<Date> ret = new ArrayList<Date>();
            List<Date> newCandidates = new ArrayList<>();
            newCandidates.add(cal.getTime());

            int interval = intervalPart != null ? intervalPart.getInterval() : 1;

            for (int i = 1; i < SEEDS; i++) {
                cal.add(frequency.getCalendarField(), interval);
                newCandidates.add(cal.getTime());
            }

            ret.addAll(newCandidates);
            return ret;
        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return null;
        }

        @Override
        public int order() {
            return 3;
        }

    }

    public class IntervalExpressionPart extends RecurrenceExpressionPart {

        public IntervalExpressionPart(String s) throws ParseException {
            super(s);
        }

        int interval;

        public int getInterval() {
            return interval;
        }

        @Override
        public void parse() throws ParseException {
            try {
                interval = Integer.parseInt(getPart());
                if (interval <= 0) {
                    throw new IllegalArgumentException("Inteval must be a postive integer");
                }
            } catch (NumberFormatException pe) {
                throw new ParseException("Invalid integer value for INTERVAL : " + getPart(), 0);
            }
        }

        @Override
        public List<Date> apply(Date startDate, List<Date> candidates) {
            return candidates;
        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return null;
        }

        @Override
        public int order() {
            return 1;
        }

    }

    public class WeekStartExpressionPart extends RecurrenceExpressionPart {

        public WeekStartExpressionPart(String s) throws ParseException {
            super(s);
        }

        WeekDay weekStart;

        public WeekDay getWeekStart() {
            return weekStart;
        }

        @Override
        public void parse() throws ParseException {
            try {
                weekStart = WeekDay.getWeekDay(getPart());
            } catch (Exception f) {
                throw new ParseException("Invalid value for WKST: " + getPart(), 0);
            }
        }

        @Override
        public List<Date> apply(Date startDate, List<Date> candidates) {
            return candidates;
        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return null;
        }

        @Override
        public int order() {
            return 2;
        }
    }

    public class MonthExpressionPart extends IntegerListRecurrenceExpressionPart {

        protected static final int MIN_MONTH = 1;
        protected static final int MAX_MONTH = 12;

        public MonthExpressionPart(String s) throws ParseException {
            super(s);
        }

        @Override
        public List<Date> apply(Date startDate, List<Date> candidates) {

            FrequencyExpressionPart freqpart = getExpressionPart(FrequencyExpressionPart.class);
            Frequency frequency = freqpart.getFrequency();

            if (frequency == Frequency.YEARLY) {
                List<Date> newCandidates = new ArrayList<>();
                List<Date> oldCandidates = candidates;
                final Calendar cal = Calendar.getInstance(getTimeZone());

                for (Date date : candidates) {
                    cal.setTime(date);
                    for (Integer element : getValueSet()) {
                        cal.roll(Calendar.MONTH, (element - 1) - cal.get(Calendar.MONTH));
                        newCandidates.add(cal.getTime());
                    }
                }
                candidates.removeAll(oldCandidates);
                candidates.addAll(newCandidates);
            } else {
                List<Date> pruneCandidates = new ArrayList<>();
                final Calendar cal = Calendar.getInstance(getTimeZone());

                for (Date aDate : candidates) {
                    cal.setTime(aDate);
                    if (!getValueSet().contains(cal.get(Calendar.MONTH) + (getValueSet().is1indexed ? 1 : 0))) {
                        pruneCandidates.add(aDate);
                    }
                }
                candidates.removeAll(pruneCandidates);
            }
            return candidates;
        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return new BoundedIntegerSet(MIN_MONTH, MAX_MONTH, false, true);
        }

        @Override
        public int order() {
            return 4;
        }
    }

    public class WeekNumberExpressionPart extends IntegerListRecurrenceExpressionPart {

        private static final int MIN_WEEKNO = 1;
        private static final int MAX_WEEKNO = 53;

        public WeekNumberExpressionPart(String s) throws ParseException {
            super(s);
        }

        @Override
        public List<Date> apply(Date startDate, List<Date> candidates) {

            FrequencyExpressionPart freqpart = getExpressionPart(FrequencyExpressionPart.class);
            Frequency frequency = freqpart.getFrequency();

            WeekStartExpressionPart weekStartPart = getExpressionPart(WeekStartExpressionPart.class);
            WeekDay weekStart = weekStartPart != null ? weekStartPart.getWeekStart() : WeekDay.MONDAY;

            if (frequency == Frequency.YEARLY) {
                List<Date> newCandidates = new ArrayList<>();
                List<Date> oldCandidates = candidates;

                final Calendar cal = Calendar.getInstance(getTimeZone());
                cal.setFirstDayOfWeek(weekStart.getCalendarDay());

                for (Date date : candidates) {
                    for (Integer element : getValueSet()) {
                        cal.setTime(date);
                        if (element > 0 && element <= cal.getMaximum(Calendar.WEEK_OF_YEAR)) {
                            cal.set(Calendar.WEEK_OF_YEAR, element);
                            newCandidates.add(cal.getTime());
                        } else {
                            if (cal.getMaximum(MAX_WEEKNO) + element + 1 > 0) {
                                cal.set(Calendar.WEEK_OF_YEAR, cal.getMaximum(MAX_WEEKNO) + element + 1);
                                newCandidates.add(cal.getTime());
                            }
                        }
                    }
                }
                candidates.removeAll(oldCandidates);
                candidates.addAll(newCandidates);
            } else {
                logger.warn("BYWEEKNO can only be used together with YEARLY");
            }

            return candidates;
        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return new BoundedIntegerSet(MIN_WEEKNO, MAX_WEEKNO, true, true);
        }

        @Override
        public int order() {
            return 5;
        }
    }

    public class YearDayExpressionPart extends IntegerListRecurrenceExpressionPart {

        private static final int MIN_YEARDAY = 1;
        private static final int MAX_YEARDAY = 366;

        public YearDayExpressionPart(String s) throws ParseException {
            super(s);
        }

        @Override
        public List<Date> apply(Date startDate, List<Date> candidates) {

            FrequencyExpressionPart freqpart = getExpressionPart(FrequencyExpressionPart.class);
            Frequency frequency = freqpart.getFrequency();

            if (frequency == Frequency.YEARLY) {
                List<Date> newCandidates = new ArrayList<>();
                List<Date> oldCandidates = candidates;

                final Calendar cal = Calendar.getInstance(getTimeZone());

                for (Date date : candidates) {
                    for (Integer element : getValueSet()) {
                        cal.setTime(date);
                        if (element > 0 && element <= cal.getMaximum(Calendar.DAY_OF_YEAR)) {
                            cal.set(Calendar.DAY_OF_YEAR, element);
                            newCandidates.add(cal.getTime());
                        } else {
                            if (cal.getMaximum(Calendar.DAY_OF_YEAR) + element + 1 > 0) {
                                cal.set(Calendar.DAY_OF_YEAR, cal.getMaximum(Calendar.DAY_OF_YEAR) + element + 1);
                                newCandidates.add(cal.getTime());
                            }
                        }
                    }
                }
                candidates.removeAll(oldCandidates);
                candidates.addAll(newCandidates);
            } else if (frequency == Frequency.SECONDLY || frequency == Frequency.MINUTELY
                    || frequency == Frequency.HOURLY) {
                List<Date> pruneCandidates = new ArrayList<>();
                final Calendar cal = Calendar.getInstance(getTimeZone());

                for (Date aDate : candidates) {
                    cal.setTime(aDate);
                    if (!getValueSet().contains(cal.get(Calendar.DAY_OF_YEAR))) {
                        pruneCandidates.add(aDate);
                    }
                }
                candidates.removeAll(pruneCandidates);
            }

            return candidates;
        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return new BoundedIntegerSet(MIN_YEARDAY, MAX_YEARDAY, true, true);
        }

        @Override
        public int order() {
            return 6;
        }
    }

    public class MonthDayExpressionPart extends IntegerListRecurrenceExpressionPart {

        protected static final int MIN_MONTHDAY = 1;
        protected static final int MAX_MONTHDAY = 31;

        public MonthDayExpressionPart(String s) throws ParseException {
            super(s);
        }

        @Override
        public List<Date> apply(Date startDate, List<Date> candidates) {

            FrequencyExpressionPart freqpart = getExpressionPart(FrequencyExpressionPart.class);
            Frequency frequency = freqpart.getFrequency();

            if (frequency == Frequency.YEARLY || frequency == Frequency.MONTHLY) {
                List<Date> newCandidates = new ArrayList<>();
                List<Date> oldCandidates = candidates;

                final Calendar cal = Calendar.getInstance(getTimeZone());

                for (Date date : candidates) {
                    for (Integer element : getValueSet()) {
                        cal.setTime(date);
                        if (element > 0 && element <= cal.getMaximum(Calendar.DAY_OF_MONTH)) {
                            cal.set(Calendar.DAY_OF_MONTH, element);
                            newCandidates.add(cal.getTime());
                        } else {
                            if (cal.getMaximum(Calendar.DAY_OF_MONTH) + element + 1 > 0) {
                                cal.set(Calendar.DAY_OF_MONTH, cal.getMaximum(Calendar.DAY_OF_MONTH) + element + 1);
                                newCandidates.add(cal.getTime());
                            }
                        }
                    }
                }
                candidates.removeAll(oldCandidates);
                candidates.addAll(newCandidates);
            } else if (frequency == Frequency.SECONDLY || frequency == Frequency.MINUTELY
                    || frequency == Frequency.HOURLY || frequency == Frequency.DAILY) {
                List<Date> pruneCandidates = new ArrayList<>();
                final Calendar cal = Calendar.getInstance(getTimeZone());

                for (Date aDate : candidates) {
                    cal.setTime(aDate);
                    if (!getValueSet().contains(cal.get(Calendar.DAY_OF_MONTH))) {
                        pruneCandidates.add(aDate);
                    }
                }
                candidates.removeAll(pruneCandidates);
            }

            return candidates;

        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return new BoundedIntegerSet(MIN_MONTHDAY, MAX_MONTHDAY, true, true);
        }

        @Override
        public int order() {
            return 7;
        }
    }

    public class DayExpressionPart extends DayListRecurrenceExpressionPart {

        protected static final int MIN_MONTHDAY = 1;
        protected static final int MAX_MONTHDAY = 31;

        public DayExpressionPart(String s) throws ParseException {
            super(s);
        }

        @Override
        public List<Date> apply(Date startDate, List<Date> candidates) {

            FrequencyExpressionPart freqpart = getExpressionPart(FrequencyExpressionPart.class);
            Frequency frequency = freqpart.getFrequency();

            WeekStartExpressionPart weekStartPart = getExpressionPart(WeekStartExpressionPart.class);
            WeekDay weekStart = weekStartPart != null ? weekStartPart.getWeekStart() : WeekDay.MONDAY;

            boolean isByYearDay = getExpressionPart(YearDayExpressionPart.class) != null ? true : false;

            boolean isByMonthDay = getExpressionPart(MonthDayExpressionPart.class) != null ? true : false;

            boolean isByWeekNumber = getExpressionPart(WeekNumberExpressionPart.class) != null ? true : false;

            boolean isByMonth = getExpressionPart(MonthExpressionPart.class) != null ? true : false;

            if (frequency == Frequency.YEARLY) {
                if (isByYearDay || isByMonthDay) {

                    List<Date> pruneCandidates = new ArrayList<>();
                    final Calendar cal = Calendar.getInstance(getTimeZone());

                    for (Date aDate : candidates) {
                        cal.setTime(aDate);
                        if (!dayList.keySet().contains(WeekDay.getWeekDay(cal.get(Calendar.DAY_OF_WEEK) - 1))) {
                            pruneCandidates.add(aDate);
                        }
                    }
                    candidates.removeAll(pruneCandidates);
                } else if (isByWeekNumber) {

                    List<Date> newCandidates = new ArrayList<>();
                    List<Date> oldCandidates = candidates;

                    final Calendar cal = Calendar.getInstance(getTimeZone());

                    for (Date date : candidates) {
                        for (WeekDay aDay : dayList.keySet()) {
                            cal.setTime(date);
                            cal.setFirstDayOfWeek(weekStart.getCalendarDay());
                            cal.set(Calendar.DAY_OF_WEEK, aDay.getCalendarDay());
                            newCandidates.add(cal.getTime());
                        }
                    }
                    candidates.removeAll(oldCandidates);
                    candidates.addAll(newCandidates);
                } else if (isByMonth) {

                    List<Date> newCandidates = new ArrayList<>();
                    List<Date> oldCandidates = candidates;

                    final Calendar cal = Calendar.getInstance(getTimeZone());

                    for (Date date : candidates) {
                        for (WeekDay aDay : dayList.keySet()) {
                            cal.setTime(date);

                            cal.set(Calendar.DAY_OF_MONTH, 1);
                            List<Date> datesInMonth = new ArrayList<>();
                            int setMonth = cal.get(Calendar.MONTH);
                            while (cal.get(Calendar.DAY_OF_MONTH) <= cal.getMaximum(Calendar.DAY_OF_MONTH)
                                    && cal.get(Calendar.MONTH) == setMonth) {
                                if (cal.get(Calendar.DAY_OF_WEEK) == aDay.getCalendarDay()) {
                                    datesInMonth.add(cal.getTime());
                                }
                                cal.add(Calendar.DAY_OF_YEAR, 1);
                            }

                            cal.setTime(date);
                            logger.debug("date is {}, weekday is {}, offset is {}, datesInMonth {}",
                                    new Object[] { date, aDay, dayList.get(aDay), datesInMonth.size() });
                            if (dayList.get(aDay) > 0 && dayList.get(aDay) <= datesInMonth.size()) {
                                newCandidates.add(datesInMonth.get(dayList.get(aDay) - 1));
                            } else if (dayList.get(aDay) < 0 && dayList.get(aDay) >= -datesInMonth.size()) {
                                logger.debug("Adding new candidate {}",
                                        datesInMonth.get(datesInMonth.size() + dayList.get(aDay)));
                                newCandidates.add(datesInMonth.get(datesInMonth.size() + dayList.get(aDay)));
                            } else if (dayList.get(aDay) == 0) {
                                newCandidates.addAll(datesInMonth);
                            }
                        }
                    }
                    candidates.removeAll(oldCandidates);
                    candidates.addAll(newCandidates);
                } else {

                    List<Date> newCandidates = new ArrayList<>();
                    List<Date> oldCandidates = candidates;

                    final Calendar cal = Calendar.getInstance(getTimeZone());

                    for (Date date : candidates) {
                        for (WeekDay aDay : dayList.keySet()) {
                            cal.setTime(date);

                            cal.set(Calendar.MONTH, 0);
                            cal.set(Calendar.DAY_OF_MONTH, 1);
                            int setYear = cal.get(Calendar.YEAR);
                            List<Date> datesInYear = new ArrayList<>();
                            while (cal.get(Calendar.DAY_OF_YEAR) <= cal.getMaximum(Calendar.DAY_OF_YEAR)
                                    && cal.get(Calendar.YEAR) == setYear) {
                                if (cal.get(Calendar.DAY_OF_WEEK) == aDay.getCalendarDay()) {
                                    datesInYear.add(cal.getTime());
                                }
                                cal.add(Calendar.DAY_OF_YEAR, 1);
                            }

                            cal.setTime(date);
                            if (dayList.get(aDay) > 0 && dayList.get(aDay) <= datesInYear.size()) {
                                newCandidates.add(datesInYear.get(dayList.get(aDay) - 1));
                            } else if (dayList.get(aDay) < 0 && dayList.get(aDay) >= -datesInYear.size()) {
                                newCandidates.add(datesInYear.get(datesInYear.size() + dayList.get(aDay)));
                            } else if (dayList.get(aDay) == 0) {
                                newCandidates.addAll(datesInYear);
                            }
                        }
                    }
                    candidates.removeAll(oldCandidates);
                    candidates.addAll(newCandidates);
                }
            } else if (frequency == Frequency.MONTHLY) {
                if (!isByMonthDay) {
                    List<Date> newCandidates = new ArrayList<>();
                    List<Date> oldCandidates = candidates;

                    final Calendar cal = Calendar.getInstance(getTimeZone());

                    for (Date date : candidates) {
                        for (WeekDay aDay : dayList.keySet()) {
                            cal.setTime(date);

                            cal.set(Calendar.DAY_OF_MONTH, 1);
                            List<Date> datesInMonth = new ArrayList<>();
                            int setMonth = cal.get(Calendar.MONTH);
                            while (cal.get(Calendar.DAY_OF_MONTH) <= cal.getMaximum(Calendar.DAY_OF_MONTH)
                                    && cal.get(Calendar.MONTH) == setMonth) {
                                if (cal.get(Calendar.DAY_OF_WEEK) == aDay.getCalendarDay()) {
                                    datesInMonth.add(cal.getTime());
                                }
                                cal.add(Calendar.DAY_OF_YEAR, 1);
                            }

                            cal.setTime(date);
                            if (dayList.get(aDay) > 0 && dayList.get(aDay) <= datesInMonth.size()) {
                                newCandidates.add(datesInMonth.get(dayList.get(aDay) - 1));
                            } else if (dayList.get(aDay) < 0 && dayList.get(aDay) >= -datesInMonth.size()) {
                                newCandidates.add(datesInMonth.get(datesInMonth.size() + dayList.get(aDay)));
                            } else if (dayList.get(aDay) == 0) {
                                newCandidates.addAll(datesInMonth);
                            }
                        }
                    }
                    candidates.removeAll(oldCandidates);
                    candidates.addAll(newCandidates);
                } else {

                    List<Date> pruneCandidates = new ArrayList<>();
                    final Calendar cal = Calendar.getInstance(getTimeZone());

                    for (Date aDate : candidates) {
                        cal.setTime(aDate);
                        if (!dayList.keySet().contains(WeekDay.getWeekDay(cal.get(Calendar.DAY_OF_WEEK) - 1))) {
                            pruneCandidates.add(aDate);
                        }
                    }
                    candidates.removeAll(pruneCandidates);
                }
            } else if (frequency == Frequency.WEEKLY) {

                List<Date> newCandidates = new ArrayList<>();
                List<Date> oldCandidates = candidates;

                final Calendar cal = Calendar.getInstance(getTimeZone());

                for (Date date : candidates) {
                    for (WeekDay aDay : dayList.keySet()) {
                        cal.setTime(date);
                        cal.setFirstDayOfWeek(weekStart.getCalendarDay());
                        cal.set(Calendar.DAY_OF_WEEK, aDay.getCalendarDay());
                        newCandidates.add(cal.getTime());
                    }
                }
                candidates.removeAll(oldCandidates);
                candidates.addAll(newCandidates);
            } else {

                List<Date> pruneCandidates = new ArrayList<>();
                final Calendar cal = Calendar.getInstance(getTimeZone());

                for (Date aDate : candidates) {
                    cal.setTime(aDate);
                    if (!dayList.keySet().contains(WeekDay.getWeekDay(cal.get(Calendar.DAY_OF_WEEK) - 1))) {
                        pruneCandidates.add(aDate);
                    }
                }
                candidates.removeAll(pruneCandidates);
            }

            return candidates;
        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return new BoundedIntegerSet(MIN_MONTHDAY, MAX_MONTHDAY, true, true);
        }

        @Override
        public int order() {
            return 8;
        }
    }

    public class HourExpressionPart extends IntegerListRecurrenceExpressionPart {

        protected static final int MIN_HOUR = 0;
        protected static final int MAX_HOUR = 23;

        public HourExpressionPart(String s) throws ParseException {
            super(s);
        }

        @Override
        public List<Date> apply(Date startDate, List<Date> candidates) {

            FrequencyExpressionPart freqpart = getExpressionPart(FrequencyExpressionPart.class);
            Frequency frequency = freqpart.getFrequency();

            if (frequency == Frequency.YEARLY || frequency == Frequency.MONTHLY || frequency == Frequency.WEEKLY
                    || frequency == Frequency.DAILY) {
                List<Date> newCandidates = new ArrayList<>();
                List<Date> oldCandidates = candidates;

                final Calendar cal = Calendar.getInstance(getTimeZone());

                for (Date date : candidates) {
                    cal.setTime(date);
                    for (Integer element : getValueSet()) {
                        cal.set(Calendar.HOUR_OF_DAY, element);
                        newCandidates.add(cal.getTime());
                    }
                }
                candidates.removeAll(oldCandidates);
                candidates.addAll(newCandidates);
            } else {
                List<Date> pruneCandidates = new ArrayList<>();
                final Calendar cal = Calendar.getInstance(getTimeZone());

                for (Date aDate : candidates) {
                    cal.setTime(aDate);
                    if (!getValueSet().contains(cal.get(Calendar.HOUR_OF_DAY))) {
                        pruneCandidates.add(aDate);
                    }
                }
                candidates.removeAll(pruneCandidates);
            }
            return candidates;

        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return new BoundedIntegerSet(MIN_HOUR, MAX_HOUR, false, false);
        }

        @Override
        public int order() {
            return 9;
        }
    }

    public class MinuteExpressionPart extends IntegerListRecurrenceExpressionPart {

        protected static final int MIN_MINUTE = 0;
        protected static final int MAX_MINUTE = 59;

        public MinuteExpressionPart(String s) throws ParseException {
            super(s);
        }

        @Override
        public List<Date> apply(Date startDate, List<Date> candidates) {

            FrequencyExpressionPart freqpart = getExpressionPart(FrequencyExpressionPart.class);
            Frequency frequency = freqpart.getFrequency();

            if (frequency == Frequency.YEARLY || frequency == Frequency.MONTHLY || frequency == Frequency.WEEKLY
                    || frequency == Frequency.DAILY || frequency == Frequency.HOURLY) {
                List<Date> newCandidates = new ArrayList<>();
                List<Date> oldCandidates = candidates;

                final Calendar cal = Calendar.getInstance(getTimeZone());

                for (Date date : candidates) {
                    cal.setTime(date);
                    for (Integer element : getValueSet()) {
                        cal.set(Calendar.MINUTE, element);
                        newCandidates.add(cal.getTime());
                    }
                }
                candidates.removeAll(oldCandidates);
                candidates.addAll(newCandidates);
            } else {
                List<Date> pruneCandidates = new ArrayList<>();
                final Calendar cal = Calendar.getInstance(getTimeZone());

                for (Date aDate : candidates) {
                    cal.setTime(aDate);
                    if (!getValueSet().contains(cal.get(Calendar.MINUTE))) {
                        pruneCandidates.add(aDate);
                    }
                }
                candidates.removeAll(pruneCandidates);
            }
            return candidates;

        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return new BoundedIntegerSet(MIN_MINUTE, MAX_MINUTE, false, true);
        }

        @Override
        public int order() {
            return 10;
        }
    }

    public class SecondExpressionPart extends IntegerListRecurrenceExpressionPart {

        protected static final int MIN_SECOND = 0;
        protected static final int MAX_SECOND = 59;

        public SecondExpressionPart(String s) throws ParseException {
            super(s);
        }

        @Override
        public List<Date> apply(Date startDate, List<Date> candidates) {

            FrequencyExpressionPart freqpart = getExpressionPart(FrequencyExpressionPart.class);
            Frequency frequency = freqpart.getFrequency();

            if (frequency == Frequency.YEARLY || frequency == Frequency.MONTHLY || frequency == Frequency.WEEKLY
                    || frequency == Frequency.DAILY || frequency == Frequency.HOURLY
                    || frequency == Frequency.MINUTELY) {
                List<Date> newCandidates = new ArrayList<>();
                List<Date> oldCandidates = candidates;

                final Calendar cal = Calendar.getInstance(getTimeZone());

                for (Date date : candidates) {
                    cal.setTime(date);
                    for (Integer element : getValueSet()) {
                        cal.set(Calendar.SECOND, element);
                        newCandidates.add(cal.getTime());
                    }
                }
                candidates.removeAll(oldCandidates);
                candidates.addAll(newCandidates);
            } else {
                List<Date> pruneCandidates = new ArrayList<>();
                final Calendar cal = Calendar.getInstance(getTimeZone());

                for (Date aDate : candidates) {
                    cal.setTime(aDate);
                    if (!getValueSet().contains(cal.get(Calendar.SECOND))) {
                        pruneCandidates.add(aDate);
                    }
                }
                candidates.removeAll(pruneCandidates);
            }
            return candidates;

        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return new BoundedIntegerSet(MIN_SECOND, MAX_SECOND, false, false);
        }

        @Override
        public int order() {
            return 11;
        }
    }

    public class PositionExpressionPart extends IntegerListRecurrenceExpressionPart {

        private static final int MIN_SETPOS = 1;
        private static final int MAX_SETPOS = 366;

        public PositionExpressionPart(String s) throws ParseException {
            super(s);
        }

        @SuppressWarnings("null")
        @Override
        public List<Date> apply(Date startDate, List<Date> candidates) {

            List<Date> selectCandidates = new ArrayList<>();

            FrequencyExpressionPart freqpart = getExpressionPart(FrequencyExpressionPart.class);
            Frequency frequency = freqpart.getFrequency();

            Collections.sort(candidates);

            List<List<Date>> segments = new ArrayList<>();
            List<Date> segment = null;
            Calendar segmentStart = null;
            boolean segmentEnded = false;

            for (Date aDate : candidates) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(aDate);
                if (segmentStart == null || segmentEnded) {
                    if (segmentEnded) {
                        segmentEnded = false;
                        segments.add(segment);
                        segment = new ArrayList<>();
                        segment.add(segmentStart.getTime());
                    } else {
                        segmentStart = cal;
                        segment = new ArrayList<>();
                        segment.add(aDate);
                    }
                } else {
                    switch (frequency) {
                        case DAILY:
                            if (segmentStart.get(Calendar.DAY_OF_WEEK) == cal.get(Calendar.DAY_OF_WEEK)) {
                                segment.add(aDate);
                            } else {
                                segmentEnded = true;
                                segmentStart = cal;
                            }
                            break;
                        case HOURLY:
                            if (segmentStart.get(Calendar.HOUR) == cal.get(Calendar.HOUR)) {
                                segment.add(aDate);
                            } else {
                                segmentEnded = true;
                                segmentStart = cal;
                            }
                            break;
                        case MINUTELY:
                            if (segmentStart.get(Calendar.MINUTE) == cal.get(Calendar.MINUTE)) {
                                segment.add(aDate);
                            } else {
                                segmentEnded = true;
                                segmentStart = cal;
                            }
                            break;
                        case MONTHLY:
                            if (segmentStart.get(Calendar.MONTH) == cal.get(Calendar.MONTH)) {
                                segment.add(aDate);
                            } else {
                                segmentEnded = true;
                                segmentStart = cal;
                            }
                            break;
                        case SECONDLY:
                            if (segmentStart.get(Calendar.SECOND) == cal.get(Calendar.SECOND)) {
                                segment.add(aDate);
                            } else {
                                segmentEnded = true;
                                segmentStart = cal;
                            }
                            break;
                        case WEEKLY:
                            if (segmentStart.get(Calendar.WEEK_OF_YEAR) == cal.get(Calendar.WEEK_OF_YEAR)) {
                                segment.add(aDate);
                            } else {
                                segmentEnded = true;
                                segmentStart = cal;
                            }
                            break;
                        case YEARLY:
                            if (segmentStart.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                                segment.add(aDate);
                            } else {
                                segmentEnded = true;
                                segmentStart = cal;
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            segments.add(segment);

            for (List<Date> sublist : segments) {
                for (Integer position : getValueSet()) {
                    if (position > 0 && position <= sublist.size()) {
                        selectCandidates.add(sublist.get(position - 1));
                    } else if (position < 0 && position >= -sublist.size()) {
                        selectCandidates.add(sublist.get(sublist.size() + position));
                    }
                }
            }
            return selectCandidates;
        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return new BoundedIntegerSet(MIN_SETPOS, MAX_SETPOS, true, true);
        }

        @Override
        public int order() {
            return 12;
        }
    }

    public class CountExpressionPart extends RecurrenceExpressionPart {

        public CountExpressionPart(String s) throws ParseException {
            super(s);
        }

        int count;

        public int getCount() {
            return count;
        }

        @Override
        public void parse() throws ParseException {
            try {
                count = Integer.parseInt(getPart());
                if (count <= 0) {
                    throw new IllegalArgumentException("Count must be a postive integer");
                }
            } catch (NumberFormatException pe) {
                throw new ParseException("Invalid integer value for COUNT : " + getPart(), 0);
            }
        }

        @Override
        public List<Date> apply(Date startDate, List<Date> candidates) {

            List<Date> countedCandidates = new ArrayList<>();

            Collections.sort(candidates);

            int maxToCount = Math.min(candidates.size(), count);

            for (int i = 0; i < maxToCount; i++) {
                countedCandidates.add(candidates.get(i));
            }

            return countedCandidates;
        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return null;
        }

        @Override
        public int order() {
            return 13;
        }
    }

    public class UntilExpressionPart extends RecurrenceExpressionPart {

        private static final String LOCALTIME_FORMAT = "yyyyMMdd'T'HHmmss";
        private static final String UTCTIME_FORMAT = "yyyyMMdd'T'HHmmss'Z'";
        private static final String DEFAULT_FORMAT = "yyyyMMdd";

        public UntilExpressionPart(String s) throws ParseException {
            super(s);
        }

        Date until;

        public Date getUntil() {
            return until;
        }

        @Override
        public void parse() throws ParseException {

            DateFormat format = null;

            if (getPart().contains("T")) {
                if (getPart().contains("Z")) {
                    format = new SimpleDateFormat(UTCTIME_FORMAT);
                    format.setLenient(false);
                    format.setTimeZone(TimeZone.getTimeZone("GMT"));
                } else {
                    format = new SimpleDateFormat(LOCALTIME_FORMAT);
                    format.setLenient(false);
                    format.setTimeZone(getTimeZone());
                }
            } else {
                format = new SimpleDateFormat(DEFAULT_FORMAT);
                format.setLenient(false);
                format.setTimeZone(getTimeZone());
            }

            try {
                until = format.parse(getPart());
            } catch (Exception e) {
                throw new ParseException("Invalid date format for UNTIL : " + getPart(), 0);
            }
        }

        @Override
        public List<Date> apply(Date startDate, List<Date> candidates) {

            List<Date> filtered = new ArrayList<>();

            Collections.sort(candidates);

            for (Date aDate : candidates) {
                if (aDate.before(until) || aDate.equals(until)) {
                    filtered.add(aDate);
                }
            }

            return filtered;

        }

        @Override
        BoundedIntegerSet initializeValueSet() {
            return null;
        }

        @Override
        public int order() {
            return 13;
        }
    }

    @Override
    public boolean hasFloatingStartDate() {
        return false;
    }

}
