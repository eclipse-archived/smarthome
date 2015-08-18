/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.template.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.dto.ActionDTO;
import org.eclipse.smarthome.automation.dto.ConditionDTO;
import org.eclipse.smarthome.automation.dto.TriggerDTO;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template.Visibility;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This class is responsible for custom serialization and deserialization of {@link RuleTemplate}s.
 *
 * @author Ana Dimova - Initial Contribution
 * @author Kai Kreuzer - changed naming to DTO convention
 *
 */
public class RuleTemplateDTO {

    /**
     * This field holds a list with the unique {@link TriggerDTO}s, representing the {@link Trigger}s participating in
     * the {@link Rule} and starting its execution.
     */
    public List<TriggerDTO> triggers;

    /**
     * This field holds a list with the unique {@link ConditionDTO}s, representing the {@link Condition}s participating
     * in the {@link Rule} and determine the completion of the execution.
     */
    public List<ConditionDTO> conditions;

    /**
     * This field holds a list with the unique {@link ActionDTO}s, representing the {@link Action}s participating in the
     * {@link Rule} and are the real work that will be done by the rule.
     */
    public List<ActionDTO> actions;

    /**
     * This field holds a set of non-hierarchical keywords or terms for describing the {@link RuleTemplate}.
     */
    public Set<String> tags;

    /**
     * This field holds the short, user friendly name of the {@link RuleTemplate}.
     */
    public String label;

    /**
     * This field describes the usage of the {@link Rule} and its benefits.
     */
    public String description;

    /**
     * This field determines if the template will be public or private.
     */
    public Visibility visibility;

    /**
     * This field defines a set of configuration properties of the {@link Rule}.
     */
    public Set<ConfigDescriptionParameter> configDescriptions;

    /**
     * This field holds an unique identifier of the {@link RuleTemplate} instance.
     */
    public String uid;

    /**
     * This constructor is used for deserialization of the {@link RuleTemplate}s.
     */
    public RuleTemplateDTO() {
    }

    /**
     * This constructor is used for serialization of the {@link RuleTemplate}s.
     */
    public RuleTemplateDTO(RuleTemplate rt) {
        uid = rt.getUID();
        label = rt.getLabel();
        description = rt.getDescription();
        visibility = rt.getVisibility();
        tags = rt.getTags();
        actions = new ArrayList<ActionDTO>();
        for (Action action : rt.getModules(Action.class)) {
            actions.add(new ActionDTO(action));
        }
        conditions = new ArrayList<ConditionDTO>();
        for (Condition condition : rt.getModules(Condition.class)) {
            conditions.add(new ConditionDTO(condition));
        }
        triggers = new ArrayList<TriggerDTO>();
        for (Trigger trigger : rt.getModules(Trigger.class)) {
            triggers.add(new TriggerDTO(trigger));
        }
    }

}
