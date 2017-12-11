/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import org.eclipse.smarthome.core.types.State;

/**
 * Convert a {@link State} to an {@link Item} accepted {@link State}.
 *
 * @author Henning Treu - initial refactoring as OSGi service
 *
 */
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
