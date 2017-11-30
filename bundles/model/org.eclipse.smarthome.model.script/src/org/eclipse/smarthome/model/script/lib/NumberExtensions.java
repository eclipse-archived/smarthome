/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.lib;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
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

    public static QuantityType<?> operator_plus(QuantityType<?> x, QuantityType<?> y) {
        return x == null ? y : y == null ? x : x.add((QuantityType) y);
    }

    public static BigDecimal operator_minus(Number x) {
        BigDecimal xValue = numberToBigDecimal(x);
        if (xValue == null) {
            return xValue;
        } else {
            return xValue.negate();
        }
    }

    public static QuantityType<?> operator_minus(QuantityType<?> x) {
        return x == null ? null : x.negate();
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

    public static QuantityType<?> operator_minus(QuantityType<?> x, QuantityType<?> y) {
        return x == null ? operator_minus(y) : y == null ? x : x.subtract((QuantityType) y);
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

    public static QuantityType<?> operator_multiply(Number x, QuantityType<?> y) {
        BigDecimal xValue = numberToBigDecimal(x);
        if (xValue == null) {
            return new QuantityType<>("0");
        } else if (y == null) {
            return new QuantityType<>("0");
        } else {
            return y.multiply(xValue);
        }
    }

    public static QuantityType<?> operator_multiply(QuantityType<?> x, Number y) {
        return operator_multiply(y, x);
    }

    public static QuantityType<?> operator_multiply(QuantityType<?> x, QuantityType<?> y) {
        return x == null || y == null ? new QuantityType<>("0") : x.multiply(y);
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

    public static QuantityType<?> operator_divide(QuantityType<?> x, Number y) {
        BigDecimal yValue = numberToBigDecimal(y);
        if (x == null) {
            return new QuantityType<>("0").divide(yValue);
        } else if (yValue == null) {
            return x.divide(NULL_DEFINITION); // throws an exception
        } else {
            return x.divide(yValue);
        }
    }

    public static QuantityType<?> operator_divide(Number x, QuantityType<?> y) {
        QuantityType<?> xQuantity = new QuantityType<>("" + x.doubleValue());
        return operator_divide(xQuantity, y);
    }

    public static QuantityType<?> operator_divide(QuantityType<?> x, QuantityType<?> y) {
        if (x == null) {
            return new QuantityType<>("0").divide(y);
        } else if (y == null) {
            return x.divide(NULL_DEFINITION); // throws an exception
        } else {
            return x.divide(y);
        }
    }

    // Comparison operations between numbers

    public static boolean operator_equals(Number left, Number right) {
        if (oneIsQuantity(left, right)) {
            return false;
        }
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

    public static boolean operator_equals(QuantityType<?> left, QuantityType<?> right) {
        return left.equals(right);
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

    public static boolean operator_notEquals(QuantityType<?> left, QuantityType<?> right) {
        return !operator_equals(left, right);
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
    @SuppressWarnings("unchecked")
    public static BigDecimal numberToBigDecimal(Number number) {
        if (number instanceof QuantityType) {
            QuantityType state = ((QuantityType) number).toUnit(((QuantityType) number).getUnit().getSystemUnit());
            if (state != null) {
                return new BigDecimal(state.doubleValue());
            }
            return null;
        }
        if (number != null) {
            return new BigDecimal(number.toString());
        } else {
            return null;
        }
    }

    private static boolean oneIsQuantity(Number left, Number right) {
        return left instanceof QuantityType || right instanceof QuantityType;
    }

}
