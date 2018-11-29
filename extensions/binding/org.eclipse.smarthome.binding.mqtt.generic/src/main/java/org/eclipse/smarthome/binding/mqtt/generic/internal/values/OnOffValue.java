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

import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Implements an on/off boolean value.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class OnOffValue implements Value {
    private State state = UnDefType.UNDEF;
    private OnOffType onOffValue;
    private final String onString;
    private final String offString;

    /**
     * Creates a switch On/Off type, that accepts "ON", "1" for on and "OFF","0" for off.
     */
    public OnOffValue() {
        this.onString = OnOffType.ON.name();
        this.offString = OnOffType.OFF.name();
        this.onOffValue = OnOffType.OFF;
    }

    /**
     * Creates a new SWITCH On/Off value.
     *
     * @param onValue The ON value string. This will be compared to MQTT messages.
     * @param offValue The OFF value string. This will be compared to MQTT messages.
     */
    public OnOffValue(@Nullable String onValue, @Nullable String offValue) {
        this.onString = onValue == null ? OnOffType.ON.name() : onValue;
        this.offString = offValue == null ? OnOffType.OFF.name() : offValue;
        this.onOffValue = OnOffType.OFF;
    }

    @Override
    public State getValue() {
        return state;
    }

    @Override
    public String update(Command command) throws IllegalArgumentException {
        if (command instanceof OnOffType) {
            onOffValue = ((OnOffType) command);
        } else if (command instanceof StringType) {
            onOffValue = (OnOffType) update(command.toString());
        } else {
            throw new IllegalArgumentException(
                    "Type " + command.getClass().getName() + " not supported for OnOffValue");
        }

        state = onOffValue;
        return (onOffValue == OnOffType.ON) ? onString : offString;
    }

    @Override
    public State update(String updatedValue) throws IllegalArgumentException {
        final String upperCase = updatedValue.toUpperCase();
        if (onString.equals(updatedValue) || OnOffType.ON.name().equals(upperCase)) {
            onOffValue = OnOffType.ON;
        } else if (offString.equals(updatedValue) || OnOffType.OFF.name().equals(upperCase)) {
            onOffValue = OnOffType.OFF;
        } else {
            throw new IllegalArgumentException("Didn't recognise the on/off value " + updatedValue);
        }

        state = onOffValue;
        return onOffValue;
    }

    @Override
    public String getItemType() {
        return CoreItemFactory.SWITCH;
    }

    @Override
    public StateDescription createStateDescription(String unit, boolean readOnly) {
        return new StateDescription(null, null, null, "%s " + unit.replace("%", "%%"), readOnly,
                Collections.emptyList());
    }

    @Override
    public void resetState() {
        state = UnDefType.UNDEF;
    }
}
