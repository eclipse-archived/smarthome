/**
 * Copyright (c) 2017 Kai Kreuzer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.timer.handler;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a ConditionHandler implementation, which checks the current day of the week against a specified list.
 *
 * @author Kai Kreuzer - Initial Contribution
 *
 */
public class DayOfWeekConditionHandler extends BaseModuleHandler<Condition> implements ConditionHandler {

    private final Logger logger = LoggerFactory.getLogger(DayOfWeekConditionHandler.class);

    public static final String MODULE_TYPE_ID = "timer.DayOfWeekCondition";
    public static final String MODULE_CONTEXT_NAME = "MODULE";

    private static final String CFG_DAYS = "days";

    private final Set<Integer> days;

    @SuppressWarnings("unchecked")
    public DayOfWeekConditionHandler(Condition module) {
        super(module);
        try {
            days = new HashSet<>();
            for (String day : (Iterable<String>) module.getConfiguration().get(CFG_DAYS)) {
                switch (day) {
                    case "SUN":
                        days.add(1);
                        break;
                    case "MON":
                        days.add(2);
                        break;
                    case "TUE":
                        days.add(3);
                        break;
                    case "WED":
                        days.add(4);
                        break;
                    case "THU":
                        days.add(5);
                        break;
                    case "FRI":
                        days.add(6);
                        break;
                    case "SAT":
                        days.add(7);
                        break;
                    default:
                        logger.warn("Ignoring illegal weekday '{}'", day);
                        break;
                }
            }
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("'days' parameter must be an array of strings.");
        }
    }

    @Override
    public boolean isSatisfied(Map<String, ?> context) {
        int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return days.contains(dow);
    }
}
