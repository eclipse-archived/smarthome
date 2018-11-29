package org.eclipse.smarthome.model.script.actions;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This class implements some of the methods of org.joda.time.DateTime that
 * we lost in the move to java.time.ZonedDateTime.
 *
 * @author Jon Evans - Initial contribution
 *
 */
public class JodaTimeCompatibilityExtensions {

    /**
     * Returns a copy of the ZonedDateTime plus the specified number of millis.
     *
     * @param zdt the ZonedDateTime
     * @param millis the number of milliseconds to add
     * @deprecated use {@link ZonedDateTime#plus(long, ChronoUnit.MILLIS)}
     */
    @Deprecated
    public static ZonedDateTime plusMillis(ZonedDateTime zdt, int millis) {
        return zdt.plus(millis, ChronoUnit.MILLIS);
    }

    /**
     * Returns a copy of the ZonedDateTime minus the specified number of millis.
     *
     * @param zdt the ZonedDateTime
     * @param millis the number of milliseconds to subtract
     * @deprecated use {@link ZonedDateTime#minus(long, ChronoUnit.MILLIS)}
     */
    @Deprecated
    public static ZonedDateTime minusMillis(ZonedDateTime zdt, int millis) {
        return zdt.minus(millis, ChronoUnit.MILLIS);
    }

    // ==== int -> long compatibility methods

    /**
     * Returns a copy of the ZonedDateTime plus the specified number of years.
     *
     * @param zdt the ZonedDateTime
     * @param years the number of years to add
     * @deprecated use {@link ZonedDateTime#plusYears(long)}
     */
    @Deprecated
    public static ZonedDateTime plusYears(ZonedDateTime zdt, int years) {
        return zdt.plusYears(years);
    }

    /**
     * Returns a copy of the ZonedDateTime plus the specified number of months.
     *
     * @param zdt the ZonedDateTime
     * @param months the number of months to add
     * @deprecated use {@link ZonedDateTime#plusMonths(long)}
     */
    @Deprecated
    public static ZonedDateTime plusMonths(ZonedDateTime zdt, int months) {
        return zdt.plusMonths(months);
    }

    /**
     * Returns a copy of the ZonedDateTime plus the specified number of weeks.
     *
     * @param zdt the ZonedDateTime
     * @param weeks the number of weeks to add
     * @deprecated use {@link ZonedDateTime#plusWeeks(long)}
     */
    @Deprecated
    public static ZonedDateTime plusWeeks(ZonedDateTime zdt, int weeks) {
        return zdt.plusWeeks(weeks);
    }

    /**
     * Returns a copy of the ZonedDateTime plus the specified number of days.
     *
     * @param zdt the ZonedDateTime
     * @param days the number of days to add
     * @deprecated use {@link ZonedDateTime#plusDays(long)}
     */
    @Deprecated
    public static ZonedDateTime plusDays(ZonedDateTime zdt, int days) {
        return zdt.plusDays(days);
    }

    /**
     * Returns a copy of the ZonedDateTime plus the specified number of hours.
     *
     * @param zdt the ZonedDateTime
     * @param hours the number of hours to add
     * @deprecated use {@link ZonedDateTime#plusHours(long)}
     */
    @Deprecated
    public static ZonedDateTime plusHours(ZonedDateTime zdt, int hours) {
        return zdt.plusHours(hours);
    }

    /**
     * Returns a copy of the ZonedDateTime plus the specified number of minutes.
     *
     * @param zdt the ZonedDateTime
     * @param minutes the number of minutes to add
     * @deprecated use {@link ZonedDateTime#plusMinutes(long)}
     */
    @Deprecated
    public static ZonedDateTime plusMinutes(ZonedDateTime zdt, int minutes) {
        return zdt.plusMinutes(minutes);
    }

    /**
     * Returns a copy of the ZonedDateTime plus the specified number of seconds.
     *
     * @param zdt the ZonedDateTime
     * @param seconds the number of seconds to add
     * @deprecated use {@link ZonedDateTime#plusSeconds(long)}
     */
    @Deprecated
    public static ZonedDateTime plusSeconds(ZonedDateTime zdt, int seconds) {
        return zdt.plusSeconds(seconds);
    }

    /**
     * Returns a copy of the ZonedDateTime minus the specified number of years.
     *
     * @param zdt the ZonedDateTime
     * @param years the number of years to subtract
     * @deprecated use {@link ZonedDateTime#minusYears(long)}
     */
    @Deprecated
    public static ZonedDateTime minusYears(ZonedDateTime zdt, int years) {
        return zdt.minusYears(years);
    }

