/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.handler;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common implementation for {@link TriggerHandler}s.
 *
 * @author Vasil Ilchev - Initial Contribution
 *
 */
public abstract class BaseTriggerHandler extends BaseModuleHandler<Trigger>implements TriggerHandler {
    protected RuleEngineCallback ruleCallBack;
    private Logger log;

    public BaseTriggerHandler(Trigger module, List<ModuleType> moduleTypes) {
        super(module, moduleTypes);
        log = LoggerFactory.getLogger(BaseTriggerHandler.class);
    }

    @Override
    public void setRuleEngineCallback(RuleEngineCallback ruleCallback) {
        this.ruleCallBack = ruleCallback;
    }

    /**
     * Entry point for triggering the TriggerHandler.
     * Calling this method will cause triggering the TriggerHandler.
     */
    public void trigger() {
        if (ruleCallBack != null) {
            Map<String, Object> triggerValues = getTriggerValues();
            Map<String, Object> resolvedInputs = getResolvedInputs(null);
            Map<String, Object> resolvedConfiguration = getResolvedConfiguration(resolvedInputs);
            Map<String, Object> resolvedOutputs = getResolvedOutputs(resolvedConfiguration, resolvedInputs,
                    triggerValues);
            ruleCallBack.triggered(module, resolvedOutputs);
        } else {
            log.error("RuleCallback in TriggerHandler with [typeUID=" + module.getTypeUID() + "is null");
        }

    }

    /**
     * {@link Output}s can have reference(in their <code>reference</code> property) to the mechanism that has triggered
     * this {@link Trigger}.
     * By implementing this method inheritors may pass subset(or the whole) trigger mechanism to their {@link Output}s
     * If {@link Output} has reference to the trigger mechanism but <code>null</code> is returned {@link Output}
     * defaultValues
     * will be used.
     *
     * @return subset(or the whole) trigger mechanism represented by Map.
     */
    protected abstract Map<String, Object> getTriggerValues();

}
