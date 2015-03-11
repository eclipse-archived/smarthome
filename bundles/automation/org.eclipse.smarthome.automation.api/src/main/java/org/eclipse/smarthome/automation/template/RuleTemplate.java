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

package org.eclipse.smarthome.automation.template;

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
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 */
public class RuleTemplate implements Template {

    private String UID;
    private List<Trigger> triggers;
    private List<Condition> conditions;
    private List<Action> actions;
    private Set<String> tags;
    private String label;
    private String description;
    private Visibility visibility;
    private Set<ConfigDescriptionParameter> configDescriptions;

    /**
     * This constructor creates a {@link RuleTemplate} instance.
     *
     * @param UID unique identifier of the rule template
     * @param triggers - list of unique {@link Trigger}s participating in the {@link Rule}
     * @param conditions - list of unique {@link Condition}s participating in the {@link Rule}
     * @param actions - list of unique {@link Action}s participating in the {@link Rule}
     * @param configDescriptions - set of configuration properties of the {@link Rule}
     * @param visibility visibility of template. It can be public or private
     */
    public RuleTemplate(String UID,
            List<Trigger> triggers, //
            List<Condition> conditions, List<Action> actions, Set<ConfigDescriptionParameter> configDescriptions,
            Visibility visibility) {

        this.UID = UID;
        this.triggers = triggers;
        this.conditions = conditions;
        this.actions = actions;
        this.configDescriptions = configDescriptions;
        this.visibility = visibility;
    }

    /**
     * This method is used for getting the type of Template. It is unique in scope
     * of RuleEngine.
     *
     * @return the unique id of Template.
     */
    @Override
    public String getUID() {
        return UID;
    }

    /**
     * Templates can have <li><code>tags</code> - non-hierarchical keywords or terms for describing them. The tags are
     * used to filter the templates. This method is used for getting the assign tags to this Template.
     *
     * @return tags of the template
     */
    @Override
    public Set<String> getTags() {
        return new HashSet<String>(tags);
    }

    /**
     * Templates can have <li><code>tags</code> - non-hierarchical keywords or terms for describing them. The tags are
     * used to filter the templates. This method is used for assigning tags to this Template.
     *
     * @param tags set of tags assign to the template.
     */
    @Override
    public void setTags(Set<String> tags) {
        tags = new HashSet<String>(tags);
    }

    /**
     * This method is used for getting the label of the Template. The label is a
     * short, user friendly name of the Template defined by this descriptor.
     *
     * @return the label of the Template.
     */
    @Override
    public String getLabel() {
        return label;
    }

    /**
     * This method is used for setting the label of the Template. The label is a
     * short, user friendly name of the Template defined by this descriptor.
     *
     * @param label of the Template.
     */
    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * This method is used for getting the description of the Template. The
     * description is a long, user friendly description of the Template defined by
     * this descriptor.
     *
     * @return the description of the Template.
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * This method is used for setting the description of the Template. The
     * description is a long, user friendly description of the Template defined by
     * this descriptor.
     *
     * @param description of the Template.
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * This method is used to show visibility of the template
     *
     * @return visibility of template
     */
    @Override
    public Visibility getVisibility() {
        return visibility;
    }

    /**
     * This method is used for getting the Set with {@link ConfigDescriptionParameter}s defining meta info for
     * configuration
     * properties of the Rule.<br/>
     *
     * @return a {@link Set} of {@link ConfigDescriptionParameter}s.
     */
    public Set<ConfigDescriptionParameter> getConfigurationDescription() {
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
    public <T extends Module> List<T> getModules(Class<T> clazz) {
        if (clazz == Trigger.class) {
            return (List<T>) triggers;
        } else if (clazz == Condition.class) {
            return (List<T>) conditions;
        } else if (clazz == Action.class) {
            return (List<T>) actions;
        }
        return null;
    }

}
