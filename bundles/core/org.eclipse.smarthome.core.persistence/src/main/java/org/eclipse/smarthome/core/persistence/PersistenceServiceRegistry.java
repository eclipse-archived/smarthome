/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence;

import java.util.Set;

/**
 * This is the interface for a central service that provides access to {@link PersistenceService}s.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public interface PersistenceServiceRegistry {

    /**
     * Get the default persistence service.
     *
     * @return {@link PersistenceService} default service
     */
    PersistenceService getDefault();

    /**
     * Get the persistence service with the given id.
     *
     * @param serviceId the service id
     * @return {@link PersistenceService} the service with the id or null, if not present
     */
    PersistenceService get(String serviceId);

    /**
     * Get the id of the default persistence service.
     *
     * @return the id of the default persistence service or null, if no default service is defined
     */
    String getDefaultId();

    /**
     * Returns all available persistence services.
     *
     * @return all available persistence services
     */
    Set<PersistenceService> getAll();

}