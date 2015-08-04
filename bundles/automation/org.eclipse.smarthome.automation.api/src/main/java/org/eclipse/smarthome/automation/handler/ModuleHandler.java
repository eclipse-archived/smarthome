/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.handler;

/**
 * A common interface for all module Handler interfaces. The Handler interfaces are
 * bridge between RuleEngine and external modules used by the RuleEngine.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @see ModuleHandlerFactory
 */
public interface ModuleHandler {

    /**
     * The method is called by RuleEngine to free resources when {@link ModuleHandler} is released.
     */
    public void dispose();
}
