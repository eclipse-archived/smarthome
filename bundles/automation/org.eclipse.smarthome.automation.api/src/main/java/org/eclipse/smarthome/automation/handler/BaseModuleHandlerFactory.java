/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.automation.Module;
import org.osgi.framework.BundleContext;

/**
 * This is a base class that can be used by any ModuleHandlerFactory implementation
 *
 * @author Kai Kreuzer - Initial Contribution
 * @author Benedikt Niehues - change behavior for unregistering ModuleHandler
 */
abstract public class BaseModuleHandlerFactory implements ModuleHandlerFactory {

    private Map<String, ModuleHandler> handlers = new HashMap<String, ModuleHandler>();
    protected BundleContext bundleContext;

    public void activate(BundleContext bundleContext) {
        if (bundleContext == null) {
            throw new IllegalArgumentException("BundleContext must not be null.");
        }
        this.bundleContext = bundleContext;
    }

    public void deactivate() {
        dispose();
    }

    protected Map<String, ModuleHandler> getHandlers() {
        return Collections.unmodifiableMap(handlers);
    }

    @Override
    public ModuleHandler getHandler(Module module, String ruleUID) {
        ModuleHandler handler = handlers.get(ruleUID + module.getId());
        if (handler == null) {
            handler = internalCreate(module, ruleUID);
            if (handler != null) {
                handlers.put(ruleUID + module.getId(), handler);
            }
        }
        return handler;
    }

    /**
     * Create a new handler for the given module.
     *
     * @param module the {@link Module} for which a handler shoult be created
     * @param ruleUID the id of the rule for which the handler should be created
     * @return A {@link ModuleHandler} instance or <code>null</code> if thins module type is not supported
     */
    abstract protected ModuleHandler internalCreate(Module module, String ruleUID);

    public void dispose() {
        for (ModuleHandler handler : handlers.values()) {
            if (handler != null) {
                handler.dispose();
            }
        }
        handlers.clear();
    }

    @Override
    public void ungetHandler(Module module, String ruleUID, ModuleHandler hdlr) {
        ModuleHandler handler = handlers.get(ruleUID + module.getId());
        if (handler != null) {
            this.handlers.remove(ruleUID + module.getId());
            if (!this.handlers.containsValue(hdlr)) {
                handler.dispose();
                handler = null;
            }
        }

    }
}
