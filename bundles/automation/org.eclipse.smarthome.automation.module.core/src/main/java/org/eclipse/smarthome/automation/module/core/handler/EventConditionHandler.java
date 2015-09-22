/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.core.handler;

import java.util.Map;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of a event condition which checks if inputs matches configured values.
 *
 * @author BenediktNiehues
 * @author Kai Kreuzer - refactored and simplified customized module handling
 *
 */
public class EventConditionHandler extends BaseModuleHandler<Condition>implements ConditionHandler {
    public final Logger logger = LoggerFactory.getLogger(EventConditionHandler.class);

    public static final String MODULETYPE_ID = "EventCondition";

    private static final String TOPIC = "topic";
    private static final String EVENTTYPE = "eventType";
    private static final String SOURCE = "source";
    private static final String PAYLOAD = "payload";

    public EventConditionHandler(Condition module) {
        super(module);
    }

    private boolean isConfiguredAndMatches(String keyParam, Map<String, ?> inputs) {
        if (module.getConfiguration().get(keyParam) != null) {
            if (inputs.get(keyParam) != null) {
                return ((String) inputs.get(keyParam)).matches((String) module.getConfiguration().get(keyParam));
            } else {
                return false;
            }
        } else
            return true;
    }

    @Override
    public boolean isSatisfied(Map<String, ?> inputs) {
        return isConfiguredAndMatches(TOPIC, inputs) && isConfiguredAndMatches(SOURCE, inputs)
                && isConfiguredAndMatches(PAYLOAD, inputs) && isConfiguredAndMatches(EVENTTYPE, inputs);
    }

}
