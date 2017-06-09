/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import java.math.BigDecimal;

import javax.measure.Dimension;
import javax.measure.IncommensurableException;
import javax.measure.Quantity;
import javax.measure.UnconvertibleException;
import javax.measure.Unit;
import javax.measure.UnitConverter;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tec.uom.se.AbstractUnit;
import tec.uom.se.quantity.Quantities;

/**
 * The measure type extends DecimalType to handle physical unit measurement
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class QuantityType extends Number implements PrimitiveType, State, Command, Comparable<QuantityType> {
    private final static Logger logger = LoggerFactory.getLogger(QuantityType.class);

    private static final long serialVersionUID = 8828949721938234629L;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    // Regular expression to split unit from value
    private static final String UNIT_PATTERN = "(?<=\\d)\\s*(?=[a-zA-Z°µ%])";

    public Quantity<?> quantity;

    public QuantityType(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Constructor argument must not be null");
        }

        String[] constituents = value.split(UNIT_PATTERN);

        // getQuantity needs a space between numeric value and unit
        String formatted = String.join(" ", constituents);
        try {
            quantity = Quantities.getQuantity(formatted);
        } catch (IllegalArgumentException e) {
            logger.debug("Unable to convert {} to QuantityType", value);
            throw e;
        }
    }

    public QuantityType(double value, Unit<?> unit) {
        quantity = Quantities.getQuantity(value, unit);
    }

    public static QuantityType valueOf(double value, Unit<?> unit) {
        return new QuantityType(value, unit);
    }

    @Override
    public String toString() {
        return toFullString();
    }

    public static QuantityType valueOf(String value) {
        return new QuantityType(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof QuantityType)) {
            return false;
        }
        QuantityType other = (QuantityType) obj;
        if (quantity == null) {
            if (other.quantity != null) {
                return false;
            }
        } else if (quantity.getUnit().getDimension() != other.quantity.getUnit().getDimension()) {
            return false;
        } else if (compareTo(other) != 0) {
            return false;
        }

        return true;
    }

    @Override
    public int compareTo(QuantityType o) {
        if (quantity.getUnit().isCompatible(o.quantity.getUnit())) {
            QuantityType v1 = this.toUnit(getUnit().getSystemUnit());
            QuantityType v2 = o.toUnit(o.getUnit().getSystemUnit());
            return Double.compare(v1.doubleValue(), v2.doubleValue());
        } else {
            throw new IllegalArgumentException("Can not compare incompatible units");
        }
    }

    public Unit<?> getUnit() {
        return quantity.getUnit();
    }

    public Dimension getDimension() {
        return getUnit().getDimension();
    }

    public QuantityType toUnit(Unit<?> targetUnit) {
        if (!targetUnit.equals(getUnit())) {
            try {
                UnitConverter uc = getUnit().getConverterToAny(targetUnit);
                Quantity<?> result = Quantities.getQuantity(uc.convert(quantity.getValue()), targetUnit);

                return new QuantityType(result.getValue().doubleValue(), targetUnit);
            } catch (UnconvertibleException | IncommensurableException e) {
                logger.debug("Unable to convert unit from {} to {}",
                        new Object[] { getUnit().toString(), targetUnit.toString() });
                return null;
            }
        }
        return this;
    }

    public QuantityType toUnit(String targetUnit) {
        if (targetUnit != null) {
            Unit<?> unit = AbstractUnit.parse(targetUnit);
            if (unit != null) {
                return toUnit(unit);
            }

        }
        return null;
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal(doubleValue());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int tmp = prime * (getUnit() == null ? 0 : getUnit().hashCode());
        tmp += prime * (quantity.getValue() == null ? 0 : quantity.getValue().hashCode());
        return tmp;
    }

    @Override
    public String format(String pattern) {
        return String.format(pattern, quantity.getValue());
    }

    @Override
    public int intValue() {
        return quantity.getValue().intValue();
    }

    @Override
    public long longValue() {
        return quantity.getValue().longValue();
    }

    @Override
    public float floatValue() {
        return quantity.getValue().floatValue();
    }

    @Override
    public double doubleValue() {
        return quantity.getValue().doubleValue();
    }

    @Override
    public String toFullString() {
        return quantity.toString();
    }

    @Override
    public State as(Class<? extends State> target) {
        if (target == OnOffType.class) {
            if (intValue() == 0) {
                return OnOffType.OFF;
            } else if (toBigDecimal().compareTo(BigDecimal.ONE) == 0) {
                return OnOffType.ON;
            } else {
                return UnDefType.UNDEF;
            }
        } else if (target == UpDownType.class) {
            if (doubleValue() == 0) {
                return UpDownType.UP;
            } else if (toBigDecimal().compareTo(BigDecimal.ONE) == 0) {
                return UpDownType.DOWN;
            } else {
                return UnDefType.UNDEF;
            }
        } else if (target == OpenClosedType.class) {
            if (doubleValue() == 0) {
                return OpenClosedType.CLOSED;
            } else if (toBigDecimal().compareTo(BigDecimal.ONE) == 0) {
                return OpenClosedType.OPEN;
            } else {
                return UnDefType.UNDEF;
            }
        } else if (target == HSBType.class) {
            return new HSBType(DecimalType.ZERO, PercentType.ZERO,
                    new PercentType(this.toBigDecimal().multiply(HUNDRED)));
        } else if (target == PercentType.class) {
            return new PercentType(toBigDecimal().multiply(HUNDRED));
        } else if (target == DecimalType.class) {
            return new DecimalType(toBigDecimal());
        } else {
            return State.super.as(target);
        }
    }

}
