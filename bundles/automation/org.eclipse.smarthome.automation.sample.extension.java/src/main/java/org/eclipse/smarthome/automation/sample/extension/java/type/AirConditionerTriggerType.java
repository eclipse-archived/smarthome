/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.sample.extension.java.type;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;

/**
 * The purpose of this class is to illustrate how to create {@link TriggerType}
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class AirConditionerTriggerType extends TriggerType {

    public static String UID = "AirConditionerTrigger";

    public static TriggerType initialize() {
        List<Output> output = new ArrayList<Output>();
        Output state = new Output(StateConditionType.INPUT_CURRENT_STATE, String.class.getName(), "State",
                "Indicates if the Air Conditioner is switched On or Off.", null, null, null);
        Output temperature = new Output(TemperatureConditionType.INPUT_CURRENT_TEMPERATURE, Integer.class.getName(),
                "Temperature", "Indicates the current room temperature", null, null, null);
        output.add(state);
        output.add(temperature);
        return new AirConditionerTriggerType(output);
    }

    public AirConditionerTriggerType(List<Output> output) {
        super(UID, null, "Air Conditioner Rule Trigger", "Template for creation of a Air Conditioner Rule Trigger.",
                null, Visibility.VISIBLE, output);
    }
}
