/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.notification.consumer;

import java.util.List;

import org.eclipse.smarthome.notification.Notification;

/**
 * A notification service which can be used to route events from ESH to an external notification provider
 *
 * @author Karel Goderis - Initial contribution and API
 */
public interface NotificationService {

    /**
     * Returns the name of this {@link NotificationService}.
     * This name is used to uniquely identify the {@link NotificationService}.
     *
     * @return the name to uniquely identify the {@link NotificationService}.
     */
    String getName();

    void notify(String target, List<String> options, Notification notification);

}
