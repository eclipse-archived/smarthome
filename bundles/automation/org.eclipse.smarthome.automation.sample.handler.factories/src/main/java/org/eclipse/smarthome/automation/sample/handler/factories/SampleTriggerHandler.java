/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.sample.handler.factories;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.AbstractModuleHandler;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.parser.Converter;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.slf4j.Logger;

/**
 * Trigger Handler sample implementation
 *
 * @author Vasil Ilchev - Initial Contribution
 */
public class SampleTriggerHandler extends AbstractModuleHandler implements TriggerHandler {
    public static final String PARSE_OUTPUT_REFERENCE = "consoleInput";
    public static final String PARSE_DOLLAR_SYMBOL = "$";
    protected Map<String, ?> configuration;
    protected String functionalItemUID;
    protected Trigger trigger;
    protected TriggerType triggerType;
    //
    private RuleEngineCallback ruleCallBack;
    private Logger log;
    private SampleHandlerFactory handlerFactory;

    public SampleTriggerHandler(SampleHandlerFactory handlerFactory, Trigger trigger, TriggerType triggerType,
            Logger log) {
        super(trigger);
        this.trigger = trigger;
        this.triggerType = triggerType;
        this.configuration = trigger.getConfiguration();
        this.log = log;
        this.handlerFactory = handlerFactory;
    }

    @Override
    public void setRuleEngineCallback(RuleEngineCallback ruleCallback) {
        this.ruleCallBack = ruleCallback;
    }

    public void trigger(String param) {
        if (ruleCallBack != null) {
            Map<String, Object> resolvedConfiguration = getResolvedConfiguration(null);
            Map<String, Object> systemOutputs = getSystemOutputsValues(triggerType.getOutputs(), param);
            Map<String, Object> resolvedOutputs = getResolvedOutputs(resolvedConfiguration, null, systemOutputs);
            ruleCallBack.triggered(trigger, resolvedOutputs);
        } else {
            log.error("RuleCallback in TriggerHandler is null");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        handlerFactory.disposeHandler(this);
    }

    protected Map<String, Object> getSystemOutputsValues(Set<Output> outputs, String param) {
        Map<String, Object> resultOutputs = null;
        for (Output output : outputs) {
            if (resultOutputs == null) {
                resultOutputs = new HashMap<String, Object>(11);
            }
            String propertyName = output.getName();
            Object propertyValue = null;
            if (output.getReference() != null) {
                if (output.getReference().equals(PARSE_OUTPUT_REFERENCE)) {
                    propertyValue = param;
                } else {
                    propertyValue = parse(output.getReference());
                }
            }
            if (propertyValue == null) {
                propertyValue = output.getDefaultValue();
            }
            resultOutputs.put(propertyName, propertyValue);
        }
        return resultOutputs;
    }

    protected Object parse(String reference) {
        String parsedReference = parseReference(reference);
        return configuration.get(parsedReference);
    }

    String getTriggerID() {
        return trigger.getId();
    }

    @Override
    protected ModuleTypeRegistry getModuleTypeRegistry() {
        return SampleHandlerFactory.getModuleTypeRegistry();
    }

    @Override
    protected Converter getConverter() {
        return SampleHandlerFactory.getConverter();
    }

    private String parseReference(String reference) {
        if (reference.startsWith("$")) {
            reference = reference.substring(1);
        }
        return reference;
    }
}
