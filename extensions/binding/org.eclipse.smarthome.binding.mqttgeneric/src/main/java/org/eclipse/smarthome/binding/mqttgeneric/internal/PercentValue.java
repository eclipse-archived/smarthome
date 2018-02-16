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
    private final double min;
    private final double max;
    private final double step;
    private final Boolean isFloat;

    private PercentType percentValue;

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
