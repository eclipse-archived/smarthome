/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.scheduler;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>AbstractExpression</code> is an abstract implementation of {@link Expression} that provides common
 * functionality to other concrete implementations of <code>Expression</code>
 *
 * @author Karel Goderis - Initial Contribution
 *
 */
public abstract class AbstractExpression<E extends AbstractExpressionPart> implements Expression {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private int minimumCandidates = 10;
    private int maximumCandidates = 100;

    private String expression;
    private String delimiters;
    private ArrayList<E> expressionParts = new ArrayList<E>();

    private boolean continueSearch;
    private ArrayList<Date> candidates = new ArrayList<Date>();
    private Date startDate = null;
    private TimeZone timeZone = null;

    /**
     * Build an {@link Expression}
     *
     * @param expression the expression
     * @param delimiters delimiters to consider when splitting the expression into expression parts
     * @param startDate the start date of the expression
     * @param timeZone the time zone of the expression
     * @param minimumCandidates the minimum number of candidate dates to calculate
     * @throws ParseException when the expression cannot be parsed correctly
     */
    public AbstractExpression(String expression, String delimiters, Date startDate, TimeZone timeZone,
            int minimumCandidates) throws ParseException {

        if (expression == null) {
            throw new IllegalArgumentException("The expression cannot be null");
        }

        this.expression = expression;
        this.delimiters = delimiters;
        this.startDate = startDate;
        this.timeZone = timeZone;
        this.minimumCandidates = minimumCandidates;

        if (startDate == null) {
            throw new IllegalArgumentException("The start date of the rule must not be null");
        }
        this.startDate = startDate;

        setTimeZone(timeZone);
        parseExpression(expression);
    }

    @Override
    public final Date getStartDate() {
        if (startDate == null) {
            startDate = Calendar.getInstance(getTimeZone()).getTime();
        }
        return startDate;
    }

    @Override
    public void setStartDate(Date startDate) throws IllegalArgumentException, ParseException {
        if (startDate == null) {
            throw new IllegalArgumentException("The start date of the rule must not be null");
        }
        this.startDate = startDate;
        parseExpression(expression);
    }

    @Override
    public TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        return timeZone;
    }

    @Override
    public final void setTimeZone(TimeZone timeZone) throws IllegalArgumentException, ParseException {
        if (timeZone == null) {
            throw new IllegalArgumentException("The time zone must not be null");
        }
        this.timeZone = timeZone;
        parseExpression(expression);
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public void setExpression(String expression) throws ParseException {
        this.expression = expression;
        parseExpression(expression);
    }

    @Override
    public String toString() {
        return expression;
    }

    /**
     * Parse the given expression
     *
     * @param expression the expression to parse
     * @throws ParseException when the expression cannot be successfully be parsed
     * @throws IllegalArgumentException when expression parts conflict with each other
     */
    public final void parseExpression(String expression) throws ParseException, IllegalArgumentException {

        StringTokenizer expressionTokenizer = new StringTokenizer(expression, delimiters, false);
        int position = 0;

        setExpressionParts(new ArrayList<E>());
        setCandidates(new ArrayList<Date>());

        while (expressionTokenizer.hasMoreTokens()) {
            String token = expressionTokenizer.nextToken().trim();
            position++;
            getExpressionParts().add(parseToken(token, position));
        }

        validateExpression();

        if (startDate == null) {
            setStartDate(Calendar.getInstance().getTime());
        }

        applyExpressionParts();

        synchronized (this) {
            continueSearch = true;
            while (getCandidates().size() < minimumCandidates && continueSearch) {
                populateWithSeeds();
                getCandidates().clear();
                applyExpressionParts();
            }
            continueSearch = false;
        }

        for (Date aDate : getCandidates()) {
            logger.trace("Final candidate {} is {}", getCandidates().indexOf(aDate), aDate);
        }
    }

    abstract protected void validateExpression() throws IllegalArgumentException;

    protected void applyExpressionParts() {
        Collections.sort(getExpressionParts());
        for (ExpressionPart part : getExpressionParts()) {
            logger.trace("Expanding {} from {} candidates", part.getClass().getSimpleName(), getCandidates().size());
            setCandidates(part.apply(startDate, getCandidates()));
            logger.trace("Expanded to {} candidates", getCandidates().size());
            for (Date aDate : getCandidates()) {
                logger.trace("Candidate {} is {}", getCandidates().indexOf(aDate), aDate);
            }
            prune();
        }
    }

    protected void prune() {
        Collections.sort(getCandidates());

        ArrayList<Date> beforeDates = new ArrayList<Date>();

        for (Date candidate : getCandidates()) {
            if (candidate.before(startDate)) {
                beforeDates.add(candidate);
            }
        }

        getCandidates().removeAll(beforeDates);

        if (getCandidates().size() > maximumCandidates) {
            logger.trace("Pruning {} candidates to {}", getCandidates().size(), maximumCandidates);
            int size = getCandidates().size();
            for (int i = maximumCandidates; i < size; i++) {
                getCandidates().remove(getCandidates().size() - 1);
            }
        }
    }

    @Override
    public Date getTimeAfter(Date afterTime) {
        if (getCandidates().isEmpty()) {
            try {
                setStartDate(afterTime);
                parseExpression(expression);
            } catch (ParseException e) {
                logger.error("An exception occurred while parsing the expression : '{}'", e.getMessage());
            }
        }

        if (!getCandidates().isEmpty()) {

            Collections.sort(getCandidates());

            for (Date candidate : getCandidates()) {
                if (candidate.after(afterTime)) {
                    return candidate;
                }
            }
        }

        return null;
    }

    @Override
    public Date getFinalFireTime() {
        if (getCandidates().isEmpty()) {
            try {
                parseExpression(getExpression());
            } catch (ParseException e) {
                logger.error("An exception occurred while parsing the expression : '{}'", e.getMessage());
            }
        }

        if (getCandidates().isEmpty()) {
            return null;
        }

        return getCandidates().get(getCandidates().size() - 1);
    }

    /**
     * Parses a token from the expression into an {@link ExpressionPart}
     */
    abstract protected E parseToken(String token, int position) throws ParseException;

    /**
     * Helper function that is called to populate the list of candidates dates in case not enough candidates were
     * generated in a first instance
     */
    abstract protected void populateWithSeeds();

    public ExpressionPart getExpressionPart(Class<?> part) {
        for (ExpressionPart aPart : getExpressionParts()) {
            if (aPart.getClass().equals(part)) {
                return aPart;
            }
        }
        return null;
    }

    protected ArrayList<Date> getCandidates() {
        return candidates;
    }

    protected void setCandidates(ArrayList<Date> candidates) {
        this.candidates = candidates;
    }

    public ArrayList<E> getExpressionParts() {
        return expressionParts;
    }

    public void setExpressionParts(ArrayList<E> expressionParts) {
        synchronized (this) {
            this.expressionParts = expressionParts;
        }
    }

}
