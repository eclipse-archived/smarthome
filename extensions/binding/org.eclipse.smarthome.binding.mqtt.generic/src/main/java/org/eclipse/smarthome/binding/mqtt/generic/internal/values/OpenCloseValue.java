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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Implements an open/close boolean value.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class OpenCloseValue implements Value {
    private State state = UnDefType.UNDEF;
    private OpenClosedType boolValue;
    private final String openString;
    private final String closeString;
    private List<Class<? extends Command>> commandTypes = Stream.of(OpenClosedType.class, StringType.class)
            .collect(Collectors.toList());

    /**
     * Creates a contact Open/Close type.
     */
    public OpenCloseValue() {
        this.openString = OpenClosedType.OPEN.name();
        this.closeString = OpenClosedType.CLOSED.name();
        this.boolValue = OpenClosedType.CLOSED;
    }

    /**
     * Creates a new contact Open/Close value.
     *
     * @param openValue The ON value string. This will be compared to MQTT messages.
     * @param closeValue The OFF value string. This will be compared to MQTT messages.
     */
    public OpenCloseValue(@Nullable String openValue, @Nullable String closeValue) {
        this.openString = openValue == null ? OpenClosedType.OPEN.name() : openValue;
        this.closeString = closeValue == null ? OpenClosedType.CLOSED.name() : closeValue;
        this.boolValue = OpenClosedType.CLOSED;
    }

    @Override
    public State getValue() {
        return state;
    }

    @Override
    public String update(Command command) throws IllegalArgumentException {
        if (command instanceof OpenClosedType) {
            boolValue = ((OpenClosedType) command);
        } else if (command instanceof StringType) {
            final String updatedValue = ((StringType) command).toString();
            final String upperCase = updatedValue.toUpperCase();
            if (openString.equals(updatedValue) || OpenClosedType.OPEN.name().equals(upperCase)) {
                boolValue = OpenClosedType.OPEN;
            } else if (closeString.equals(updatedValue) || OpenClosedType.CLOSED.name().equals(upperCase)) {
                boolValue = OpenClosedType.CLOSED;
            } else {
                throw new IllegalArgumentException("Didn't recognise the open/closed value " + updatedValue);
            }
        } else {
            throw new IllegalArgumentException(
                    "Type " + command.getClass().getName() + " not supported for OpenCloseValue");
        }

        state = boolValue;
        return (boolValue == OpenClosedType.OPEN) ? openString : closeString;
    }

    @Override
    public String getItemType() {
        return CoreItemFactory.CONTACT;
    }

    @Override
    public StateDescription createStateDescription(String unit, boolean readOnly) {
        return new StateDescription(null, null, null, "%s " + unit.replace("%", "%%"), readOnly,
                Collections.emptyList());
    }

    @Override
    public List<Class<? extends Command>> getSupportedCommandTypes() {
        return commandTypes;
    }

    @Override
    public void resetState() {
        state = UnDefType.UNDEF;
    }
}