    /**
     * Returns a copy of the ZonedDateTime minus the specified number of months.
     *
     * @param zdt the ZonedDateTime
     * @param months the number of months to subtract
     * @deprecated use {@link ZonedDateTime#minusMonths(long)}
     */
    @Deprecated
    public static ZonedDateTime minusMonths(ZonedDateTime zdt, int months) {
        return zdt.minusMonths(months);
    }

    /**
     * Returns a copy of the ZonedDateTime minus the specified number of weeks.
     *
     * @param zdt the ZonedDateTime
     * @param weeks the number of weeks to subtract
     * @deprecated use {@link ZonedDateTime#minusWeeks(long)}
     */
    @Deprecated
    public static ZonedDateTime minusWeeks(ZonedDateTime zdt, int weeks) {
        return zdt.minusWeeks(weeks);
    }

    /**
     * Returns a copy of the ZonedDateTime minus the specified number of days.
     *
     * @param zdt the ZonedDateTime
     * @param days the number of days to subtract
     * @deprecated use {@link ZonedDateTime#minusDays(long)}
     */
    @Deprecated
    public static ZonedDateTime minusDays(ZonedDateTime zdt, int days) {
        return zdt.minusDays(days);
    }

    /**
     * Returns a copy of the ZonedDateTime minus the specified number of hours.
     *
     * @param zdt the ZonedDateTime
     * @param hours the number of hours to subtract
     * @deprecated use {@link ZonedDateTime#minusHours(long)}
     */
    @Deprecated
    public static ZonedDateTime minusHours(ZonedDateTime zdt, int hours) {
        return zdt.minusHours(hours);
    }

    /**
     * Returns a copy of the ZonedDateTime minus the specified number of minutes.
     *
     * @param zdt the ZonedDateTime
     * @param minutes the number of minutes to subtract
     * @deprecated use {@link ZonedDateTime#minusMinutes(long)}
     */
    @Deprecated
    public static ZonedDateTime minusMinutes(ZonedDateTime zdt, int minutes) {
        return zdt.minusMinutes(minutes);
    }

    /**
     * Returns a copy of the ZonedDateTime minus the specified number of seconds.
     *
     * @param zdt the ZonedDateTime
     * @param seconds the number of seconds to subtract
     * @deprecated use {@link ZonedDateTime#minusSeconds(long)}
     */
    @Deprecated
    public static ZonedDateTime minusSeconds(ZonedDateTime zdt, int seconds) {
        return zdt.minusSeconds(seconds);
    }

    // ===== get<Field>Of<Period>() methods

    /**
     * Get the century of era
     *
     * @param zdt the ZonedDateTime
     * @deprecated use <code>{@link ZonedDateTime#get(ChronoField.YEAR_OF_ERA)} / 100</code>.
     */
    @Deprecated
    public static int getCenturyOfEra(ZonedDateTime zdt) {
        return zdt.get(ChronoField.YEAR_OF_ERA) / 100;
    }

    /**
     * Get the year of the century
     *
     * @param zdt the ZonedDateTime
     * @deprecated use <code>{@link ZonedDateTime#get(ChronoField.YEAR_OF_ERA))} % 100</code>
     */
    @Deprecated
    public static long getYearOfCentury(ZonedDateTime zdt) {
        return zdt.get(ChronoField.YEAR_OF_ERA) % 100;
    }

    /**
     * Get the hour of day
     *
     * @param zdt the ZonedDateTime
     * @deprecated use {@link ZonedDateTime#getHour()}
     */
    @Deprecated
    public static int getHourOfDay(ZonedDateTime zdt) {
        return zdt.getHour();
    }

    /**
     * Get the minute of day
     *
     * @param zdt the ZonedDateTime
     * @deprecated use <code>ChronoUnit.MINUTES.between(zdt.toLocalDate().atStartOfDay(zdt.getZone()), zdt)</code>
     */
    @Deprecated
    public static long getMinuteOfDay(ZonedDateTime zdt) {
        return ChronoUnit.MINUTES.between(zdt.toLocalDate().atStartOfDay(zdt.getZone()), zdt);
    }

    /**
     * Get the second of day
     *
     * @param zdt the ZonedDateTime
     * @deprecated use <code>ChronoUnit.SECONDS.between(zdt.toLocalDate().atStartOfDay(zdt.getZone()), zdt)</code>
     */
    @Deprecated
    public static long getSecondOfDay(ZonedDateTime zdt) {
        return ChronoUnit.SECONDS.between(zdt.toLocalDate().atStartOfDay(zdt.getZone()), zdt);
    }

