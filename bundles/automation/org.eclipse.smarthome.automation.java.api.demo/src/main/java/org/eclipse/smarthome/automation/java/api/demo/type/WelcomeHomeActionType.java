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
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;

/**
 * The purpose of this class is to illustrate how to create {@link ActionType}
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class WelcomeHomeActionType extends ActionType {

    public static final String UID = "WelcomeHomeAction";

    public static final String CONFIG_DEVICE = "device";
    public static final String CONFIG_RESULT = "result";

    public static WelcomeHomeActionType initialize() {
        ConfigDescriptionParameter device = new ConfigDescriptionParameter(CONFIG_DEVICE, Type.TEXT, null, null, null,
                null, true, true, false, null, null, "Device", "Device description", null, null, null, null, null,
                null);
        ConfigDescriptionParameter result = new ConfigDescriptionParameter(CONFIG_RESULT, Type.TEXT, null, null, null,
                null, true, true, false, null, null, "Result", "Result description", null, null, null, null, null,
                null);
        List<ConfigDescriptionParameter> config = new ArrayList<ConfigDescriptionParameter>();
        config.add(device);
        config.add(result);
        return new WelcomeHomeActionType(config);
    }

    public WelcomeHomeActionType(List<ConfigDescriptionParameter> config) {
        super(UID, config, "Welcome Home Action Template", "Template for creation of a Welcome Home Action.", null,
                Visibility.VISIBLE, null, null);
    }
}
