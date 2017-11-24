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

import java.util.Collection;

import org.eclipse.smarthome.automation.Module;

/**
 * This interface is a factory of {@link ModuleHandler} instances. It is used to
 * create {@link TriggerHandler}, {@link ConditionHandler} and {@link ActionHandler} objects
 * base on the type of the passed {@link Module} instance. The ModuleHandlerFactory
 * is register as service in OSGi framework and it can serve more then one
 * module types. It is used by automation parser to associate {@link ModuleHandler} instance to passed {@link Module}
 * instance.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Benedikt Niehues - change behavior for unregistering ModuleHandler
 */
public interface ModuleHandlerFactory {

    /**
     * This method is used to return UIDs of module types supported by this {@link ModuleHandlerFactory}
     *
     * @return collection of module type unequal UID supported by this factory.
     */
    public Collection<String> getTypes();

    /**
     * This method is used to get a ModuleHandler instance for the passed module
     * instance
     *
     * @param module module instance for which the {@link ModuleHandler} instance is created.
     * @param ruleUID the UID of the rule for which the handler instance is created.
     * @return {@link ModuleHandler} instance.
     */
    public ModuleHandler getHandler(Module module, String ruleUID);

    /**
     * This method signalises the Factory that a ModuleHandler for the passed module is not needed anymore. Implementors
     * must take care of invalidating caches and disposing the Handlers.
     *
     * @param module
     */
    public void ungetHandler(Module module, String ruleUID, ModuleHandler handler);

}
