/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.library.types;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.State;

/**
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Erdoan Hadzhiyusein - Refactored to use ZonedDateTime
 */
public class DateTimeType implements PrimitiveType, State, Command {

    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_PATTERN_WITH_TZ = "yyyy-MM-dd'T'HH:mm:ssz";

    // this pattern returns the time zone in RFC822 format
    public static final String DATE_PATTERN_WITH_TZ_AND_MS = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static final String DATE_PATTERN_WITH_TZ_AND_MS_GENERAL = "yyyy-MM-dd'T'HH:mm:ss.SSSz";
    public static final String DATE_PATTERN_WITH_TZ_AND_MS_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    private ZonedDateTime zonedDateTime;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private final DateTimeFormatter formatterTz = DateTimeFormatter.ofPattern(DATE_PATTERN_WITH_TZ);
    private final DateTimeFormatter formatterTzMs = DateTimeFormatter.ofPattern(DATE_PATTERN_WITH_TZ_AND_MS_GENERAL);
    private final DateTimeFormatter formatterTzMsRFC = DateTimeFormatter.ofPattern(DATE_PATTERN_WITH_TZ_AND_MS);
    private final DateTimeFormatter formatterTzMsIso = DateTimeFormatter.ofPattern(DATE_PATTERN_WITH_TZ_AND_MS_ISO);

    /**
     * @deprecated The constructor uses Calendar object hence it doesn't store time zone. A new constructor is
     *             available. Use {@link #DateTimeType(ZonedDateTime)} instead.
     * 
     * @param calendar - The Calendar object containing the time stamp.
     */
    @Deprecated
    public DateTimeType(Calendar calendar) {
        this.zonedDateTime = ZonedDateTime.ofInstant(calendar.toInstant(), TimeZone.getDefault().toZoneId())
                .withFixedOffsetZone();
    }

    public DateTimeType() {
        this(ZonedDateTime.now());
    }

    public DateTimeType(ZonedDateTime zoned) {
        this.zonedDateTime = ZonedDateTime.from(zoned).withFixedOffsetZone();
    }

    public DateTimeType(String zonedValue) {
        ZonedDateTime date = null;

        try {
            try {
                date = ZonedDateTime.parse(zonedValue, formatterTzMsRFC);
            } catch (DateTimeParseException tzMsRfcException) {
                try {
                    date = ZonedDateTime.parse(zonedValue, formatterTzMsIso);
                } catch (DateTimeParseException tzMsException) {
                    try {
                        date = ZonedDateTime.parse(zonedValue, formatterTz);
                    } catch (DateTimeParseException tzException) {
                        try {
                            date = ZonedDateTime.parse(zonedValue, formatterTzMs);
                        } catch (DateTimeParseException regularFormatException) {
                            // A ZonedDateTime object cannot be creating by parsing directly a pattern without zone
                            LocalDateTime localDateTime = LocalDateTime.parse(zonedValue, formatter);
                            date = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
                        }
                    }
                }
            }
        } catch (DateTimeParseException invalidFormatException) {
            throw new IllegalArgumentException(zonedValue + " is not in a valid format.", invalidFormatException);
        }

        if (date != null) {
            zonedDateTime = date;
        }
    }

    /**
     * @deprecated The method is deprecated. You can use {@link #getZonedDateTime()} instead.
     */
    @Deprecated
    public Calendar getCalendar() {
        return GregorianCalendar.from(zonedDateTime);
    }

    public ZonedDateTime getZonedDateTime() {
        return zonedDateTime;
    }

    public static DateTimeType valueOf(String value) {
        return new DateTimeType(value);
    }

    @Override
    public String format(String pattern) {
        try {
            return String.format(pattern, zonedDateTime);
        } catch (NullPointerException npe) {
            return DateTimeFormatter.ofPattern(DATE_PATTERN).format(zonedDateTime);
        }
    }

    public String format(Locale locale, String pattern) {
        return String.format(locale, pattern, zonedDateTime);
    }

    @Override
    public String toString() {
        return toFullString();
    }

    @Override
    public String toFullString() {
        return zonedDateTime.format(formatterTzMsRFC);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getZonedDateTime() == null) ? 0 : getZonedDateTime().hashCode());
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
        if (zonedDateTime == null) {
            if (other.zonedDateTime != null) {
                return false;
            }
        } else if (zonedDateTime.compareTo(other.zonedDateTime) != 0) {
            return false;
        }
        return true;
    }
}
