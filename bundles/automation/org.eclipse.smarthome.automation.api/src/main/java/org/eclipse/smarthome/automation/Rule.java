/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * Rule is built from {@link Module}s and consists of three sections:
 * ON/IF/THEN.
 * <ul>
 * <li>ON - contains {@link Trigger} modules. The triggers defines what fires the the Rule.
 * <li>IF - contains {@link Condition} modules which determinate if the Rule is satisfied or not. When all conditions
 * are satisfied then the Rule can proceed with execution of THEN part.
 * <li>THEN - contains actions which have to be executed by the {@link Rule}
 * </ul>
 * Rules can have <code>tags</code> - non-hierarchical keywords or terms for
 * describing them. They help for classifying the items and allow them to be
 * found.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public class Rule {

    protected List<Trigger> triggers;
    protected List<Condition> conditions;
    protected List<Action> actions;
    protected String scopeId;
    protected Map<String, ?> configurations;
    protected Set<ConfigDescriptionParameter> configDescriptions;
    protected String ruleTemplateUID;
    protected String uid;
    private String name;
    private Set<String> tags;
    private String description;

    public Rule() {
    }

    /**
     * This constructor is used when creating the rule from template and there is not provided UID for the rule.
     *
     * @param ruleTemplateUID is the unique identifier of the template, used for creation of the rule.
     * @param are values of the configuration parameters that are needed for configuring the rule, represented as pairs
     *            key-value, where the key is the name of the configuration parameter and the value is its value.
     */
    public Rule(String ruleTemplateUID, Map<String, ?> configurations) {
        this.ruleTemplateUID = ruleTemplateUID;
        this.configurations = configurations;
    }

    /**
     * This constructor is used when creating the rule from template and there is provided UID for the rule.
     *
     * @param uid is the unique identifier of the rule provided by its creator.
     * @param ruleTemplateUID is the unique identifier of the template, used for creation of the rule.
     * @param configurations are values of the configuration parameters that are needed for configuring the rule,
     *            represented as pairs key-value, where the key is the name of the configuration parameter and the value
     *            is its value.
     */
    public Rule(String uid, String ruleTemplateUID, Map<String, ?> configurations) {
        this.uid = uid;
        this.ruleTemplateUID = ruleTemplateUID;
        this.configurations = configurations;
    }

    /**
     * This constructor is used when creating the rule and there is not provided UID for the rule.
     *
     * @param triggers
     * @param conditions
     * @param actions
     * @param configDescriptions
     * @param configurations
     */
    public Rule(List<Trigger> triggers, //
            List<Condition> conditions, //
            List<Action> actions, //
            Set<ConfigDescriptionParameter> configDescriptions, //
            Map<String, ?> configurations) {
        this.triggers = triggers;
        this.conditions = conditions;
        this.actions = actions;
        this.configDescriptions = configDescriptions;
        setConfiguration(configurations);
    }

    /**
     * This constructor is used when creating the rule and there is provided UID for the rule.
     *
     * @param uid is the unique identifier of the rule provided by its creator.
     * @param triggers
     * @param conditions
     * @param actions
     * @param configDescriptions
     * @param configurations are values of the configuration parameters that are needed for configuring the rule,
     *            represented as pairs key-value, where the key is the name of the configuration parameter and the value
     *            is its value.
     */
    public Rule(String uid, List<Trigger> triggers, //
            List<Condition> conditions, //
            List<Action> actions, Set<ConfigDescriptionParameter> configDescriptions, //
            Map<String, ?> configurations) {
        this.uid = uid;
        this.triggers = triggers;
        this.conditions = conditions;
        this.actions = actions;
        this.configDescriptions = configDescriptions;
        setConfiguration(configurations);
    }

    /**
     * This method is used for getting the unique identifier of the Rule. This property is set by the RuleEngine when
     * the {@link Rule} is added. It's optional property.
     *
     * @return unique id of this {@link Rule}
     */
    public String getUID() {
        return uid;
    }

    /**
     * This method is used for getting the unique identifier of the RuleTemplate. This property is set by the RuleEngine
     * when the {@link Rule} is added and it is created from template. It's optional property.
     *
     * @return unique id of this {@link Rule}
     */
    public String getTemplateUID() {
        return ruleTemplateUID;
    }

    /**
     * This method is used for getting the user friendly name of the {@link Rule}. It's optional property.
     *
     * @return the name of rule or null.
     */
    public String getName() {
        return name;
    }

    /**
     * This method is used for setting a friendly name of the Rule. This property
     * can be changed only when the Rule is not in active state.
     *
     * @param ruleName a new name.
     * @throws IllegalStateException when the rule is in active state
     */
    public void setName(String ruleName) throws IllegalStateException {
        name = ruleName;
    }

    /**
     * Rules can have
     * <li><code>tags</code> - non-hierarchical keywords or terms for describing them. This method is
     * used for getting the tags assign to this Rule. The tags are used to filter the rules.
     *
     * @return a list of tags
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * Rules can have
     * <li><code>tags</code> - non-hierarchical keywords or terms for describing them. This method is
     * used for setting the tags to this rule. This property can be changed only when the Rule is not in active state.
     * The tags are used to filter the rules.
     *
     * @param ruleTags list of tags assign to this Rule.
     * @throws IllegalStateException IllegalStateException when the rule is in
     *             active state.
     */
    public void setTags(Set<String> ruleTags) throws IllegalStateException {
        tags = ruleTags;
    }

    /**
     * This method is used for getting the description of the Rule. The
     * description is a long, user friendly description of the Rule defined by
     * this descriptor.
     *
     * @return the description of the Rule.
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method is used for setting the description of the Rule. The
     * description is a long, user friendly description of the Rule defined by
     * this descriptor.
     *
     * @param ruleDescription of the Rule.
     */
    public void setDescription(String ruleDescription) {
        description = ruleDescription;
    }

    /**
     * This method is used for getting the Set with {@link ConfigDescriptionParameter}s defining meta info for
     * configuration
     * properties of the Rule.<br/>
     *
     * @return a {@link Set} of {@link ConfigDescriptionParameter}s.
     */
    public Set<ConfigDescriptionParameter> getConfigurationDescriptions() {
        return configDescriptions;
    }

    /**
     * This method is used for getting Map with configuration values of the {@link Rule} Key -id of the
     * {@link ConfigDescriptionParameter} Value - the
     * value of the corresponding property
     *
     * @return current configuration values
     */
    public Map<String, ?> getConfiguration() {
        return configurations;
    }

    /**
     * This method is used for setting the Map with configuration values of the {@link Rule}. Key - id of the
     * {@link ConfigDescriptionParameter} Value -
     * the value of the corresponding property
     *
     * @param ruleConfiguration new configuration values.
     */
    public void setConfiguration(Map<String, ?> ruleConfiguration) {
        configurations = ruleConfiguration;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public List<Action> getActions() {
        return actions;
    }

    public List<Trigger> getTriggers() {
        return triggers;
    }

    /**
     * This method is used to get a module participating in Rule
     *
     * @param moduleId unique id of the module in this rule.
     * @return module with specified id or null when it does not exist.
     */
    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(String moduleId) {

        if (triggers != null) {
            for (T module : (List<? extends T>) triggers) {
                if (module.getId().equals(moduleId))
                    return module;
            }
        }
        if (conditions != null) {
            for (T module : (List<? extends T>) conditions) {
                if (module.getId().equals(moduleId))
                    return module;
            }
        }
        if (actions != null) {
            for (T module : (List<? extends T>) actions) {
                if (module.getId().equals(moduleId))
                    return module;
            }
        }
        return null;
    }

    /**
     * This method is used to return a group of module of this rule
     *
     * @param moduleClazz optional parameter defining type looking modules. The
     *            types are {@link Trigger}, {@link Condition} or {@link Action}
     * @return list of modules of defined type or all modules when the type is not
     *         specified.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public <T extends Module> List<T> getModules(Class<T> moduleClazz) {
        if (moduleClazz == null) {
            List<T> result = new ArrayList<T>();
            if (triggers != null) {
                result.addAll((Collection<? extends T>) triggers);
            }
            if (conditions != null) {
                result.addAll((Collection<? extends T>) conditions);
            }
            if (actions != null) {
                result.addAll((Collection<? extends T>) actions);
            }
            return result;
        }
        if (Trigger.class == moduleClazz) {
            return (List<T>) triggers;
        }
        if (Condition.class == moduleClazz) {
            return (List<T>) conditions;
        }
        if (Action.class == moduleClazz) {
            return (List<T>) actions;
        }
        return null;
    }

    /**
     * This method is used to get the identity scope of this Rule. The identity
     * defines a scope where the rule belongs to. It is set automatically by the
     * RuleEngine and is based on identity of rule creator.<br>
     * For example the identity can be application name or user name of creator.
     *
     * @return Rule's identity.
     */
    public String getScopeIdentifier() {
        return scopeId;
    }

}
