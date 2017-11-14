/**
 * Copyright (c) 2015-2017 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.rulesupport.internal.delegates;

import java.util.Map;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple.SimpleConditionHandler;

/**
 * The SimpleConditionHandlerDelegate allows the registration of {@link SimpleConditionHandler}s to the RuleEngine.
 *
 * @author Simon Merschjohann
 *
 */
public class SimpleConditionHandlerDelegate extends BaseModuleHandler<Condition> implements ConditionHandler {

    private SimpleConditionHandler conditionHandler;

    public SimpleConditionHandlerDelegate(Condition condition, SimpleConditionHandler scriptedHandler) {
        super(condition);
        this.conditionHandler = scriptedHandler;
        scriptedHandler.init(condition);
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isSatisfied(Map<String, Object> inputs) {
        return conditionHandler.isSatisfied(module, inputs);
    }
}
