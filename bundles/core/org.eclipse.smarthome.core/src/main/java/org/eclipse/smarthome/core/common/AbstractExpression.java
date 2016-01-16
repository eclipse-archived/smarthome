/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.util.TreeSet;

/**
 * <code>AbstractExpression</code> is an abstract implementation of {@link Expression} that provides common
 * functionality to
 * other concrete implementations of <code>Expression</code>
 *
 * @author Karel Goderis - Initial Contribution
 */
public abstract class AbstractExpression implements Expression {

    private static final long serialVersionUID = -1517470717297248772L;

    protected static final int MIN_SECOND = 0;
    protected static final int MAX_SECOND = 59;
    protected static final int MIN_MINUTE = 0;
    protected static final int MAX_MINUTE = 59;
    protected static final int MIN_HOUR = 0;
    protected static final int MAX_HOUR = 23;
    protected static final int MIN_MONTHDAY = 1;
    protected static final int MAX_MONTHDAY = 31;
    protected static final int MIN_MONTH = 1;
    protected static final int MAX_MONTH = 12;

    protected String expression = "";
    protected Date startDate = null;
    protected TimeZone timeZone = null;

    protected class BoundedIntegerList extends TreeSet<Integer> {
        private static final long serialVersionUID = 19296179649170335L;
        protected final int max;
        protected final int min;
        protected final boolean negative;
        protected boolean is1indexed;

        /**
         * Constructor.
         *
         * @param absMax
         *            absolute max value.
         * @param absMin
         *            absolute min value.
         * @param negativeValuesAllowed
         *            True if negative values are allowed.
         */
        BoundedIntegerList(final int absMin, final int absMax, final boolean negativeValuesAllowed,
                final boolean is1indexed) {
            this.min = absMin;
            this.max = absMax;
            this.negative = negativeValuesAllowed;
            this.is1indexed = is1indexed;
        }

        /**
         * Checks if the input is in the specified range.
         *
         * @param integer
         *            The input
         */
        private void checkInput(final Integer integer) {
            if (!negative) {
                if (integer < min || integer > max) {
                    throw new IllegalArgumentException(
                            "Invalid integer value (value not in range [" + min + ", " + max + "])");

                }
            } else {
                final int abs = Math.abs(integer);
                if (abs < min || abs > max) {
                    throw new IllegalArgumentException("Invalid integer value (value not in range [" + (-max) + ", "
                            + -min + "] U [" + min + ", " + max + "])");
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean add(final Integer e) {
            checkInput(e);
            return super.add(e);
        }

        public boolean add(final Integer start, final Integer end, final Integer increment) {

            if (end != null && end > max) {
                throw new IllegalArgumentException(
                        "Invalid integer value (end not in range [" + min + ", " + max + "])");
            }

            int stopAt;
            if (end == null) {
                stopAt = this.max;
            } else {
                stopAt = end;
            }

            int startAt;
            if (start == null) {
                startAt = this.min;
            } else {
                startAt = start;
            }

            boolean overflow = false;
            if (stopAt < startAt) {
                stopAt = stopAt + this.max;
                overflow = true;
            }

            for (int i = startAt; i <= stopAt; i += increment) {
                if (!overflow) {
                    add(i);
                } else {
                    // take the modulus to get the real value
                    int i2 = i % max;

                    // 1-indexed ranges should not include 0, and should include their max
                    if (i2 == 0 && is1indexed) {
                        i2 = max;
                    }
                    add(i2);
                }
            }

            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean addAll(final Collection<? extends Integer> c) {
            for (Integer integer : c) {
                checkInput(integer);
            }
            return super.addAll(c);
        }
    }

    @Override
    public final Date getStartDate() {
        if (startDate == null) {
            startDate = Calendar.getInstance(getTimeZone()).getTime();
        }
        return startDate;
    }

    @Override
    public final void setStartDate(final Date startTime) {
        setStartDate(startTime, false);
    }

    @Override
    public void setStartDate(final Date startTime, final boolean check) {
        if (startTime == null) {
            throw new IllegalArgumentException("The start time of the rule can not be null");
        }
        if (check) {
            validateStartDate(startTime);
        }
        this.startDate = startTime;
    }

    @Override
    public TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        return timeZone;
    }

    @Override
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return expression;
    }

}
