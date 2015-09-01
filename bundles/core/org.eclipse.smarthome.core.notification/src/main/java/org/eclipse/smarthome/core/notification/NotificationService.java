/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.notification;

import java.util.List;

import org.eclipse.smarthome.core.events.Event;

/**
 * A persistence service which can be used to store data from openHAB.
 * This must not necessarily be a local database, a persistence service
 * can also be cloud-based or a simply data-export facility (e.g.
 * for sending data to an IoT (Internet of Things) service.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public interface NotificationService {

    /**
     * Returns the name of this {@link NotificationService}.
     * This name is used to uniquely identify the {@link NotificationService}.
     *
     * @return the name to uniquely identify the {@link NotificationService}.
     */
    String getName();

    /**
     * Stores the current value of the given item.
     * <p>
     * Implementors should keep in mind that all registered {@link PersistenceService}s are called synchronously. Hence
     * long running operations should be processed asynchronously. E.g. <code>store</code> adds things to a queue which
     * is processed by some asynchronous workers (Quartz Job, Thread, etc.).
     * </p>
     *
     * @param payload
     *
     * @param item the item which state should be persisted.
     */
    void notify(String target, List<String> options, Event event);

}
