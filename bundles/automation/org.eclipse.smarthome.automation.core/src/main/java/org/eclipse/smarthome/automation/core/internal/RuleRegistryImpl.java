/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.RuleStatusInfo;
import org.eclipse.smarthome.automation.StatusInfoCallback;
import org.eclipse.smarthome.automation.events.RuleEventFactory;
import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main implementation of the {@link RuleRegistry}, which is registered as a service.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Persistence implementation & updating rules from providers
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation and other fixes
 * @author Benedikt Niehues - added events for rules
 */
public class RuleRegistryImpl extends AbstractRegistry<Rule, String>implements RuleRegistry, StatusInfoCallback {

    private RuleEngine ruleEngine;
    private Logger logger;
    private Storage<Boolean> disabledRulesStorage;
    private boolean hasManagedRuleProvider;

    private static final String SOURCE = RuleRegistryImpl.class.getSimpleName();

    public RuleRegistryImpl(RuleEngine ruleEngine) {
        logger = LoggerFactory.getLogger(getClass());
        this.ruleEngine = ruleEngine;
        ruleEngine.setStatusInfoCallback(this);
    }

    @Override
    protected synchronized void addProvider(Provider<Rule> provider) {
        if (provider instanceof ManagedRuleProvider) {
            hasManagedRuleProvider = true;
        }
        Collection<Rule> rules = provider.getAll();
        for (Iterator<Rule> it = rules.iterator(); it.hasNext();) {
            Rule rule = it.next();
            try {
                String rUID = rule.getUID();
                if (rUID != null && disabledRulesStorage != null && disabledRulesStorage.get(rUID) != null) {
                    ruleEngine.addRule(rule, false);
                } else {
                    ruleEngine.addRule(rule, true);
                }
            } catch (Exception e) {
                logger.error("Can't add rule: " + rule.getUID() + " into rule engine from provider " + provider, e);
            }
        }
        super.addProvider(provider);
    }

    @Override
    protected synchronized void removeProvider(Provider<Rule> provider) {
        Collection<Rule> rules = provider.getAll();
        for (Iterator<Rule> it = rules.iterator(); it.hasNext();) {
            Rule rule = it.next();
            String uid = rule.getUID();
            if (ruleEngine.removeRule(uid)) {
                postEvent(RuleEventFactory.createRuleRemovedEvent(rule, SOURCE));
            }
            if (disabledRulesStorage != null) {
                disabledRulesStorage.remove(uid);
            }
        }
        if (provider instanceof ManagedRuleProvider) {
            hasManagedRuleProvider = false;
        }
        super.removeProvider(provider);
    }

    @Override
    public synchronized Rule add(Rule element) {
        if (element == null) {
            throw new IllegalArgumentException("The added rule must not be null!");
        }
        String rUID = element.getUID();
        Rule ruleToPersist;
        if (rUID != null && disabledRulesStorage != null && disabledRulesStorage.get(rUID) != null) {
            ruleToPersist = ruleEngine.addRule(element, false);
        } else {
            ruleToPersist = ruleEngine.addRule(element, true);
        }
        super.add(ruleToPersist);
        postEvent(RuleEventFactory.createRuleAddedEvent(ruleToPersist, SOURCE));
        return ruleToPersist;
    }

    @Override
    public synchronized Rule remove(String key) {
        Rule rule = super.remove(key);
        if (ruleEngine.removeRule(key)) {
            postEvent(RuleEventFactory.createRuleRemovedEvent(rule, SOURCE));
        }
        if (disabledRulesStorage != null) {
            disabledRulesStorage.remove(key);
        }
        return rule;
    }

    @Override
    public synchronized Rule update(Rule element) {
        Rule old = null;
        if (element != null) {
            old = super.update(element); // update storage with new rule and return old rule
            if (old != null) {
                onUpdateElement(old, element); // update memory map
            }
        }
        return old;
    }

    @Override
    protected void onUpdateElement(Rule oldElement, Rule element) throws IllegalArgumentException {
        postEvent(RuleEventFactory.createRuleUpdatedEvent(element, oldElement, SOURCE));
        String rUID = element.getUID();
        if (disabledRulesStorage != null && disabledRulesStorage.get(rUID) != null) {
            ruleEngine.setRuleEnabled(rUID, false);
        }
        ruleEngine.updateRule(element);
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
    public synchronized void setEnabled(String uid, boolean isEnabled) {
        ruleEngine.setRuleEnabled(uid, isEnabled);
        if (disabledRulesStorage != null) {
            if (isEnabled) {
                disabledRulesStorage.remove(uid);
            } else {
                disabledRulesStorage.put(uid, isEnabled);
            }
        }
    }

    @Override
    public RuleStatusInfo getStatus(String ruleUID) {
        return ruleEngine.getRuleStatusInfo(ruleUID);
    }

    protected void setDisabledRuleStorage(Storage<Boolean> disabledRulesStorage) {
        this.disabledRulesStorage = disabledRulesStorage;
        for (Rule rule : ruleEngine.getRules()) {
            String uid = rule.getUID();
            if (ruleEngine.getRuleStatus(uid).equals(RuleStatus.DISABLED)) {
                disabledRulesStorage.put(uid, false);
            } else {
                disabledRulesStorage.remove(uid);
            }
        }
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        super.setEventPublisher(eventPublisher);
    }

    @Override
    public void unsetEventPublisher(EventPublisher eventPublisher) {
        super.unsetEventPublisher(eventPublisher);
    }

    @Override
    public void statusInfoChanged(String ruleUID, RuleStatusInfo statusInfo) {
        postEvent(RuleEventFactory.createRuleStatusInfoEvent(statusInfo, ruleUID, SOURCE));
    }

    @Override
    public Boolean isEnabled(String ruleUID) {
        if (disabledRulesStorage != null && disabledRulesStorage.get(ruleUID) != null) {
            return Boolean.FALSE;
        }
        return ruleEngine.hasRule(ruleUID) ? !ruleEngine.getRuleStatus(ruleUID).equals(RuleStatus.DISABLED) : null;
    }

    public void dispose() {
    }
}
