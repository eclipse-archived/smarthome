/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.smarthome.automation.Rule;
import org.osgi.framework.BundleContext;

/**
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public abstract class RuleManager {

    public static final String ID_PREFIX = "rule_"; //$NON-NLS-1$
    private static RuleEngine re;

    private static int maxId;

    public RuleManager(BundleContext bc) {
        Set<RuleImpl> rules = loadRules();
        if (rules == null) {
            rules = new HashSet<RuleImpl>(20);
        }
        maxId = getMaxId(ID_PREFIX, rules) + 1;
        if (re == null) {
            re = new RuleEngine(bc);
        }
        for (Iterator<RuleImpl> it = rules.iterator(); it.hasNext();) {
            re.setRule(it.next());
        }
    }

    public void dispose() {
        if (re != null) {
            re.dispose();
            re = null;
        }
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.RuleRegistry#getRule(java.lang.String)
     */
    public synchronized Rule getRule(String ruleUID) {
        RuleImpl oldR = re.getRule(ruleUID);
        if (oldR == null)
            return null;
        return new RuleImpl(oldR);
    }

    /**
     * @see org.eclipse.smarthome.automation.RuleRegistry#getRules(java.lang.String)
     */
    public synchronized Collection<Rule> getRules(String ruleFilter) {
        // TODO impl filtering
        return re.getRules(ruleFilter);
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.RuleRegistry#getRulesByTag(java.lang.String )
     */
    public synchronized Collection<Rule> getRulesByTag(String tag) {
        return re.getRulesByTag(tag);
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.RuleRegistry#addRule(com.prosyst.mbs.services .automation.Rule)
     */
    public synchronized void addRule(Rule rule) {
        addRule0(rule, getScopeIdentifier());
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.RuleRegistry#addRule(com.prosyst.mbs.services .automation.Rule,
     *      java.lang.String)
     */
    public synchronized void addRule(Rule rule, String identity) {
        // TODO check permissions
        addRule0(rule, identity);
    }

    /**
     * @param rule
     * @param identity
     * @throws IllegalArgumentException when the rule is already added or tring to add illegal instance of rule.
     */
    private void addRule0(Rule rule, String identity) {
        if (!(rule instanceof RuleImpl)) {
            throw new IllegalArgumentException("Illegal instance of Rule: " + rule);
        }
        RuleImpl r = (RuleImpl) rule;
        String rUID = r.getUID();
        if (rUID != null) {
            if (re.getRule(rUID) != null) {
                throw new IllegalArgumentException("The rule: " + rUID + " is already added.");
            }
        } else {
            rUID = getUniqueId();
            r.setUID(rUID);
        }
        RuleImpl r1 = new RuleImpl(r);
        r1.setScopeIdentifier(identity);
        r1.setUID(rUID);
        storeRule(r1);
        re.setRule(r1);
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.RuleRegistry#updateRule(com.prosyst.mbs .services.automation.Rule)
     */
    public synchronized void updateRule(Rule rule) {
        if (rule instanceof RuleImpl) {
            throw new IllegalArgumentException("Illegal instance of Rule: " + rule);
        }
        RuleImpl r = (RuleImpl) rule;
        String rUID = r.getUID();
        if ((rUID == null) || (re.getRule(rUID) == null)) {
            throw new IllegalArgumentException("The rule: " + rule + " is not added!");
        }
        RuleImpl oldR = re.getRule(rUID);
        if (oldR == null) {
            throw new IllegalArgumentException("The rule: " + rUID
                    + " is not added. Please add the rule before update it.");
        }
        if (oldR.isEnabled()) {
            throw new IllegalStateException("The rule: " + rUID
                    + " is enabled. Please dissable the rule before update operation.");
        }
        RuleImpl r1 = new RuleImpl(r);
        re.setRule(r);
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.RuleRegistry#removeRule(java.lang.String)
     */
    public synchronized boolean removeRule(String ruleUID) {
        return re.removeRule(ruleUID) != null;
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.RuleRegistry#removeRules(java.lang.String)
     */
    public synchronized void removeRules(String filter) {
        // TODO implement it.
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.RuleRegistry#setRuleEnabled(java.lang. String, boolean)
     */
    public synchronized void setRuleEnabled(String ruleUID, boolean isEnabled) {
        re.setRuleEnable(ruleUID, isEnabled);

    }

    /**
     *
     *
     * @see org.eclipse.smarthome.automation.RuleRegistry#isRuleEnabled(java.lang.String )
     */
    public synchronized boolean isRuleEnabled(String ruleUID) {
        return re.getRule(ruleUID).isEnabled();
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.RuleRegistry#isRuleRunning(java.lang.String )
     */
    public synchronized boolean isRuleRunning(String ruleUID) {
        return re.isRunning(ruleUID);
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.RuleRegistry#getScopeIdentifiers()
     */
    public synchronized Collection<String> getScopeIdentifiers() {
        // TODO check permissions
        return re.getScopeIds();
    }

    protected String getScopeIdentifier() {
        // TODO get the caller scope id.
        return null;
    }

    protected String getUniqueId() {
        return ID_PREFIX + (++maxId);
    }

    /**
     * @param idPrefix
     * @param rules2
     * @return
     */
    private int getMaxId(String idPrefix, Collection<RuleImpl> col) {
        int result = 0;
        if (col != null) {
            for (Iterator<RuleImpl> it = col.iterator(); it.hasNext();) {
                Rule r = it.next();
                String rUID = r.getUID();
                String sNum = rUID.substring(idPrefix.length());
                int i;
                try {
                    i = Integer.parseInt(sNum);
                    result = i > result ? i : result; // find bigger key
                } catch (NumberFormatException e) {
                    // skip this key
                }
            }
        }
        return result;
    }

    /**
     * Persist the rule
     *
     * @param rule object which has to be persist.
     */
    protected abstract void storeRule(RuleImpl rule);

    /**
     * @return set of persisted rules.
     */
    protected abstract Set<RuleImpl> loadRules();

}