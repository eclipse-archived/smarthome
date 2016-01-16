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
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author Karel Goderis - Intial Contribution
 *
 */
public class DateExpression extends AbstractExpression {

    private final static Logger logger = LoggerFactory.getLogger(DateExpression.class);

    public DateExpression(final String date) throws ParseException {
        this(date, Calendar.getInstance().getTime(), TimeZone.getDefault());
    }

    private DateExpression(final String dateExpression, final Date startTime, final TimeZone zone)
            throws ParseException {
        if (dateExpression == null) {
            throw new IllegalArgumentException("date cannot be null");
        }
        buildExpression(dateExpression);
        setTimeZone(zone);
    }

    public DateExpression(Expression expression) {
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

    protected void buildExpression(final String expression) throws ParseException {

        // Unfortunately, the time zone formats available to SimpleDateFormat (Java 6 and earlier) are not ISO 8601
        // compliant. SimpleDateFormat understands time zone strings like "GMT+01:00" or "+0100", the latter according
        // to RFC # 822.
        //
        // Even if Java 7 added support for time zone descriptors according to ISO 8601, SimpleDateFormat is still not
        // able to properly parse a complete date string, as it has no support for optional parts.
        //
        // Reformatting the input string using regexp is certainly one possibility, but the replacement rules are not
        // as simple
        //
        // Some time zones are not full hours off UTC, so the string does not necessarily end with ":00".
        // ISO8601 allows only the number of hours to be included in the time zone, so "+01" is equivalent to "+01:00"
        // ISO8601 allows the usage of "Z" to indicate UTC instead of "+00:00".
        // The easier solution is possibly to use the data type converter in JAXB, since JAXB must be able to parse
        // ISO8601 date string according to the XML Schema specification.

        try {
            Calendar cal = DatatypeConverter.parseDateTime(expression);
            startDate = cal.getTime();
        } catch (Exception e) {
            logger.warn("{} is not an ISO8601 formatted date", expression);
            startDate = null;
        }
    }

    @Override
    public boolean isSatisfiedBy(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return date.equals(startDate);
    }

    public static boolean isValidExpression(String dateExpression) {
        try {
            new DateExpression(dateExpression);
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    public static void validateExpression(String dateExpression) throws ParseException {
        new DateExpression(dateExpression);
    }

    @Override
    public void validateStartDate(Date startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
    }

    @Override
    public Date getTimeAfter(Date afterTime) {
        if (afterTime == null) {
            throw new IllegalArgumentException("After time cannot be null");
        }
        try {
            if (afterTime.before(startDate)) {
                return startDate;
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public Date getFinalFireTime() {
        return startDate;
    }

    @Override
    public String getExpression() {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(timeZone);
        c.setTime(startDate);
        expression = javax.xml.bind.DatatypeConverter.printDateTime(c);
        return expression;
    }

    @Override
    public String toString() {
        return getExpression();
    }

}
