/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.handler;

/**
 * This Handler interface is used by the RuleEngine to set a callback interface to
 * itself. The callback has to implemented {@link RuleEngineCallback} interface
 * and it is used to notify the RuleEngine when {@link TriggerHandler} was triggered
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public interface TriggerHandler extends ModuleHandler {

    /**
     * This method is used to set a callback object to the RuleEngine
     *
     * @param ruleCallback a callback object to the RuleEngine.
     */
    public void setRuleEngineCallback(RuleEngineCallback ruleCallback);
}
