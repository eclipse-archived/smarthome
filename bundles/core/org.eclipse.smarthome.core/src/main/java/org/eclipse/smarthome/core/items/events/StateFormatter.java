/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items.events;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.State;

/**
 * Formats and parses State instances.
 *
 * @author Henning Treu - initial contribution
 *
 */
class StateFormatter extends AbstractTypeFormatter<State> {

    private static final String STATE_PROPERTY_UNIT = "unit";
    private static final String STATE_PROPERTY_VALUE = "value";

    @Override
    String format(State state) {
        return state.toFullString();
    }

    @Override
    State parse(String type, String value, Map<String, String> stateMap) {
        String valueToParse = value;
        if (type.equals("Quantity") && stateMap != null && !stateMap.isEmpty()) {
            valueToParse = stateMap.get(STATE_PROPERTY_VALUE) + " " + stateMap.get(STATE_PROPERTY_UNIT);
        }
        return parseType(type, valueToParse, State.class);
    }

    Map<String, String> getStateMap(State state) {
        if (state instanceof QuantityType) {
            Map<String, String> stateMap = new HashMap<>();
            stateMap.put(STATE_PROPERTY_VALUE, ((QuantityType) state).toBigDecimal().toPlainString());
            stateMap.put(STATE_PROPERTY_UNIT, ((QuantityType) state).getUnit().toString());

            return stateMap;
        }
        return Collections.emptyMap();
    }

}