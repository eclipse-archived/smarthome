/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.mqttgeneric.internal;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * Implements a percentage value. Minimum and maximum are definable.
 *
 * @author David Graeff - Initial contribution
 */
public class PercentValue implements AbstractMqttThingValue {
    private double min;
    private double max;
    private double step;
    private Boolean isFloat;

    PercentType percentValue;

    public PercentValue(Boolean isFloat, BigDecimal min, BigDecimal max, BigDecimal step) {
        this.isFloat = isFloat == null ? true : isFloat;
        this.min = min == null ? 0.0 : min.doubleValue();
        this.max = max == null ? 100.0 : max.doubleValue();
        this.step = step == null ? 1.0 : step.doubleValue();
        percentValue = new PercentType();
    }

    @Override
    public State getValue() {
        return percentValue;
    }

    @Override
    public String update(Command command) throws IllegalArgumentException {
        if (command instanceof StringType) {
            double v = Double.valueOf(((StringType) command).toString());
            v = (v - min) * 100.0 / (max - min);
            percentValue = new PercentType(new BigDecimal(v));
        } else if (command instanceof PercentType) {
            percentValue = ((PercentType) command);
        } else if (command instanceof DecimalType) {
            double v = ((DecimalType) command).doubleValue();
            v = (v - min) * 100.0 / (max - min);
            percentValue = new PercentType(new BigDecimal(v));
        } else if (command instanceof IncreaseDecreaseType) {
            if (((IncreaseDecreaseType) command) == IncreaseDecreaseType.INCREASE) {
                double v = percentValue.doubleValue() - step;
                if (v >= min) {
                    percentValue = new PercentType(new BigDecimal(v));
                }
            } else {
                double v = percentValue.doubleValue() + step;
                if (v <= max) {
                    percentValue = new PercentType(new BigDecimal(v));
                }
            }
        } else {
            throw new IllegalArgumentException(
                    "Type " + command.getClass().getName() + " not supported for PercentValue");
        }

        if (isFloat) {
            return percentValue.toString();
        } else {
            return String.valueOf(percentValue.intValue());
        }
    }

    @Override
    public State update(String updatedValue) throws IllegalArgumentException {
        percentValue = PercentType.valueOf(updatedValue);
        return percentValue;
    }
}
