/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.sample.handler.factories;

import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.AbstractModuleHandler;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.parser.Converter;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;

/**
 * Action Handler sample implementation
 *
 * @author Vasil Ilchev - Initial Contribution
 */
public class SampleActionHandler extends AbstractModuleHandler implements ActionHandler {
    public static final String ACTION_INPUT_NAME = "actionInput";
    protected Map<String, ?> configuration;
    protected String functionalItemUID;
    protected Action action;
    protected ActionType actionType;

    /**
     * Constructs SampleActionHandler
     *
     * @param action
     * @param actionType
     */
    public SampleActionHandler(Action action, ActionType actionType) {
        super(action);
        this.action = action;
        this.actionType = actionType;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public Map<String, Object> execute(Map<String, ?> inputs) {
        Map<String, Object> resolvedInputs = getResolvedInputs(inputs);
        Map<String, Object> resolvedConfigration = getResolvedConfiguration(resolvedInputs);
        Map<String, Object> resolvedOutputs = getResolvedOutputs(resolvedConfigration, resolvedInputs, null);
        Object message = getMessage(resolvedConfigration);
        if (message == null) {
            message = "";
        }
        System.out.println("[Automation demo] " + actionType.getUID() + "/" + action.getId() + ": " + message);
        return resolvedOutputs;
    }

    @Override
    protected ModuleTypeRegistry getModuleTypeRegistry() {
        return SampleHandlerFactory.getModuleTypeRegistry();
    }

    @Override
    protected Converter getConverter() {
        return SampleHandlerFactory.getConverter();
    }

    private Object getMessage(Map<String, ?> resolvedConfigration) {
        return resolvedConfigration != null ? resolvedConfigration.get("message") : null;
    }
}
