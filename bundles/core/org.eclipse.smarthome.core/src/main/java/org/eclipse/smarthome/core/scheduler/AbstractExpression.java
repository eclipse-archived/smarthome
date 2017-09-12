/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
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
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNull;
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

    private int minimumCandidates = 1;
    private int maximumCandidates = 100;

    private String expression;
    private String delimiters;
    private List<@NonNull E> expressionParts = Collections.emptyList();

    private boolean continueSearch;
    private List<Date> candidates = new ArrayList<>();
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
            int minimumCandidates, int maximumCandidates) throws ParseException {

        if (expression == null) {
            throw new IllegalArgumentException("The expression cannot be null");
        }

        this.expression = expression;
        this.delimiters = delimiters;
        this.startDate = startDate;
        this.timeZone = timeZone;
        this.minimumCandidates = minimumCandidates;
        this.maximumCandidates = maximumCandidates;

        if (startDate == null) {
            throw new IllegalArgumentException("The start date of the rule must not be null");
        }
        setStartDate(startDate);

        setTimeZone(timeZone);
        parseExpression(expression);
    }

    @Override
    public final Date getStartDate() {
        if (startDate == null) {
            try {
                setStartDate(Calendar.getInstance(getTimeZone()).getTime());
            } catch (Exception e) {
                // This code will never be reached
            }
        }
        return startDate;
    }

    @Override
    public void setStartDate(Date startDate) throws IllegalArgumentException, ParseException {
        if (startDate == null) {
            throw new IllegalArgumentException("The start date of the rule must not be null");
        }
        this.startDate = startDate;
        logger.trace("Setting the start date to {}", startDate);
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
        parseExpression(expression, true);
    }

    /**
     * Parse the given expression
     *
     * @param expression the expression to parse
     * @param searchMode keep nearest/farthest dates when true/false
     * @throws ParseException when the expression cannot be successfully be parsed
     * @throws IllegalArgumentException when expression parts conflict with each other
     */
    public final void parseExpression(String expression, boolean searchMode)
            throws ParseException, IllegalArgumentException {

        StringTokenizer expressionTokenizer = new StringTokenizer(expression, delimiters, false);
        int position = 0;

        setCandidates(new ArrayList<Date>());

        List<E> parts = new LinkedList<E>();
        while (expressionTokenizer.hasMoreTokens()) {
            String token = expressionTokenizer.nextToken().trim();
            position++;
            parts.add(parseToken(token, position));
        }
        setExpressionParts(parts);

        validateExpression();

        if (startDate == null) {
            setStartDate(Calendar.getInstance().getTime());
        }

        applyExpressionParts(searchMode);

        synchronized (this) {
            continueSearch = true;
            while (getCandidates().size() < minimumCandidates && continueSearch) {
                populateWithSeeds();
                getCandidates().clear();
                applyExpressionParts(searchMode);
            }
            continueSearch = false;
        }

        if (logger.isTraceEnabled()) {
            for (Date aDate : getCandidates()) {
                logger.trace("Final candidate {} is {}", getCandidates().indexOf(aDate), aDate);
            }
        }
    }

    abstract protected void validateExpression() throws IllegalArgumentException;

    protected void applyExpressionParts(boolean searchMode) {
        for (ExpressionPart part : getExpressionParts()) {
            logger.trace("Expanding {} from {} candidates", part.getClass().getSimpleName(), getCandidates().size());
            setCandidates(part.apply(startDate, getCandidates()));
            if (logger.isTraceEnabled()) {
                logger.trace("Expanded to {} candidates", getCandidates().size());
                for (Date aDate : getCandidates()) {
                    logger.trace("Candidate {} is {}", getCandidates().indexOf(aDate), aDate);
                }
            }
            if (searchMode) {
                pruneFarthest();
            } else {
                pruneNearest();
            }
        }
    }

    protected void pruneFarthest() {
        Collections.sort(getCandidates());

        ArrayList<Date> beforeDates = new ArrayList<Date>();

        for (Date candidate : getCandidates()) {
            if (candidate.before(startDate)) {
                beforeDates.add(candidate);
            }
        }

        getCandidates().removeAll(beforeDates);

        if (getCandidates().size() > maximumCandidates) {
            logger.trace("Pruning from {} to {} nearest candidates", getCandidates().size(), maximumCandidates);
            int size = getCandidates().size();
            for (int i = maximumCandidates; i < size; i++) {
                getCandidates().remove(getCandidates().size() - 1);
            }
        }
    }

    protected void pruneNearest() {
        Collections.sort(getCandidates());

        ArrayList<Date> beforeDates = new ArrayList<Date>();

        for (Date candidate : getCandidates()) {
            if (candidate.before(startDate)) {
                beforeDates.add(candidate);
            }
        }

        getCandidates().removeAll(beforeDates);

        if (getCandidates().size() > maximumCandidates) {
            logger.trace("Pruning from {} to {} farthest candidates", getCandidates().size(), maximumCandidates);
            int size = getCandidates().size();
            for (int i = 1; i <= size - maximumCandidates; i++) {
                getCandidates().remove(0);
            }
        }
    }

    @Override
    public Date getTimeAfter(Date afterTime) {

        Date currentStartDate = getStartDate();

        if (hasFloatingStartDate()) {
            try {
                clearCandidates();
                setStartDate(afterTime);
            } catch (IllegalArgumentException | ParseException e) {
                logger.error("An exception occurred while setting the start date : '{}'", e.getMessage());
            }
        } else if (getCandidates().isEmpty()) {
            try {
                setStartDate(afterTime);
            } catch (ParseException e) {
                logger.error("An exception occurred while setting the start date : '{}'", e.getMessage());
            }
        }

        if (!getCandidates().isEmpty()) {
            if (getCandidates().size() == 1) {
                return getCandidates().get(0);
            } else {
                while (getCandidates().size() > 1) {

                    Collections.sort(getCandidates());

                    Date newStartDate = null;

                    try {

                        for (Date candidate : getCandidates()) {
                            newStartDate = candidate;
                            if (candidate.after(afterTime)) {
                                setStartDate(currentStartDate);
                                return candidate;
                            }
                        }

                        clearCandidates();
                        setStartDate(newStartDate);
                    } catch (IllegalArgumentException | ParseException e) {
                        logger.error("An exception occurred while parsing the expression : '{}'", e.getMessage());
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Date getFinalFireTime() {

        try {
            parseExpression(getExpression(), false);
        } catch (ParseException e) {
            logger.error("An exception occurred while parsing the expression : '{}'", e.getMessage());
        }

        Date lastCandidate = null;

        if (!getCandidates().isEmpty()) {
            lastCandidate = getCandidates().get(getCandidates().size() - 1);
        }

        return lastCandidate;
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

    public <T extends ExpressionPart> T getExpressionPart(Class<T> part) {
        for (ExpressionPart aPart : getExpressionParts()) {
            if (aPart.getClass().equals(part)) {
                return part.cast(aPart);
            }
        }
        return null;
    }

    protected List<Date> getCandidates() {
        return candidates;
    }

    protected void setCandidates(List<Date> candidates) {
        this.candidates = candidates;
    }

    protected void clearCandidates() {
        this.candidates = null;
    }

    public List<@NonNull E> getExpressionParts() {
        return expressionParts;
    }

    public void setExpressionParts(List<@NonNull E> expressionParts) {
        synchronized (this) {
            Collections.sort(expressionParts);
            this.expressionParts = Collections.unmodifiableList(new LinkedList<>(expressionParts));
        }
    }

}
