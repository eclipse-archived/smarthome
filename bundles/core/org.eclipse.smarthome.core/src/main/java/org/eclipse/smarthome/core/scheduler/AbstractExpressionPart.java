/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.scheduler;

import java.text.ParseException;
import java.util.Collection;
import java.util.TreeSet;

/**
 * <code>AbstractExpressionPart</code> is an abstract implementation of {@link ExpressionPart} that capture the common
 * functionality to all ExpressionPart implementations.
 *
 * @author Karel Goderis - Initial contribution
 *
 */
abstract class AbstractExpressionPart implements ExpressionPart {

    private String part;
    private BoundedIntegerSet valueSet;

    public AbstractExpressionPart(String s) throws ParseException {
        this.set(s);
        this.parse();
    }

    @Override
    public void set(String s) {
        this.setPart(s);
    }

    /**
     * @return the set of values that are valid for the expression part
     */
    public BoundedIntegerSet getValueSet() {
        return valueSet;
    }

    /**
     * @param set the set of values that the epxression part should take into consideration
     */
    public void setValueSet(BoundedIntegerSet set) {
        this.valueSet = set;
    }

    abstract BoundedIntegerSet initializeValueSet();

    /**
     * A <code>BoundedIntegerSet</code> is a set of Integers that is bound by a minimum and maximum value, that does
     * allow or not allow negative values, and that is either starts from index 0 or 1
     *
     * @author Karel Goderis
     *
     */
    protected class BoundedIntegerSet extends TreeSet<Integer> {
        private static final long serialVersionUID = 19296179649170335L;
        protected final int max;
        protected final int min;
        protected final boolean negative;
        protected boolean is1indexed;

        BoundedIntegerSet(final int min, final int max, final boolean negativeValuesAllowed, final boolean is1indexed) {
            this.min = min;
            this.max = max;
            this.negative = negativeValuesAllowed;
            this.is1indexed = is1indexed;
        }

        /**
         * Validates if an integer can be added to the set
         *
         * @param integer the integer to validate
         */
        private void validate(final Integer integer) {
            if (negative) {
                final int abs = Math.abs(integer);
                if (abs < min || abs > max) {
                    throw new IllegalArgumentException("Invalid integer value (value not in range [" + (-max) + ", "
                            + -min + "] U [" + min + ", " + max + "])");
                }
            } else {
                if (integer < min || integer > max) {
                    throw new IllegalArgumentException(
                            "Invalid integer value (value not in range [" + min + ", " + max + "])");
                }
            }
        }

        @Override
        public boolean add(final Integer e) {
            validate(e);
            return super.add(e);
        }

        /**
         * Adds a series of integers to the set, starting from start, and ending at end, with a given increment
         *
         * @param start value to start at
         * @param end value to end at (inclusive)
         * @param increment increment to advance
         * @return true if the addition of integers was successful
         */
        public boolean add(final Integer start, final Integer end, final Integer increment) {

            if (end != null && end > max) {
                throw new IllegalArgumentException(
                        "Invalid integer value (end not in range [" + min + ", " + max + "])");
            }

            if (start != null && start < min) {
                throw new IllegalArgumentException(
                        "Invalid integer value (start not in range [" + min + ", " + max + "])");
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
                    int i2 = i % max;

                    if (i2 == 0 && is1indexed) {
                        i2 = max;
                    }
                    add(i2);
                }
            }

            return true;
        }

        @Override
        public boolean addAll(final Collection<? extends Integer> c) {
            for (Integer integer : c) {
                validate(integer);
            }
            return super.addAll(c);
        }
    }

    @Override
    public abstract int order();

    @Override
    public int compareTo(ExpressionPart o) {
        if (this.order() < o.order()) {
            return -1;
        } else if (this.order() < o.order()) {
            return 1;
        } else {
            return 0;
        }
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }
}
