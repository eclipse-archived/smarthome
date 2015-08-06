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
import org.eclipse.smarthome.core.common.registry.DefaultAbstractManagedProvider;
import org.osgi.framework.BundleContext;

/**
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Persistence implementation
 */
public class RuleProvider extends DefaultAbstractManagedProvider<Rule, String> {

    private RuleManagerImpl rm;

    public RuleProvider(RuleManagerImpl rm, BundleContext context) {
        this.rm = rm;
    }

    @Override
    public Collection<Rule> getAll() {
        return rm.getRules();
    }

    @Override
    public Rule update(Rule element) {
        String id = ((Rule) element).getUID();
        rm.updateRule((Rule) element);
        Rule newElement = get(id);
        notifyListenersAboutUpdatedElement(element, newElement);
        return newElement;
    }

    @Override
    public Rule get(String key) {
        String ruleUID = (String) key;
        Rule r = rm.getRule(ruleUID);
        return r;
    }

    @Override
    public void add(Rule element) {
        rm.addRule((Rule) element);
        notifyListenersAboutAddedElement(element);
    }

    @Override
    public Rule remove(String key) {
        String ruleUID = (String) key;
        Rule r = rm.getRule(ruleUID);
        rm.removeRule(ruleUID);
        notifyListenersAboutRemovedElement(r);
        return r;
    }

    @Override
    protected String getKey(Rule element) {
        return element.getUID();
    }

    @Override
    protected String getStorageName() {
        return "automation_rules";
    }

    @Override
    protected String keyToString(String key) {
        return key;
    }

}
