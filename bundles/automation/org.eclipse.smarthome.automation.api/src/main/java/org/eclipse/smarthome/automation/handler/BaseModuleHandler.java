/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.handler;

import org.eclipse.smarthome.automation.Module;

/**
 * This is a base class that can be used by any ModuleHandler implementation
 *
 * @author Kai Kreuzer - Initial Contribution
 */
public class BaseModuleHandler<T extends Module> implements ModuleHandler {

    protected T module;

    public BaseModuleHandler(T module) {
        this.module = module;
    }

    @Override
    public void dispose() {
        // can be overridden
    }

}
