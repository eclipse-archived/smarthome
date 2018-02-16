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

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * Implements an on/off boolean value.
 *
 * @author David Graeff - Initial contribution
 */
public class OnOffValue implements AbstractMqttThingValue {
    private OnOffType boolValue;
    private boolean isInversedOnOff = false;
    private final String onValue;
    private final String offValue;

    public OnOffValue(String onValue, String offValue, Boolean isInversedOnOff) {
        this.onValue = onValue == null ? "ON" : onValue;
        this.offValue = offValue == null ? "OFF" : offValue;
        this.isInversedOnOff = isInversedOnOff == null ? false : isInversedOnOff;
        boolValue = OnOffType.OFF;
    }

    @Override
    public State getValue() {
        return boolValue;
    }

    @Override
    public String update(Command command) throws IllegalArgumentException {
        if (command instanceof OnOffType) {
            boolValue = ((OnOffType) command);
        } else if (command instanceof OpenClosedType) {
            boolValue = ((OpenClosedType) command) == OpenClosedType.OPEN ? OnOffType.ON : OnOffType.OFF;
        } else if (command instanceof StringType) {
            update(command.toString());
        } else {
            throw new IllegalArgumentException(
                    "Type " + command.getClass().getName() + " not supported for OnOffValue");
        }

        if (isInversedOnOff) {
            return boolValue == OnOffType.OFF ? onValue : offValue;
        } else {
            return boolValue == OnOffType.ON ? onValue : offValue;
        }
    }

    @Override
    public State update(String updatedValue) throws IllegalArgumentException {
        if (onValue != null && onValue.equals(updatedValue) || "ON".equals(updatedValue.toUpperCase())
                || "1".equals(updatedValue)) {
            boolValue = OnOffType.ON;
        } else if (offValue != null && offValue.equals(updatedValue) || "OFF".equals(updatedValue.toUpperCase())
                || "0".equals(updatedValue)) {
            boolValue = OnOffType.OFF;
        } else {
            throw new IllegalArgumentException("Didn't recognise the on/off value " + updatedValue);
        }

        if (isInversedOnOff) {
            return boolValue == OnOffType.OFF ? OnOffType.ON : OnOffType.OFF;
        } else {
            return boolValue == OnOffType.ON ? OnOffType.OFF : OnOffType.ON;
        }
    }

    /**
     * Enables the inverse mode.
     * This is used for tests.
     */
    public void setInverse(boolean v) {
        isInversedOnOff = v;
    }
}
