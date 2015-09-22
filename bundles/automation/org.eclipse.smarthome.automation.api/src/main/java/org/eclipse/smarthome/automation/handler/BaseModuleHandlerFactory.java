/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.automation.Module;
import org.osgi.framework.BundleContext;

/**
 * This is a base class that can be used by any ModuleHandlerFactory implementation
 *
 * @author Kai Kreuzer - Initial Contribution
 */
abstract public class BaseModuleHandlerFactory implements ModuleHandlerFactory {

    protected List<ModuleHandler> handlers;
    protected BundleContext bundleContext;

    public BaseModuleHandlerFactory(BundleContext bundleContext) {
        if (bundleContext == null) {
            throw new IllegalArgumentException("BundleContext must not be null.");
        }
        this.bundleContext = bundleContext;
        handlers = new ArrayList<ModuleHandler>();
    }

    @Override
    public ModuleHandler create(Module module) {
        ModuleHandler handler = internalCreate(module);
        handlers.add(handler);
        return handler;
    }

    abstract protected ModuleHandler internalCreate(Module module);

    public void dispose() {
        for (ModuleHandler handler : handlers) {
            handler.dispose();
        }
        handlers.clear();
    }
}
