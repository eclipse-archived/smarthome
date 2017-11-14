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
 * <p>
 * This interface must be implemented by all classes that want to be notified about changes in the state of an item.
 * 
 * <p>
 * The {@link GenericItem} class provides the possibility to register such listeners.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public interface StateChangeListener {

    /**
     * This method is called, if a state has changed.
     * 
     * @param item the item whose state has changed
     * @param oldState the previous state
     * @param newState the new state
     */
    public void stateChanged(Item item, State oldState, State newState);

    /**
     * This method is called, if a state was updated, but has not changed
     * 
     * @param item the item whose state was updated
     * @param state the current state, same before and after the update
     */
    public void stateUpdated(Item item, State state);

}
