/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import java.util.Collection;
import java.util.Set;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.core.common.registry.AbstractRegistry;

/**
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Persistence implementation
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 */
public class RuleRegistryImpl extends AbstractRegistry<Rule, String>implements RuleRegistry {

    private RuleEngine ruleEngine;

    public RuleRegistryImpl(RuleEngine ruleManager, ManagedRuleProvider rp) {
        this.ruleEngine = ruleManager;
        if (rp != null) {
            addProvider(rp);
        }
    }

    @Override
    public void add(Rule element) {
        RuleImpl ruleWithId;
        if (element.getUID() == null) {
            ruleWithId = ruleEngine.addRule0(element, ruleEngine.getScopeIdentifier());
        } else {
            ruleWithId = (RuleImpl) element;
        }
        super.add(ruleWithId);
    }

    @Override
    public Rule remove(String key) {
        ruleEngine.removeRule(key);
        return super.remove(key);
    }

    @Override
    public Rule update(Rule element) {
        if (element != null) {
            ruleEngine.updateRule(element);// update memory map
            element = super.update(element);// update storage with new rule and return old rule
        }
        return element;
    }

    @Override
    public Rule get(String key) {
        return ruleEngine.getRule(key);
    }

    @Override
    public Collection<Rule> getByTag(String tag) {
        return ruleEngine.getRulesByTag(tag);
    }

    @Override
    public Collection<Rule> getByTags(Set<String> tags) {
        return ruleEngine.getRulesByTags(tags);
    }

    @Override
    public void setEnabled(String uid, boolean isEnabled) {
        ruleEngine.setRuleEnabled(uid, isEnabled);
    }

    @Override
    public RuleStatus getStatus(String ruleUID) {
        return ruleEngine.getRuleStatus(ruleUID);
    }

    public void dispose() {
        ruleEngine.dispose();
    }

}
