/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.handler;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.BaseConditionHandler;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of a event condition which checks if inputs matches configured values.
 * 
 * @author BenediktNiehues
 *
 */
public class EventConditionHandler extends BaseConditionHandler {
    public final Logger logger = LoggerFactory.getLogger(EventConditionHandler.class);

    public static final String MODULETYPE_ID = "EventCondition";

    private static final String TOPIC = "topic";
    private static final String EVENTTYPE = "eventType";
    private static final String SOURCE = "source";
    private static final String PAYLOAD = "payload";

    public EventConditionHandler(Condition module, List<ModuleType> moduleTypes) {
        super(module, moduleTypes);
    }

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    protected boolean evaluateCondition(Map<String, Object> resolvedInputs, Map<String, Object> resolvedConfiguration) {
        return isConfiguredAndMatches(TOPIC, resolvedInputs, resolvedConfiguration)
                && isConfiguredAndMatches(SOURCE, resolvedInputs, resolvedConfiguration)
                && isConfiguredAndMatches(PAYLOAD, resolvedInputs, resolvedConfiguration)
                && isConfiguredAndMatches(EVENTTYPE, resolvedInputs, resolvedConfiguration);
    }

    private boolean isConfiguredAndMatches(String keyParam, Map<String, Object> resolvedInputs,
            Map<String, Object> resolvedConfiguration) {
        if (resolvedConfiguration.get(keyParam) != null) {
            if (resolvedInputs.get(keyParam) != null) {
                return ((String) resolvedInputs.get(keyParam)).matches((String) resolvedConfiguration.get(keyParam));
            } else {
                return false;
            }
        } else
            return true;
    }

}
