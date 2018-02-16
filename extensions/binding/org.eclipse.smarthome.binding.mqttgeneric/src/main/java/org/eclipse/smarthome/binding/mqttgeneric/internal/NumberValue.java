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
 * Implements a number value
 *
 * @author David Graeff - Initial contribution
 */
public class NumberValue implements AbstractMqttThingValue {
    private DecimalType numberValue;
    private final boolean isFloat;
    private final double step;

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
            numberValue = DecimalType.valueOf(((StringType) command).toString());
        } else if (command instanceof DecimalType) {
            numberValue = (DecimalType) command;
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
                    "Type " + command.getClass().getName() + " not supported for NumberValue");
        }

        if (isFloat) {
            return numberValue.toString();
        } else {
            return String.valueOf(numberValue.intValue());
        }
    }

    @Override
    public State update(String updatedValue) throws IllegalArgumentException {
        numberValue = DecimalType.valueOf(updatedValue);
        return numberValue;
    }
}
