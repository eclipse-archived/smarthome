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
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;

/**
 * This is a class to be used within the {@link RuleEngine} for resolved rules.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 *
 */
public class RuntimeRule extends Rule {

    private Map<String, Module> moduleMap;

    /**
     * Utility constructor creating copy of the Rule or create a new empty instance.
     *
     * @param rule a rule which has to be copied or null when an empty instance of rule
     *            has to be created.
     */
    @SuppressWarnings("null")
    protected RuntimeRule(Rule rule) {
        super(rule.getUID(), getRuntimeTriggersCopy(rule.getTriggers()), getRuntimeConditionsCopy(rule.getConditions()),
                getRuntimeActionsCopy(rule.getActions()), rule.getConfigurationDescriptions(), rule.getConfiguration(),
                rule.getTemplateUID(), rule.getVisibility());
        setName(rule.getName());
        setTags(rule.getTags());
        setDescription(rule.getDescription());
    }

    @Override
    public Module getModule(String moduleId) {
        if (moduleMap == null) {
            moduleMap = initModuleMap();
        }
        return moduleMap.get(moduleId);
    }

    private Map<String, Module> initModuleMap() {
        moduleMap = new HashMap<String, Module>(20);
        for (Module m : getTriggers()) {
            moduleMap.put(m.getId(), m);
        }
        for (Module m : getConditions()) {
            moduleMap.put(m.getId(), m);

        }
        for (Module m : getActions()) {
            moduleMap.put(m.getId(), m);
        }
        return moduleMap;
    }

    private static List<Action> getRuntimeActionsCopy(List<Action> actions) {
        List<Action> res = new ArrayList<Action>();
        if (actions != null) {
            for (Action action : actions) {
                res.add(new RuntimeAction(action));
            }
        }
        return res;
    }

    private static List<Condition> getRuntimeConditionsCopy(List<Condition> conditions) {
        List<Condition> res = new ArrayList<Condition>();
        if (conditions != null) {
            for (Condition condition : conditions) {
                res.add(new RuntimeCondition(condition));
            }
        }
        return res;
    }

    private static List<Trigger> getRuntimeTriggersCopy(List<Trigger> triggers) {
        List<Trigger> res = new ArrayList<Trigger>();
        if (triggers != null) {
            for (Trigger trigger : triggers) {
                res.add(new RuntimeTrigger(trigger));
            }
        }
        return res;
    }

}
