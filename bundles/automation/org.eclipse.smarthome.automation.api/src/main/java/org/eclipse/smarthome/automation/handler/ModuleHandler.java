/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
