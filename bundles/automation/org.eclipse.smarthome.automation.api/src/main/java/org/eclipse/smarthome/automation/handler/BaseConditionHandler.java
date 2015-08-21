/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.handler;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.ModuleType;

/**
 * Common implementation for {@link ConditionHandler}s.
 *
 * @author Vasil Ilchev - Initial Contribution
 *
 */
public abstract class BaseConditionHandler extends BaseModuleHandler<Condition>implements ConditionHandler {

    public BaseConditionHandler(Condition module, List<ModuleType> moduleTypes) {
        super(module, moduleTypes);
    }

    @Override
    public boolean isSatisfied(Map<String, ?> inputs) {
        Map<String, Object> resolvedInputs = getResolvedInputs(inputs);
        Map<String, Object> resolvedConfiguration = getResolvedConfiguration(resolvedInputs);
        return evaluateCondition(resolvedInputs, resolvedConfiguration);
    }

    /**
     * After RuleEngine has called the {@link #isSatisfied(Map)} and {@link Input}s and Configuration of the
     * {@link ConditionHandler} are resolved this method is called giving all needed information in order
     * {@link ConditionHandler} perform its evaluation.
     *
     * @param resolvedInputs the resolved {@link Inputs} that {@link ConditionHandler} will be ready to work with.
     * @param resolvedConfiguration the resolved Configuration that {@link Condition Handler} will be ready to work
     *            with.
     * @return true if this {@link ConditionHandler} is satisfied, false otherwise. Result will be passed to
     *         {@link #isSatisfied(Map)}
     */
    protected abstract boolean evaluateCondition(Map<String, Object> resolvedInputs,
            Map<String, Object> resolvedConfiguration);

}
