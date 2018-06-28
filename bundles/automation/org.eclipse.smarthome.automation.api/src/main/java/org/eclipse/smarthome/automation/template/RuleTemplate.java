/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.automation.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * The {@link RuleTemplate} defines a shared, ready to use - rule definition, which can be configured to produce
 * {@link Rule} instances.
 * <p>
 * The {@link RuleTemplate}s can be used by any creator of Rules, but they can be modified only by its creator.
 * The template modification is done by updating the {@link RuleTemplate}.
 * <p>
 * Templates can have <code>tags</code> - non-hierarchical keywords or terms for describing them.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 * @author Markus Rathgeb - Add default constructor for deserialization
 */
@NonNullByDefault
public class RuleTemplate implements Template {

    /**
     * This field holds the {@link RuleTemplate}'s identifier, specified by its creator or randomly generated.
     */
    private final String uid;

    /**
     * This field holds a list with the {@link Trigger}s participating in the {@link RuleTemplate}.
     */
    private final List<Trigger> triggers;

    /**
     * This field holds a list with the {@link Condition}s participating in the {@link RuleTemplate}.
     */
    private final List<Condition> conditions;

    /**
     * This field holds a list with the {@link Action}s participating in the {@link RuleTemplate}.
     */
    private final List<Action> actions;

    /**
     * This field holds a set of non-hierarchical keywords or terms for describing the {@link RuleTemplate}.
     */
    private final Set<String> tags;

    /**
     * This field holds the short, human-readable label of the {@link RuleTemplate}.
     */
    @Nullable
    private final String label;

    /**
     * This field describes the usage of the {@link RuleTemplate} and its benefits.
     */
    @Nullable
    private final String description;

    /**
     * This field determines {@link Visibility} of the {@link RuleTemplate}.
     */
    private final Visibility visibility;

    /**
     * This field defines a set of configuration properties of the future {@link Rule} instances.
     */
    private final List<ConfigDescriptionParameter> configDescriptions;

    /**
     * This constructor creates a {@link RuleTemplate} instance that will be used for creating {@link Rule}s from a
     * set
     * of modules, belong to the template. When {@code null} is passed for the {@code uid} parameter, the
     * {@link RuleTemplate}'s identifier will be randomly generated.
     *
     * @param uid                the {@link RuleTemplate}'s identifier, or {@code null} if a random identifier should be
     *                           generated.
     * @param label              the short human-readable {@link RuleTemplate}'s label.
     * @param description        a detailed human-readable {@link RuleTemplate}'s description.
     * @param tags               the {@link RuleTemplate}'s assigned tags.
     * @param triggers           the {@link RuleTemplate}'s triggers list, or {@code null} if the {@link RuleTemplate}
     *                           should have
     *                           no triggers.
     * @param conditions         the {@link RuleTemplate}'s conditions list, or {@code null} if the {@link RuleTemplate}
     *                           should
     *                           have no conditions.
     * @param actions            the {@link RuleTemplate}'s actions list, or {@code null} if the {@link RuleTemplate}
     *                           should have
     *                           no actions.
     * @param configDescriptions describing metadata for the configuration of the future {@link Rule} instances.
     * @param visibility         the {@link RuleTemplate}'s visibility.
     */
    public RuleTemplate(@Nullable String UID, @Nullable String label, @Nullable String description,
            @Nullable Set<String> tags, @Nullable List<Trigger> triggers, @Nullable List<Condition> conditions,
            @Nullable List<Action> actions, @Nullable List<ConfigDescriptionParameter> configDescriptions,
            @Nullable Visibility visibility) {
        this.uid = UID == null ? UUID.randomUUID().toString() : UID;
        this.label = label;
        this.description = description;
        this.triggers = triggers == null ? Collections.emptyList() : Collections.unmodifiableList(triggers);
        this.conditions = conditions == null ? Collections.emptyList() : Collections.unmodifiableList(conditions);
        this.actions = actions == null ? Collections.emptyList() : Collections.unmodifiableList(actions);
        this.configDescriptions = configDescriptions == null ? Collections.emptyList()
                : Collections.unmodifiableList(configDescriptions);
        this.visibility = visibility == null ? Visibility.VISIBLE : visibility;
        this.tags = tags == null ? Collections.emptySet() : Collections.unmodifiableSet(tags);
    }

    /**
     * This method is used to obtain the identifier of the {@link RuleTemplate}. It can be specified by the
     * {@link RuleTemplate}'s creator, or randomly generated.
     *
     * @return an identifier of this {@link RuleTemplate}. Can't be {@code null}.
     */
    @Override
    public String getUID() {
        return uid;
    }

    /**
     * This method is used to obtain the {@link RuleTemplate}'s assigned tags.
     *
     * @return the {@link RuleTemplate}'s assigned tags.
     */
    @Override
    public Set<String> getTags() {
        return tags;
    }

    /**
     * This method is used to obtain the {@link RuleTemplate}'s human-readable label.
     *
     * @return the {@link RuleTemplate}'s human-readable label, or {@code null}.
     */
    @Override
    public @Nullable String getLabel() {
        return label;
    }

    /**
     * This method is used to obtain the human-readable description of the purpose of the {@link RuleTemplate}.
     *
     * @return the {@link RuleTemplate}'s human-readable description, or {@code null}.
     */
    @Override
    public @Nullable String getDescription() {
        return description;
    }

    /**
     * This method is used to obtain the {@link RuleTemplate}'s {@link Visibility}.
     *
     * @return the {@link RuleTemplate}'s {@link Visibility} value.
     */
    @Override
    public Visibility getVisibility() {
        return visibility;
    }

    /**
     * This method is used to obtain the {@link List} with {@link ConfigDescriptionParameter}s
     * defining meta info for configuration properties of the future {@link Rule} instances.
     *
     * @return a {@link List} of {@link ConfigDescriptionParameter}s.
     */
    public List<ConfigDescriptionParameter> getConfigurationDescriptions() {
        return configDescriptions;
    }

    /**
     * This method is used to get a {@link Module} participating in {@link RuleTemplate}
     *
     * @param moduleId unique id of the module in this {@link RuleTemplate}.
     * @return module with specified id or null when it does not exist.
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
     * This method is used to obtain the modules of the {@link RuleTemplate}, corresponding to the specified class.
     *
     * @param moduleClazz defines the class of the looking modules. It can be {@link Module}, {@link Trigger},
     *                    {@link Condition} or {@link Action}.
     * @return the modules of defined type or empty list if the {@link RuleTemplate} has no modules that belong to the
     *         specified type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Module> List<T> getModules(Class<T> moduleClazz) {
        final List<T> result;
        if (Module.class == moduleClazz) {
            List<Module> modules = new ArrayList<>();
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

    /**
     * This method is used to get the triggers participating in {@link RuleTemplate}.
     *
     * @return a list with the triggers that belong to this {@link RuleTemplate}.
     */
    public List<Trigger> getTriggers() {
        return triggers;
    }

    /**
     * This method is used to get the conditions participating in {@link RuleTemplate}.
     *
     * @return a list with the conditions that belong to this {@link RuleTemplate}.
     */
    public List<Condition> getConditions() {
        return conditions;
    }

    /**
     * This method is used to get the actions participating in {@link RuleTemplate}.
     *
     * @return a list with the actions that belong to this {@link RuleTemplate}.
     */
    public List<Action> getActions() {
        return actions;
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
        if (!(obj instanceof RuleTemplate)) {
            return false;
        }
        RuleTemplate other = (RuleTemplate) obj;
        if (!uid.equals(other.uid)) {
            return false;
        }
        return true;
    }
}
