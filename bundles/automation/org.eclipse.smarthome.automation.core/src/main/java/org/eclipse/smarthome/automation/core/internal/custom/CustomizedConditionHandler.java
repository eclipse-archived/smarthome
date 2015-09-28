/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal.custom;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.type.ModuleType;

/**
 * Common implementation for {@link ConditionHandler}s.
 *
 * @author Vasil Ilchev - Initial Contribution
 * @author Kai Kreuzer - refactored and simplified customized module handling
 *
 */
public class CustomizedConditionHandler extends AbstractCustomizedModuleHandler<ConditionHandler, Condition>
        implements ConditionHandler {

    public CustomizedConditionHandler(ConditionHandler parentHandler, Condition parentModule, Condition module,
            List<ModuleType> moduleTypes) {
        super(parentHandler, parentModule, module, moduleTypes);
    }

    @Override
    public boolean isSatisfied(Map<String, ?> inputs) {
        Map<String, Object> resolvedInputs = getResolvedInputs(inputs);
        Map<String, Object> resolvedConfiguration = getResolvedConfiguration(resolvedInputs);
        parentModule.setConfiguration(resolvedConfiguration);
        return parentHandler.isSatisfied(resolvedInputs);
    }

}
