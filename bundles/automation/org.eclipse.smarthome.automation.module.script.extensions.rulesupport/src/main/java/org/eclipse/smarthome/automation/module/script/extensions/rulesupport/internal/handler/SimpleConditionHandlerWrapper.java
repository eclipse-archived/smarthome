/**
 * Copyright (c) 2015-2016 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.handler;

import java.util.Map;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.simple.SimpleConditionHandler;

/**
 * Condition Handler Implementation.
 *
 * @author Simon Merschjohann
 *
 */
public class SimpleConditionHandlerWrapper extends BaseModuleHandler<Condition>implements ConditionHandler {

    private org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.simple.SimpleConditionHandler conditionHandler;

    public SimpleConditionHandlerWrapper(Condition condition, SimpleConditionHandler scriptedHandler) {
        super(condition);

        scriptedHandler.init(condition);
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isSatisfied(Map<String, ?> inputs) {
        return conditionHandler.isSatisfied(module, inputs);
    }
}
