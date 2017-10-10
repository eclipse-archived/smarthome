/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.IllegalFormatConversionException;
import java.util.Map;

import javax.measure.Dimension;
import javax.measure.IncommensurableException;
import javax.measure.Quantity;
import javax.measure.UnconvertibleException;
import javax.measure.Unit;
import javax.measure.UnitConverter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.MeasurementSystem;
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
@NonNullByDefault
public class QuantityType extends Number implements PrimitiveType, State, Command, Comparable<QuantityType> {
    private final static Logger logger = LoggerFactory.getLogger(QuantityType.class);

    private static final long serialVersionUID = 8828949721938234629L;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    // Regular expression to split unit from value
    private static final String UNIT_PATTERN = "(?<=\\d)\\s*(?=[a-zA-Z°µ%])";

    private Quantity<?> quantity;
    private Map<MeasurementSystem, Unit<?>> conversionUnits = new HashMap<MeasurementSystem, Unit<?>>(2);

    /**
     * Creates a new {@link QuantityType} with the given value. The value may contain a unit. The specific
     * {@link Quantity} is obtained by {@link Quantities#getQuantity(CharSequence)}.
     *
     * @param value the non null value representing a quantity with an optional unit.
     */
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

    /**
     * Creates a new {@link QuantityType} with the given value and {@link Unit}.
     *
     * @param value the non null measurement value.
     * @param unit the non null measurement unit.
     */
    public QuantityType(double value, Unit<?> unit) {
        this(value, unit, null);
    }

    /**
     * Creates a new {@link QuantityType} with the given value and {@link Unit}.
     *
     * @param value the non null measurement value.
     * @param unit the non null measurement unit.
     * @param conversionUnits the optional unit map which is used to determine the {@link MeasurementSystem} specific
     *            unit for conversion.
     */
    public QuantityType(double value, Unit<?> unit, @Nullable Map<MeasurementSystem, Unit<?>> conversionUnits) {
        // Avoid scientific notation for double
        BigDecimal bd = new BigDecimal(value);
        quantity = Quantities.getQuantity(bd, unit);
        if (conversionUnits != null && !conversionUnits.isEmpty()) {
            this.conversionUnits.putAll(conversionUnits);
        }
    }

    /**
     * Static access to {@link QuantityType#QuantityType(double, Unit)}.
     *
     * @param value the non null measurement value.
     * @param unit the non null measurement unit.
     * @return a new {@link QuantityType}
     */
    public static QuantityType valueOf(double value, Unit<?> unit) {
        return new QuantityType(value, unit);
    }

    @Override
    public String toString() {
        return quantity.toString();
    }

    public static QuantityType valueOf(String value) {
        return new QuantityType(value);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
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

    /**
     * Convert this QuantityType to a new {@link QuantityType} using the given target unit.
     *
     * @param targetUnit the unit to which this {@link QuantityType} will be converted to.
     * @return the new {@link QuantityType} in the given {@link Unit} or {@code null} in case of a
     */
    public @Nullable QuantityType toUnit(Unit<?> targetUnit) {
        if (!targetUnit.equals(getUnit())) {
            try {
                UnitConverter uc = getUnit().getConverterToAny(targetUnit);
                Quantity<?> result = Quantities.getQuantity(uc.convert(quantity.getValue()), targetUnit);

                return new QuantityType(result.getValue().doubleValue(), targetUnit, conversionUnits);
            } catch (UnconvertibleException | IncommensurableException e) {
                logger.debug("Unable to convert unit from {} to {}",
                        new Object[] { getUnit().toString(), targetUnit.toString() });
                return null;
            }
        }
        return this;
    }

    public @Nullable QuantityType toUnit(String targetUnit) {
        Unit<?> unit = AbstractUnit.parse(targetUnit);
        if (unit != null) {
            return toUnit(unit);
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
    public String format(@Nullable String pattern) {
        // The value could be an integer value. Try to convert to BigInteger in
        // order to have access to more conversion formats.
        try {
            return String.format(pattern, toBigDecimal().toBigIntegerExact());
        } catch (ArithmeticException ae) {
            // Could not convert to integer value without loss of
            // information. Fall through to default behavior.
        } catch (IllegalFormatConversionException ifce) {
            // The conversion is not valid for the type BigInteger. This
            // happens, if the format is like "%.1f" but the value is an
            // integer. Fall through to default behavior.
        }

        return String.format(pattern, toBigDecimal());
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
        // For backward compatibility we expose only the scalar value here.
        // For adequate rendering the state´s unit must be retrieved separately.
        // The REST interface does this by providing a "unit" attribute for "/rest/things"
        // and a "stateMap" attribute with "unit" and "value" keys for stateUpdateEvents.
        return toBigDecimal().toPlainString();
    }

    public Unit<?> getConversionUnit(MeasurementSystem ms) {
        return conversionUnits.get(ms);
    }

    /**
     * Whether this {@link QuantityType} needs a conversion to the given MeasurementSystem. This uses the
     * conversionUnits map from the {@link QuantityType#QuantityType(double, Unit, Map)} constructor.
     *
     * @param ms
     * @return
     */
    public boolean needsConversion(MeasurementSystem ms) {
        if (quantity != null && conversionUnits.get(ms) != null && conversionUnits.get(ms).equals(quantity.getUnit())) {
            return false;
        }

        return true;
    }

    @Override
    public State as(@Nullable Class<? extends @Nullable State> target) {
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
