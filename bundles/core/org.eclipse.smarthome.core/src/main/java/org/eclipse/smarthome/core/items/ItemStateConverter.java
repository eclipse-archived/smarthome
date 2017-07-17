package org.eclipse.smarthome.core.items;

import org.eclipse.smarthome.core.types.State;

public interface ItemStateConverter {

    /**
     * Convert the given {@link State} to a state which is acceptable for the given {@link Item}.
     *
     * @param state the {@link State} to be converted.
     * @param item the {@link Item} for which the given state will be converted.
     * @return the converted {@link State} according to an accepted States of the given Item.
     */
    State convertToAcceptedState(State state, Item item);
}
