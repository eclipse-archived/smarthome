/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
import org.eclipse.smarthome.automation.core.util.ConnectionValidator;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;

/**
 * @author Yordan Mihaylov - Initial Contribution
 *
 */
public class RuleImpl implements Rule {
    /**
     * Module configuration properties can have reference to Rule configuration properties.
     * This symbol is put as prefix to the name of the Rule configuration property.
     */
    private static final char REFERENCE_SYMBOL = '$';
    private String uid;
    private String name;
    private Set<String> tags;
    private String description;
    protected List<Trigger> triggers;
    protected List<Condition> conditions;
    protected List<Action> actions;
    protected Set<ConfigDescriptionParameter> configDescriptions;
    private Map<String, ?> configurations;
    private String scopeId;
    private Map<String, Module> moduleMap;

    protected boolean initialEnabled = true;

    // private boolean isInitialized = false;
    // private List<RuleError> errors;

    public RuleImpl(List<Trigger> triggers, //
            List<Condition> conditions, //
            List<Action> actions, Set<ConfigDescriptionParameter> configDescriptions, //
            Map<String, ?> configurations) {

        // the rule must not be created if connections are incorrect
        ConnectionValidator.validateConnections(Activator.moduleTypeRegistry, triggers, conditions, actions);

        this.triggers = triggers;
        this.conditions = conditions;
        this.actions = actions;
        this.configDescriptions = configDescriptions;
        this.configurations = configurations;

        handleModuleConfigReferences(triggers, conditions, actions, configurations);
    }

    /**
     * @param ruleTemplateUID
     * @param configurations
     * @throws Exception
     */
    public RuleImpl(String ruleTemplateUID, Map<String, Object> configurations) {
        RuleTemplate template = (RuleTemplate) Activator.templateRegistry.get(ruleTemplateUID);
        this.triggers = template.getModules(Trigger.class);
        this.conditions = template.getModules(Condition.class);
        this.actions = template.getModules(Action.class);
        configDescriptions = template.getConfigurationDescription();

        // the rule must not be created if configuration is incorrect
        validateConfiguration(configDescriptions, new HashMap(configurations));
        this.configurations = configurations;
        handleModuleConfigReferences(triggers, conditions, actions, configurations);
    }

    /**
     * Utility constructor creating copy of the Rule or create a new empty instance.
     *
     * @param rule a rule which has to be copied or null when an empty instance of rule
     *            has to be created.
     */
    protected RuleImpl(RuleImpl rule) {
        if (rule != null) {
            this.triggers = rule.getModules(Trigger.class);
            this.conditions = rule.getModules(Condition.class);
            this.actions = rule.getModules(Action.class);
            this.configDescriptions = rule.getConfigurationDescriptions();
            this.configurations = rule.getConfiguration();
            uid = rule.getUID();
            setName(rule.getName());
            setTags(rule.getTags());
            setDescription(rule.getDescription());
            // setEnabled(rule.isEnabled());
        }
    }

    @Override
    public String getUID() {
        return uid;
    }

    protected void setUID(String uid) {
        this.uid = uid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String ruleName) throws IllegalStateException {
        this.name = ruleName;

    }

    @Override
    public Set<String> getTags() {
        return tags;
    }

    @Override
    public void setTags(Set<String> ruleTags) throws IllegalStateException {
        this.tags = ruleTags;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String ruleDescription) {
        this.description = ruleDescription;

    }

    @Override
    public Set<ConfigDescriptionParameter> getConfigurationDescriptions() {
        return configDescriptions;
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return configurations != null ? new HashMap<String, Object>(configurations) : null;
    }

    @Override
    public void setConfiguration(Map<String, ?> ruleConfiguration) {
        this.configurations = ruleConfiguration != null ? new HashMap<String, Object>(ruleConfiguration) : null;
    }

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

    @Override
    public String getScopeIdentifier() {
        return scopeId;
    }

    protected void setScopeIdentifier(String scopeId) {
        this.scopeId = scopeId;
    }

