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
import org.eclipse.smarthome.core.library.types.OpenClosedType;
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
    private OnOffType boolValue;
    private final String onValue;
    private final String offValue;
    private final boolean receivesOnly;

    /**
     * Creates a switch On/Off type, that accepts "ON", "1" for on and "OFF","0" for off.
     */
    public OnOffValue() {
        this.onValue = "ON";
        this.offValue = "OFF";
        this.boolValue = OnOffType.OFF;
        this.receivesOnly = false;
    }

    /**
     * Creates a new SWITCH On/Off value.
     *
     * @param onValue The ON value string. This will be compared to MQTT messages.
     * @param offValue The OFF value string. This will be compared to MQTT messages.
     */
    public OnOffValue(@Nullable String onValue, @Nullable String offValue) {
        this.onValue = onValue == null ? "ON" : onValue;
        this.offValue = offValue == null ? "OFF" : offValue;
        this.boolValue = OnOffType.OFF;
        this.receivesOnly = false;
    }

    /**
     * Creates a new On/Off value that either corresponds to a SWITCH ESH type (if isSettable==true) or to a CONTACT
     * type otherwise.
     *
     * @param onValue The ON value string. This will be compared to MQTT messages.
     * @param offValue The OFF value string. This will be compared to MQTT messages.
     * @param receivesOnly Determines the ESH type. SWITCH if true, CONTACT otherwise
     */
    private OnOffValue(@Nullable String onValue, @Nullable String offValue, boolean receivesOnly) {
        this.onValue = onValue == null ? "ON" : onValue;
        this.offValue = offValue == null ? "OFF" : offValue;
        this.boolValue = OnOffType.OFF;
        this.receivesOnly = receivesOnly;
    }

    /**
     * Creates a new CONTACT On/Off value.
     *
     * @param onValue The ON value string. This will be compared to MQTT messages.
     * @param offValue The OFF value string. This will be compared to MQTT messages.
     * @param isInversedOnOff If true, inverses ON/OFF interpretations.
     */
    public static OnOffValue createReceiveOnly(@Nullable String onValue, @Nullable String offValue) {
        return new OnOffValue(onValue, offValue, true);
    }

    @Override
    public State getValue() {
        return state;
    }

    @Override
    public String update(Command command) throws IllegalArgumentException {
        if (command instanceof OnOffType) {
            boolValue = ((OnOffType) command);
        } else if (command instanceof OpenClosedType) {
            boolValue = ((OpenClosedType) command) == OpenClosedType.OPEN ? OnOffType.ON : OnOffType.OFF;
        } else if (command instanceof StringType) {
            boolValue = (OnOffType) update(command.toString());
        } else {
            throw new IllegalArgumentException(
                    "Type " + command.getClass().getName() + " not supported for OnOffValue");
        }

        state = boolValue;
        return (boolValue == OnOffType.ON) ? onValue : offValue;
    }

    @Override
    public State update(String updatedValue) throws IllegalArgumentException {
        if (onValue.equals(updatedValue) || "ON".equals(updatedValue.toUpperCase()) || "1".equals(updatedValue)) {
            boolValue = OnOffType.ON;
        } else if (offValue.equals(updatedValue) || "OFF".equals(updatedValue.toUpperCase())
                || "0".equals(updatedValue)) {
            boolValue = OnOffType.OFF;
        } else {
            throw new IllegalArgumentException("Didn't recognise the on/off value " + updatedValue);
        }

        state = boolValue;
        return boolValue;
    }

    @Override
    public String channelTypeID() {
        return CoreItemFactory.SWITCH;
    }

    @Override
    public StateDescription createStateDescription(String unit, boolean readOnly) {
        return new StateDescription(null, null, null, "%s " + unit.replace("%", "%%"), receivesOnly || readOnly,
                Collections.emptyList());
    }

    @Override
    public void resetState() {
        state = UnDefType.UNDEF;
    }
}
