/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.sample.handler.factories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseTriggerHandler;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.type.ModuleType;

/**
 * Trigger Handler sample implementation
 *
 * @author Vasil Ilchev - Initial Contribution
 */
public class SampleTriggerHandler extends BaseTriggerHandler implements TriggerHandler {
    private static final String OUTPUT_REFERENCE = "consoleInput";
    private SampleHandlerFactory handlerFactory;
    private String triggerParam;

    public SampleTriggerHandler(SampleHandlerFactory handlerFactory, Trigger module, List<ModuleType> moduleTypes) {
        super(module, moduleTypes);
        this.handlerFactory = handlerFactory;
    }

    public void trigger(String triggerParam) {
        this.triggerParam = triggerParam;
        trigger();
    }

    @Override
    public void dispose() {
        handlerFactory.disposeHandler(this);
    }

    @Override
    protected Map<String, Object> getTriggerValues() {
        Map<String, Object> triggerValues = new HashMap<String, Object>();
        triggerValues.put(OUTPUT_REFERENCE, triggerParam);
        return triggerValues;
    }

    String getTriggerID() {
        return module.getId();
    }

}