    /**
     * Get the millisecond of day
     *
     * @param zdt the ZonedDateTime
     * @deprecated use <code>ChronoUnit.MILLIS.between(zdt.toLocalDate().atStartOfDay(zdt.getZone()), zdt)</code>
     */
    @Deprecated
    public static long getMillisOfDay(ZonedDateTime zdt) {
        return ChronoUnit.MILLIS.between(zdt.toLocalDate().atStartOfDay(zdt.getZone()), zdt);
    }

    /**
     * Get the minute of hour
     *
     * @param zdt the ZonedDateTime
     * @deprecated use {@link ZonedDateTime#getMinute()}
     */
    @Deprecated
    public static int getMinuteOfHour(ZonedDateTime zdt) {
        return zdt.getMinute();
    }

    /**
     * Get the second of minute
     *
     * @param zdt the ZonedDateTime
     * @deprecated use {@link ZonedDateTime#getSecond()}
     */
    @Deprecated
    public static long getSecondOfMinute(ZonedDateTime zdt) {
        return zdt.getSecond();
    }

    /**
     * Get the milliseconds of second
     *
     * @param zdt the ZonedDateTime
     * @deprecated use {@link ZonedDateTime#getLong(ChronoField.MILLI_OF_SECOND)}
     */
    @Deprecated
    public static long getMillisOfSecond(ZonedDateTime zdt) {
        return zdt.getLong(ChronoField.MILLI_OF_SECOND);
    }

    /**
     * Get the month of year
     *
     * @param zdt the ZonedDateTime
     * @deprecated use {@link ZonedDateTime#getMonthValue()}
     */
    @Deprecated
    public static long getMonthOfYear(ZonedDateTime zdt) {
        return zdt.getMonthValue();
    }

    /**
     * Get the year of era
     *
     * @param zdt the ZonedDateTime
     * @deprecated use {@link ZonedDateTime#get(ChronoField.YEAR_OF_ERA)}
     */
    @Deprecated
    public static long getYearOfEra(ZonedDateTime zdt) {
        return zdt.get(ChronoField.YEAR_OF_ERA);
    }

    /**
     * Get the era
     *
     * @param zdt the ZonedDateTime
     * @deprecated use {@link ZonedDateTime#get(ChronoField.ERA)}
     */
    @Deprecated
    public static long getEra(ZonedDateTime zdt) {
        return zdt.get(ChronoField.ERA);
    }

    /**
     * Get the week of weekyear
     *
     * @param zdt the ZonedDateTime
     * @deprecated use {@link ZonedDateTime#get(WeekFields.ISO.weekOfWeekBasedYear())}
     */
    @Deprecated
    public static long getWeekOfWeekyear(ZonedDateTime zdt) {
        return zdt.get(WeekFields.ISO.weekOfWeekBasedYear());
    }

    /**
     * Get the number of milliseconds since the epoch
     *
     * @return the value as milliseconds
     * @deprecated use {@link ZonedDateTime#toInstant().toEpochMilli()}
     */
    @Deprecated
    public static long getMillis(ZonedDateTime zdt) {
        return zdt.toInstant().toEpochMilli();
    }

    /**
     * Get the weekyear
     *
     * @param zdt the ZonedDateTime
     * @return the weekyear
     * @deprecated use {@link ZonedDateTime#get(WeekFields.ISO.weekBasedYear())}
     */
    @Deprecated
    public static long getWeekyear(ZonedDateTime zdt) {
        return zdt.get(WeekFields.ISO.weekBasedYear());
    }

    // ==== with methods

    /**
     * Returns a copy of this ZonedDateTime with the given weekyear
     *
     * @param zdt the ZonedDateTime
     * @param weekOfWeekyear the week of weekyear
     * @return a ZonedDateTime
     * @deprecated use {@link ZonedDateTime#with(WeekFields.ISO.weekOfWeekBasedYear(), long)}
     */
    @Deprecated
    public static ZonedDateTime withWeekOfWeekyear(ZonedDateTime zdt, int weekOfWeekyear) {
        return zdt.with(WeekFields.ISO.weekOfWeekBasedYear(), weekOfWeekyear);
    }

    /**
     * Returns a copy of this ZonedDateTime with the time set to the start of the day
     *
     * @param zdt the ZonedDateTime
     * @return a ZonedDateTime
     * @deprecated use <code>toInstant().truncatedTo(ChronoUnit.DAYS).atZone(zdt.getZone())</code>
     */
    @Deprecated
    public static ZonedDateTime withTimeAtStartOfDay(ZonedDateTime zdt) {
        return zdt.toInstant().truncatedTo(ChronoUnit.DAYS).atZone(zdt.getZone());
    }

