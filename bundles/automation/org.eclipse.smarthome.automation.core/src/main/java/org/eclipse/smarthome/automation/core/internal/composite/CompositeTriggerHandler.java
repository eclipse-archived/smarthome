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
import java.util.StringTokenizer;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.core.internal.ReferenceResolverUtil;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.Output;

/**
 * This class is a handler implementation for {@link CompositeTriggerType}. The trigger which has
 * {@link CompositeTriggerType} has to be notified by the handlers of child triggers and it will be triggered when some
 * of them is triggered. The handler has to put outputs of the trigger, base on the outputs of the child triggers, into
 * rule context. The outputs of the child triggers are not visible out of context of the trigger.
 *
 * @author Yordan Mihaylov - Initial Contribution
 *
 */
public class CompositeTriggerHandler
        extends AbstractCompositeModuleHandler<Trigger, CompositeTriggerType, TriggerHandler>
        implements TriggerHandler, RuleEngineCallback {

    private RuleEngineCallback ruleCallback;

    /**
     * Constructor of this system handler.
     *
     * @param trigger trigger of composite type (parent trigger).
     * @param mt module type of parent trigger
     * @param mapModuleToHandler map of pairs child triggers to their handlers
     * @param ruleUID UID of rule where the parent trigger is part of
     */
    public CompositeTriggerHandler(Trigger trigger, CompositeTriggerType mt,
            LinkedHashMap<Trigger, TriggerHandler> mapModuleToHandler, String ruleUID) {
        super(trigger, mt, mapModuleToHandler);
    }

    /**
     * This method is called by the child triggers defined by the {@link CompositeTriggerType} of parent trigger.
     * The method goes through the outputs of the parent trigger and fill them base on the ouput's reference value.
     * The ouput's reference value can contain more then one references to the child outputs separated by comma. In this
     * case the method will try to fill the output value in sequence defined in the reference value. The letter
     * reference can be overwritten by the previous ones.
     *
     * @see org.eclipse.smarthome.automation.handler.RuleEngineCallback#triggered(org.eclipse.smarthome.automation.Trigger,
     *      java.util.Map)
     */
    @Override
    public void triggered(Trigger trigger, Map<String, ?> context) {
        if (ruleCallback != null) {
            List<Output> outputs = moduleType.getOutputs();
            Map<String, Object> result = new HashMap<String, Object>(11);
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
                        Object value = null;
                        int idx = ReferenceResolverUtil.getNextRefToken(ref, 1);
                        if (idx < ref.length()) {
                            String outputId = ref.substring(0, idx);
                            value = ReferenceResolverUtil.getValue(context.get(outputId), ref.substring(idx + 1));
                        } else {
                            value = context.get(ref);
                        }
                        if (value != null) {
                            result.put(output.getName(), value);
                        }
                    }
                }
            }
            ruleCallback.triggered(module, result);
        }
    }

    /**
     * The {@link CompositeTriggerHandler} sets itself as callback to the child triggers and store the callback to the
     * rule engine. In this way the trigger of composite type will be notified always when some of the child triggers
     * are triggered and has an opportunity to set the outputs of parent trigger to the rule context.
     *
     * @see org.eclipse.smarthome.automation.handler.TriggerHandler#setRuleEngineCallback(org.eclipse.smarthome.automation.handler.RuleEngineCallback)
     */
    @Override
    public void setRuleEngineCallback(RuleEngineCallback ruleCallback) {
        this.ruleCallback = ruleCallback;
        if (ruleCallback != null) {// could be called with 'null' from dispose
            List<Trigger> children = getChildren();
            for (Trigger child : children) {
                TriggerHandler handler = moduleHandlerMap.get(child);
                handler.setRuleEngineCallback(this);
            }
        }
    }

    @Override
    public void dispose() {
        setRuleEngineCallback(null);
        super.dispose();
    }

    @Override
    protected List<Trigger> getChildren() {
        return moduleType.getChildren();
    }

}
