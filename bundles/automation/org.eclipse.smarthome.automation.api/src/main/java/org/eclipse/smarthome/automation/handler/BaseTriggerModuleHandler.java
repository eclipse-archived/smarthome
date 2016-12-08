/**
 * Copyright (c) 1997, 2016 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
