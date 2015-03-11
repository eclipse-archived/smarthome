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

import org.eclipse.smarthome.automation.Condition;

/**
 * This interface is implemented by external modules, which are called by the
 * RuleEngine, when it has to check if the {@link Condition} is satisfied or
 * not.
 *
 */
public interface ConditionHandler extends ModuleHandler {

    /**
     * Method that states if the Condition is satisfied or not
     *
     * @param inputs the values of condition inputs.
     * @return true if Condition is satisfied, false otherwise
     */
    public boolean isSatisfied(Map<String, ?> inputs);

}
