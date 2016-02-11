/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.sample.extension.java.handler;

import java.util.Map;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.sample.extension.java.type.TemperatureConditionType;

/**
 * This class serves to handle the Condition types provided by this application. It is used to help the RuleEngine
 * to decide to continue with the execution of the rule or to terminate it.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class TemperatureConditionHandler extends BaseModuleHandler<Condition>implements ConditionHandler {

    public TemperatureConditionHandler(Condition module) {
        super(module);
    }

    @Override
    public boolean isSatisfied(Map<String, ?> context) {
        Integer left = (Integer) context.get(TemperatureConditionType.INPUT_CURRENT_TEMPERATURE);
        Integer right = (Integer) module.getConfiguration().get(TemperatureConditionType.CONFIG_TEMPERATURE);
        String operator = (String) module.getConfiguration().get(TemperatureConditionType.CONFIG_OPERATOR);
        if (TemperatureConditionType.OPERATOR_HEATING.equals(operator)) {
            if (left != null && right != null && left < right) {
                return true;
            }
        } else if (TemperatureConditionType.OPERATOR_COOLING.equals(operator)) {
            if (left != null && right != null && left > right) {
                return true;
            }
        }
        return false;
    }

}
