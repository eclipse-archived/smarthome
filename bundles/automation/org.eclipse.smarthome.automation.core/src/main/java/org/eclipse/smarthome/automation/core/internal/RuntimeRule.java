/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;

/**
 * This is a class to be used within the {@link RuleEngine} for resolved rules.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 *
 */
public class RuntimeRule extends Rule {
    /**
     * Module configuration properties can have reference to Rule configuration properties.
     * This symbol is put as prefix to the name of the Rule configuration property.
     */
    private static final char REFERENCE_SYMBOL = '$';

    private Map<String, Module> moduleMap;

    /**
     * @param ruleTemplateUID
     * @param configurations
     * @throws Exception
     */
    public RuntimeRule(String ruleTemplateUID, Map<String, ?> configurations) {
        super(ruleTemplateUID, configurations);
    }

    public RuntimeRule(Rule rule, RuleTemplate template) {
        super(rule.getUID(), getRuntimeTriggersCopy(template.getTriggers()),
                getRuntimeConditionsCopy(template.getConditions()), getRuntimeActionsCopy(template.getActions()), null,
                null, template.getVisibility());
        validateConfiguration(template.getConfigurationDescription(), rule.getConfiguration());
        setName(rule.getName());
        setTags(template.getTags());
        setDescription(template.getDescription());
    }

    /**
     * Utility constructor creating copy of the Rule or create a new empty instance.
     *
     * @param rule a rule which has to be copied or null when an empty instance of rule
     *            has to be created.
     */
    protected RuntimeRule(Rule rule) {
        super(rule.getUID(), getRuntimeTriggersCopy(rule.getTriggers()), getRuntimeConditionsCopy(rule.getConditions()),
                getRuntimeActionsCopy(rule.getActions()), rule.getConfigurationDescriptions(), rule.getConfiguration(),
                rule.getTemplateUID(), rule.getVisibility());
        setName(rule.getName());
        setTags(rule.getTags());
        setDescription(rule.getDescription());
    }

    @Override
    public void setConfiguration(Map<String, ?> ruleConfiguration) {
        this.configuration = ruleConfiguration != null ? new HashMap<String, Object>(ruleConfiguration)
                : new HashMap<String, Object>(11);
    }

    @Override
    public Module getModule(String moduleId) {
        if (moduleMap == null) {
            moduleMap = initModuleMap();
        }
        return moduleMap.get(moduleId);
    }

