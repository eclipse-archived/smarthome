/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.handler;

import java.util.Map;

import org.eclipse.smarthome.automation.Module;

/**
 * A common interface for all module handler interfaces. The handler interfaces are
 * bridge between RuleEngine and external modules used by the RuleEngine.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @see ModuleHandlerFactory
 */
public interface ModuleHandler {

    /**
     * This method update configuration values set to corresponding of {@link Module} instance
     *
     * @param configuration updated configuration values of {@link Module} instance
     */
    public void setConfiguration(Map<String, ?> configuration);

    /**
     * The method is called by RuleEngine to free resources when {@link ModuleHandler} is released.
     */
    public void dispose();
}
