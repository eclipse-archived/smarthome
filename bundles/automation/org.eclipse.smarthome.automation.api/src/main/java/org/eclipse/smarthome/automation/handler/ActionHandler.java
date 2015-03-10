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

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Rule;

/**
 * This interface is implemented by external modules which are called by the
 * RuleEngine to execute {@link Action}s of the {@link Rule}s.
 */
public interface ActionHandler extends ModuleHandler {

    /**
     * The Method is called by the RuleEngine to execute a {@link Rule} {@link Action}.
     *
     *
     * @param inputs input values of the Action
     * @return values map of values which must be set to outputs of the {@link Action}.
     */
    public Map<String, Object> execute(Map<String, ?> inputs);

}
