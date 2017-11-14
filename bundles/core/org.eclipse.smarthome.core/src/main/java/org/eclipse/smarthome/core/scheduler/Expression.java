/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.scheduler;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

/**
 * <code>Expression<code> is the interface that all Expressions have to implement. Expression are typically evaluated as
 * from a given start date in a given timezone. Once set, the expression can be queried for the next date, from the
 * start date, that will match the expression
 *
 * @author Karel Goderis
 *
 */
public interface Expression {

    /**
     * Indicates whether the given date satisfies the expression. Note that
     * milliseconds are ignored, so two Dates falling on different milliseconds
     * of the same second will always have the same result here.
     *
     * @param date the date to evaluate
     * @return a boolean indicating whether the given date satisfies the expression
     */
    boolean isSatisfiedBy(Date date);

    /**
     * Returns the next date/time <I>after</I> the given date/time which
     * satisfies the expression.
     *
     * @param date the date/time at which to begin the search for the next valid date/time
     * @return the next valid date/time, or null if there is no occurrence date found.
     */
    Date getTimeAfter(Date date);

    /**
     * Returns the final time that the <code>Expression</code> will match.
     *
     * @return the last date the expression will fire, or null when the final time can not be calculated
     */
    Date getFinalFireTime();

    /**
     * Returns the time zone for which this <code>Expression</code> will be resolved.
     *
     * @return the time zone
     */
    TimeZone getTimeZone();

    /**
     * Sets the time zone for which this <code>Expression</code> will be resolved.
     *
     * @param timeZone time zone to set
     * @throws ParseException - when the expression can not be parsed correctly after setting the time zone
     * @throws IllegalArgumentException - when the supplied timeZone is null
     */
    void setTimeZone(TimeZone timeZone) throws IllegalArgumentException, ParseException;

    /**
     * Returns the expression
     *
     * @return the expression previously set
     */
    String getExpression();

    /**
     * Sets the expression
     *
     * @param expression the expression to set
     * @throws ParseException when the expression can not be parsed correctly
     */
    void setExpression(String expression) throws ParseException;

    /**
     * Returns the start date of the expression.
     *
     * @return the start date, as from which the expression will be evaluated
     */

    Date getStartDate();

    /**
     * Sets the start date of the rule
     *
     * @param startTime the start date to set
     * @throws ParseException when the expression can not be parsed correctly after the start date was set
     */
    void setStartDate(Date startTime) throws ParseException;

    /**
     * Indicates whether the expression does not need an explicit start date in order to be evaluated. 'infinite' style
     * expression types, e.g. without an explicit start date part of their definition, like cron, should return true
     *
     * @return true, if the start date does not matter for evaluating the expression
     */
    boolean hasFloatingStartDate();

}