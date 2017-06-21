/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.scheduler;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.EMPTY;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;

/**
 * Helper class to create CRON expressions from calendar or temporal instance. 
 *
 * @author Amit Kumar Mondal - Initial Contribution
 */
public final class CronHelper {

    /** CRON Constants */
    private static final String ANY = "*";
    private static final String SPACE = " ";
    private static final String EACH = "/";
    private static final String DAYS_OF_WEEK = "?";

    public static final String DAILY_MIDNIGHT = "0 0 0 * * ? *";

    /** Constructor */
    private CronHelper() {
        throw new IllegalAccessError("Non-instantiable");
    }

    /**
     * Returns CRON expression from the provided {@link Calendar} instance
     *
     * @param calendar the {@link Calendar} instance
     * @return the CRON expression
     * @throws NullPointerException
     *             if {@code calendar} is null
     */
    public static String createCronFromCalendar(Calendar calendar) {
        requireNonNull(calendar, "Calendar instance cannot be null");
        LocalDateTime temporal = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
        return createCronFromTemporal(temporal);
    }

    /**
     * Returns CRON expression from the provided {@link LocalDateTime} instance
     *
     * @param localDateTime the {@link LocalDateTime} instance
     * @return the CRON expression
     * @throws NullPointerException
     *             if {@code localDateTime} is null
     */
    public static String createCronFromTemporal(LocalDateTime localDateTime) {
        requireNonNull(localDateTime, "Temporal instance cannot be null");
        int second = localDateTime.getSecond();
        int minute = localDateTime.getMinute();
        int hour = localDateTime.getHour();
        int day = localDateTime.getDayOfMonth();
        int month = localDateTime.getMonth().getValue();
        int year = localDateTime.getYear();

        StringBuilder builder = new StringBuilder();
        builder.append(second).append(SPACE).append(minute).append(SPACE).append(hour).append(SPACE).append(day)
                .append(SPACE).append(month).append(SPACE).append(DAYS_OF_WEEK).append(SPACE).append(year);
        return builder.toString();
    }

    /**
     * Returns CRON expression that denotes the repetition every provided
     * seconds
     *
     * @param totalSecs the seconds (cannot be zero or negative or more than 86400)
     * @return the CRON expression or empty string
     * @throws IllegalArgumentException
     *             if {@code totalSecs} is zero or negative or more than 86400
     */
    public static String createCronForRepeatEverySeconds(int totalSecs) {
        if(totalSecs < 0 && totalSecs <= 86400) {
            throw new IllegalArgumentException("Seconds cannot be zero or negative or more than 86400");
        }

        StringBuilder builder = new StringBuilder();
        if (totalSecs < 60) {
            builder.append(ANY).append(EACH).append(totalSecs).append(SPACE).append(ANY).append(SPACE).append(ANY)
                    .append(SPACE).append(ANY).append(SPACE).append(ANY).append(SPACE).append(DAYS_OF_WEEK)
                    .append(SPACE).append(ANY);
            return builder.toString();
        }
        if (totalSecs >= 60 && totalSecs < 60 * 60) {
            int secs = totalSecs % 60;
            int mins = totalSecs / 60;

            builder.append(secs).append(SPACE).append(ANY).append(EACH).append(mins).append(SPACE).append(ANY)
                    .append(SPACE).append(ANY).append(SPACE).append(ANY).append(SPACE).append(DAYS_OF_WEEK)
                    .append(SPACE).append(ANY);
            return builder.toString();
        }
        if (totalSecs >= 60 * 60 && totalSecs < 60 * 60 * 24) {
            int secs = totalSecs % 60;
            int mins = totalSecs % 3600 / 60;
            int hours = totalSecs / 3600;

            builder.append(secs).append(SPACE).append(mins).append(SPACE).append(ANY).append(EACH).append(hours)
                    .append(SPACE).append(ANY).append(SPACE).append(ANY).append(SPACE).append(DAYS_OF_WEEK)
                    .append(SPACE).append(ANY);
            return builder.toString();
        }
        if (totalSecs == 60 * 60 * 24) {
            LocalDateTime now = LocalDateTime.now();
            int minute = now.getMinute();
            int hour = now.getHour();

            builder.append("0").append(SPACE).append(minute).append(SPACE).append(hour).append(SPACE).append(ANY)
                    .append(SPACE).append(ANY).append(SPACE).append(DAYS_OF_WEEK).append(SPACE).append(ANY);
            return builder.toString();
        }
        return EMPTY;
    }

}