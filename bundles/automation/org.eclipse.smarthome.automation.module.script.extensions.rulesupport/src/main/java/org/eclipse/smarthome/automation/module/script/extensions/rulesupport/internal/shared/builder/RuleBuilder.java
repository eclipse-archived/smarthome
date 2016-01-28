/**
 * Copyright (c) 2015-2016 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

import com.google.common.base.Preconditions;

public class RuleBuilder {
    private List<Trigger> triggers = new ArrayList<>();
    private List<Condition> conditions = new ArrayList<>();
    private List<Action> actions = new ArrayList<>();
    private Map<String, Object> configurations = new HashMap<>();
    private List<ConfigDescriptionParameter> configDescriptions = new ArrayList<>();
    private String templateUID;
    private String uid;
    private String name;
    private Set<String> tags;
    private Visibility visibility = Visibility.VISIBLE;
    private String description;

    public RuleBuilder addTrigger(Trigger trigger) {
        this.triggers.add(trigger);
        return this;
    }

    public RuleBuilder addTrigger(TriggerBuilder trigger) {
        this.triggers.add(trigger.build());
        return this;
    }

    public RuleBuilder addCondition(Condition condition) {
        this.conditions.add(condition);
        return this;
    }

    public RuleBuilder addCondition(ConditionBuilder condition) {
        this.conditions.add(condition.build());
        return this;
    }

    public RuleBuilder addAction(Action action) {
        this.actions.add(action);
        return this;
    }

    public RuleBuilder addAction(ActionBuilder action) {
        this.actions.add(action.build());
        return this;
    }

    public RuleBuilder addConfiguration(String key, Object value) {
        this.configurations.put(key, value);
        return this;
    }

    public RuleBuilder addConfigDescription(ConfigDescriptionParameter configDescription) {
        this.configDescriptions.add(configDescription);
        return this;
    }

    public RuleBuilder setTemplateUID(String templateUID) {
        this.templateUID = templateUID;
        return this;
    }

    public RuleBuilder setUID(String uid) {
        this.uid = uid;
        return this;
    }

    public RuleBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public RuleBuilder addTag(String tag) {
        this.tags.add(tag);
        return this;
    }

    public RuleBuilder setVisibility(Visibility visibility) {
        this.visibility = visibility;
        return this;
    }

    public RuleBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public Rule build() {
        validate();

        Rule r = new Rule(uid, triggers, conditions, actions, configDescriptions, configurations, templateUID,
                visibility);

        r.setDescription(description);
        r.setTags(tags);

        return r;
    }

    private void validate() {
        Preconditions.checkArgument(!StringUtils.isBlank(uid), "uid may not be blank");
        Preconditions.checkArgument(!StringUtils.isBlank(name), "name may not be blank");

        if (templateUID != null && !templateUID.isEmpty()) {
            Preconditions.checkNotNull(configurations, "configuration may not be blank");
            Preconditions.checkArgument(!configurations.isEmpty(), "configuration may not be empty");
        } else {
            Preconditions.checkNotNull(triggers, "triggers may not be null");
            Preconditions.checkArgument(!triggers.isEmpty(), "triggers may not be empty");
            Preconditions.checkNotNull(actions, "actions may not be null");
            Preconditions.checkArgument(!actions.isEmpty(), "actions may not be empty");
        }

        // Preconditions.checkArgument(!StringUtils.isBlank(scopeId), "scopeId may not be blank");

        // Preconditions.checkNotNull(configDescriptions, "configDescriptions may not be null");
        // Preconditions.checkArgument(!configDescriptions.isEmpty(), "configDescriptions may not be empty");

        // Preconditions.checkNotNull(tags, "tags may not be blank");
        // Preconditions.checkNotNull(visibility, "visibility may not be null");
        // Preconditions.checkArgument(!StringUtils.isBlank(description), "description may not be blank");
    }
}
