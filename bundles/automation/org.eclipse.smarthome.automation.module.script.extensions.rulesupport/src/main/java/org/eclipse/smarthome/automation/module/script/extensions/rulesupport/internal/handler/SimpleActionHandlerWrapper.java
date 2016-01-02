/**
 * Copyright (c) 2015-2016 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;

/**
 * Scripted Action Handler implementation
 *
 * @author Simon Merschjohann
 */
public class SimpleActionHandlerWrapper extends BaseModuleHandler<Action>implements ActionHandler {

    private org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.simple.SimpleActionHandler actionHandler;

    public SimpleActionHandlerWrapper(Action module,
            org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.simple.SimpleActionHandler actionHandler) {
        super(module);

        this.actionHandler = actionHandler;
    }

    @Override
    public void dispose() {
    }

    @Override
    public Map<String, Object> execute(Map<String, ?> inputs) {
        Object result = actionHandler.execute(module, inputs);
        HashMap<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("result", result);
        return resultMap;
    }
}
