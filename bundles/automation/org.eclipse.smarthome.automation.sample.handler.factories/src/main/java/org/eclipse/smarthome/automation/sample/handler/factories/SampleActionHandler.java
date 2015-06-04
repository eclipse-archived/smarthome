/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
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

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.type.ActionType;
import org.osgi.framework.BundleContext;

/**
 * Action Handler sample implementation
 *
 * @author Vasil Ilchev - Initial Contribution
 */
public class SampleActionHandler implements ActionHandler {
    public static final String ACTION_INPUT_NAME = "actionInput";
    protected Map<String, ?> configuration;
    protected String functionalItemUID;
    protected Action action;
    protected ActionType actionType;
    //
    protected BundleContext bc;
    private RuleEngineCallback ruleCallBack;
    private SampleHandlerFactory handlerFactory;

    public SampleActionHandler(SampleHandlerFactory handlerFactory, Action action, ActionType actionType,
            BundleContext bc) {
        this.action = action;
        this.actionType = actionType;
        this.configuration = action.getConfiguration();
        this.bc = bc;
        this.handlerFactory = handlerFactory;
    }

    @Override
    public void setConfiguration(Map<String, ?> configuration) {
        this.configuration = configuration;
    }

    @Override
    public void dispose() {
        handlerFactory.disposeHandler(this);
    }

    private Object getPrefix() {
        return configuration != null ? configuration.get("prefix") : null;
    }

    @Override
    public Map<String, Object> execute(Map<String, ?> inputs) {
        String inputData = (String) inputs.get(ACTION_INPUT_NAME);
        Object prefix = getPrefix();
        if (prefix == null) {
            prefix = "";
        }
        System.out.println("[Automation demo] " + actionType.getUID() + "/" + action.getId() + ": " + prefix + " "
                + inputData);
        return null;
    }
}
