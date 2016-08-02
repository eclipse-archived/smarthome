/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.sample.extension.java.handler;

import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.sample.extension.java.type.WelcomeHomeActionType;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This class serves to handle the Action types provided by this application. It is used to help the RuleEngine
 * to execute the {@link Action}s.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class WelcomeHomeActionHandler extends BaseModuleHandler<Action>implements ActionHandler {

    public WelcomeHomeActionHandler(Action module) {
        super(module);
    }

    @Override
    public Map<String, Object> execute(Map<String, ?> context) {
        String device = getDevice(module.getConfiguration());
        String result = getResult(module.getConfiguration());
        System.out.println("[Automation Java API Demo : " + module.getTypeUID() + "] " + device + ": " + result);
        return null;
    }

    /**
     * This method provides the way to configure which device to execute the action.
     *
     * @param configration
     *            of the {@link Action} module.
     * @return
     *         the string representing the device.
     */
    private String getDevice(Configuration configration) {
        return (String) (configration != null ? configration.get(WelcomeHomeActionType.CONFIG_DEVICE) : null);
    }

    /**
     * This method provides the way to configure the command which to be executed.
     *
     * @param configration
     *            of the {@link Action} module.
     * @return
     *         the command which to be executed.
     */
    private String getResult(Configuration configration) {
        return (String) (configration != null ? configration.get(WelcomeHomeActionType.CONFIG_RESULT) : null);
    }
}
