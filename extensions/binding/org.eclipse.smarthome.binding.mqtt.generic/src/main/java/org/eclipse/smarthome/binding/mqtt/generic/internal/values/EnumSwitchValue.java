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
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Implements a switch with multiple possible states.
 * If you want a binary switch only, use {@link OnOffValue} instead.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class EnumSwitchValue implements Value {
    private State state = UnDefType.UNDEF;
    private StringType strValue;
    private final Set<String> states;

    public EnumSwitchValue(String[] states, int initialStateIndex) {
        if (states.length == 0) {
            throw new IllegalArgumentException("At least one state need to be known!");
        }
        strValue = new StringType(states[initialStateIndex]);
        this.states = Stream.of(states).collect(Collectors.toSet());
    }

    @Override
    public State getValue() {
        return state;
    }

    @Override
    public String update(Command command) throws IllegalArgumentException {
        String value = command.toString();
        if (!states.contains(value)) {
            throw new IllegalArgumentException("Value " + value + " not within range");
        }
        strValue = new StringType(value);
        state = strValue;
        return strValue.toString();
    }

    @Override
    public State update(String value) throws IllegalArgumentException {
        if (!this.states.contains(value)) {
            throw new IllegalArgumentException("Value " + value + " not within range");
        }
        strValue = new StringType(value);
        state = strValue;
        return strValue;
    }

    @Override
    public String channelTypeID() {
        return CoreItemFactory.STRING;
    }

    /**
     * @return Valid states
     */
    public Set<String> getStates() {
        return states;
    }

    @Override
    public StateDescription createStateDescription(String unit, boolean readOnly) {
        List<StateOption> stateOptions = new ArrayList<>();
        for (String state : states) {
            stateOptions.add(new StateOption(state, state));
        }
        return new StateDescription(null, null, null, "%s " + unit, readOnly, stateOptions);
    }

    @Override
    public void resetState() {
        state = UnDefType.UNDEF;
    }
}
