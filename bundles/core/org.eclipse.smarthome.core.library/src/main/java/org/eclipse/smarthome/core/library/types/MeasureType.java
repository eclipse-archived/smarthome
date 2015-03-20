/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import java.math.BigDecimal;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

/**
 * The measure type extends DecimalType to handle physical unit measurement
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class MeasureType extends DecimalType {

	private static final long serialVersionUID = 8828949721938234629L;

	// Regular expression to split unit from value
    static final public String UNIT_PATTERN = "(?<=\\d)\\s*(?=[a-zA-Z°µ])";

	protected Unit<?> unit;

	public MeasureType(String value) {
		if (value != null) {
			String[] constituents = value.split(UNIT_PATTERN);
			if (constituents.length == 2) {
				this.value = new BigDecimal(constituents[0]);
				this.unit = Unit.valueOf(constituents[1]);
			} else {
				throw new IllegalArgumentException(value
						+ " is not a valid MeasureType syntax");
			}
		} else {
			throw new IllegalArgumentException(
					"Constructor argument must not be null");
		}
	}

	public MeasureType(double value, Unit<?> unit) {
		this.value = new BigDecimal(value);
		this.unit = unit;
	}
	
    public static MeasureType valueOf(double value, Unit<?> unit) {
        return new MeasureType(value,unit);
    }

	@Override
	public String toString() {
		return value.toString() + " " + unit.toString();
	}

	public static MeasureType valueOf(String value) {
		return new MeasureType(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MeasureType))
			return false;
		MeasureType other = (MeasureType) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (unit.getDimension() != other.unit.getDimension()) // they are of the same dimension
			return false;
		else if (compareTo(other) != 0)
			return false;

		return true;
	}

	public int compareTo(MeasureType o) {
		if (unit.isCompatible(o.unit)) {
			BigDecimal v1 = toUnit(unit.getStandardUnit());
			BigDecimal v2 = o.toUnit(o.unit.getStandardUnit());
			
			return v1.compareTo(v2);
		} else {
			throw new IllegalArgumentException(
					"Can not compare incompatible units");
		}
	}

	public Unit<?> getUnit() {
		return this.unit;
	}

	public BigDecimal toUnit(Unit<?> targetUnit) {
		if (unit.isCompatible(targetUnit)) {
			UnitConverter uc = unit.getConverterTo(targetUnit);
			return new BigDecimal(uc.convert(this.doubleValue()));
		} else
			return null;
	}

}
