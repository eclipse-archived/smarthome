/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.rest.internal.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleStatusInfo;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This is a data transfer object that is used to serialize rules with dynamic data like the status.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
public class EnrichedRuleDTO {

    public boolean enabled;
    public RuleStatusInfo status;
    public Map<String, Object> configuration;
    public List<Action> actions;
    public List<Condition> conditions;
    public List<ConfigDescriptionParameter> configDescriptions;
    public String templateUID;
    public List<Trigger> triggers;
    public String uid;
    public String name;
    public Set<String> tags;
    public String description;

    public EnrichedRuleDTO(Rule rule) {
        this.actions = rule.getActions();
        this.conditions = rule.getConditions();
        this.configDescriptions = rule.getConfigurationDescriptions();
        this.configuration = rule.getConfiguration().getProperties();
        this.templateUID = rule.getTemplateUID();
        this.triggers = rule.getTriggers();
        this.uid = rule.getUID();
        this.name = rule.getName();
        this.tags = rule.getTags();
        this.description = rule.getDescription();
    }
}
