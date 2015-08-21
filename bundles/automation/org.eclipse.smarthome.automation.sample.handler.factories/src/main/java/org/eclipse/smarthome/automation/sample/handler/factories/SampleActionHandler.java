/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.sample.handler.factories;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.BaseActionHandler;
import org.eclipse.smarthome.automation.type.ModuleType;

/**
 * Action Handler sample implementation
 *
 * @author Vasil Ilchev - Initial Contribution
 */
public class SampleActionHandler extends BaseActionHandler implements ActionHandler {

    /**
     * Constructs SampleActionHandler
     *
     * @param module
     * @param actionType
     */
    public SampleActionHandler(Action module, List<ModuleType> moduleTypes) {
        super(module, moduleTypes);
    }

    @Override
    public void dispose() {
    }

    @Override
    protected Map<String, Object> performOperation(Map<String, Object> resolvedInputs,
            Map<String, Object> resolvedConfiguration) {
        Object message = getMessage(resolvedConfiguration);
        if (message == null) {
            message = "";
        }
        System.out.println("[Automation demo] " + module.getTypeUID() + "/" + module.getId() + ": " + message);
        return null;
    }

    private Object getMessage(Map<String, ?> resolvedConfigration) {
        return resolvedConfigration != null ? resolvedConfigration.get("message") : null;
    }
}