    protected boolean isInitialEnabled() {
        return initialEnabled;
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

    /**
     * @param configDescriptions
     * @param configurations
     * @throws Exception
     */
    private void validateConfiguration(Set<ConfigDescriptionParameter> configDescriptions,
            Map<String, Object> configurations) {
        if (configurations == null || configurations.isEmpty()) {
            if (isOptionalConfig(configDescriptions)) {
                return;
            } else
                throw new IllegalArgumentException("Missing required configuration properties!");
        } else {
            for (ConfigDescriptionParameter configParameter : configDescriptions) {
                String configParameterName = configParameter.getName();
                Object configValue = configurations.remove(configParameterName);
                if (configValue != null) {
                    processValue(configValue, configParameter);
                }
            }
            if (!configurations.isEmpty()) {
                String msg = "\"";
                Iterator<ConfigDescriptionParameter> i = configDescriptions.iterator();
                while (i.hasNext()) {
                    ConfigDescriptionParameter configParameter = i.next();
                    if (i.hasNext())
                        msg = msg + configParameter.getName() + "\", ";
                    else
                        msg = msg + configParameter.getName();
                }
                throw new IllegalArgumentException("Extra configuration properties : " + msg + "\"!");
            }
        }

    }

    private boolean isOptionalConfig(Set<ConfigDescriptionParameter> configDescriptions) {
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

    /**
     * @param configValue
     * @param configParameter
     * @throws Exception
     */
    private void checkType(Object configValue, ConfigDescriptionParameter configParameter) {
        Type type = configParameter.getType();
        if (configParameter.isMultiple()) {
            if (configValue instanceof List) {
                int size = ((List) configValue).size();
                for (int index = 0; index < size; index++) {
                    boolean error = false;
                    if (Type.TEXT.equals(type)) {
                        if (((List) configValue).get(index) instanceof String) {
                            error = true;
                        }
                    } else if (Type.BOOLEAN.equals(type)) {
                        if (((List) configValue).get(index) instanceof Boolean) {
                            error = true;
                        }
                    } else if (Type.INTEGER.equals(type)) {
                        if (((List) configValue).get(index) instanceof Integer) {
                            error = true;
                        }
                    } else if (Type.DECIMAL.equals(type)) {
                        if (((List) configValue).get(index) instanceof Double) {
                            error = true;
                        }
                    }
                    if (error) {
                        throw new IllegalArgumentException("Unexpected value for configuration property \""
                                + configParameter.getName() + "\". Expected type: " + type);
                    }
                }
            }
            throw new IllegalArgumentException(
                    "Unexpected value for configuration property \"" + configParameter.getName()
                            + "\". Expected is Array with type for elements : " + type.toString() + "!");
        } else {
            if (Type.TEXT.equals(type) && configValue instanceof String)
                return;
            else if (Type.BOOLEAN.equals(type) && configValue instanceof Boolean)
                return;
            else if (Type.INTEGER.equals(type) && (configValue instanceof Short || configValue instanceof Byte
                    || configValue instanceof Integer || configValue instanceof Long))
                return;
            else if (Type.DECIMAL.equals(type) && (configValue instanceof Float || configValue instanceof Double))
                return;
            else {
                throw new IllegalArgumentException("Unexpected value for configuration property \""
                        + configParameter.getName() + "\". Expected is " + type.toString() + "!");
            }
        }
    }

    private void handleModuleConfigReferences(List<? extends Module> triggers, List<? extends Module> conditions,
            List<? extends Module> actions, Map<String, ?> ruleConfiguration) {
        if (ruleConfiguration != null) {
            handleModuleConfigReferences0(triggers, ruleConfiguration);
            handleModuleConfigReferences0(conditions, ruleConfiguration);
            handleModuleConfigReferences0(actions, ruleConfiguration);
        }
    }

    private void handleModuleConfigReferences0(List<? extends Module> modules, Map<String, ?> ruleConfiguration) {
        if (modules != null) {
            for (Module module : modules) {
                Map<String, Object> moduleConfiguration = module.getConfiguration();
                if (moduleConfiguration != null) {
                    for (Map.Entry<String, Object> entry : moduleConfiguration.entrySet()) {
                        String configName = entry.getKey();
                        Object configValue = entry.getValue();
                        if (configValue instanceof String) {
                            String configValueStr = (String) configValue;
                            if (configValueStr.charAt(0) == REFERENCE_SYMBOL) {
                                String referredRuleConfigName = configValueStr.substring(1);
                                Object referredRuleConfigValue = ruleConfiguration.get(referredRuleConfigName);
                                moduleConfiguration.put(configName, referredRuleConfigValue);
                            }
                        }
                    }
                }
            }
        }
    }

}
