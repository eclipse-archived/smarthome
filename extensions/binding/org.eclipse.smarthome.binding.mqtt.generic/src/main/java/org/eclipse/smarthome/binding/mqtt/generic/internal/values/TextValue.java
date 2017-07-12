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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;

/**
 * Implements a text/string value. Allows to restrict the incoming value to a set of states.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class TextValue implements AbstractMqttThingValue {
    private StringType strValue;
    private final @Nullable Set<String> states;

    public TextValue(String[] states) {
        strValue = new StringType();
        this.states = states.length > 0 ? Stream.of(states).collect(Collectors.toSet()) : null;
    }

    public TextValue() {
        strValue = new StringType();
        this.states = null;
    }

    @Override
    public State getValue() {
        return strValue;
    }

    @Override
    public String update(Command command) throws IllegalArgumentException {
        final Set<String> states = this.states;
        String value = command.toString();
        if (states != null && !states.contains(value)) {
            throw new IllegalArgumentException("Value " + value + " not within range");
        }
        strValue = new StringType(value);
        return strValue.toString();
    }

    @Override
    public State update(String value) throws IllegalArgumentException {
        final Set<String> states = this.states;
        if (states != null && !states.contains(value)) {
            throw new IllegalArgumentException("Value " + value + " not within range");
        }
        strValue = new StringType(value);
        return strValue;
    }

    @Override
    public String channelTypeID() {
        return CoreItemFactory.STRING;
    }

    /**
     * @return valid states. Can be null.
     */
    public @Nullable Set<String> getStates() {
        return states;
    }

    @Override
    public StateDescription createStateDescription(String unit, boolean readOnly) {
        List<StateOption> stateOptions = new ArrayList<>();
        final Set<String> states = this.states;
        if (states != null) {
            for (String state : states) {
                stateOptions.add(new StateOption(state, state));
            }
        }
        return new StateDescription(null, null, null, "%s " + unit, readOnly, stateOptions);
    }
}
