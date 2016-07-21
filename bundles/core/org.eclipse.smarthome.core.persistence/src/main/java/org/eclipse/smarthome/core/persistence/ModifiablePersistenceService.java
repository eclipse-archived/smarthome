/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence;

import java.util.Date;

import org.eclipse.smarthome.core.items.Item;

/**
 * This class provides an interface to the a {@link PersistenceService} to allow data to be stored
 * at a specific time. This allows bindings that interface to devices that store data internally,
 * and then periodically provide it to the server to be accommodated.
 *
 * @author Chris Jackson - Initial implementation and API
 *
 */
public interface ModifiablePersistenceService extends QueryablePersistenceService {
    /**
     * <p>
     * Stores the historic item value. This allows the item, time and value to be specified.
     * </p>
     * <p>
     * Implementors should keep in mind that all registered {@link PersistenceService}s are called synchronously. Hence
     * long running operations should be processed asynchronously. E.g. <code>store</code> adds things to a queue which
     * is processed by some asynchronous workers (Quartz Job, Thread, etc.).
     * </p>
     *
     * @param date the date of the record
     * @param item the data to be stored
     */
    public void store(Date date, Item item);

    /**
     * Removes a data from a persistence store.
     *
     * @param filter the filter to apply to the data removal
     * @return true if the query executed successfully
     */
    public boolean remove(FilterCriteria filter);

    /**
     * Removes an item and all data associated with the item
     *
     * @return true if the item was deleted
     */
    public boolean removeItemData(String itemName);
}