    /**
     * Returns a copy of this ZonedDateTime with the given era
     *
     * @param zdt the ZonedDateTime
     * @param era the era
     * @return a ZonedDateTime
     * @deprecated use {@link ZonedDateTime#with(ChronoField.ERA, long)}
     */
    @Deprecated
    public static ZonedDateTime withEra(ZonedDateTime zdt, int era) {
        return zdt.with(ChronoField.ERA, era);
    }

    /**
     * Returns a copy of this ZonedDateTime with the given day of week
     *
     * @param zdt the ZonedDateTime
     * @param dayOfWeek the dayOfWeek
     * @return a ZonedDateTime
     * @deprecated use {@link ZonedDateTime#with(ChronoField.DAY_OF_WEEK, long)}
     */
    @Deprecated
    public static ZonedDateTime withDayOfWeek(ZonedDateTime zdt, int dayOfWeek) {
        return zdt.with(ChronoField.DAY_OF_WEEK, dayOfWeek);
    }

    // ==== comparison methods

    /**
     * Is the ZonedDateTime after the given millisecond instant
     *
     * @param zdt the ZonedDateTime
     * @param instant the instant
     * @return true if this time interval is after the instant
     * @deprecated use <code>ZonedDateTime#toInstant().toEpochMilli() > instant</code>
     */
    @Deprecated
    public static boolean isAfter(ZonedDateTime zdt, long instant) {
        return zdt.toInstant().toEpochMilli() > instant;
    }

    /**
     * Is the ZonedDateTime before the given millisecond instant
     *
     * @param zdt the ZonedDateTime
     * @param instant the instant
     * @return true if this time interval is before the instant
     * @deprecated use <code>ZonedDateTime#toInstant().toEpochMilli() < instant</code>
     */
    @Deprecated
    public static boolean isBefore(ZonedDateTime zdt, long instant) {
        return zdt.toInstant().toEpochMilli() < instant;
    }

    /**
     * Is the ZonedDateTime after now
     *
     * @param zdt the ZonedDateTime
     * @return true if this time interval is after now
     * @deprecated use <code>ZonedDateTime#isAfter(ZonedDateTime.now())</code>
     */
    @Deprecated
    public static boolean isAfterNow(ZonedDateTime zdt) {
        return zdt.isAfter(ZonedDateTime.now());
    }

    /**
     * Is the ZonedDateTime before now
     *
     * @param zdt the ZonedDateTime
     * @return true if this time interval is before now
     * @deprecated use <code>ZonedDateTime#isBefore(ZonedDateTime.now())</code>
     */
    @Deprecated
    public static boolean isBeforeNow(ZonedDateTime zdt) {
        return zdt.isBefore(ZonedDateTime.now());
    }

    /**
     * Is the ZonedDateTime equal to now
     *
     * @param zdt the ZonedDateTime
     * @return true if this time interval is equal to now
     * @deprecated use <code>ZonedDateTime.now().equals(zdt)</code>
     */
    @Deprecated
    public static boolean isEqualNow(ZonedDateTime zdt) {
        return ZonedDateTime.now().equals(zdt);
    }

    // ==== conversion methods

    /**
     * Return a Date object representing the ZonedDateTime
     *
     * @param zdt the ZonedDateTime
     * @deprecated use <code>Date.from(zdt.toInstant())</code>
     */
    @Deprecated
    public static Date toDate(ZonedDateTime zdt) {
        return Date.from(zdt.toInstant());
    }

    /**
     * Return a GregorianCalendar object representing the ZonedDateTime
     *
     * @param zdt the ZonedDateTime
     * @deprecated use <code>GregorianCalendar.from(zdt)</code>
     */
    @Deprecated
    public static GregorianCalendar toGregorianCalendar(ZonedDateTime zdt) {
        return GregorianCalendar.from(zdt);
    }

    /**
     * Return a Calendar object representing the ZonedDateTime
     *
     * @param zdt the ZonedDateTime
     * @param locale the Locale
     * @deprecated use <code>GregorianCalendar.from(zdt)</code>
     */
    @Deprecated
    public static Calendar toCalendar(ZonedDateTime zdt, Locale locale) {
        return new Calendar.Builder().setLocale(locale).setTimeZone(TimeZone.getTimeZone(zdt.getZone()))
                .setInstant(zdt.toInstant().toEpochMilli()).build();
    }

}
