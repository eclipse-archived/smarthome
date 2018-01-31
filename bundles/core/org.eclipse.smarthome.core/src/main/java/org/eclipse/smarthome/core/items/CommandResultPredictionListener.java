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
package org.eclipse.smarthome.core.items;

import org.eclipse.smarthome.core.types.State;

/**
 * Interface which announces potential item state outcomes when a command was received.
 * <p>
 * This interface is intended to be implemented only within the framework itself and not meant to be implemented by
 * bindings.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public interface CommandResultPredictionListener {

    /**
     * Denotes that the item state is most likely going to change to the given value.
     *
     * @param item
     * @param state
     */
    void changeStateTo(Item item, State state);

    /**
     * Reconfirms the previous item state because a command for sure will not be successfully executed.
     * <p>
     * This is actually the exact opposite of {@link #changeStateTo(Item, State)}: it denotes that a recently received
     * command presumably will not result in a state change, e.g. because no handler currently is capable of delivering
     * such an event to its device.
     *
     * @param item
     */
    void keepCurrentState(Item item);

}
