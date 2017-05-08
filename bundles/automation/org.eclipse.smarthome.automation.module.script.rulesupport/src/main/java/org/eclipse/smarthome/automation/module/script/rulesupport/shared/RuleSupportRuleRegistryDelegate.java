/**
 * Copyright (c) 2015-2017 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.rulesupport.shared;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.RuleStatusInfo;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;

/**
 * The {@link RuleSupportRuleRegistryDelegate} is wrapping a {@link RuleRegistry} to provide a comfortable way to add
 * rules to the RuleEngine without worrying about the need to remove rules again. Nonetheless, using the addPermanent
 * method it is still possible to add rules permanently.
 *
 * @author Simon Merschjohann
 *
 */
public class RuleSupportRuleRegistryDelegate implements RuleRegistry {
    private RuleRegistry ruleRegistry;

    private HashSet<String> rules = new HashSet<>();

    private ScriptedRuleProvider ruleProvider;

    public RuleSupportRuleRegistryDelegate(RuleRegistry ruleRegistry, ScriptedRuleProvider ruleProvider) {
        this.ruleRegistry = ruleRegistry;
        this.ruleProvider = ruleProvider;
    }

    @Override
    public void addRegistryChangeListener(RegistryChangeListener<Rule> listener) {
        ruleRegistry.addRegistryChangeListener(listener);
    }

    @Override
    public Collection<Rule> getAll() {
        return ruleRegistry.getAll();
    }

    @Override
    public Stream<Rule> stream() {
        return ruleRegistry.stream();
    }

    @Override
    public Rule get(String key) {
        return ruleRegistry.get(key);
    }

    @Override
    public void removeRegistryChangeListener(RegistryChangeListener<Rule> listener) {
        ruleRegistry.removeRegistryChangeListener(listener);
    }

    @Override
    public Rule add(Rule element) {
        ruleProvider.addRule(element);
        rules.add(element.getUID());

        return element;
    }

    /**
     * add a rule permanently to the RuleEngine
     *
     * @param element the rule
     */
    public void addPermanent(Rule element) {
        ruleRegistry.add(element);
    }

    @Override
    public Rule update(Rule element) {
        return ruleRegistry.update(element);
    }

    @Override
    public Rule remove(String key) {
        if (rules.remove(key)) {
            ruleProvider.removeRule(key);
        }

        return ruleRegistry.remove(key);
    }

    @Override
    public Collection<Rule> getByTag(String tag) {
        return ruleRegistry.getByTag(tag);
    }

    @Override
    public void setEnabled(String uid, boolean isEnabled) {
        ruleRegistry.setEnabled(uid, isEnabled);
    }

    @Override
    public Boolean isEnabled(String ruleUID) {
        return ruleRegistry.isEnabled(ruleUID);
    }

    /**
     * called when the script is unloaded or reloaded
     */
    public void removeAllAddedByScript() {
        for (String rule : rules) {
            try {
                ruleProvider.removeRule(rule);
            } catch (Exception ex) {
                // ignore
            }
        }
        rules.clear();
    }

    @Override
    public Collection<Rule> getByTags(String... tags) {
        return ruleRegistry.getByTags(tags);
    }

    @Override
    public RuleStatusInfo getStatusInfo(String ruleUID) {
        return ruleRegistry.getStatusInfo(ruleUID);
    }

    @Override
    public RuleStatus getStatus(String ruleUID) {
        return ruleRegistry.getStatus(ruleUID);
    }

    @Override
    public void runNow(String ruleUID) {
        ruleRegistry.runNow(ruleUID);
    }

    @Override
    public void runNow(String ruleUID, boolean considerConditions, Map<String, Object> context) {
        ruleRegistry.runNow(ruleUID, considerConditions, context);
    }

}
