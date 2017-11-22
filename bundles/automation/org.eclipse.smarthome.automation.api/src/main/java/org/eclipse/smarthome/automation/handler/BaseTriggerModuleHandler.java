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

import org.eclipse.smarthome.automation.Trigger;

/**
 * This is a base class that can be used by TriggerModuleHandler implementations
 *
 * @author Vasil Ilchev
 */
public class BaseTriggerModuleHandler extends BaseModuleHandler<Trigger> implements TriggerHandler {

    /**
     * Use this callback to 'trigger' this TriggerHandler's Rule
     */
    protected RuleEngineCallback ruleEngineCallback;

    public BaseTriggerModuleHandler(Trigger module) {
        super(module);
    }

    @Override
    public void setRuleEngineCallback(RuleEngineCallback ruleCallback) {
        this.ruleEngineCallback = ruleCallback;
    }

    /**
     * Remove ruleEngineCallback reference - stop triggering the Rule.
     */
    @Override
    public void dispose() {
        super.dispose();
        this.ruleEngineCallback = null;
    }

}
