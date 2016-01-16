package org.eclipse.smarthome.core.common;

import java.util.Date;
import java.util.TimeZone;

public interface Expression {

    /**
     * Indicates whether the given date satisfies the expression. Note that
     * milliseconds are ignored, so two Dates falling on different milliseconds
     * of the same second will always have the same result here.
     *
     * @param date the date to evaluate
     * @return a boolean indicating whether the given date satisfies the
     *         expression
     */
    boolean isSatisfiedBy(Date date);

    /**
     * Returns the next date/time <I>after</I> the given date/time which
     * satisfies the expression.
     *
     * @param date the date/time at which to begin the search for the next valid
     *            date/time
     * @return the next valid date/time
     */
    /**
     * Returns the occurrence of the recurrence rule just after a date or null
     * if there is no occurrence date found.
     *
     * @param afterTime
     *            the reference date that is just before the occurrence to
     *            return.
     * @return the date of the occurrence just after the date in the recurrence
     *         set .
     */
    Date getTimeAfter(Date afterTime);

    /**
     * NOT YET IMPLEMENTED: Returns the final time that the
     * <code>Expression</code> will match.
     */
    Date getFinalFireTime();

    /**
     * Sets the time zone for which this <code>Expression</code>
     * will be resolved.
     */
    void setTimeZone(TimeZone timeZone);

    /**
     * Returns the time zone for which this <code>Expression</code>
     * will be resolved.
     */
    TimeZone getTimeZone();

    String getExpression();

    /**
     * Sets the start date of the recurrence rule.
     *
     * @param startTime
     *            the startDate to set
     * @param check
     *            True if the start date will be verified against the
     *            rule.
     */
    void setStartDate(final Date startTime, final boolean check);

    /**
     * Sets the start date of the rule.
     *
     * @param startTime
     *            the startDate to set
     */
    void setStartDate(final Date startTime);

    /**
     * Checks if the start date is synchronized with the rule. The
     * method throws an IllegalArgumentException if the start date is not
     * synchronized with the rule.
     *
     * @param startTime
     *            The start date to check
     */
    void validateStartDate(final Date startTime);

    /**
     * Returns the start date of the recurrence rule.
     *
     * @return the startDate The start date.
     */
    Date getStartDate();

}