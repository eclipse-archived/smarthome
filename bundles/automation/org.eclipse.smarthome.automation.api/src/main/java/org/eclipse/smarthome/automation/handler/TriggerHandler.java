/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation.handler;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;

/**
 * This interface provides common functionality for processing {@link Trigger} modules. It is used to set a callback
 * interface to itself. The callback has to implemented {@link TriggerHandlerCallback} interface and it is used to
 * notify when the rule's {@link Trigger} was triggered.
 *
 * @see ModuleHandler
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public interface TriggerHandler extends ModuleHandler {

    /**
     * Injects the {@link TriggerHandlerCallback} instance to the handler through this method.
     *
     * @param callback a {@link TriggerHandlerCallback} instance that can be used to notify when the {@link Rule}
     *                 should trigger its execution.
     */
    @Override
    void setCallback(ModuleHandlerCallback callback);
}
