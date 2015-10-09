/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.automation.core.internal.composite;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.Output;

public class CompositeTriggerHandler
        extends AbstractCompositeModuleHandler<Trigger, CompositeTriggerType, TriggerHandler>
        implements TriggerHandler, RuleEngineCallback {

    private RuleEngineCallback ruleCallback;

    public CompositeTriggerHandler(Trigger trigger, CompositeTriggerType mt,
            LinkedHashMap<Trigger, TriggerHandler> mapModuleToHandler, String ruleUID) {
        super(trigger, mt, mapModuleToHandler);
    }

    @Override
    public void triggered(Trigger trigger, Map<String, ?> context) {
        Set<Output> outputs = moduleType.getOutputs();
        Map<String, Object> result = new HashMap<>(11);
        for (Output output : outputs) {
            String refs = output.getReference();
            if (refs != null) {
                String ref;
                StringTokenizer st = new StringTokenizer(refs, ",");
                while (st.hasMoreTokens()) {
                    ref = st.nextToken().trim();
                    int i = ref.indexOf('.');
                    if (i != -1) {
                        String childModuleId = ref.substring(0, i);
                        if (trigger.getId().equals(childModuleId)) {
                            ref = ref.substring(i + 1);
                        }
                    }
                    Object value = context.get(ref);
                    if (value != null) {
                        result.put(output.getName(), value);
                    }
                }
            }
        }
        ruleCallback.triggered(module, result);
    }

    @Override
    public void setRuleEngineCallback(RuleEngineCallback ruleCallback) {
        this.ruleCallback = ruleCallback;
        List<Trigger> children = moduleType.getModules();
        for (Trigger child : children) {
            TriggerHandler handler = moduleHandlerMap.get(child);
            handler.setRuleEngineCallback(this);
        }
    }

    @Override
    public void dispose() {
        List<Trigger> children = moduleType.getModules();
        for (Trigger child : children) {
            TriggerHandler handler = moduleHandlerMap.get(child);
            handler.setRuleEngineCallback(null);
        }
        setRuleEngineCallback(null);
        super.dispose();
    }

}
