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
package org.eclipse.smarthome.automation.core.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.RuleStatusDetail;
import org.eclipse.smarthome.automation.RuleStatusInfo;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * A rule that is managed by the rule engine.
 *
 * @author Markus Rathgeb - Initial Contribution and API
 */
@NonNullByDefault
public class ManagedRule implements Rule {

    private static <T extends ModuleImpl, U extends Module> List<T> map(final List<U> in, Function<U, T> factory,
            final Collection<ModuleImpl> coll) {
        return Collections.unmodifiableList(in.stream().map(module -> {
            final T impl = factory.apply(module);
            coll.add(impl);
            return impl;
        }).collect(Collectors.toList()));

    }

    private final Rule rule;

    private RuleStatusInfo statusInfo = new RuleStatusInfo(RuleStatus.UNINITIALIZED, RuleStatusDetail.NONE);

    private final List<ModuleImpl> modules;
    private final List<ActionImpl> actions;
    private final List<ConditionImpl> conditions;
    private final List<TriggerImpl> triggers;

    public ManagedRule(final Rule rule) {
        this.rule = rule;
        final LinkedList<ModuleImpl> modules = new LinkedList<>();
        this.actions = map(rule.getActions(), ActionImpl::new, modules);
        this.conditions = map(rule.getConditions(), ConditionImpl::new, modules);
        this.triggers = map(rule.getTriggers(), TriggerImpl::new, modules);
        this.modules = Collections.unmodifiableList(modules);
    }

    @Override
    public final String getUID() {
        return rule.getUID();
    }

    public RuleStatusInfo getStatusInfo() {
        return statusInfo;
    }

    public void setStatusInfo(final RuleStatusInfo statusInfo) {
        this.statusInfo = statusInfo;
    }

    public List<ActionImpl> getActionImpls() {
        return actions;
    }

    public List<ConditionImpl> getConditionImpls() {
        return conditions;
    }

    public List<TriggerImpl> getTriggerImpls() {
        return triggers;
    }

    public List<ModuleImpl> getModuleImpls() {
        return modules;
    }

    @Override
    public @Nullable String getTemplateUID() {
        return rule.getTemplateUID();
    }

    @Override
    public @Nullable String getName() {
        return rule.getName();
    }

    @Override
    public Set<String> getTags() {
        return rule.getTags();
    }

    @Override
    public @Nullable String getDescription() {
        return rule.getDescription();
    }

    @Override
    public Visibility getVisibility() {
        return rule.getVisibility();
    }

    @Override
    public Configuration getConfiguration() {
        return rule.getConfiguration();
    }

    @Override
    public List<ConfigDescriptionParameter> getConfigurationDescriptions() {
        return rule.getConfigurationDescriptions();
    }

    @Override
    public List<Condition> getConditions() {
        return downCast(conditions);
    }

    @Override
    public List<Action> getActions() {
        return downCast(actions);
    }

    @Override
    public List<Trigger> getTriggers() {
        return downCast(triggers);
    }

    @Override
    public List<Module> getModules() {
        return downCast(modules);
    }

    @SuppressWarnings("unchecked")
    private <P, C extends P> List<P> downCast(List<C> list) {
        return (List<P>) list;
    }

}
