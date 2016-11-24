/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.smarthome.core.scheduler.DateExpression.DateExpressionPart;

/**
 * <code>DateExpression</code> is an implementation of {@link Expression} that provides a parser and evaluator for for
 * ISO8601 date expressions (https://en.wikipedia.org/wiki/ISO_8601). ISO8601 expressions provide the ability to specify
 * simple yet precise dates.
 * <P>
 * ISO8601 expressions are comprised of 7 required fields described as follows:
 *
 * YYYY-MM-DDThh:mm:ss.sTZD
 *
 * where
 *
 * YYYY = four-digit year
 * MM = two-digit month (01=January, etc.)
 * DD = two-digit day of month (01 through 31)
 * hh = two digits of hour (00 through 23) (am/pm NOT allowed)
 * mm = two digits of minute (00 through 59)
 * ss = two digits of second (00 through 59)
 * s = one or more digits representing a decimal fraction of a second
 * TZD = time zone designator (Z or +hh:mm or -hh:mm)
 *
 * IMPORTANT NOTE : ISO8601 expressions that specify fractions of seconds are NOT supported
 *
 * @author Karel Goderis - Intial Contribution
 *
 */
public class DateExpression extends AbstractExpression<DateExpressionPart> {

    public DateExpression(final String date) throws ParseException {
        this(date, Calendar.getInstance().getTime(), TimeZone.getDefault());
    }

    public DateExpression(final String dateExpression, final Date startTime, final TimeZone zone)
            throws ParseException {
        super(dateExpression, "", startTime, zone, 0, 1);
    }

    @Override
    public boolean isSatisfiedBy(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        Calendar testDateCal = Calendar.getInstance(getTimeZone());
        testDateCal.setTime(date);
        testDateCal.set(Calendar.MILLISECOND, 0);
        Date originalDate = testDateCal.getTime();

        testDateCal.add(Calendar.SECOND, -1);

        Date timeAfter = getTimeAfter(testDateCal.getTime());

        return ((timeAfter != null) && (timeAfter.equals(originalDate)));
    }

    public static boolean isValidExpression(String dateExpression) {
        try {
            new DateExpression(dateExpression);
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    @Override
    protected void validateExpression() throws IllegalArgumentException {
        // Nothing to do here
    }

    @Override
    protected void populateWithSeeds() {
        // Nothing to do here
    }

    @Override
    protected DateExpressionPart parseToken(String token, int position) throws ParseException {
        return new DateExpressionPart(token);
    }

    protected class DateExpressionPart extends AbstractExpressionPart {

        private Date theDate;

        public DateExpressionPart(String s) throws ParseException {
            super(s);
        }

        public Date getDate() {
            return theDate;
        }

        @Override
        public void parse() throws ParseException {
            // Unfortunately, the time zone formats available to SimpleDateFormat (Java 6 and earlier) are not ISO 8601
            // compliant. SimpleDateFormat understands time zone strings like "GMT+01:00" or "+0100", the latter
            // according to RFC # 822.
            //
            // Even if Java 7 added support for time zone descriptors according to ISO 8601, SimpleDateFormat is still
            // not able to properly parse a complete date string, as it has no support for optional parts.
            //
            // Reformatting the input string using regexp is certainly one possibility, but the replacement rules are
            // not as simple
            //
            // Some time zones are not full hours off UTC, so the string does not necessarily end with ":00".
            // ISO8601 allows only the number of hours to be included in the time zone, so "+01" is equivalent to
            // "+01:00". ISO8601 allows the usage of "Z" to indicate UTC instead of "+00:00".
            // The easier solution is to use the data type converter in JAXB, since JAXB must be able to parse
            // ISO8601 date string according to the XML Schema specification.

            // try {
            // Calendar cal = DatatypeConverter.parseDateTime(getExpression());
            // theDate = cal.getTime();
            // } catch (Exception e) {
            // throw new ParseException(getPart() + " is not an ISO8601 formatted date", 0);
            // }

            if (!parseFormats(
                    new String[] { "yyyy-MM-dd'T'HH:mm:ssX", "yyyy-MM-dd'T'HH:mm:ssXX", "yyyy-MM-dd'T'HH:mm:ssXXX" })) {
                throw new ParseException(getPart() + " is not an ISO8601 formatted date", 0);
            }
        }

        /**
         * Try to parse using a set of valid formats.
         *
         * @param formats the date format strings in the order they should be used
         * @return true if parsing succeeded by a format, otherwise false
         */
        private boolean parseFormats(final String[] formats) {
            for (final String format : formats) {
                try {
                    final DateFormat df = new SimpleDateFormat(format);
                    df.setTimeZone(getTimeZone());
                    theDate = df.parse(getExpression());
                    return true;
                } catch (final Exception e) {
                    // Try next one...
                }
            }
            return false;
        }

        @Override
        public ArrayList<Date> apply(Date startDate, ArrayList<Date> candidates) {
            candidates.add(theDate);
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

    @Override
    public boolean hasFloatingStartDate() {
        return false;
    }

}
