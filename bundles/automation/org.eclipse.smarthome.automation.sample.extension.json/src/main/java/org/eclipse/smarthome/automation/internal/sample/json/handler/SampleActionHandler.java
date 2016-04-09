/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.sample.json.handler;

import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;

/**
 * Action Handler sample implementation
 *
 * @author Vasil Ilchev - Initial Contribution
 * @author Kai Kreuzer - refactored and simplified customized module handling
 */
public class SampleActionHandler extends BaseModuleHandler<Action>implements ActionHandler {

    /**
     * Constructs SampleActionHandler
     *
     * @param module
     * @param actionType
     */
    public SampleActionHandler(Action module) {
        super(module);
    }

    @Override
    public void dispose() {
    }

    @Override
    public Map<String, Object> execute(Map<String, ?> inputs) {
        Object message = getMessage(inputs);
        if (message == null) {
            message = "";
        }
        System.out.println("[Automation demo] " + module.getTypeUID() + "/" + module.getId() + ": " + message);
        return null;
    }

    private Object getMessage(Map<String, ?> inputs) {
        return inputs != null ? inputs.get("message") : null;
    }
}
