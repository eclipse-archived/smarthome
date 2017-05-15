/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import java.math.BigDecimal;
import java.util.IllegalFormatConversionException;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * The decimal type uses a BigDecimal internally and thus can be used for
 * integers, longs and floating point numbers alike.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class DecimalType extends Number implements PrimitiveType, State, Command, Comparable<DecimalType> {

    private static final long serialVersionUID = 4226845847123464690L;

    final static public DecimalType ZERO = new DecimalType(0);

    protected BigDecimal value;

    public DecimalType() {
        this.value = BigDecimal.ZERO;
    }

    public DecimalType(BigDecimal value) {
        this.value = value;
    }

    public DecimalType(long value) {
        this.value = BigDecimal.valueOf(value);
    }

    public DecimalType(double value) {
        this.value = BigDecimal.valueOf(value);
    }

    public DecimalType(String value) {
        this.value = new BigDecimal(value);
    }

    @Override
    public String toString() {
        return toFullString();
    }

    @Override
    public String toFullString() {
        return value.toPlainString();
    }

    public static DecimalType valueOf(String value) {
        return new DecimalType(value);
    }

    @Override
    public String format(String pattern) {
        // The value could be an integer value. Try to convert to BigInteger in
        // order to have access to more conversion formats.
        try {
            return String.format(pattern, value.toBigIntegerExact());
        } catch (ArithmeticException ae) {
            // Could not convert to integer value without loss of
            // information. Fall through to default behavior.
        } catch (IllegalFormatConversionException ifce) {
            // The conversion is not valid for the type BigInteger. This
            // happens, if the format is like "%.1f" but the value is an
            // integer. Fall through to default behavior.
        }

        return String.format(pattern, value);
    }

    public BigDecimal toBigDecimal() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DecimalType)) {
            return false;
        }
        DecimalType other = (DecimalType) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (value.compareTo(other.value) != 0) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(DecimalType o) {
        return value.compareTo(o.toBigDecimal());
    }

    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public float floatValue() {
        return value.floatValue();
    }

    @Override
    public int intValue() {
        return value.intValue();
    }

    @Override
    public long longValue() {
        return value.longValue();
    }

    protected State defaultConversion(Class<? extends State> target) {
        return State.super.as(target);
    }

    @Override
    public State as(Class<? extends State> target) {
        if (target == OnOffType.class) {
            return equals(ZERO) ? OnOffType.OFF : OnOffType.ON;
        } else if (target == PercentType.class) {
            return new PercentType(toBigDecimal().multiply(BigDecimal.valueOf(100)));
        } else if (target == UpDownType.class) {
            if (equals(ZERO)) {
                return UpDownType.UP;
            } else if (toBigDecimal().compareTo(BigDecimal.valueOf(1)) == 0) {
                return UpDownType.DOWN;
            } else {
                return UnDefType.UNDEF;
            }
        } else if (target == OpenClosedType.class) {
            if (equals(ZERO)) {
                return OpenClosedType.CLOSED;
            } else if (toBigDecimal().compareTo(BigDecimal.valueOf(1)) == 0) {
                return OpenClosedType.OPEN;
            } else {
                return UnDefType.UNDEF;
            }
        } else if (target == HSBType.class) {
            return new HSBType(DecimalType.ZERO, PercentType.ZERO,
                    new PercentType(this.toBigDecimal().multiply(BigDecimal.valueOf(100))));
        } else {
            return defaultConversion(target);
        }
    }

}
