/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This class provides functionality for (de)serialization of {@link Rule}s.
 *
 * @author Ana Dimova - Initial Contribution
 * @author Kai Kreuzer - changed naming to DTO convention
 *
 */
public class RuleDTO {

    /**
     * The uid of the {@link Rule}.
     */
    public String uid;

    /**
     * This field holds the name of this {@link Rule}.
     */
    public String name;

    /**
     * This field holds the set of tags of this {@link Rule}.
     */
    public Set<String> tags;

    /**
     * This field holds a short, user friendly description of this {@link Rule}.
     */
    public String description;

    /**
     * This field holds the list of triggers of this {@link Rule}.
     */
    public List<TriggerDTO> triggers;

    /**
     * This field holds the list of conditions of this {@link Rule}.
     */
    public List<ConditionDTO> conditions;

    /**
     * This field holds the list of actions of this {@link Rule}.
     */
    public List<ActionDTO> actions;

    /**
     * This field holds the set of config description parameters of this {@link Rule}.
     */
    public Set<ConfigDescriptionParameter> configDescriptions;

    /**
     * This field holds the current configuration values of this {@link Rule}.
     */
    public Map<String, ?> configurations;

    /**
     * This field holds the scope id of this {@link Rule}.
     */
    public String scopeId;

    /**
     * This constructor is used for deserialization of the {@link Rule}s.
     */
    public RuleDTO() {}

    /**
     * This constructor is used for serialization of the {@link Rule}s.
     */
    public RuleDTO(Rule element) {
        uid = element.getUID();
        name = element.getName();
        description = element.getDescription();
        tags = element.getTags();
        configDescriptions = element.getConfigurationDescriptions();
        configurations = element.getConfiguration();
        scopeId = element.getScopeIdentifier();
        List<Action> actions = element.getModules(Action.class);
        this.actions = new ArrayList<ActionDTO>();
        for (Action action : actions) {
            this.actions.add(new ActionDTO(action));
        }
        List<Condition> conds = element.getModules(Condition.class);
        if (conds != null) {
            conditions = new ArrayList<ConditionDTO>();
            for (Condition condition : conds) {
                conditions.add(new ConditionDTO(condition));
            }
        }
        List<Trigger> triggers = element.getModules(Trigger.class);
        this.triggers = new ArrayList<TriggerDTO>();
        for (Trigger trigger : triggers) {
            this.triggers.add(new TriggerDTO(trigger));
        }
    }
}
