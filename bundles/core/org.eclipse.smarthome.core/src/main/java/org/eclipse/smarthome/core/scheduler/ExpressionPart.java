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
import java.util.List;

/**
 * <code>ExpressionPart</code> are the building blocks that make up an {@link Expression}. Each part can be set, then
 * parsed and be applied to the set of candidate dates that finally constitute the list of dates the expression will
 * adhere to
 *
 * @author Karel Goderis
 *
 */
interface ExpressionPart extends Comparable<ExpressionPart> {

    /**
     * Set the input string for the part of the expression
     *
     * @param s
     */
    public void set(String s);

    /**
     * Parse that part of the expression
     *
     * @throws ParseException when the set string is not valid or can not be parsed correctly
     */
    public void parse() throws ParseException;

    /**
     * Apply the expression part to the set of given candidates. Expression parts can either modify the candidates,
     * expand the list of candidates, or reduce the list of candidates
     *
     * @param startDate - the start date to take into consideration when applying the expression part
     * @param candidates - the list of candidates
     * @return a list of candidates post application of the expression part
     */
    public List<Date> apply(Date startDate, List<Date> candidates);

    /**
     * Get the "order" of the expression part. When an expression is parsed into a list of expression part, then the
     * expression parts will be applied in the order of the values returned by order()
     *
     * @return the relative position of this expression part in the chain of expression parts that make up the
     *         expression
     */
    public int order();

    @Override
    int compareTo(ExpressionPart o);
}
