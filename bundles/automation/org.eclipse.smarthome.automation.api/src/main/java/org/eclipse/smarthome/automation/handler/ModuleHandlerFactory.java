/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 */
public interface ModuleHandlerFactory {

    /**
     * This method is used to return UIDs of module types supported by this {@link ModuleHandlerFactory}
     *
     * @return collection of module type unequal ids supported by this factory.
     */
    public Collection<String> getTypes();

    /**
     * This method is used to create ModuleHandler instance for the passed module
     * instance
     *
     * @param module module instance for which the {@link ModuleHandler} instance is
     *            created for.
     *
     * @return ModuleHandler instance.
     */
    public <T extends ModuleHandler> T create(Module module);

}
