/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.sample.extension.java.handler;

import java.util.Map;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseTriggerModuleHandler;

/**
 * This class serves to handle the Trigger types provided by this application. It is used to notify the RuleEngine about
 * firing the {@link Trigger}s.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class WelcomeHomeTriggerHandler extends BaseTriggerModuleHandler {

    public WelcomeHomeTriggerHandler(Trigger module) {
        super(module);
    }

    /**
     * This method is used to notify the RuleEngine about firing the {@link Trigger}s.
     *
     * @param context
     *            is used to provide the output of the {@link Trigger}.
     */
    public void trigger(Map<String, ?> context) {
        ruleEngineCallback.triggered(module, context);
    }
}
