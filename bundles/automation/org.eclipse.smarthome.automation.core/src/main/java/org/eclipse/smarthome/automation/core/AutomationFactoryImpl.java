/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.dto.ActionDTO;
import org.eclipse.smarthome.automation.dto.ConditionDTO;
import org.eclipse.smarthome.automation.dto.RuleDTO;
import org.eclipse.smarthome.automation.dto.TriggerDTO;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This class is an implementation of {@link AutomationFactory} interface and it is used to create automation modules:
 * {@link Trigger}, {@link Condition}s , {@link Action}s and to create {@link Rule} objects base on connected automation
 * modules.
 *
 * @see AutomationFactory
 * @author Yordan Mihaylov - Initial implementation
 */
public class AutomationFactoryImpl implements AutomationFactory {

    @Override
    public Trigger createTrigger(String id, String typeUID, Map<String, ?> configurations) {
        return new TriggerImpl(id, typeUID, configurations);
    }

    @Override
    public Condition createCondition(String id, String typeUID, Map<String, ?> configuration,
            Set<Connection> connections) {
        return new ConditionImpl(id, typeUID, configuration, connections);
    }

    @Override
    public Action createAction(String id, String typeUID, Map<String, ?> configuration, Set<Connection> connections) {
        return new ActionImpl(id, typeUID, configuration, connections);
    }

    @Override
    public Rule createRule(List<Trigger> triggers, List<Condition> conditions, List<Action> actions,
            Set<ConfigDescriptionParameter> configDescriptions, Map<String, ?> configurations) {
        return new RuleImpl(triggers, conditions, actions, configDescriptions, configurations);
    }

    @Override
    public Rule createRule(String ruleTemplateUID, Map<String, Object> configurations) {
        return new RuleImpl(ruleTemplateUID, configurations);
    }

    @Override
    public Rule createRule(String uid, List<Trigger> triggers, List<Condition> conditions, List<Action> actions,
            Set<ConfigDescriptionParameter> configDescriptions, Map<String, Object> configurations) {
        RuleImpl rule = new RuleImpl(triggers, conditions, actions, configDescriptions, configurations);
        rule.setUID(uid);
        return rule;
    }

    @Override
    public Rule createRule(String uid, String ruleTemplateUID, Map<String, Object> configurations) {
        RuleImpl rule = new RuleImpl(ruleTemplateUID, configurations);
        rule.setUID(uid);
        return rule;
    }

    @Override
    public Trigger createTrigger(TriggerDTO triggerDTO) {
        Trigger trigger = createTrigger(triggerDTO.id, triggerDTO.typeUID, triggerDTO.configurations);
        trigger.setDescription(triggerDTO.description);
        trigger.setLabel(triggerDTO.label);
        return trigger;
    }

    @Override
    public Condition createCondition(ConditionDTO conditionDTO) {
        Condition condition = createCondition(conditionDTO.id, conditionDTO.typeUID, conditionDTO.configurations,
                conditionDTO.connections);
        condition.setDescription(conditionDTO.description);
        condition.setLabel(conditionDTO.label);
        return condition;
    }

    @Override
    public Action createAction(ActionDTO actionDTO) {
        Action action = createAction(actionDTO.id, actionDTO.typeUID, actionDTO.configurations, actionDTO.connections);
        action.setDescription(actionDTO.description);
        action.setLabel(actionDTO.label);
        return action;
    }

    @Override
    public Rule createRule(RuleDTO ruleDTO) {
        Rule rule;
        if (ruleDTO.ruleTemplateUID != null) {
            if (ruleDTO.uid != null)
                rule = createRule(ruleDTO.uid, ruleDTO.ruleTemplateUID, ruleDTO.configurations);
            else
                rule = createRule(ruleDTO.ruleTemplateUID, ruleDTO.configurations);
        } else {
            List<Trigger> triggers = new ArrayList<Trigger>(ruleDTO.triggers.size());
            for (TriggerDTO trigger : ruleDTO.triggers) {
                triggers.add(trigger.createTrigger(this));
            }
            List<Condition> conditions = new ArrayList<Condition>(ruleDTO.conditions.size());
            for (ConditionDTO condition : ruleDTO.conditions) {
                conditions.add(condition.createCondition(this));
            }
            List<Action> actions = new ArrayList<Action>(ruleDTO.actions.size());
            for (ActionDTO action : ruleDTO.actions) {
                actions.add(action.createAction(this));
            }
            if (ruleDTO.uid != null) {
                rule = createRule(ruleDTO.uid, triggers, conditions, actions, ruleDTO.configDescriptions,
                        ruleDTO.configurations);
            } else {
                rule = createRule(triggers, conditions, actions, ruleDTO.configDescriptions, ruleDTO.configurations);
            }
        }
        rule.setName(ruleDTO.name);
        rule.setDescription(ruleDTO.description);
        rule.setTags(ruleDTO.tags);
        return rule;
    }

}
