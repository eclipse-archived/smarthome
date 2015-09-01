/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.TriggerHandler;

/**
 * @author Yordan Mihaylov - Initial Contribution
 */
public class RuntimeTrigger extends Trigger implements SourceModule {

    private TriggerHandler triggerHandler;
    private Map<String, ?> outputs;

    public RuntimeTrigger(String id, String typeUID, Map<String, ?> configuration) {
        super(id, typeUID, configuration);
    }

    // protected TriggerImpl(TriggerImpl trigger) {
    // super(trigger.getId(), trigger.getTypeUID(), trigger.getConfiguration());
    // setLabel(trigger.getLabel());
    // setDescription(trigger.getDescription());
    // }

    public RuntimeTrigger(Trigger trigger) {
        super(trigger.getId(), trigger.getTypeUID(), trigger.getConfiguration());
        setLabel(trigger.getLabel());
        setDescription(trigger.getDescription());
    }

    @Override
    public void setConfiguration(Map<String, ?> configuration) {
        this.configuration = configuration != null ? new HashMap<String, Object>(configuration) : null;
    }

    @Override
    public void setOutputs(Map<String, ?> outputs) {
        this.outputs = outputs;
    }

    @Override
    public Object getOutputValue(String outName) {
        return outputs != null ? outputs.get(outName) : null;
    }

    /**
     * This method gets handler which is responsible for handling of this module.
     *
     * @return handler of the module or null.
     */
    TriggerHandler getModuleHandler() {
        return triggerHandler;
    }

    /**
     * This method sets handler of the module.
     *
     * @param triggerHandler
     */
    void setModuleHandler(TriggerHandler triggerHandler) {
        this.triggerHandler = triggerHandler;
    }

}
