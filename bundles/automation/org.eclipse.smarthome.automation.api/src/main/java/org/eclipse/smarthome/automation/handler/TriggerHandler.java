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