    private Map<String, Module> initModuleMap() {
        moduleMap = new HashMap<String, Module>(20);
        for (Module m : getTriggers()) {
            moduleMap.put(m.getId(), m);
        }
        for (Module m : getConditions()) {
            moduleMap.put(m.getId(), m);

        }
        for (Module m : getActions()) {
            moduleMap.put(m.getId(), m);
        }
        return moduleMap;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RuntimeRule && getUID() != null) {
            RuntimeRule r = (RuntimeRule) obj;
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

    /**
     *
     * @param configurations
     */
    private void validateConfiguration0(List<ConfigDescriptionParameter> configDescriptions,
            Map<String, Object> configurations) {
        if (configurations == null || configurations.isEmpty()) {
            if (isOptionalConfig(configDescriptions)) {
                return;
            } else {
                throw new IllegalArgumentException("Missing required configuration properties!");
            }
        } else {
            for (ConfigDescriptionParameter configParameter : configDescriptions) {
                String configParameterName = configParameter.getName();
                processValue(configurations.remove(configParameterName), configParameter);
            }
            if (!configurations.isEmpty()) {
                String msg = "\"";
                Iterator<ConfigDescriptionParameter> i = configDescriptions.iterator();
                while (i.hasNext()) {
                    ConfigDescriptionParameter configParameter = i.next();
                    if (i.hasNext()) {
                        msg = msg + configParameter.getName() + "\", ";
                    } else {
                        msg = msg + configParameter.getName();
                    }
                }
                throw new IllegalArgumentException("Extra configuration properties : " + msg + "\"!");
            }
        }

    }

    private boolean isOptionalConfig(List<ConfigDescriptionParameter> configDescriptions) {
        if (configDescriptions != null && !configDescriptions.isEmpty()) {
            boolean required = false;
            Iterator<ConfigDescriptionParameter> i = configDescriptions.iterator();
            while (i.hasNext()) {
                ConfigDescriptionParameter param = i.next();
                required = required || param.isRequired();
            }
            return !required;
        }
        return true;
    }

    private void processValue(Object configValue, ConfigDescriptionParameter configParameter) {
        if (configValue != null) {
            checkType(configValue, configParameter);
            return;
        }
        if (configParameter.getDefault() != null) {
            return;
        }
        if (configParameter.isRequired()) {
            throw new IllegalArgumentException(
                    "Required configuration property missing: \"" + configParameter.getName() + "\"!");
        }
    }

    @SuppressWarnings("rawtypes")
    private void checkType(Object configValue, ConfigDescriptionParameter configParameter) {
        Type type = configParameter.getType();
        if (configParameter.isMultiple()) {
            if (configValue instanceof List) {
                List lConfigValues = (List) configValue;
                for (Object value : lConfigValues) {
                    if (!checkType(type, value)) {
                        throw new IllegalArgumentException("Unexpected value for configuration property \""
                                + configParameter.getName() + "\". Expected type: " + type);
                    }
                }
            }
            throw new IllegalArgumentException(
                    "Unexpected value for configuration property \"" + configParameter.getName()
                            + "\". Expected is Array with type for elements : " + type.toString() + "!");
        } else {
            if (!checkType(type, configValue)) {
                throw new IllegalArgumentException("Unexpected value for configuration property \""
                        + configParameter.getName() + "\". Expected is " + type.toString() + "!");
            }
        }
    }

    private boolean checkType(Type type, Object configValue) {
        switch (type) {
            case TEXT:
                return configValue instanceof String;
            case BOOLEAN:
                return configValue instanceof Boolean;
            case INTEGER:
                return configValue instanceof BigDecimal || configValue instanceof Integer
                        || configValue instanceof Double && ((Double) configValue).intValue() == (double) configValue;
            case DECIMAL:
                return configValue instanceof BigDecimal || configValue instanceof Double;
        }
        return false;
    }

    private void validateConfiguration(List<ConfigDescriptionParameter> configDescriptions,
            Map<String, ?> ruleConfiguration) {
        if (ruleConfiguration != null) {
            validateConfiguration0(configDescriptions, new HashMap<String, Object>(ruleConfiguration));
            handleModuleConfigReferences(getTriggers(), ruleConfiguration);
            handleModuleConfigReferences(getConditions(), ruleConfiguration);
            handleModuleConfigReferences(getActions(), ruleConfiguration);
        }
    }

    private void handleModuleConfigReferences(List<? extends Module> modules, Map<String, ?> ruleConfiguration) {
        if (modules != null) {
            for (Module module : modules) {
                ReferenceResolverUtil.updateModuleConfiguration(module, ruleConfiguration);
            }
        }
    }

    protected void setUID(String rUID) {
        uid = rUID;
    }

    private static List<Action> getActionsCopy(List<Action> actions) {
        List<Action> res = new ArrayList<Action>();
        if (actions != null) {
            for (Action a : actions) {
                Action action = new Action(a.getId(), a.getTypeUID(), a.getConfiguration(), a.getInputs());
                action.setLabel(a.getLabel());
                action.setDescription(a.getDescription());
                res.add(action);
            }
        }
        return res;
    }

    private static List<Action> getRuntimeActionsCopy(List<Action> actions) {
        List<Action> res = new ArrayList<Action>();
        if (actions != null) {
            for (Action action : actions) {
                res.add(new RuntimeAction(action));
            }
        }
        return res;
    }

    private static List<Condition> getConditionsCopy(List<Condition> conditions) {
        List<Condition> res = new ArrayList<Condition>(11);
        if (conditions != null) {
            for (Condition c : conditions) {
                Condition condition = new Condition(c.getId(), c.getTypeUID(), c.getConfiguration(), c.getInputs());
                condition.setLabel(c.getLabel());
                condition.setDescription(c.getDescription());
                res.add(condition);
            }
        }
        return res;
    }

    private static List<Condition> getRuntimeConditionsCopy(List<Condition> conditions) {
        List<Condition> res = new ArrayList<Condition>(11);
        if (conditions != null) {
            for (Condition condition : conditions) {
                res.add(new RuntimeCondition(condition));
            }
        }
        return res;
    }

    private static List<Trigger> getTriggersCopy(List<Trigger> triggers) {
        List<Trigger> res = new ArrayList<Trigger>(11);
        if (triggers != null) {
            for (Trigger t : triggers) {
                Trigger trigger = new Trigger(t.getId(), t.getTypeUID(), t.getConfiguration());
                trigger.setLabel(t.getLabel());
                trigger.setDescription(t.getDescription());
                res.add(trigger);
            }
        }
        return res;
    }

    private static List<Trigger> getRuntimeTriggersCopy(List<Trigger> triggers) {
        List<Trigger> res = new ArrayList<Trigger>(11);
        if (triggers != null) {
            for (Trigger trigger : triggers) {
                res.add(new RuntimeTrigger(trigger));
            }
        }
        return res;
    }

    protected Rule getRuleCopy() {
        Rule rule = new Rule(getUID(), getTriggersCopy(getTriggers()), getConditionsCopy(getConditions()),
                getActionsCopy(getActions()), getConfigurationDescriptions(), getConfiguration(), getTemplateUID(),
                getVisibility());
        rule.setName(getName());
        rule.setTags(getTags());
        rule.setDescription(getDescription());
        return rule;
    }
}
