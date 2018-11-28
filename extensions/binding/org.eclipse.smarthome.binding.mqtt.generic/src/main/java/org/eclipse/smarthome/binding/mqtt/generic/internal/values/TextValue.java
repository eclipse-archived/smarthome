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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Implements a text/string value. Allows to restrict the incoming value to a set of states.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class TextValue implements Value {
    private State state = UnDefType.UNDEF;
    private StringType strValue;
    private final @Nullable Set<String> states;

    /**
     * Create a string value with a limited number of allowed states.
     *
     * @param states Allowed states. Empty states are filtered out. If the resulting set is empty, all string values
     *            will be allowed.
     */
    public TextValue(String[] states) {
        strValue = new StringType();
        Set<String> s = Stream.of(states).filter(e -> StringUtils.isNotBlank(e)).collect(Collectors.toSet());
        if (s.size() > 0) {
            this.states = s;
        } else {
            this.states = null;
        }
    }

    public TextValue() {
        strValue = new StringType();
        this.states = null;
    }

    @Override
    public State getValue() {
        return state;
    }

    @Override
    public String update(Command command) throws IllegalArgumentException {
        final Set<String> states = this.states;
        String value = command.toString();
        if (states != null && !states.contains(value)) {
            throw new IllegalArgumentException("Value " + value + " not within range");
        }
        strValue = new StringType(value);
        state = strValue;
        return value;
    }

    @Override
    public State update(String value) throws IllegalArgumentException {
        final Set<String> states = this.states;
        if (states != null && !states.contains(value)) {
            throw new IllegalArgumentException("Value " + value + " not within range");
        }
        strValue = new StringType(value);
        state = strValue;
        return strValue;
    }

    @Override
    public String getItemType() {
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
        return new StateDescription(null, null, null, "%s " + unit.replace("%", "%%"), readOnly, stateOptions);
    }

    @Override
    public void resetState() {
        state = UnDefType.UNDEF;
    }
}
