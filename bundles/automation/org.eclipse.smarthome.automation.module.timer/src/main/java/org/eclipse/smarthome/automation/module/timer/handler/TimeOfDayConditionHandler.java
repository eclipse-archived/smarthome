/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation.module.timer.handler;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConditionHandler implementation for time based conditions.
 *
 * @author Dominik Schlierf - initial contribution
 *
 */
public class TimeOfDayConditionHandler extends BaseModuleHandler<Condition> implements ConditionHandler {

    private final Logger logger = LoggerFactory.getLogger(TimeOfDayConditionHandler.class);

    public static final String MODULE_TYPE_ID = "core.TimeOfDayCondition";

    /**
     * Constants for Config-Parameters corresponding to Definition in
     * TimeOfDayConditionHandler.json
     */
    private static final String OPERATOR = "operator";
    private static final String TIME = "time";

    public TimeOfDayConditionHandler(Condition condition) {
        super(condition);
    }

    @Override
    public boolean isSatisfied(Map<String, Object> inputs) {
        String state = (String) module.getConfiguration().get(TIME);
        String operator = (String) module.getConfiguration().get(OPERATOR);
        if (operator == null || state == null) {
            logger.error("Module is not well configured: operator={}  state = {}", operator, state);
            return false;
        }
        LocalTime currentTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalTime comparedTime = LocalTime.parse(state).truncatedTo(ChronoUnit.MINUTES);
        logger.debug("Comparing time values --> {} {} {}", currentTime, operator, comparedTime);
        switch (operator) {
            case "EQUALS":
                return currentTime.equals(comparedTime);
            case "EARLIER":
                return currentTime.isBefore(comparedTime);
            case "LATER":
                return currentTime.isAfter(comparedTime);
            default:
                logger.debug("Invalid operator in time comparison.");
        }
        return false;
    }

}
