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
 * Implements a number value
 *
 * @author David Graeff - Initial contribution
 */
public class NumberValue implements AbstractMqttThingValue {
    DecimalType numberValue;
    boolean isFloat;
    private double step;

    public NumberValue(Boolean isFloat, BigDecimal step) {
        this.isFloat = isFloat == null ? true : isFloat;
        this.step = step == null ? 1.0 : step.doubleValue();
        numberValue = new DecimalType();
    }

    @Override
    public State getValue() {
        return numberValue;
    }

    @Override
    public String update(Command command) throws IllegalArgumentException {
        if (command instanceof StringType) {
            numberValue = PercentType.valueOf(((StringType) command).toString());
        } else if (command instanceof PercentType) {
            numberValue = ((PercentType) command);
        } else if (command instanceof IncreaseDecreaseType) {
            double v;
            if (((IncreaseDecreaseType) command) == IncreaseDecreaseType.INCREASE) {
                v = numberValue.doubleValue() - step;
            } else {
                v = numberValue.doubleValue() + step;
            }
            numberValue = new PercentType(new BigDecimal(v));
        } else {
            throw new IllegalArgumentException(
                    "Type " + command.getClass().getName() + " not supported for PercentValue");
        }

        if (isFloat) {
            return numberValue.toString();
        } else {
            return String.valueOf(numberValue.intValue());
        }
    }

    @Override
    public State update(String updatedValue) throws IllegalArgumentException {
        numberValue = PercentType.valueOf(updatedValue);
        return numberValue;
    }
}
