/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.handler;

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

    protected Map<String, ModuleHandler> handlers;
    protected BundleContext bundleContext;

    public BaseModuleHandlerFactory(BundleContext bundleContext) {
        if (bundleContext == null) {
            throw new IllegalArgumentException("BundleContext must not be null.");
        }
        this.bundleContext = bundleContext;
        handlers = new HashMap<String, ModuleHandler>();
    }

    @Override
    public ModuleHandler getHandler(Module module, String ruleUID) {
        ModuleHandler handler = internalCreate(module, ruleUID);
        if (handler != null) {
            handlers.put(ruleUID + module.getId(), handler);
        }
        return handler;
    }

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
        hdlr.dispose();
        ModuleHandler handler = handlers.remove(ruleUID + module.getId());
        if (handler != null) {
            if (hdlr != handler) {
                handler.dispose();
            }
        }

    }
}
