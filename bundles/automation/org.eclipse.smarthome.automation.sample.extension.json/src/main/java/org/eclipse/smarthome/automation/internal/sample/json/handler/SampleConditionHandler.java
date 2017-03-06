/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/*******************************************************************************
s * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.internal.sample.json.handler;

import java.util.Map;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * Condition Handler Sample Implementation.
 *
 * @author Vasil Ilchev - Initial Contribution
 * @author Kai Kreuzer - refactored and simplified customized module handling
 *
 */
public class SampleConditionHandler extends BaseModuleHandler<Condition> implements ConditionHandler {
    public static final String OPERATOR_LESS = "<";
    public static final String OPERATOR_GREATER = ">";
    public static final String OPERATOR_EQUAL = "=";
    public static final String OPERATOR_NOT_EQUAL = "!=";
    //
    public static final String PROPERTY_OPERATOR = "operator";
    public static final String PROPERTY_CONSTRAINT = "constraint";
    public static final String CONDITION_INPUT_NAME = "conditionInput";

    /**
     * Constructs SampleConditionHandler.
     *
     * @param condition
     */
    public SampleConditionHandler(Condition condition) {
        super(condition);
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isSatisfied(Map<String, Object> inputs) {
        String conditionInput = (String) inputs.get(CONDITION_INPUT_NAME);
        if (conditionInput == null) {
            conditionInput = "";
        }
        Configuration config = module.getConfiguration();
        String operator = (String) config.get(PROPERTY_OPERATOR);
        String constraint = (String) config.get(PROPERTY_CONSTRAINT);
        boolean evaluation = false;
        if (OPERATOR_EQUAL.equals(operator)) {
            evaluation = conditionInput.equals(constraint);
        } else if (OPERATOR_NOT_EQUAL.equals(operator)) {
            evaluation = !(conditionInput.equals(constraint));
        } else if (OPERATOR_LESS.equals(operator)) {
            int compersion = conditionInput.compareTo(constraint);
            evaluation = compersion < 0 ? true : false;
        } else if (OPERATOR_GREATER.equals(operator)) {
            int comperison = conditionInput.compareTo(constraint);
            evaluation = comperison > 0 ? true : false;
        } else {
            throw new IllegalArgumentException("[SampleConditionHandler]Invalid comparison operator: " + operator);
        }
        return evaluation;
    }

}
