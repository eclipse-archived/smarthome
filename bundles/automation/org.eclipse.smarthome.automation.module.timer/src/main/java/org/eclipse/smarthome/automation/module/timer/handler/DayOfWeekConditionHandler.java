/**
 * Copyright (c) 2017 Kai Kreuzer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.timer.handler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;

/**
 * This is an ModuleHandler implementation for Triggers which trigger the rule
 * based on a cron expression. The cron expression can be set with the
 * configuration.
 *
 * @author Kai Kreuzer - Initial Contribution
 *
 */
public class DayOfWeekConditionHandler extends BaseModuleHandler<Condition> implements ConditionHandler {

    public static final String MODULE_TYPE_ID = "timer.DayOfWeekCondition";
    public static final String MODULE_CONTEXT_NAME = "MODULE";

    private static final String CFG_DAYS = "days";

    private final ArrayList<Integer> days;

    @SuppressWarnings("unchecked")
    public DayOfWeekConditionHandler(Condition module) {
        super(module);
        try {
            days = new ArrayList<>();
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
                }
            }
            days.toArray(new Integer[days.size()]);
        } catch (Exception e) {
            throw new IllegalArgumentException("'days' parameter must be an array of strings.");
        }
    }

    @Override
    public boolean isSatisfied(Map<String, ?> context) {
        int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return days.contains(dow);
    }
}
