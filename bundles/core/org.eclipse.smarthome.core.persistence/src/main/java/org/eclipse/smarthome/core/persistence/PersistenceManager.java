/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence;

/**
 * A persistence manager service which could be used to start event handling or supply configuration for persistence
 * services.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public interface PersistenceManager {
    /**
     * Add a configuration for a persistence service.
     *
     * @param dbId the database id used by the persistence service
     * @param config the configuration of the persistence service
     */
    void addConfig(String dbId, PersistenceServiceConfiguration config);

    /**
     * Remove a configuration for a persistence service.
     *
     * @param dbId the database id used by the persistence service
     */
    void removeConfig(String dbId);

    /**
     * Start the event handling for a persistence service.
     *
     * @param dbId the database id used by the persistence service
     */
    void startEventHandling(String dbId);

    /**
     * Stop the event handling for a persistence service.
     *
     * @param dbId the database id used by the persistence service
     */
    void stopEventHandling(String dbId);
}
