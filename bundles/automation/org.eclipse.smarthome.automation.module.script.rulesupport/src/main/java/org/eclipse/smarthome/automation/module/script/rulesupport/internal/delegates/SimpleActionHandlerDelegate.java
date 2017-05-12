/**
 * Copyright (c) 2015-2017 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.rulesupport.internal.delegates;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple.SimpleActionHandler;

/**
 * The SimpleActionHandlerDelegate allows the registration of {@link SimpleActionHandler}s to the RuleEngine.
 *
 * @author Simon Merschjohann
 */
public class SimpleActionHandlerDelegate extends BaseModuleHandler<Action> implements ActionHandler {

    private org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple.SimpleActionHandler actionHandler;

    public SimpleActionHandlerDelegate(Action module,
            org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple.SimpleActionHandler actionHandler) {
        super(module);
        this.actionHandler = actionHandler;
    }

    @Override
    public void dispose() {
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> inputs) {
        Set<String> keys = new HashSet<String>(inputs.keySet());

        Map<String, Object> extendedInputs = new HashMap<>(inputs);
        for (String key : keys) {
            Object value = extendedInputs.get(key);
            int dotIndex = key.indexOf('.');
            if (dotIndex != -1) {
                String moduleName = key.substring(0, dotIndex);
                extendedInputs.put("module", moduleName);
                String newKey = key.substring(dotIndex + 1);
                extendedInputs.put(newKey, value);
            }
        }

        Object result = actionHandler.execute(module, extendedInputs);
        HashMap<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("result", result);
        return resultMap;
    }
}
