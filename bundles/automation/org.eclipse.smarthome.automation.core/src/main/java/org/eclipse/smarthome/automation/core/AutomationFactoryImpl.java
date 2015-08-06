/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
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

}
