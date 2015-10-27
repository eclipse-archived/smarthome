/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.lib;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.Type;

/**
 * This class contains all kinds of extensions to be used by scripts and not
 * provided by Xbase. These include things like number handling and comparisons.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class NumberExtensions {

    /**
     * It is the definition of Java null pointer for the rules language.
     * Actually its value is 0 (rules variables are number) but we can use
     * the null pointer and throws an NPE when a null value is used.
     * I think this concept should not exist for those who writes the rules.
     */
    public static final BigDecimal NULL_DEFINITION = new BigDecimal(0);

    // Calculation operators for numbers

    public static BigDecimal operator_plus(Number x, Number y) {
        BigDecimal xValue = numberToBigDecimal(x);
        BigDecimal yValue = numberToBigDecimal(y);
        if (xValue == null) {
            return yValue;
        } else if (yValue == null) {
            return xValue;
        } else {
            return xValue.add(yValue);
        }
    }

    public static BigDecimal operator_minus(Number x) {
        BigDecimal xValue = numberToBigDecimal(x);
        if (xValue == null) {
            return xValue;
        } else {
            return xValue.negate();
        }
    }

    public static BigDecimal operator_minus(Number x, Number y) {
        BigDecimal xValue = numberToBigDecimal(x);
        BigDecimal yValue = numberToBigDecimal(y);
        if (xValue == null) {
            return operator_minus(yValue);
        } else if (yValue == null) {
            return xValue;
        } else {
            return xValue.subtract(yValue);
        }
    }

    public static BigDecimal operator_multiply(Number x, Number y) {
        BigDecimal xValue = numberToBigDecimal(x);
        BigDecimal yValue = numberToBigDecimal(y);
        if (xValue == null) {
            return NULL_DEFINITION;
        } else if (yValue == null) {
            return NULL_DEFINITION;
        } else {
            return xValue.multiply(yValue);
        }
    }

    public static BigDecimal operator_divide(Number x, Number y) {
        BigDecimal xValue = numberToBigDecimal(x);
        BigDecimal yValue = numberToBigDecimal(y);
        if (xValue == null) {
            return NULL_DEFINITION.divide(yValue, 8, RoundingMode.HALF_UP);
        } else if (yValue == null) {
            return xValue.divide(NULL_DEFINITION, 8, RoundingMode.HALF_UP); // throws an exception
        } else {
            return xValue.divide(yValue, 8, RoundingMode.HALF_UP);
        }
    }

    // Comparison operations between numbers

    public static boolean operator_equals(Number left, Number right) {
        BigDecimal leftValue = numberToBigDecimal(left);
        BigDecimal rightValue = numberToBigDecimal(right);
        if (leftValue == null) {
            return rightValue == null;
        } else if (rightValue == null) {
            return false;
        } else {
            return leftValue.compareTo(rightValue) == 0;
        }
    }

    public static boolean operator_notEquals(Number left, Number right) {
        BigDecimal leftValue = numberToBigDecimal(left);
        BigDecimal rightValue = numberToBigDecimal(right);
        if (leftValue == null) {
            return rightValue != null;
        } else if (rightValue == null) {
            return true;
        } else {
            return leftValue.compareTo(rightValue) != 0;
        }
    }

    public static boolean operator_lessThan(Number left, Number right) {
        BigDecimal leftValue = numberToBigDecimal(left);
        BigDecimal rightValue = numberToBigDecimal(right);
        if (leftValue == null) {
            return true;
        } else if (rightValue == null) {
            return false;
        } else {
            return leftValue.compareTo(rightValue) < 0;
        }
    }

    public static boolean operator_greaterThan(Number left, Number right) {
        BigDecimal leftValue = numberToBigDecimal(left);
        BigDecimal rightValue = numberToBigDecimal(right);
        if (leftValue == null) {
            return false;
        } else if (rightValue == null) {
            return true;
        } else {
            return leftValue.compareTo(rightValue) > 0;
        }
    }

    public static boolean operator_lessEqualsThan(Number left, Number right) {
        BigDecimal leftValue = numberToBigDecimal(left);
        BigDecimal rightValue = numberToBigDecimal(right);
        if (leftValue == null) {
            return true;
        } else if (rightValue == null) {
            return false;
        } else {
            return leftValue.compareTo(rightValue) <= 0;
        }
    }

    public static boolean operator_greaterEqualsThan(Number left, Number right) {
        BigDecimal leftValue = numberToBigDecimal(left);
        BigDecimal rightValue = numberToBigDecimal(right);
        if (leftValue == null) {
            return (rightValue != null) ? false : true;
        } else if (rightValue == null) {
            return true;
        } else {
            return leftValue.compareTo(rightValue) >= 0;
        }
    }

    // Comparison operators between ESH types and numbers

    public static boolean operator_equals(Type type, Number x) {
        if (type != null && type instanceof DecimalType && x != null) {
            return ((DecimalType) type).toBigDecimal().compareTo(numberToBigDecimal(x)) == 0;
        } else {
            return type == x; // both might be null, then we should return true
        }
    }

    public static boolean operator_notEquals(Type type, Number x) {
        if (type != null && type instanceof DecimalType && x != null) {
            return ((DecimalType) type).toBigDecimal().compareTo(numberToBigDecimal(x)) != 0;
        } else {
            return type != x; // both might be null, then we should return
                              // false, otherwise true
        }
    }

    public static boolean operator_greaterThan(Type type, Number x) {
        if (type != null && type instanceof DecimalType && x != null) {
            return ((DecimalType) type).toBigDecimal().compareTo(numberToBigDecimal(x)) > 0;
        } else {
            return false;
        }
    }

    public static boolean operator_greaterEqualsThan(Type type, Number x) {
        if (type != null && type instanceof DecimalType && x != null) {
            return ((DecimalType) type).toBigDecimal().compareTo(numberToBigDecimal(x)) >= 0;
        } else {
            return false;
        }
    }

    public static boolean operator_lessThan(Type type, Number x) {
        if (type != null && type instanceof DecimalType && x != null) {
            return ((DecimalType) type).toBigDecimal().compareTo(numberToBigDecimal(x)) < 0;
        } else {
            return false;
        }
    }

    public static boolean operator_lessEqualsThan(Type type, Number x) {
        if (type != null && type instanceof DecimalType && x != null) {
            return ((DecimalType) type).toBigDecimal().compareTo(numberToBigDecimal(x)) <= 0;
        } else {
            return false;
        }
    }

    /**
     * Convert the given number into a BigDecimal
     *
     * @param number
     *            the number to convert
     * @return the given number as BigDecimal or null if number is null
     */
    public static BigDecimal numberToBigDecimal(Number number) {
        if (number != null) {
            return new BigDecimal(number.toString());
        } else {
            return null;
        }
    }
}
