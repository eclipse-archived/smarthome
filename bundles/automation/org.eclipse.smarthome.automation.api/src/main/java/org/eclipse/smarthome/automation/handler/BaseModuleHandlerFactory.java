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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.Module;

/**
 * This is a base class that can be used by any ModuleHandlerFactory implementation
 *
 * @author Kai Kreuzer - Initial Contribution
 * @author Benedikt Niehues - change behavior for unregistering ModuleHandler
 */
@NonNullByDefault
public abstract class BaseModuleHandlerFactory implements ModuleHandlerFactory {

    private final Map<@NonNull String, @NonNull ModuleHandler> handlers = new HashMap<>();

    protected void deactivate() {
        for (ModuleHandler handler : handlers.values()) {
            handler.dispose();
        }
        handlers.clear();
    }

    protected Map<String, ModuleHandler> getHandlers() {
        return Collections.unmodifiableMap(handlers);
    }

    @Override
    @SuppressWarnings("null")
    public @Nullable ModuleHandler getHandler(Module module, String ruleUID) {
        String id = ruleUID + module.getId();
        ModuleHandler handler = handlers.get(id);
        handler = handler == null ? internalCreate(module, ruleUID) : handler;
        if (handler != null) {
            handlers.put(id, handler);
        }
        return handler;
    }

    /**
     * Create a new handler for the given module.
     *
     * @param module  the {@link Module} for which a handler should be created
     * @param ruleUID the id of the rule for which the handler should be created
     * @return A {@link ModuleHandler} instance or {@code null} if thins module type is not supported
     */
    protected abstract @Nullable ModuleHandler internalCreate(Module module, String ruleUID);

    @Override
    public void ungetHandler(Module module, String ruleUID, ModuleHandler handler) {
        if (handlers.remove(ruleUID + module.getId(), handler)) {
            handler.dispose();
        }
    }
}
