/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence;

import org.eclipse.smarthome.core.items.Item;

/**
 * Provides an administration interface to allow the system to purge data from the persistence store.
 *
 * @author Chris Jackson - Initial implementation and API
 *
 */
public interface PersistenceAdminService {
    /**
     * Returns a list of items that are stored in the persistence service
     *
     * This is returned as a string to allow the persistence service to return items that are no long available as an
     * openHAB {@link Item}.
     *
     * @return list of strings of item names contained in the store
     */
    public Iterable<String> getItems();

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
    public boolean removeItem(String itemName);
}
