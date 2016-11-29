/*******************************************************************************
 *
 * Copyright (c) 2016  Bosch Software Innovations GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * Plamen Peev - Bosch Software Innovations GmbH - Please refer to git log
 *
 *******************************************************************************/
package org.eclipse.smarthome.automation.sample.java.demo;

import java.util.ArrayList;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.config.core.Configuration;
import org.osgi.service.component.ComponentContext;

/**
 * This class shows how to create a rule, using the Java API.It also shows how to add it to the rule engine via
 * RuleRegistry interface.
 */
public class SampleJavaDemo {

    /**
     * Reference to a {@link RuleRegistry} service. The demo uses the service to add and remove a rule.
     */
    private static RuleRegistry ruleRegistry;

    /**
     * The UID of the added {@link Rule}.
     */
    private static final String RULE_UID = "JavaDemoRule";

    void addRule() {
        final Configuration triggerConfig = new Configuration();
        triggerConfig.put("itemName", "DemoSwitch");
        final Trigger ruleTrigger = new Trigger("RuleTrigger", "ItemStateChangeTrigger", triggerConfig);

        final Configuration actionConfig = new Configuration();
        actionConfig.put("itemName", "DemoDimmer");
        actionConfig.put("command", "ON");
        final Action ruleAction = new Action("RuleAction", "ItemPostCommandAction", actionConfig, null);

        final ArrayList<Trigger> triggers = new ArrayList<Trigger>();
        triggers.add(ruleTrigger);
        final ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(ruleAction);
        final Rule r = new Rule(RULE_UID, triggers, null, actions, null, null, null, Visibility.VISIBLE);
        r.setName("DemoRule");
        ruleRegistry.add(r);
    }

    /**
     * Called from DS when all of the required services are available.
     *
     * @param componentContext - the component's context.
     */
    protected void activate(ComponentContext componentContext) {
        addRule();
    }

    /**
     * Called from DS when one of the required services becomes unavailable.
     *
     * @param componentContext - the component's context.
     */
    protected void deactivate(ComponentContext componentContext) {
        SampleJavaDemo.ruleRegistry.remove(RULE_UID);
    }

    /**
     * Bind the {@link RuleRegistry} service - called from DS.
     *
     * @param ruleRegistry: RuleRegistry service.
     */
    protected void setRuleRegistry(RuleRegistry ruleRegistry) {
        SampleJavaDemo.ruleRegistry = ruleRegistry;
    }

    /**
     * Unbind the {@link RuleRegistry} service - called from DS.
     *
     * @param ruleRegistry: RuleRegistry service.
     */
    protected void unsetRuleRegistry(RuleRegistry ruleRegistry) {
        SampleJavaDemo.ruleRegistry = null;
    }
}
