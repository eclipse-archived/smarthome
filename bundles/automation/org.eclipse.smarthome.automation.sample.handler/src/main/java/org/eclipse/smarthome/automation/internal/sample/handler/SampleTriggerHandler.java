/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.sample.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.handler.TriggerHandler;

/**
 * Trigger Handler sample implementation
 *
 * @author Vasil Ilchev - Initial Contribution
 * @author Kai Kreuzer - refactored and simplified customized module handling
 */
public class SampleTriggerHandler extends BaseModuleHandler<Trigger>implements TriggerHandler {
    private static final String OUTPUT_REFERENCE = "triggerOutput";
    private RuleEngineCallback ruleCallback;
    private String ruleUID;

    public SampleTriggerHandler(Trigger module, String ruleUID) {
        super(module);
        this.ruleUID = ruleUID;
    }

    public void trigger(String triggerParam) {
        Map<String, Object> outputs = new HashMap<String, Object>();
        outputs.put(OUTPUT_REFERENCE, triggerParam);
        ruleCallback.triggered(module, outputs);
    }

    String getTriggerID() {
        return module.getId();
    }

    @Override
    public void setRuleEngineCallback(RuleEngineCallback ruleCallback) {
        this.ruleCallback = ruleCallback;
    }

    public String getRuleUID() {
        return ruleUID;
    }

}
