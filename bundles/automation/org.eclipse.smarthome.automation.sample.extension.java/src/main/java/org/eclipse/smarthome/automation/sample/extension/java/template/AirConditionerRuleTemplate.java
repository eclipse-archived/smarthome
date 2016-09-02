/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.sample.extension.java.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.automation.sample.extension.java.WelcomeHomeRulesProvider;
import org.eclipse.smarthome.automation.sample.extension.java.type.AirConditionerTriggerType;
import org.eclipse.smarthome.automation.sample.extension.java.type.StateConditionType;
import org.eclipse.smarthome.automation.sample.extension.java.type.TemperatureConditionType;
import org.eclipse.smarthome.automation.sample.extension.java.type.WelcomeHomeActionType;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * The purpose of this class is to illustrate how to create {@link RuleTemplate}
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class AirConditionerRuleTemplate extends RuleTemplate {

    public static final String UID = "AirConditionerRuleTemplate";
    public static final String CONFIG_TARGET_TEMPERATURE = "targetTemperature";
    public static final String CONFIG_OPERATION = "operation";
    public static final String TRIGGER_ID = "AirConditionerRuleTrigger";

    public static AirConditionerRuleTemplate initialize() {

        // initialize triggers
        List<Trigger> triggers = new ArrayList<Trigger>();
        triggers.add(new Trigger(TRIGGER_ID, AirConditionerTriggerType.UID, null));

        // initialize conditions
        // here the tricky part is the giving a value to the condition configuration parameter.
        Configuration conditionConfig = new Configuration();
        conditionConfig.put(StateConditionType.CONFIG_STATE, "on");

        // here the tricky part is the referring into the condition input - trigger output.
        // The syntax is a similar to the JUEL syntax.
        Map<String, String> conditionInputs = new HashMap<String, String>();
        conditionInputs.put(StateConditionType.INPUT_CURRENT_STATE,
                TRIGGER_ID + "." + StateConditionType.INPUT_CURRENT_STATE);

        Condition stateCondition = new Condition("AirConditionerStateCondition", StateConditionType.UID,
                conditionConfig, conditionInputs);

        // here the tricky part is the referring into the condition configuration parameter - the
        // template configuration parameter. The syntax is a similar to the JUEL syntax.
        conditionConfig = new Configuration();
        conditionConfig.put(TemperatureConditionType.CONFIG_TEMPERATURE, "$" + CONFIG_TARGET_TEMPERATURE);
        conditionConfig.put(TemperatureConditionType.CONFIG_OPERATOR, "$" + CONFIG_OPERATION);

        // here the tricky part is the referring into the condition input - trigger output.
        // The syntax is a similar to the JUEL syntax.
        conditionInputs = new HashMap<String, String>();
        conditionInputs.put(TemperatureConditionType.INPUT_CURRENT_TEMPERATURE,
                TRIGGER_ID + "." + TemperatureConditionType.INPUT_CURRENT_TEMPERATURE);

        Condition temperatureCondition = new Condition("AirConditionerTemperatureCondition",
                TemperatureConditionType.UID, conditionConfig, conditionInputs);

        List<Condition> conditions = new ArrayList<Condition>();
        conditions.add(stateCondition);
        conditions.add(temperatureCondition);

        // initialize actions - here the tricky part is the referring into the action configuration parameter - the
        // template configuration parameter. The syntax is a similar to the JUEL syntax.
        Configuration actionConfig = new Configuration();
        actionConfig.put(WelcomeHomeActionType.CONFIG_DEVICE, "$" + WelcomeHomeRulesProvider.CONFIG_UNIT);
        actionConfig.put(WelcomeHomeActionType.CONFIG_RESULT, "$" + WelcomeHomeRulesProvider.CONFIG_EXPECTED_RESULT);

        List<Action> actions = new ArrayList<Action>();
        actions.add(new Action("AirConditionerSwitchOnAction", WelcomeHomeActionType.UID, actionConfig, null));

        // initialize configDescriptions
        List<ConfigDescriptionParameter> configDescriptions = new ArrayList<ConfigDescriptionParameter>();
        final ConfigDescriptionParameter device = ConfigDescriptionParameterBuilder
                .create(WelcomeHomeRulesProvider.CONFIG_UNIT, Type.TEXT).withRequired(true).withReadOnly(true)
                .withMultiple(false).withLabel("Device").withDescription("Device description").build();
        final ConfigDescriptionParameter result = ConfigDescriptionParameterBuilder
                .create(WelcomeHomeRulesProvider.CONFIG_EXPECTED_RESULT, Type.TEXT).withRequired(true)
                .withReadOnly(true).withMultiple(false).withLabel("Result").withDescription("Result description")
                .build();
        final ConfigDescriptionParameter temperature = ConfigDescriptionParameterBuilder
                .create(CONFIG_TARGET_TEMPERATURE, Type.INTEGER).withRequired(true).withReadOnly(true)
                .withMultiple(false).withLabel("Target temperature")
                .withDescription("Indicates the target temperature.").build();
        final ConfigDescriptionParameter operation = ConfigDescriptionParameterBuilder
                .create(CONFIG_OPERATION, Type.TEXT).withRequired(true).withReadOnly(true).withMultiple(false)
                .withLabel("Heating/Cooling").withDescription("Indicates Heating or Cooling is set.").build();
        configDescriptions.add(device);
        configDescriptions.add(result);
        configDescriptions.add(temperature);
        configDescriptions.add(operation);

        // initialize tags
        Set<String> tags = new HashSet<String>();
        tags.add("AirConditioner");
        tags.add("LivingRoom");

        // create the template
        return new AirConditionerRuleTemplate(tags, triggers, conditions, actions, configDescriptions);
    }

    public AirConditionerRuleTemplate(Set<String> tags, List<Trigger> triggers, List<Condition> conditions,
            List<Action> actions, List<ConfigDescriptionParameter> configDescriptions) {
        super(UID, "Managing Air Conditioner Rule Template",
                "Template for creation of a rule managing the Air Conditioner in the living room.", tags, triggers,
                conditions, actions, configDescriptions, Visibility.VISIBLE);
    }
}
