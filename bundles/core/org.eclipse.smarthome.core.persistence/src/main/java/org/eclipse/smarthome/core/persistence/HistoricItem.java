/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence;

import java.util.Date;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.types.State;

/**
 * This interface is used by persistence services to represent an item
 * with a certain state at a given point in time.
 *
 * <p>
 * Note that this interface does not extend {@link Item} as the persistence services could not provide an implementation
 * that correctly implement getAcceptedXTypes() and getGroupNames().
 * </p>
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public interface HistoricItem {

    /**
     * returns the timestamp of the persisted item
     *
     * @return the timestamp of the item
     */
    Date getTimestamp();

    /**
     * returns the current state of the item
     *
     * @return the current state
     */
    State getState();

    /**
     * returns the name of the item
     *
     * @return the name of the item
     */
    String getName();

}
