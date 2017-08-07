/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.State;

/**
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class DateTimeType implements PrimitiveType, State, Command {

    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_PATTERN_WITH_TZ = "yyyy-MM-dd'T'HH:mm:ssz";
    public static final String DATE_PATTERN_WITH_TZ_AND_MS = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String DATE_PATTERN_WITH_TZ_AND_MS_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    private Calendar calendar;

    public DateTimeType() {
        this(Calendar.getInstance());
    }

    public DateTimeType(Calendar calendar) {
        this.calendar = (Calendar) calendar.clone();
    }

    public DateTimeType(String calendarValue) {
        Date date = null;

        try {
            try {
                date = new SimpleDateFormat(DATE_PATTERN_WITH_TZ_AND_MS).parse(calendarValue);
            } catch (ParseException fpe3) {
                try {
                    date = new SimpleDateFormat(DATE_PATTERN_WITH_TZ_AND_MS_ISO).parse(calendarValue);
                } catch (ParseException fpe4) {
                    try {
                        date = new SimpleDateFormat(DATE_PATTERN_WITH_TZ).parse(calendarValue);
                    } catch (ParseException fpe2) {
                        date = new SimpleDateFormat(DATE_PATTERN).parse(calendarValue);
                    }
                }
            }
        } catch (ParseException fpe) {
            throw new IllegalArgumentException(calendarValue + " is not in a valid format.", fpe);
        }

        if (date != null) {
            calendar = Calendar.getInstance();
            calendar.setTime(date);
        }
    }

    public Calendar getCalendar() {
        return (Calendar) calendar.clone();
    }

    public static DateTimeType valueOf(String value) {
        return new DateTimeType(value);
    }

    @Override
    public String format(String pattern) {
        try {
            return String.format(pattern, calendar);
        } catch (NullPointerException npe) {
            return new SimpleDateFormat(DATE_PATTERN).format(calendar.getTime());
        }
    }

    public String format(Locale locale, String pattern) {
        return String.format(locale, pattern, calendar);
    }

    @Override
    public String toString() {
        return toFullString();
    }

    @Override
    public String toFullString() {
        return new SimpleDateFormat(DATE_PATTERN_WITH_TZ_AND_MS).format(calendar.getTime());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((calendar == null) ? 0 : calendar.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DateTimeType other = (DateTimeType) obj;
        if (calendar == null) {
            if (other.calendar != null) {
                return false;
            }
        } else if (calendar.compareTo(other.calendar) != 0) {
            return false;
        }
        return true;
    }

}
