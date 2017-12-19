/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.automation.core.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * An utility class creating copy of the rule
 *
 * @author Yordan Mihaylov - initial content
 */
public class RuleUtils {

    /**
     * This method creates deep copy of list of conditions
     *
     * @param conditions list of conditions
     * @return deep copy of list of conditions or empty list when the parameter is null.
     */
    public static List<Condition> getConditionsCopy(List<Condition> conditions) {
        List<Condition> res = new ArrayList<Condition>();
        if (conditions != null) {
            for (Condition c : conditions) {
                Condition condition = new Condition(c.getId(), c.getTypeUID(),
                        new Configuration(c.getConfiguration().getProperties()),
                        new HashMap<String, String>(c.getInputs()));
                condition.setLabel(c.getLabel());
                condition.setDescription(c.getDescription());
                res.add(condition);
            }
        }
        return res;
    }

    /**
     * This method creates deep copy of list of actions
     *
     * @param actions list of actions
     * @return deep copy of list of actions or empty list when the parameter is null.
     */
    public static List<Action> getActionsCopy(List<Action> actions) {
        List<Action> res = new ArrayList<Action>();
        if (actions != null) {
            for (Action a : actions) {
                Action action = new Action(a.getId(), a.getTypeUID(),
                        new Configuration(a.getConfiguration().getProperties()),
                        new HashMap<String, String>(a.getInputs()));
                action.setLabel(a.getLabel());
                action.setDescription(a.getDescription());
                res.add(action);
            }
        }
        return res;
    }

    /**
     * This method creates deep copy of list of triggers
     *
     * @param triggers list of triggers
     * @return deep copy of list of triggers or empty list when parameter is null.
     */
    public static List<Trigger> getTriggersCopy(List<Trigger> triggers) {
        List<Trigger> res = new ArrayList<Trigger>();
        if (triggers != null) {
            for (Trigger t : triggers) {
                Trigger trigger = new Trigger(t.getId(), t.getTypeUID(),
                        new Configuration(t.getConfiguration().getProperties()));
                trigger.setLabel(t.getLabel());
                trigger.setDescription(t.getDescription());
                res.add(trigger);
            }
        }
        return res;
    }

    /**
     * This method creates copy of the rule
     *
     * @param rule the rule which has to be copied
     * @return copy of the rule.
     */
    public static Rule getRuleCopy(Rule r) {
        Rule rule = new Rule(r.getUID(), getTriggersCopy(r.getTriggers()), getConditionsCopy(r.getConditions()),
                getActionsCopy(r.getActions()), new ArrayList<>(r.getConfigurationDescriptions()),
                new Configuration(r.getConfiguration().getProperties()), r.getTemplateUID(), r.getVisibility());
        String name = r.getName();
        if (name != null) {
            rule.setName(name);
        }
        rule.setTags(new HashSet<String>(r.getTags()));
        String description = r.getDescription();
        if (description != null) {
            rule.setDescription(description);
        }
        return rule;
    }

}
