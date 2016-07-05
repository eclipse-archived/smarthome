/**
 * Copyright (c) 2016 Markus Rathgeb
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.automation.rest.internal.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This is a data transfer object that is used to serialize rules.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class RuleDTO {

    public List<TriggerDTO> triggers;
    public List<ConditionDTO> conditions;
    public List<ActionDTO> actions;
    public Map<String, Object> configuration;
    public List<ConfigDescriptionParameter> configDescriptions;
    public String templateUID;
    public String uid;
    public String name;
    public Set<String> tags;
    public Visibility visibility;
    public String description;

    public RuleDTO(final Rule rule) {
        this.triggers = fromTriggerList(rule.getTriggers());
        this.conditions = fromConditionList(rule.getConditions());
        this.actions = fromActionList(rule.getActions());
        this.configuration = rule.getConfiguration().getProperties();
        this.configDescriptions = rule.getConfigurationDescriptions();
        this.templateUID = rule.getTemplateUID();
        this.uid = rule.getUID();
        this.name = rule.getName();
        this.tags = rule.getTags();
        this.visibility = rule.getVisibility();
        this.description = rule.getDescription();
    }

    public Rule createRule() {
        final Rule rule = new Rule(uid, toTriggerList(triggers), toConditionList(conditions), toActionList(actions),
                configDescriptions, new Configuration(configuration), templateUID, visibility);
        rule.setTags(tags);
        rule.setName(name);
        rule.setDescription(description);
        return rule;
    }

    private List<ActionDTO> fromActionList(final List<Action> actions) {
        if (actions == null) {
            return null;
        }
        final List<ActionDTO> dtos = new ArrayList<>(actions.size());
        for (final Action action : actions) {
            dtos.add(new ActionDTO(action));
        }
        return dtos;
    }

    private List<Action> toActionList(final List<ActionDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        final List<Action> actions = new ArrayList<>(dtos.size());
        for (final ActionDTO dto : dtos) {
            actions.add(dto.createAction());
        }
        return actions;
    }

    private List<ConditionDTO> fromConditionList(final List<Condition> conditions) {
        if (conditions == null) {
            return null;
        }
        final List<ConditionDTO> dtos = new ArrayList<>(conditions.size());
        for (final Condition action : conditions) {
            dtos.add(new ConditionDTO(action));
        }
        return dtos;
    }

    private List<Condition> toConditionList(final List<ConditionDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        final List<Condition> conditions = new ArrayList<>(dtos.size());
        for (final ConditionDTO dto : dtos) {
            conditions.add(dto.createCondition());
        }
        return conditions;
    }

    private List<TriggerDTO> fromTriggerList(final List<Trigger> triggers) {
        if (triggers == null) {
            return null;
        }
        final List<TriggerDTO> dtos = new ArrayList<>(triggers.size());
        for (final Trigger action : triggers) {
            dtos.add(new TriggerDTO(action));
        }
        return dtos;
    }

    private List<Trigger> toTriggerList(final List<TriggerDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        final List<Trigger> triggers = new ArrayList<>(dtos.size());
        for (final TriggerDTO dto : dtos) {
            triggers.add(dto.createTrigger());
        }
        return triggers;
    }

}
