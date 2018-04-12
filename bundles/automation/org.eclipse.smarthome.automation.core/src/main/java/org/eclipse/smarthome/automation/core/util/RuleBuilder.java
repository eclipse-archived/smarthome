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
package org.eclipse.smarthome.automation.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.automation.core.internal.ActionImpl;
import org.eclipse.smarthome.automation.core.internal.ConditionImpl;
import org.eclipse.smarthome.automation.core.internal.RuleImpl;
import org.eclipse.smarthome.automation.core.internal.TriggerImpl;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This class allows the easy construction of a {@link Rule} instance using the builder pattern.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@NonNullByDefault
public class RuleBuilder {

    private final RuleImpl rule;

    protected RuleBuilder(RuleImpl rule) {
        this.rule = rule;
    }

    public static RuleBuilder create(String ruleId) {
        RuleImpl rule = new RuleImpl(ruleId);
        return new RuleBuilder(rule);
    }

    public static RuleBuilder create(Rule r) {
        return create(r.getUID()).withActions(r.getActions()).withConditions(r.getConditions())
                .withTriggers(r.getTriggers()).withConfiguration(r.getConfiguration())
                .withConfigurationDescriptions(r.getConfigurationDescriptions()).withDescription(r.getDescription())
                .withName(r.getName()).withTags(r.getTags());
    }

    public RuleBuilder withName(@Nullable String name) {
        this.rule.setName(name);
        return this;
    }

    public RuleBuilder withDescription(@Nullable String description) {
        this.rule.setDescription(description);
        return this;
    }

    public RuleBuilder withTemplateUID(String uid) {
        this.rule.setTemplateUID(uid);
        return this;
    }

    public RuleBuilder withVisibility(Visibility visibility) {
        this.rule.setVisibility(visibility);
        return this;
    }

    public RuleBuilder withTriggers(Trigger... triggers) {
        return withTriggers(Arrays.asList(triggers));
    }

    public RuleBuilder withTriggers(List<? extends Trigger> triggers) {
        ArrayList<TriggerImpl> triggerList = new ArrayList<>(triggers.size());
        triggers.forEach(t -> triggerList.add((TriggerImpl) ModuleBuilder.createTrigger(t).build()));
        this.rule.setTriggers(triggerList);
        return this;
    }

    public RuleBuilder withConditions(Condition... conditions) {
        return withConditions(Arrays.asList(conditions));
    }

    public RuleBuilder withConditions(List<? extends Condition> conditions) {
        ArrayList<ConditionImpl> conditionList = new ArrayList<>(conditions.size());
        conditions.forEach(c -> conditionList.add((ConditionImpl) ModuleBuilder.createCondition(c).build()));
        this.rule.setConditions(conditionList);
        return this;
    }

    public RuleBuilder withActions(Action... actions) {
        return withActions(Arrays.asList(actions));
    }

    public RuleBuilder withActions(List<? extends Action> actions) {
        ArrayList<ActionImpl> actionList = new ArrayList<>(actions.size());
        actions.forEach(a -> actionList.add((ActionImpl) ModuleBuilder.createAction(a).build()));
        this.rule.setActions(actionList);
        return this;
    }

    public RuleBuilder withTags(String... tags) {
        withTags(new HashSet<>(Arrays.asList(tags)));
        return this;
    }

    public RuleBuilder withTags(Set<String> tags) {
        this.rule.setTags(new HashSet<>(tags));
        return this;
    }

    public RuleBuilder withConfiguration(Configuration ruleConfiguration) {
        this.rule.setConfiguration(ruleConfiguration);
        return this;
    }

    public RuleBuilder withConfigurationDescriptions(List<ConfigDescriptionParameter> configDescs) {
        this.rule.setConfigurationDescriptions(new ArrayList<>(configDescs));
        return this;
    }

    public Rule build() {
        return this.rule;
    }

}
