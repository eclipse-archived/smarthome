/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.java.api.demo.type;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;

/**
 * The purpose of this class is to illustrate how to create {@link ConditionType}
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class StateConditionType extends ConditionType {

    public static final String UID = "StateCondition";

    public static final String CONFIG_STATE = "state";
    public static final String INPUT_CURRENT_STATE = "currentState";

    public static StateConditionType initialize() {
        ConfigDescriptionParameter state = new ConfigDescriptionParameter(CONFIG_STATE, Type.TEXT, null, null, null,
                null, true, true, false, null, null, "State", "State of the unit", null, null, null, null, null, null);
        List<ConfigDescriptionParameter> config = new ArrayList<ConfigDescriptionParameter>();
        config.add(state);
        Input leftOperand = new Input(INPUT_CURRENT_STATE, String.class.getName(), "Current State",
                "Current state of the unit", null, true, null, null);
        List<Input> input = new ArrayList<Input>();
        input.add(leftOperand);
        return new StateConditionType(config, input);
    }

    public StateConditionType(List<ConfigDescriptionParameter> config, List<Input> input) {
        super(UID, config, "State Condition Template", "Template for creation of a State Condition.", null,
                Visibility.VISIBLE, input);
    }
}
