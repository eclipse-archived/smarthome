/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.mqtt.generic.internal.values;

import java.math.BigDecimal;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Implements a percentage value. Minimum and maximum are definable.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class NumberValue implements Value {
    private State state = UnDefType.UNDEF;
    private final double min;
    private final double max;
    private final double step;
    private final Boolean isDecimal;
    private final boolean isPercent;

    private DecimalType numberValue;

    public NumberValue(@Nullable Boolean isDecimal, @Nullable BigDecimal min, @Nullable BigDecimal max,
            @Nullable BigDecimal step, boolean isPercent) {
        this.isDecimal = isDecimal == null ? false : isDecimal;
        this.min = min == null ? 0.0 : min.doubleValue();
        this.max = max == null ? 100.0 : max.doubleValue();
        if (isPercent && this.min >= this.max) {
            throw new IllegalArgumentException("Min need to be smaller than max!");
        }
        this.step = step == null ? 1.0 : step.doubleValue();
        this.isPercent = isPercent;
        numberValue = new DecimalType();
    }

    @Override
    public State getValue() {
        return state;
    }

    @Override
    public String update(Command command) throws IllegalArgumentException {
        if (isPercent) {
            if (command instanceof StringType) {
                double v = Double.valueOf(((StringType) command).toString());
                v = (v - min) * 100.0 / (max - min);
                numberValue = new PercentType(new BigDecimal(v));
            } else if (command instanceof PercentType) {
                numberValue = ((PercentType) command);
            } else if (command instanceof DecimalType) {
                double v = ((DecimalType) command).doubleValue();
                v = (v - min) * 100.0 / (max - min);
                numberValue = new PercentType(new BigDecimal(v));
            } else if (command instanceof IncreaseDecreaseType) {
                if (((IncreaseDecreaseType) command) == IncreaseDecreaseType.INCREASE) {
                    final double v = numberValue.doubleValue() + step;
                    numberValue = new PercentType(new BigDecimal(v <= max ? v : max));
                } else {
                    double v = numberValue.doubleValue() - step;
                    numberValue = new PercentType(new BigDecimal(v >= min ? v : min));
                }
            } else if (command instanceof UpDownType) {
                if (((UpDownType) command) == UpDownType.UP) {
                    final double v = numberValue.doubleValue() + step;
                    numberValue = new PercentType(new BigDecimal(v <= max ? v : max));
                } else {
                    final double v = numberValue.doubleValue() - step;
                    numberValue = new PercentType(new BigDecimal(v >= min ? v : min));
                }
            } else {
                throw new IllegalArgumentException(
                        "Type " + command.getClass().getName() + " not supported for PercentValue");
            }

            if (isDecimal) {
                state = numberValue;
                return numberValue.toString();
            } else {
                state = numberValue;
                return String.valueOf(numberValue.intValue());
            }
        } else {
            if (command instanceof StringType) {
                numberValue = DecimalType.valueOf(((StringType) command).toString());
            } else if (command instanceof DecimalType) {
                numberValue = (DecimalType) command;
            } else if (command instanceof PercentType) {
                numberValue = ((PercentType) command);
            } else if (command instanceof IncreaseDecreaseType) {
                double v;
                if (((IncreaseDecreaseType) command) == IncreaseDecreaseType.INCREASE) {
                    v = numberValue.doubleValue() + step;
                } else {
                    v = numberValue.doubleValue() - step;
                }
                numberValue = new DecimalType(v);
            } else if (command instanceof UpDownType) {
                double v;
                if (((UpDownType) command) == UpDownType.UP) {
                    v = numberValue.doubleValue() + step;
                } else {
                    v = numberValue.doubleValue() - step;
                }
                numberValue = new DecimalType(v);
            } else {
                throw new IllegalArgumentException(
                        "Type " + command.getClass().getName() + " not supported for NumberValue");
            }

            if (isDecimal) {
                state = numberValue;
                return numberValue.toString();
            } else {
                state = numberValue;
                return String.valueOf(numberValue.intValue());
            }
        }
    }

    @Override
    public State update(String updatedValue) throws IllegalArgumentException {
        numberValue = DecimalType.valueOf(updatedValue);
        state = numberValue;
        return numberValue;
    }

    @Override
    public String getItemType() {
        return isPercent ? CoreItemFactory.DIMMER : CoreItemFactory.NUMBER;
    }

    @Override
    public StateDescription createStateDescription(String unit, boolean readOnly) {
        return new StateDescription(new BigDecimal(min), new BigDecimal(max), new BigDecimal(step),
                "%s " + unit.replace("%", "%%"), readOnly, Collections.emptyList());
    }

    @Override
    public void resetState() {
        state = UnDefType.UNDEF;
    }
}
