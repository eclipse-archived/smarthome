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

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.Output;

/**
 * Common implementation for {@link ActionHandler}s.
 *
 * @author Vasil Ilchev - Initial Contribution
 *
 */
public abstract class BaseActionHandler extends BaseModuleHandler<Action>implements ActionHandler {

    public BaseActionHandler(Action module, List<ModuleType> moduleTypes) {
        super(module, moduleTypes);
    }

    @Override
    public Map<String, Object> execute(Map<String, ?> inputs) {
        Map<String, Object> resolvedInputs = getResolvedInputs(inputs);
        Map<String, Object> resolvedConfigration = getResolvedConfiguration(resolvedInputs);
        Map<String, Object> operationResults = performOperation(resolvedInputs, resolvedConfigration);
        return getResolvedOutputs(resolvedConfigration, resolvedInputs, operationResults);
    }

    /**
     * After RuleEngine has called the {@link #execute(Map)} and {@link Input}s and Configuration of the
     * {@link ActionHandler} are resolved this method is called giving all needed information in order
     * {@link ActionHandler} perform its operation
     *
     * @param resolvedInputs the resolved {@link Inputs} {@link ActionHandler} will be ready to work with
     * @param resolvedConfiguration the resolved Configurations {@link ActionHandler} will be ready to work with.
     * @return ActionHandler may provide result from their operation represented by Map as their {@link Output} can have
     *         reference to the result by its <code>reference</code> property.
     *         This way {@link ActionHandler} can pass its result from their operation to the Outputs.
     */
    protected abstract Map<String, Object> performOperation(Map<String, Object> resolvedInputs,
            Map<String, Object> resolvedConfiguration);
}
