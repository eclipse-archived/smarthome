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

    public List<TriggerDTO> triggers;
    public List<ConditionDTO> conditions;
    public List<ActionDTO> actions;

    /**
     * This field holds a set of non-hierarchical keywords or terms for describing the {@link RuleTemplate}.
     */
    public Set<String> tags;
    public String label;
    public String description;
    public Visibility visibility;
    public Set<ConfigDescriptionParameter> configDescriptions;
    public String uid;

    /**
     * This constructor is used for deserialization of the {@link RuleTemplate}s.
     */
    public RuleTemplateDTO() {}

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
