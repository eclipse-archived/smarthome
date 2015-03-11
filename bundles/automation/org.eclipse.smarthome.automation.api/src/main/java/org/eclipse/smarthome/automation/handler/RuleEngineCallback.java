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

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.type.Output;

/**
 * This is a callback interface to RuleEngine which is used by the {@link TriggerHandler} to notify the RuleEngine about
 * firing of the {@link Trigger}. These calls from {@link Trigger}s must be stored in a queue
 * and applied to the RuleAngine in order of their appearance. Each {@link Rule} has to create its own instance of
 * {@link RuleEngineCallback}.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public interface RuleEngineCallback {

    /**
     * This method is used by the {@link TriggerHandler} to notify the RuleEngine when
     * the liked {@link Trigger} instance was fired.
     *
     * @param trigger instance of trigger which was fired. When one TriggerHandler
     *            serve more then one {@link Trigger} instances, this parameter
     *            defines which trigger was fired.
     * @param outputs is a {@link Map} of output values of the triggered {@link Trigger}. Each entry of the map
     *            contains:
     *            <ul>
     *            <li><code>key</code> - the id of the {@link Output} ,
     *            <li><code>value</code> - represents output value of the {@link Trigger}'s {@link Output}
     *            </ul>
     */
    public void triggered(Trigger trigger, Map<String, ?> outputs);
}
