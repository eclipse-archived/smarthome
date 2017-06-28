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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.registry.Identifiable;

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
public class Rule implements Identifiable<String> {

    protected List<Trigger> triggers;
    protected List<Condition> conditions;
    protected List<Action> actions;
    protected Configuration configuration;
    protected List<ConfigDescriptionParameter> configDescriptions;
    protected String templateUID;
    protected String uid;
    protected String name;
    protected Set<String> tags;
    protected Visibility visibility;
    protected String description;

    /**
     * Constructor creates an empty rule. The rule has ruleUID set by the rule engine.
     */
    public Rule() {
    }

    /**
     * Constructor creates an empty rule with specified rule uid
     *
     * @param uid is the unique identifier of created rule.
     */
    public Rule(String uid) {
        this.uid = uid;
    }

    /**
     * Utility constructor which creates a rule from modules or template.
     *
     * @param uid is the unique identifier of the rule.
     * @param triggers trigger modules
     * @param conditions condition modules
     * @param actions action modules
     * @param configurations are values of rule template. It is available when the rule is created from template and the
     *            template is not resolved.
     * @param templateUID the unique identifier of RuleTemplate. It is available when the rule is created from template
     *            and the template is not resolved.
     * @param visibility visibility of rule
     */
    public Rule(String uid, List<Trigger> triggers, //
            List<Condition> conditions, //
            List<Action> actions, //
            List<ConfigDescriptionParameter> configDescriptions, //
            Configuration configurations, String templateUID, Visibility visibility) {
        this.uid = uid;
        setTriggers(triggers);
        setConditions(conditions);
        setActions(actions);
        setConfigurationDescriptions(configDescriptions);
        setConfiguration(configurations);
        setTemplateUID(templateUID);
        setVisibility(visibility);
    }

    /**
     * This method is used for getting the unique identifier of the Rule. This property is set by the RuleEngine when
     * the {@link Rule} is added. It's optional property.
     *
     * @return unique id of this {@link Rule}
     */
    @Override
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
        return templateUID;
    }

    public void setTemplateUID(String templateUID) {
        this.templateUID = templateUID;
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
     * <ul>
     * <li><code>tags</code> - non-hierarchical keywords or terms for describing them. This method is
     * used for getting the tags assign to this Rule. The tags are used to filter the rules.</li>
     * </ul>
     *
     * @return a {@link Set} of tags
     */
    public Set<String> getTags() {
        return tags = tags != null ? tags : Collections.<String> emptySet();
    }

    /**
     * Rules can have
     * <ul>
     * <li><code>tags</code> - non-hierarchical keywords or terms for describing them. This method is
     * used for setting the tags to this rule. This property can be changed only when the Rule is not in active state.
     * The tags are used to filter the rules.</li>
     * </ul>
     *
     * @param ruleTags list of tags assign to this Rule.
     * @throws IllegalStateException when the rule is in active state.
     */
    public void setTags(Set<String> ruleTags) throws IllegalStateException {
        tags = ruleTags != null ? ruleTags : Collections.<String> emptySet();
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
     * This method is used to show visibility of the Rule
     *
     * @return visibility of rule
     */
    public Visibility getVisibility() {
        if (visibility == null) {
            return Visibility.VISIBLE;
        }
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    /**
     * This method is used for getting Map with configuration values of the {@link Rule} Key -id of the
     * {@link ConfigDescriptionParameter} Value - the
     * value of the corresponding property
     *
     * @return current configuration values
     */
    public Configuration getConfiguration() {
        if (configuration == null) {
            configuration = new Configuration();
        }
        return configuration;
    }

    /**
     * This method is used for setting the Map with configuration values of the {@link Rule}. Key - id of the
     * {@link ConfigDescriptionParameter} Value -
     * the value of the corresponding property
     *
     * @param ruleConfiguration new configuration values.
     */
    public void setConfiguration(Configuration ruleConfiguration) {
        this.configuration = ruleConfiguration;
    }

    /**
     * This method is used for getting the {@link List} with {@link ConfigDescriptionParameter}s
     * defining meta info for configuration properties of the Rule.
     *
     * @return a {@link Set} of {@link ConfigDescriptionParameter}s.
     */
    public List<ConfigDescriptionParameter> getConfigurationDescriptions() {
        if (configDescriptions == null) {
            configDescriptions = new ArrayList<ConfigDescriptionParameter>(3);
        }
        return configDescriptions;
    }

    public void setConfigurationDescriptions(List<ConfigDescriptionParameter> configDescriptions) {
        this.configDescriptions = (configDescriptions == null) ? new ArrayList<ConfigDescriptionParameter>(3)
                : configDescriptions;
    }

    public List<Condition> getConditions() {
        if (conditions == null) {
            conditions = new ArrayList<Condition>(3);
        }
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = (conditions == null) ? new ArrayList<Condition>(3) : conditions;
    }

    public List<Action> getActions() {
        if (actions == null) {
            actions = new ArrayList<Action>(3);
        }
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = (actions == null) ? new ArrayList<Action>(3) : actions;
    }

    public List<Trigger> getTriggers() {
        if (triggers == null) {
            triggers = new ArrayList<Trigger>(3);
        }
        return triggers;
    }

    public void setTriggers(List<Trigger> triggers) {
        this.triggers = (triggers == null) ? new ArrayList<Trigger>(3) : triggers;
    }

    /**
     * This method is used to get a module participating in Rule
     *
     * @param moduleId unique id of the module in this rule.
     * @return module with specified id or null when it does not exist.
     */
    public Module getModule(String moduleId) {
        Module module = getModule(moduleId, triggers);
        if (module != null) {
            return module;
        }

        module = getModule(moduleId, conditions);
        if (module != null) {
            return module;
        }

        module = getModule(moduleId, actions);
        if (module != null) {
            return module;
        }
        return null;
    }

    private <T extends Module> T getModule(String moduleUID, List<T> modules) {
        if (modules != null) {
            for (T module : modules) {
                if (module.getId().equals(moduleUID)) {
                    return module;
                }
            }
        }
        return null;
    }

    /**
     * This method is used to return the module of this rule.
     *
     * @param moduleClazz optional parameter defining type looking modules. The
     *            types are {@link Trigger}, {@link Condition} or {@link Action}
     * @return list of modules of defined type or all modules when the type is not
     *         specified.
     */
    @SuppressWarnings("unchecked")
    public <T extends Module> List<T> getModules(Class<T> moduleClazz) {
        List<T> result = null;
        if (moduleClazz == null) {
            result = new ArrayList<T>();
            result.addAll((Collection<? extends T>) getTriggers());
            result.addAll((Collection<? extends T>) getConditions());
            result.addAll((Collection<? extends T>) getActions());
        } else if (Trigger.class == moduleClazz) {
            result = (List<T>) getTriggers();
        } else if (Condition.class == moduleClazz) {
            result = (List<T>) getConditions();
        } else if (Action.class == moduleClazz) {
            result = (List<T>) getActions();
        }
        return result != null ? result : Collections.<T> emptyList();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Rule)) {
            return false;
        }
        Rule other = (Rule) obj;
        if (uid == null) {
            if (other.uid != null) {
                return false;
            }
        } else if (!uid.equals(other.uid)) {
            return false;
        }
        return true;
    }

}
