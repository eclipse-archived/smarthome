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
import java.util.Iterator;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleStatus;

/**
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public abstract class RuleManager {

    public static final String ID_PREFIX = "rule_"; //$NON-NLS-1$
    protected BundleContext bc;
    protected static RuleEngine re;
    protected Logger log;

    protected static int maxId = 0;

    public RuleManager(BundleContext bc) {
        this.bc = bc;
        log = LoggerFactory.getLogger(RuleManager.class);
        if (re == null) {
            re = new RuleEngine(bc);
        }
    }

    public void dispose() {
        if (re != null) {
            re.dispose();
            re = null;
        }
    }

    public synchronized Rule getRule(String ruleUID) {
        RuleImpl oldR = re.getRule(ruleUID);
        if (oldR == null)
            return null;
        return new RuleImpl(oldR);
    }

    public synchronized Collection<Rule> getRulesByTag(String tag) {
        return re.getRulesByTag(tag);
    }

    public synchronized Collection<Rule> getRulesByTags(Set<String> tags) {
        return re.getRulesByTags(tags);
    }

    public synchronized Collection<Rule> getRules() {
        return re.getRulesByTag((String) null);
    }

    public synchronized RuleStatus getRuleStatus(String rUID) {
        return RuleEngine.getRuleStatus(rUID);
    }

    public synchronized void addRule(Rule rule) {
        addRule0(rule, getScopeIdentifier());
    }

    public synchronized void addRule(Rule rule, String identity) {
        // TODO check permissions
        addRule0(rule, identity);
    }

    /**
     * @param rule
     * @param identity
     * @throws IllegalArgumentException when the rule is already added or tring to add illegal instance of rule.
     */
    protected RuleImpl addRule0(Rule rule, String identity) {
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
        re.setRule(r1);
        return r1;
    }

    public synchronized void updateRule(Rule rule) {
        RuleImpl r1 = assertRule(rule);
        re.setRule(r1);
    }

    protected RuleImpl assertRule(Rule rule) {
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
        RuleImpl r1 = new RuleImpl(r);
        return r1;
    }

    public synchronized boolean removeRule(String ruleUID) {
        RuleImpl r = re.removeRule(ruleUID);
        return r != null;
    }

    public synchronized void setRuleEnabled(String ruleUID, boolean isEnabled) {
        re.setRuleEnable(ruleUID, isEnabled);

    }

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
    protected int getMaxId(String idPrefix, Collection<RuleImpl> col) {
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

    public RuleStatus getStatus(String ruleUID) {
        return re.getRuleStatus(ruleUID);
    }

}