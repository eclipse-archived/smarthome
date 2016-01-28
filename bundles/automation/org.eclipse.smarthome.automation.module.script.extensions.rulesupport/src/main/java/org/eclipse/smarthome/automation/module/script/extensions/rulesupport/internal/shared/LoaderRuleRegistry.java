/**
 * Copyright (c) 2015-2016 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.RuleStatusInfo;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;

public class LoaderRuleRegistry implements RuleRegistry {
    private RuleRegistry ruleRegistry;

    private HashSet<String> rules = new HashSet<>();

    public LoaderRuleRegistry(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
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
    public Rule get(String key) {
        return ruleRegistry.get(key);
    }

    @Override
    public void removeRegistryChangeListener(RegistryChangeListener<Rule> listener) {
        ruleRegistry.removeRegistryChangeListener(listener);
    }

    @Override
    public void add(Rule element) {
        ruleRegistry.add(element);
        rules.add(element.getUID());
    }

    public void addForever(Rule element) {
        ruleRegistry.add(element);
    }

    @Override
    public Rule update(Rule element) {
        return ruleRegistry.update(element);
    }

    @Override
    public Rule remove(String key) {
        rules.remove(key);
        return ruleRegistry.remove(key);
    }

    @Override
    public Collection<Rule> getByTag(String tag) {
        return ruleRegistry.getByTag(tag);
    }

    @Override
    public Collection<Rule> getByTags(Set<String> tags) {
        return ruleRegistry.getByTags(tags);
    }

    @Override
    public void setEnabled(String uid, boolean isEnabled) {
        ruleRegistry.setEnabled(uid, isEnabled);
    }

    @Override
    public RuleStatusInfo getStatus(String ruleUID) {
        return ruleRegistry.getStatus(ruleUID);
    }

    @Override
    public Boolean isEnabled(String ruleUID) {
        return ruleRegistry.isEnabled(ruleUID);
    }

    public void removeAllAddedByScript() {
        for (String rule : rules) {
            try {
                ruleRegistry.remove(rule);
            } catch (Exception ex) {
                // ignore
            }
        }
        rules.clear();
    }

}
