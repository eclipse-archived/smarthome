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

import org.eclipse.smarthome.automation.Trigger;

/**
 * This class provides a {@link TriggerHandler} base implementation.
 *
 * @author Vasil Ilchev - Initial contribution
 */
public class BaseTriggerModuleHandler extends BaseModuleHandler<Trigger> implements TriggerHandler {

    /**
     * Creates a new {@link BaseTriggerModuleHandler} instance for the given {@code module}.
     *
     * @param module the {@link Trigger} module to handle.
     */
    public BaseTriggerModuleHandler(Trigger module) {
        super(module);
    }
}
