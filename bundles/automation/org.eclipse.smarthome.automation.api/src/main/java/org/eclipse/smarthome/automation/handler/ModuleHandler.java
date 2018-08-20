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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.automation.type.ModuleType;

/**
 * This interface is used as common interface for all Module Handlers. The {@link ModuleHandler}s provide the logic
 * behind the rule's modules. Each of them is used to process a different type of modules described by corresponding
 * {@link ModuleType} definition.
 * <p>
 * Module Handlers provide a mechanism to extend the capabilities of the automation module by providing new
 * {@link ModuleType}s and the logic needed for processing them.
 * <p>
 * {@link ModuleHandler} instances are created by {@link ModuleHandlerFactory}s.
 *
 * @see ModuleHandlerFactory
 * @author Yordan Mihaylov - Initial Contribution
 */
@NonNullByDefault
public interface ModuleHandler {

    /**
     * Releases the reserved resources when a {@link ModuleHandler} instance is not needed anymore.
     */
    public void dispose();

    /**
     * The callback is injected to the handler through this method.
     *
     * @param callback a {@link ModuleHandlerCallback} instance
     */
    void setCallback(ModuleHandlerCallback callback);

}
