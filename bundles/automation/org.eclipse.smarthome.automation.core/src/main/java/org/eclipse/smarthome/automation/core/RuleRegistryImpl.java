/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import java.util.Collection;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.core.common.registry.AbstractRegistry;

/**
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Persistence implementation
 */
public class RuleRegistryImpl extends AbstractRegistry<Rule, String>implements RuleRegistry {

    private RuleManager ruleManager;
    private ManagedRuleProvider rp;

    public RuleRegistryImpl(RuleManager ruleManager, ManagedRuleProvider rp) {
        this.ruleManager = ruleManager;
        this.rp = rp;
        rp.open(this);
    }

    void providerInitCallback() {
        addProvider(rp);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.common.registry.Registry#get(java.lang.Object)
     */
    @Override
    public Rule get(String key) {
        return ruleManager.getRule(key);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.automation.RuleRegistry#getByTag(java.lang.String)
     */
    @Override
    public Collection<Rule> getByTag(String tag) {
        return ruleManager.getRulesByTag(tag);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.automation.RuleRegistry#setEnabled(java.lang.String, boolean)
     */
    @Override
    public void setEnabled(String uid, boolean isEnabled) {
        ruleManager.setRuleEnabled(uid, isEnabled);
    }

    @Override
    public RuleStatus getStatus(String ruleUID) {
        return ruleManager.getRuleStatus(ruleUID);
    }

    /**
     *
     */
    public void dispose() {
        ruleManager.dispose();
        rp.dispose();
    }

}
