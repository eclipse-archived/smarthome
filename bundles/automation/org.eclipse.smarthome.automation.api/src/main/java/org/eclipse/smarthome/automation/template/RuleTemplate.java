/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * The templates define types of shared, ready to use rule definitions, which
 * can be instantiated and configured to produce {@link Rule} instances . Each
 * Template has a unique id.
 * <p>
 * The {@link RuleTemplate}s can be used by any creator of Rules, but they can be modified only by its creator. The
 * template modification is done by updating the {@link RuleTemplate} through {@link TemplateRegistry}
 * <p>
 * Templates can have <code>tags</code> - non-hierarchical keywords or terms for describing them.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public class RuleTemplate implements Template {

    /**
     * This field holds an unique identifier of the {@link RuleTemplate} instance.
     */
    private String UID;

    /**
     * This field holds a list with the unique {@link Trigger}s participating in the {@link Rule} and starting its
     * execution.
     */
    private List<Trigger> triggers;

    /**
     * This field holds a list with the unique {@link Condition}s participating in the {@link Rule} and determine the
     * completion of the execution.
     */
    private List<Condition> conditions;

    /**
     * This field holds a list with the unique {@link Action}s participating in the {@link Rule} and are the real work
     * that will be done by the rule.
     */
    private List<Action> actions;

    /**
     * This field holds a set of non-hierarchical keywords or terms for describing the {@link RuleTemplate}.
     */
    private Set<String> tags;

    /**
     * This field holds the short, user friendly name of the Template.
     */
    private String label;

    /**
     * This field describes the usage of the {@link Rule} and its benefits.
     */
    private String description;

    /**
     * This field determines if the template will be public or private.
     */
    private Visibility visibility;

    /**
     * This field defines a set of configuration properties of the {@link Rule}.
     */
    private Set<ConfigDescriptionParameter> configDescriptions;

    /**
     * This constructor creates a {@link RuleTemplate} instance.
     *
     * @param UID is an unique identifier of the {@link RuleTemplate} instance.
     * @param triggers - list of unique {@link Trigger}s participating in the {@link Rule}
     * @param conditions - list of unique {@link Condition}s participating in the {@link Rule}
     * @param actions - list of unique {@link Action}s participating in the {@link Rule}
     * @param configDescriptions - set of configuration properties of the {@link Rule}
     * @param visibility defines if the template can be public or private.
     */
    public RuleTemplate(String UID, String label, String description, Set<String> tags, List<Trigger> triggers,
            List<Condition> conditions, List<Action> actions, Set<ConfigDescriptionParameter> configDescriptions,
            Visibility visibility) {

        this.UID = UID;
        this.label = label;
        this.description = description;
        this.triggers = triggers;
        this.conditions = conditions;
        this.actions = actions;
        this.configDescriptions = configDescriptions;
        this.visibility = visibility;

        if (tags == null || tags.isEmpty())
            return;
        tags = new HashSet<String>(tags);
    }

    /**
     * This method is used for getting the type of Template. It is unique in scope of RuleEngine.
     *
     * @return the unique id of Template.
     */
    @Override
    public String getUID() {
        return UID;
    }

    /**
     * This method is used for getting the assigned <code>tags</code> to this Template. The <code>tags</code> are
     * non-hierarchical keywords or terms that are used for describing the template and to filter the templates.
     *
     * @return tags that is a set of non-hierarchical keywords or terms, describing the template.
     */
    @Override
    public Set<String> getTags() {
        if (tags == null || tags.isEmpty())
            return null;
        return new HashSet<String>(tags);
    }

    /**
     * This method is used for getting the label of the Template.
     *
     * @return the short, user friendly name of the Template.
     */
    @Override
    public String getLabel() {
        return label;
    }

    /**
     * This method is used for getting the description of the Template. The
     * description is a long, user friendly description of the Rule defined by
     * this Template.
     *
     * @return the description of the Template.
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * This method is used to show visibility of the template
     *
     * @return visibility of template
     */
    @Override
    public Visibility getVisibility() {
        if (visibility == null) {
            return Visibility.PUBLIC;
        }
        return visibility;
    }

    /**
     * This method is used for getting the Set with {@link ConfigDescriptionParameter}s defining meta info for
     * configuration properties of the Rule.
     *
     * @return a {@link Set} of {@link ConfigDescriptionParameter}s.
     */
    public Set<ConfigDescriptionParameter> getConfigurationDescription() {
        if (configDescriptions == null)
            return null;
        return new HashSet<ConfigDescriptionParameter>(configDescriptions);
    }

    /**
     * This method is used to get a {@link Module} participating in Rule
     *
     * @param id unique id of the module in this rule.
     * @return module with specified id or null when it does not exist.
     */
    public <T extends Module> T getModule(String id) {
        return null;
    }

    /**
     * This method is used to return a group of {@link Module}s of this rule
     *
     * @param clazz optional parameter defining type looking modules. The types
     *            are {@link Trigger}, {@link Condition} or {@link Action}
     * @return list of modules of defined type or all modules when the type is not
     *         specified.
     */
    @SuppressWarnings("unchecked")
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

    /**
     * Auxiliary method used in {@link #getModules(Class)} to prevent {@code null} for the returned list.
     *
     * @param t is the result from {@link #getModules(Class)} method.
     * @return the resulted list from {@link #getModules(Class)} method or empty list if the result from
     *         {@link #getModules(Class)} method is {@code null}.
     */
    private <T extends Module> List<T> getList(List<T> t) {
        if (t != null) {
            return t;
        }
        return new ArrayList<T>();
    }

}
