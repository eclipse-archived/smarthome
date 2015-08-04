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

package org.eclipse.smarthome.automation.sample.handler.factories;

import java.util.Map;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.AbstractModuleHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.parser.Converter;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;

public class SampleConditionHandler extends AbstractModuleHandler implements ConditionHandler {
    public static final String OPERATOR_LESS = "<";
    public static final String OPERATOR_GREATER = ">";
    public static final String OPERATOR_EQUAL = "=";
    public static final String OPERATOR_NOT_EQUAL = "!=";
    //
    public static final String PROPERTY_OPERATOR = "operator";
    public static final String PROPERTY_CONSTRAINT = "constraint";
    public static final String CONDITION_INPUT_NAME = "conditionInput";
    //
    private Map<String, ?> configuration;
    private Condition condition;
    private ConditionType conditionType;
    //
    private SampleHandlerFactory handlerFactory;

    public SampleConditionHandler(SampleHandlerFactory handlerFactory, Condition condition,
            ConditionType conditionType) {
        super(condition);
        this.condition = condition;
        this.conditionType = conditionType;
        this.configuration = condition.getConfiguration();
        this.handlerFactory = handlerFactory;
    }

    @Override
    public void dispose() {
        super.dispose();
        handlerFactory.disposeHandler(this);
    }

    @Override
    public boolean isSatisfied(Map<String, ?> inputs) {
        Map resolvedInputs = getResolvedInputs(inputs);
        Map resolvedConfiguration = getResolvedConfiguration(resolvedInputs);
        String conditionInput = (String) resolvedInputs.get(CONDITION_INPUT_NAME);
        if (conditionInput == null) {
            conditionInput = "";
        }
        String operator = (String) resolvedConfiguration.get(PROPERTY_OPERATOR);
        String constraint = (String) resolvedConfiguration.get(PROPERTY_CONSTRAINT);
        boolean satisfied = false;
        if (OPERATOR_EQUAL.equals(operator)) {
            satisfied = conditionInput.equals(constraint);
        } else if (OPERATOR_NOT_EQUAL.equals(operator)) {
            satisfied = !(conditionInput.equals(constraint));
        } else if (OPERATOR_LESS.equals(operator)) {
            int compersion = conditionInput.compareTo(constraint);
            satisfied = compersion < 0 ? true : false;
        } else if (OPERATOR_GREATER.equals(operator)) {
            int comperison = conditionInput.compareTo(constraint);
            satisfied = comperison > 0 ? true : false;
        } else {
            throw new IllegalArgumentException("[SampleConditionHandler]Invalid comparison operator: " + operator);
        }
        return satisfied;
    }

    @Override
    protected ModuleTypeRegistry getModuleTypeRegistry() {
        return SampleHandlerFactory.getModuleTypeRegistry();
    }

    @Override
    protected Converter getConverter() {
        return SampleHandlerFactory.getConverter();
    }
}
