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

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.RuleManager;

/**
 * A base class that can be used to create a {@link ModuleHandler} implementation.
 *
 * @author Kai Kreuzer - Initial Contribution
 */
public class BaseModuleHandler<T extends Module> implements ModuleHandler {

    /**
     * Holds the reference to the {@link Module} instance that it handles.
     */
    protected T module;

    /**
     * Holds the {@link ModuleHandlerCallback} reference. The {@link ModuleHandler} uses this callback to communicate
     * with the {@link RuleManager}.
     */
    protected ModuleHandlerCallback callback;

    /**
     * Creates a {@link ModuleHandler} instance.
     *
     * @param module the {@link Module} instance that will be handled.
     */
    public BaseModuleHandler(T module) {
        this.module = module;
    }

    @Override
    public void setCallback(ModuleHandlerCallback callback) {
        this.callback = callback;
    }

    /**
     * Clears the {@link ModuleHandlerCallback} reference and disposes the {@link ModuleHandler}.
     */
    @Override
    public void dispose() {
        this.callback = null;
    }
}
