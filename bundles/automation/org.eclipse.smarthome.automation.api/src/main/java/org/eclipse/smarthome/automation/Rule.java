/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.registry.Identifiable;

/**
 * An automation Rule is built from {@link Module}s and consists of three parts:
 * <ul>
 * <li><b>Triggers:</b> a list of {@link Trigger} modules. Each {@link Trigger} from this list
 * can start the evaluation of the Rule. A Rule with an empty list of {@link Trigger}s can
 * only be triggered through the {@link RuleRegistry#runNow(String, boolean, java.util.Map)} method,
 * or directly executed with the {@link RuleRegistry#runNow(String)} method.
 * <li><b>Conditions:</b> a list of {@link Condition} modules. When a Rule is triggered, the
 * evaluation of the Rule's {@link Condition}s will determine if the Rule will be executed.
 * A Rule will be executed only when all it's {@link Condition}s are satisfied. If the {@link Condition}s
 * list is empty, the Rule is considered satisfied.
 * <li><b>Actions:</b> a list of {@link Action} modules. These modules determine the actions that
 * will be performed when a Rule is executed.
 * </ul>
 * Additionally, Rules can have <code><b>tags</b></code> - non-hierarchical keywords or terms for describing them.
 * They can help the user to classify or label the Rules, and to filter and search them.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
@NonNullByDefault
public class Rule implements Identifiable<String> {

    protected List<Trigger> triggers;
    protected List<Condition> conditions;
    protected List<Action> actions;
    protected Configuration configuration;
    protected List<ConfigDescriptionParameter> configDescriptions;
    @Nullable
    protected String templateUID;
    protected String uid;
    @Nullable
    protected String name;
    protected Set<String> tags;
    protected Visibility visibility;
    @Nullable
    protected String description;

    /**
     * Constructor for creating an empty {@link Rule} with a specified rule identifier.
     * When {@code null} is passed for the {@code uid} parameter, the {@link Rule}'s identifier will
     * be randomly generated.
     *
     * @param uid the rule's identifier, or {@code null} if a random identifier should be generated.
     */
    public Rule(@Nullable String uid) {
        this(uid, null, null, null, null, null, null, null);
    }

    /**
     * Utility constructor for creating a {@link Rule} from a set of modules, or from a template.
     * When {@code null} is passed for the {@code uid} parameter, the {@link Rule}'s identifier will be randomly
     * generated.
     *
     * @param uid the {@link Rule}'s identifier, or {@code null} if a random identifier should be generated.
     * @param triggers the {@link Rule}'s triggers list, or {@code null} if the {@link Rule} should have no triggers or
     *            will be created from a template.
     * @param conditions the {@link Rule}'s conditions list, or {@code null} if the {@link Rule} should have no
     *            conditions, or will be created from a template.
     * @param actions the {@link Rule}'s actions list, or {@code null} if the {@link Rule} should have no
     *            actions, or will be created from a template.
     * @param configDescriptions metadata describing the configuration of the {@link Rule}.
     * @param configuration the values that will configure the modules of the {@link Rule}.
     * @param templateUID the {@link RuleTemplate} identifier of the template that will be used by the
     *            {@link RuleRegistry} to validate the {@link Rule}'s configuration, as well as to create and configure
     *            the {@link Rule}'s modules, or null if the {@link Rule} should not be created from a template.
     * @param visibility the {@link Rule}'s visibility
     */
    public Rule(@Nullable String uid, @Nullable List<Trigger> triggers, @Nullable List<Condition> conditions,
            @Nullable List<Action> actions, @Nullable List<ConfigDescriptionParameter> configDescriptions,
            @Nullable Configuration configuration, @Nullable String templateUID, @Nullable Visibility visibility) {
        this.uid = uid == null ? UUID.randomUUID().toString() : uid;
        this.triggers = triggers == null ? new ArrayList<>() : triggers;
        this.conditions = conditions == null ? new ArrayList<>() : conditions;
        this.actions = actions == null ? new ArrayList<>() : actions;
        this.configDescriptions = configDescriptions == null ? new ArrayList<>() : configDescriptions;
        this.configuration = configuration == null ? new Configuration() : configuration;
        setTemplateUID(templateUID);
        this.visibility = visibility == null ? Visibility.VISIBLE : visibility;
        tags = new HashSet<>();
    }

    /**
     * This method is used to obtain the identifier of the Rule. It can be specified by the {@link Rule}'s creator, or
     * randomly generated.
     *
     * @return an identifier of this {@link Rule}. Can't be {@code null}.
     */
    @Override
    public String getUID() {
        return uid;
    }

    /**
     * This method is used to obtain the {@link RuleTemplate} identifier of the template the {@link Rule} was created
     * from. It will be used by the {@link RuleRegistry} to resolve the {@link Rule}: to validate the {@link Rule}'s
     * configuration, as well as to create and configure the {@link Rule}'s modules. If a {@link Rule} has not been
     * created from a template, or has been successfully resolved by the {@link RuleRegistry}, this method will return
     * {@code null}.
     *
     * @return the identifier of the {@link Rule}'s {@link RuleTemplate}, or {@code null} if the {@link Rule} has not
     *         been created from a template, or has been successfully resolved by the {@link RuleRegistry}.
     */
    public @Nullable String getTemplateUID() {
        return templateUID;
    }

    /**
     * This method is used to specify the {@link RuleTemplate} identifier of the template that will be used to
     * by the {@link RuleRegistry} to resolve the {@link Rule}: to validate the {@link Rule}'s configuration, as well as
     * to create and configure the {@link Rule}'s modules.
     */
    public void setTemplateUID(@Nullable String templateUID) {
        this.templateUID = templateUID;
    }

    /**
     * This method is used to obtain the {@link Rule}'s human-readable name.
     *
     * @return the {@link Rule}'s human-readable name, or {@code null}.
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * This method is used to specify the {@link Rule}'s human-readable name.
     *
     * @param ruleName the {@link Rule}'s human-readable name, or {@code null}.
     */
    public void setName(@Nullable String ruleName) {
        name = ruleName;
    }

    /**
     * This method is used to obtain the {@link Rule}'s assigned tags.
     *
     * @return the {@link Rule}'s assigned tags.
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * This method is used to specify the {@link Rule}'s assigned tags.
     *
     * @param ruleTags the {@link Rule}'s assigned tags.
     */
    @SuppressWarnings("null")
    public void setTags(Set<String> ruleTags) {
        tags = ruleTags != null ? ruleTags : new HashSet<>();
    }

    /**
     * This method is used to obtain the human-readable description of the purpose and consequences of the
     * {@link Rule}'s execution.
     *
     * @return the {@link Rule}'s human-readable description, or {@code null}.
     */
    public @Nullable String getDescription() {
        return description;
    }

    /**
     * This method is used to specify human-readable description of the purpose and consequences of the
     * {@link Rule}'s execution.
     *
     * @param ruleDescription the {@link Rule}'s human-readable description, or {@code null}.
     */
    public void setDescription(@Nullable String ruleDescription) {
        description = ruleDescription;
    }

    /**
     * This method is used to obtain the {@link Rule}'s {@link Visibility}.
     *
     * @return the {@link Rule}'s {@link Visibility} value.
     */
    public Visibility getVisibility() {
        return visibility;
    }

    /**
     * This method is used to specify the {@link Rule}'s {@link Visibility}.
     *
     * @param visibility the {@link Rule}'s {@link Visibility} value.
     */
    @SuppressWarnings("null")
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility == null ? Visibility.VISIBLE : visibility;
    }

    /**
     * This method is used to obtain the {@link Rule}'s {@link Configuration}.
     *
     * @return current configuration values, or an empty {@link Configuration}.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * This method is used to specify the {@link Rule}'s {@link Configuration}.
     *
     * @param ruleConfiguration the new configuration values.
     */
    @SuppressWarnings("null")
    public void setConfiguration(Configuration ruleConfiguration) {
        this.configuration = ruleConfiguration == null ? new Configuration() : ruleConfiguration;
    }

    /**
     * This method is used to obtain the {@link List} with {@link ConfigDescriptionParameter}s
     * defining meta info for configuration properties of the {@link Rule}.
     *
     * @return a {@link List} of {@link ConfigDescriptionParameter}s.
     */
    public List<ConfigDescriptionParameter> getConfigurationDescriptions() {
        return configDescriptions;
    }

    /**
     * This method is used to describe with {@link ConfigDescriptionParameter}s
     * the meta info for configuration properties of the {@link Rule}.
     */
    @SuppressWarnings("null")
    public void setConfigurationDescriptions(List<ConfigDescriptionParameter> configDescriptions) {
        this.configDescriptions = configDescriptions == null ? new ArrayList<>() : configDescriptions;
    }

    /**
     * This method is used to get the conditions participating in {@link Rule}.
     *
     * @return a list with the conditions that belong to this {@link Rule}.
     */
    public List<Condition> getConditions() {
        return conditions;
    }

    /**
     * This method is used to specify the conditions participating in {@link Rule}.
     *
     * @param conditions a list with the conditions that should belong to this {@link Rule}.
     */
    @SuppressWarnings("null")
    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions == null ? new ArrayList<>() : conditions;
    }

    /**
     * This method is used to get the actions participating in {@link Rule}.
     *
     * @return a list with the actions that belong to this {@link Rule}.
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * This method is used to specify the actions participating in {@link Rule}
     *
     * @param actions a list with the actions that should belong to this {@link Rule}.
     */
    @SuppressWarnings("null")
    public void setActions(List<Action> actions) {
        this.actions = actions == null ? new ArrayList<>() : actions;
    }

    /**
     * This method is used to get the triggers participating in {@link Rule}
     *
     * @return a list with the triggers that belong to this {@link Rule}.
     */
    public List<Trigger> getTriggers() {
        return triggers;
    }

    /**
     * This method is used to specify the triggers participating in {@link Rule}
     *
     * @param triggers a list with the triggers that should belong to this {@link Rule}.
     */
    @SuppressWarnings("null")
    public void setTriggers(List<Trigger> triggers) {
        this.triggers = triggers == null ? new ArrayList<>() : triggers;
    }

    /**
     * This method is used to get a {@link Module} participating in {@link Rule}
     *
     * @param moduleId specifies the id of a module belonging to this {@link Rule}.
     * @return module with specified id or {@code null} if it does not belong to this {@link Rule}.
     */
    public @Nullable Module getModule(String moduleId) {
        for (Module module : getModules(Module.class)) {
            if (module.getId().equals(moduleId)) {
                return module;
            }
        }
        return null;
    }

    /**
     * This method is used to obtain the modules of the {@link Rule}, corresponding to the specified class.
     *
     * @param moduleClazz defines the class of the looking modules. It can be {@link Module}, {@link Trigger},
     *            {@link Condition} or {@link Action}.
     * @return the modules of defined type or empty list if the {@link Rule} has no modules that belong to the specified
     *         type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Module> List<T> getModules(Class<T> moduleClazz) {
        final List<T> result;
        if (Module.class == moduleClazz) {
            List<Module> modules = new ArrayList<Module>();
            modules.addAll(triggers);
            modules.addAll(conditions);
            modules.addAll(actions);
            result = (List<T>) Collections.unmodifiableList(modules);
        } else if (Trigger.class == moduleClazz) {
            result = (List<T>) triggers;
        } else if (Condition.class == moduleClazz) {
            result = (List<T>) conditions;
        } else if (Action.class == moduleClazz) {
            result = (List<T>) actions;
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + uid.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
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
        if (!uid.equals(other.uid)) {
            return false;
        }
        return true;
    }

}
