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
public class LightsTriggerType extends TriggerType {

    public static final String UID = "LightsTrigger";

    public static LightsTriggerType initialize() {
        Output state = new Output(StateConditionType.INPUT_CURRENT_STATE, String.class.getName(), "State",
                "Indicates the state of Lights", null, null, null);
        List<Output> output = new ArrayList<Output>();
        output.add(state);
        return new LightsTriggerType(output);
    }

    public LightsTriggerType(List<Output> output) {
        super(UID, null, "Lights State Trigger", "Template for creation of an Lights State Rule Trigger.", null,
                Visibility.VISIBLE, output);
    }
}
