/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.items.Item;

/**
 * A queryable persistence service which can be used to store and retrieve
 * data from Eclipse SmartHome. This is most likely some kind of database system.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Chris Jackson - Added getItems method
 */
public interface QueryablePersistenceService extends PersistenceService {

    /**
     * Queries the {@link PersistenceService} for data with a given filter criteria
     *
     * @param filter the filter to apply to the query
     * @return a time series of items
     */
    @NonNull
    Iterable<@NonNull HistoricItem> query(@NonNull FilterCriteria filter);

    /**
     * Returns a list of items that are stored in the persistence service
     *
     * This is returned as a string to allow the persistence service to return items that are no long available as an
     * ESH {@link Item}.
     *
     * @return list of strings of item names contained in the store. Not null.
     */
    @NonNull
    Set<@NonNull PersistenceItemInfo> getItemInfo();
}
