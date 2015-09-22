/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.core.custom;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common implementation for {@link TriggerHandler}s.
 *
 * @author Vasil Ilchev - Initial Contribution
 * @author Kai Kreuzer - refactored and simplified customized module handling
 *
 */
public class CustomizedTriggerHandler extends AbstractCustomizedModuleHandler<TriggerHandler, Trigger>
        implements TriggerHandler, RuleEngineCallback {
    protected RuleEngineCallback ruleCallBack;
    private Logger log;

    public CustomizedTriggerHandler(TriggerHandler parentHandler, Trigger parentModule, Trigger module,
            List<ModuleType> moduleTypes) {
        super(parentHandler, parentModule, module, moduleTypes);
        log = LoggerFactory.getLogger(CustomizedTriggerHandler.class);
    }

    @Override
    public void setRuleEngineCallback(RuleEngineCallback ruleCallback) {
        this.ruleCallBack = ruleCallback;
    }

    @Override
    public void triggered(Trigger trigger, Map<String, ?> outputs) {
        if (ruleCallBack != null) {
            Map<String, ?> triggerValues = outputs;
            Map<String, Object> resolvedInputs = getResolvedInputs(null);
            Map<String, Object> resolvedConfiguration = getResolvedConfiguration(resolvedInputs);
            parentModule.setConfiguration(resolvedConfiguration);
            Map<String, Object> resolvedOutputs = getResolvedOutputs(resolvedConfiguration, resolvedInputs,
                    triggerValues);
            ruleCallBack.triggered(module, resolvedOutputs);
        } else {
            log.error("RuleCallback in TriggerHandler with [typeUID=" + module.getTypeUID() + "is null");
        }
    }

}
