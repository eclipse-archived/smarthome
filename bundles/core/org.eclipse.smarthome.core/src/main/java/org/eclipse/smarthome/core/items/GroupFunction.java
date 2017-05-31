/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Group functions are used by active group items to calculate a state for the group
 * out of the states of all its member items.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public interface GroupFunction {

    /**
     * Determines the current state of a group based on a list of items
     *
     * @param items the items to calculate a group state for
     * @return the calculated group state
     */
    State calculate(Set<Item> items);

    /**
     * Calculates the group state and returns it as a state of the requested type.
     *
     * @param items the items to calculate a group state for
     * @param stateClass the type in which the state should be returned
     * @return the calculated group state of the requested type or null, if type is not supported
     */
    State getStateAs(Set<Item> items, Class<? extends State> stateClass);

    /**
     * Returns the parameters of the function as an array.
     *
     * @return the parameters of this function
     */
    State[] getParameters();

    /**
     * This is the default group function that does nothing else than to check if all member items
     * have the same state. If this is the case, this state is returned, otherwise UNDEF is returned.
     *
     * @author Kai Kreuzer - Initial contribution and API
     *
     */
    static class Equality implements GroupFunction {

        @Override
        public State calculate(Set<Item> items) {
            if (items.size() > 0) {
                Iterator<Item> it = items.iterator();
                State state = it.next().getState();
                while (it.hasNext()) {
                    if (!state.equals(it.next().getState())) {
                        return UnDefType.UNDEF;
                    }
                }
                return state;
            } else {
                return UnDefType.UNDEF;
            }
        }

        @Override
        public State getStateAs(Set<Item> items, Class<? extends State> stateClass) {
            State state = calculate(items);
            if (stateClass.isInstance(state)) {
                return state;
            } else {
                return null;
            }
        }

        @Override
        public State[] getParameters() {
            return new State[0];
        }
    }

}
