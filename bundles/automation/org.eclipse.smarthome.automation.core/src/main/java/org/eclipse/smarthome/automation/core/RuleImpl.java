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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * @author Yordan Mihaylov - Initial Contribution
 *
 */
public class RuleImpl implements Rule {

    private String uid;
    private String name;
    private Set<String> tags;
    private String description;
    protected List<Trigger> triggers;
    protected List<Condition> conditions;
    protected List<Action> actions;
    private Set<ConfigDescriptionParameter> configDescriptions;
    private Map<String, ?> configurations;
    private String scopeId;
    private Map<String, Module> moduleMap;
    private boolean isEnabled = true;

    public RuleImpl(List<Trigger> triggers, //
            List<Condition> conditions, //
            List<Action> actions, Set<ConfigDescriptionParameter> configDescriptions,//
            Map<String, ?> configurations) {

        this.triggers = triggers;
        this.conditions = conditions;
        this.actions = actions;
        this.configDescriptions = configDescriptions;
        this.configurations = configurations;

    }

    /**
     * Utility constructor creating copy of the Rule.
     *
     * @param rule
     */
    protected RuleImpl(RuleImpl rule) {
        this(rule.getModules(Trigger.class), //
                rule.getModules(Condition.class),//
                rule.getModules(Action.class), //
                rule.getConfigurationDescriptions(), //
                rule.getConfiguration());
        uid = rule.getUID();
        setName(rule.getName());
        setTags(rule.getTags());
        setDescription(rule.getDescription());
        setEnabled(rule.isEnabled());
    }

    /**
     * @see org.eclipse.smarthome.automation.Rule#getUID()
     */
    @Override
    public String getUID() {
        return uid;
    }

    protected void setUID(String uid) {
        this.uid = uid;
    }

    /**
     * @see org.eclipse.smarthome.automation.Rule#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.Rule#setName(java.lang.String)
     */
    @Override
    public void setName(String ruleName) throws IllegalStateException {
        this.name = ruleName;

    }

    /**
     *
     * @see org.eclipse.smarthome.automation.Rule#getTags()
     */
    @Override
    public Set<String> getTags() {
        return tags;
    }

    /**
     * @see org.eclipse.smarthome.automation.Rule#setTags(java.util.Set)
     */
    @Override
    public void setTags(Set<String> ruleTags) throws IllegalStateException {
        this.tags = ruleTags;
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.Rule#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.Rule#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String ruleDescription) {
        this.description = ruleDescription;

    }

    /**
     *
     * @see org.eclipse.smarthome.automation.Rule#getConfigurationDescriptions()
     */
    @Override
    public Set<ConfigDescriptionParameter> getConfigurationDescriptions() {
        return configDescriptions;
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.Rule#getConfiguration()
     */
    @Override
    public Map<String, Object> getConfiguration() {
        return configurations != null ? new HashMap<String, Object>(configurations) : null;
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.Rule#setConfiguration(java.util.Map)
     */
    @Override
    public void setConfiguration(Map<String, ?> ruleConfiguration) {
        this.configurations = ruleConfiguration != null ? new HashMap<String, Object>(ruleConfiguration) : null;
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.Rule#getModule(java.lang.String)
     */
    @Override
    public <T extends Module> T getModule(String moduleId) {
        Module m = getModule0(moduleId);
        return (T) m;
    }

    protected Module getModule0(String moduleId) {
        if (moduleMap == null) {
            moduleMap = initModuleMap();
        }
        return moduleMap.get(moduleId);
    }

    /**
     *
     */
    private Map<String, Module> initModuleMap() {
        moduleMap = new HashMap<String, Module>(20);
        if (triggers != null) {
            for (Iterator<Trigger> it = triggers.iterator(); it.hasNext();) {
                Trigger m = it.next();
                moduleMap.put(m.getId(), m);

            }
        }
        if (conditions != null) {
            for (Iterator<Condition> it = conditions.iterator(); it.hasNext();) {
                Condition m = it.next();
                moduleMap.put(m.getId(), m);

            }
        }
        if (actions != null) {
            for (Iterator<Action> it = actions.iterator(); it.hasNext();) {
                Action m = it.next();
                moduleMap.put(m.getId(), m);
            }
        }
        return moduleMap;
    }

    /**
     * @see org.eclipse.smarthome.automation.Rule#getModules(java.lang.Class)
     */
    @Override
    public <T extends Module> List<T> getModules(Class<T> moduleClazz) {
        List<T> result = null;
        if (moduleClazz == null || Trigger.class == moduleClazz) {
            List<Trigger> l = triggers;
            if (moduleClazz != null) {// only triggers
                return (List<T>) l;
            }
            result = getList(result);
            result.addAll((List<T>) l);
        }
        if (moduleClazz == null || Condition.class == moduleClazz) {
            List<Condition> l = conditions;
            if (moduleClazz != null) {// only conditions
                return (List<T>) l;
            }
            result = getList(result);
            result.addAll((List<T>) l);
        }
        if (moduleClazz == null || Action.class == moduleClazz) {
            List<Action> l = actions;
            if (moduleClazz != null) {// only actions
                return (List<T>) l;
            }
            result = getList(result);
            result.addAll((List<T>) l);
        }
        return result;
    }

    private <T extends Module> List<T> getList(List<T> t) {
        if (t != null) {
            return t;
        }
        return new ArrayList<T>();
    }

    /**
     *
     * @see org.eclipse.smarthome.automation.Rule#getScopeIdentifier()
     */
    @Override
    public String getScopeIdentifier() {
        return scopeId;
    }

    protected void setScopeIdentifier(String scopeId) {
        this.scopeId = scopeId;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RuleImpl && getUID() != null) {
            RuleImpl r = (RuleImpl) obj;
            return getUID().equals(r.getUID());
        }
        return super.equals(obj);
    }

    /**
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        if (getUID() != null) {
            return getUID().hashCode();
        }
        return super.hashCode();
    }
}
