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
     * Adding data with the same time as an existing record should update the current record value rather than adding a
     * new record.
     * </p>
     * <p>
     * Implementors should keep in mind that all registered {@link PersistenceService}s are called synchronously. Hence
     * long running operations should be processed asynchronously. E.g. <code>store</code> adds things to a queue which
     * is processed by some asynchronous workers (Quartz Job, Thread, etc.).
     * </p>
     *
     * @param item the data to be stored
     * @param date the date of the record
     * @param state the state to be recorded
     */
    void store(Item item, Date date, State state);

    /**
     * Removes data associated with an item from a persistence service.
     * If all data is removed for the specified item, the persistence service should free any resources associated with
     * the item (eg. remove any tables or delete files from the storage).
     *
     * @param filter the filter to apply to the data removal. ItemName can not be null.
     * @return true if the query executed successfully
     * @throws {@link IllegalArgumentException} if item name is null.
     */
    boolean remove(FilterCriteria filter) throws IllegalArgumentException;
}
