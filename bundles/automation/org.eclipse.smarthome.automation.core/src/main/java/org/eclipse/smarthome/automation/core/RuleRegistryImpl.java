/*
 * Copyright (c) 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of ProSyst Software GmbH. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with ProSyst.
 */
package org.eclipse.smarthome.automation.core;

import java.util.Collection;
import java.util.Set;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.core.common.registry.AbstractRegistry;

/**
 * @author Yoradan Mihaylov
 *
 */
public class RuleRegistryImpl extends AbstractRegistry<Rule, String> implements RuleRegistry {

    private RuleManager ruleManager;

    public RuleRegistryImpl(RuleManager ruleManager, RuleProvider rp) {
        this.ruleManager = ruleManager;
        this.addProvider(rp);
    }


    /* (non-Javadoc)
     * @see org.eclipse.smarthome.core.common.registry.Registry#get(java.lang.Object)
     */
    @Override
    public Rule get(String key) {
      return ruleManager.getRule(key);
    }

    /* (non-Javadoc)
     * @see org.eclipse.smarthome.automation.RuleRegistry#getByTag(java.lang.String)
     */
    @Override
    public Collection<Rule> getByTag(String tag) {
      return ruleManager.getRulesByTag(tag);
    }

    /* (non-Javadoc)
     * @see org.eclipse.smarthome.automation.RuleRegistry#setEnabled(java.lang.String, boolean)
     */
    @Override
    public void setEnabled(String uid, boolean isEnabled) {
      ruleManager.setRuleEnabled(uid, isEnabled);
    }

    /* (non-Javadoc)
     * @see org.eclipse.smarthome.automation.RuleRegistry#isEnabled(java.lang.String)
     */
    @Override
    public boolean isEnabled(String uid) {
      return ruleManager.isRuleEnabled(uid);
    }

    /* (non-Javadoc)
     * @see org.eclipse.smarthome.automation.RuleRegistry#isRunning(java.lang.String)
     */
    @Override
    public boolean isRunning(String uid) {
      return ruleManager.isRuleRunning(uid);
    }

    protected void storeRule(RuleImpl rule) {
      // TODO Auto-generated method stub
    }

    protected Set<RuleImpl> loadRules() {
        return null;
    }


    /**
     * 
     */
    public void dispose() {
      ruleManager.dispose();
    }

}
