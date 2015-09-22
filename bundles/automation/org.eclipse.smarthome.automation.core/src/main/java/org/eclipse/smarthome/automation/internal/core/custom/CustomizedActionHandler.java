/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.core.custom;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.type.ModuleType;

/**
 * Common implementation for {@link ActionHandler}s.
 *
 * @author Vasil Ilchev - Initial Contribution
 * @author Kai Kreuzer - refactored and simplified customized module handling
 *
 */
public class CustomizedActionHandler extends AbstractCustomizedModuleHandler<ActionHandler, Action>
        implements ActionHandler {

    public CustomizedActionHandler(ActionHandler parentHandler, Action parentAction, Action action,
            List<ModuleType> moduleTypes) {
        super(parentHandler, parentAction, action, moduleTypes);
    }

    @Override
    public Map<String, Object> execute(Map<String, ?> inputs) {
        Map<String, Object> resolvedInputs = getResolvedInputs(inputs);
        Map<String, Object> resolvedConfiguration = getResolvedConfiguration(resolvedInputs);
        parentModule.setConfiguration(resolvedConfiguration);
        Map<String, Object> operationResults = parentHandler.execute(resolvedInputs);
        return getResolvedOutputs(resolvedConfiguration, resolvedInputs, operationResults);
    }

}
