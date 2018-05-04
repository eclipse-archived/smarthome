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
package org.eclipse.smarthome.automation.core.internal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.core.util.ModuleBuilder;
import org.eclipse.smarthome.automation.core.util.RuleBuilder;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.FilterCriteria;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test adding, retrieving and updating rules from the RuleEngineImpl
 *
 * @author Marin Mitev - initial version
 * @author Thomas Höfer - Added config description parameter unit
 */
public class RuleEngineTest {

    RuleEngineImpl ruleEngine;

    @Before
    public void setup() {
        ruleEngine = new RuleEngineImpl();
        ruleEngine.setModuleTypeRegistry(new ModuleTypeRegistryMockup());
        ruleEngine.setStorageService(new VolatileStorageService());
        ruleEngine.setRuleRegistry(new RuleRegistryImpl());
    }

    /**
     * test adding and retrieving rules
     *
     */
    @Test
    public void testAddRetrieveRules() {
        RuleImpl rule0 = new RuleImpl(null);
        ruleEngine.addRule(rule0);
        Collection<RuleImpl> rules = ruleEngine.getRuntimeRules();
        Assert.assertNotNull("null returned instead of rules list", rules);
        Assert.assertEquals("empty rules list is returned", 1, rules.size());
        Assert.assertNotNull("Returned rule with wrong UID", rules.iterator().next().getUID());
        RuleImpl rule1 = createRule();
        ruleEngine.addRule(rule1);
        rules = ruleEngine.getRuntimeRules();
        Assert.assertEquals("rules list should contain 2 rules", 2, rules.size());
        RuleImpl rule1Get = ruleEngine.getRuntimeRule("rule1");
        Assert.assertEquals("Returned rule with wrong UID", "rule1", rule1Get.getUID());
        RuleImpl rule2 = createRule();
        ruleEngine.addRule(rule2);
        rules = ruleEngine.getRuntimeRules();
        Assert.assertEquals("rules list should contain 2 rules", 2, rules.size());
        Assert.assertEquals("rules list should contain 2 rules", rule1Get, ruleEngine.getRuntimeRule("rule1"));
    }

    /**
     * test auto map connections of the rule
     *
     */
    @Test
    public void testAutoMapRuleConnections() {
        RuleImpl rule = createAutoMapRule();
        // check condition connections
        Map<String, String> conditionInputs = rule.getConditions().get(0).getInputs();
        Assert.assertEquals("Number of user define condition inputs", 1, conditionInputs.size());
        Assert.assertTrue("Check user define condition connection",
                "triggerId.triggerOutput".equals(conditionInputs.get("conditionInput")));

        // check action connections
        Map<String, String> actionInputs = rule.getActions().get(0).getInputs();
        Assert.assertEquals("Number of user define action inputs", 2, actionInputs.size());
        Assert.assertTrue("Check user define action connections for input actionInput",
                "triggerId.triggerOutput".equals(actionInputs.get("actionInput")));
        Assert.assertTrue("Check user define action connections for input in6",
                "triggerId.triggerOutput".equals(actionInputs.get("in6")));

        // do connections auto mapping
        ruleEngine.addRule(rule);
        RuleImpl ruleGet = ruleEngine.getRuntimeRule("AutoMapRule");
        Assert.assertEquals("Returned rule with wrong UID", "AutoMapRule", ruleGet.getUID());

        // check condition connections
        conditionInputs = ruleGet.getConditions().get(0).getInputs();
        Assert.assertEquals("Number of user define condition inputs", 2, conditionInputs.size());
        Assert.assertTrue("Check user define condition connection",
                "triggerId.triggerOutput".equals(conditionInputs.get("conditionInput")));
        Assert.assertTrue("Auto map condition intput in2[tagA, tagB] to trigger output out3[tagA, tagB, tagC]",
                "triggerId.out3".equals(conditionInputs.get("in2")));

        // check action connections
        actionInputs = ruleGet.getActions().get(0).getInputs();
        Assert.assertEquals("Number of user define action inputs", 4, actionInputs.size());
        Assert.assertTrue("Check user define action connections for input actionInput",
                "triggerId.triggerOutput".equals(actionInputs.get("actionInput")));
        Assert.assertTrue("Check user define action connections for input in6 is not changed by the auto mapping",
                "triggerId.triggerOutput".equals(actionInputs.get("in6")));
        Assert.assertTrue("Auto map action intput in5[tagA, tagB, tagC] to trigger output out3[tagA, tagB, tagC]",
                "triggerId.out3".equals(actionInputs.get("in5")));
        Assert.assertTrue("Auto map action intput in5[tagD, tagE] to action output out5[tagD, tagE]",
                "actionId.out5".equals(actionInputs.get("in4")));

    }

    /**
     * test editing rule tags
     *
     */
    @Test
    public void testRuleTags() {
        RuleImpl rule1 = new RuleImpl("ruleWithTag1");
        Set<String> ruleTags = new LinkedHashSet<String>();
        ruleTags.add("tag1");
        rule1.setTags(ruleTags);
        ruleEngine.addRule(rule1);

        RuleImpl rule2 = new RuleImpl("ruleWithTags12");
        ruleTags = new LinkedHashSet<String>();
        ruleTags.add("tag1");
        ruleTags.add("tag2");
        rule2.setTags(ruleTags);
        ruleEngine.addRule(rule2);

        RuleImpl rule1Get = ruleEngine.getRuntimeRule("ruleWithTag1");
        Assert.assertNotNull("Cannot find rule by UID", rule1Get);
        Assert.assertNotNull("rule.getTags is null", rule1Get.getTags());
        Assert.assertEquals("rule.getTags is empty", 1, rule1Get.getTags().size());

        RuleImpl rule2Get = ruleEngine.getRuntimeRule("ruleWithTags12");
        Assert.assertNotNull("Cannot find rule by UID", rule2Get);
        Assert.assertNotNull("rule.getTags is null", rule2Get.getTags());
        Assert.assertEquals("rule.getTags is empty", 2, rule2Get.getTags().size());
    }

    /**
     * test rule configurations with null
     *
     */
    @Test
    public void testRuleConfigNull() {
        Rule rule3 = RuleBuilder.create("rule3").withTriggers(createTriggers("typeUID"))
                .withConditions(createConditions("typeUID")).withActions(createActions("typeUID")).build();
        ruleEngine.addRule(rule3);
        RuleImpl rule3Get = ruleEngine.getRuntimeRule("rule3");
        Assert.assertNotNull("RuleImpl configuration is null", rule3Get.getConfiguration());
    }

    /**
     * test rule configurations with real values
     *
     */
    @Test
    public void testRuleConfigValue() {
        List<ConfigDescriptionParameter> configDescriptions = createConfigDescriptions();
        Configuration configurations = new Configuration();
        configurations.put("config1", 5);

        Rule rule4 = RuleBuilder.create("rule4").withTriggers(createTriggers("typeUID"))
                .withConditions(createConditions("typeUID")).withActions(createActions("typeUID"))
                .withConfigurationDescriptions(configDescriptions).withConfiguration(configurations).build();
        ruleEngine.addRule(rule4);
        RuleImpl rule4Get = ruleEngine.getRuntimeRule("rule4");
        Configuration rule4cfg = rule4Get.getConfiguration();
        List<ConfigDescriptionParameter> rule4cfgD = rule4Get.getConfigurationDescriptions();
        Assert.assertNotNull("RuleImpl configuration is null", rule4cfg);
        Assert.assertTrue("Missing config property in rule copy", rule4cfg.containsKey("config1"));
        Assert.assertEquals("Wrong config value", new BigDecimal(5), rule4cfg.get("config1"));

        Assert.assertNotNull("RuleImpl configuration description is null", rule4cfgD);
        Assert.assertEquals("Missing config description in rule copy", 1, rule4cfgD.size());
        ConfigDescriptionParameter rule4cfgDP = rule4cfgD.iterator().next();
        Assert.assertEquals("Wrong default value in config description", "3", rule4cfgDP.getDefault());
        Assert.assertEquals("Wrong context value in config description", "context1", rule4cfgDP.getContext());
        Assert.assertNotNull("Null options in config description", rule4cfgDP.getOptions());
        Assert.assertEquals("Wrong option value in config description", "1", rule4cfgDP.getOptions().get(0).getValue());
        Assert.assertEquals("Wrong option label in config description", "one",
                rule4cfgDP.getOptions().get(0).getLabel());
    }

    /**
     * test rule actions
     *
     */
    @Test
    public void testRuleActions() {
        RuleImpl rule1 = createRule();
        List<ActionImpl> actions = rule1.getActions();
        ruleEngine.addRule(rule1);

        RuleImpl rule1Get = ruleEngine.getRuntimeRule("rule1");
        List<ActionImpl> actionsGet = rule1Get.getActions();
        Assert.assertNotNull("Null actions list", actionsGet);
        Assert.assertEquals("Empty actions list", 1, actionsGet.size());
        Assert.assertEquals("Returned actions list should not be a copy", actionsGet, rule1Get.getActions());

        actions.add((ActionImpl) ModuleBuilder.createAction().withId("actionId2").withTypeUID("typeUID2").build());
        ruleEngine.addRule(rule1);
        rule1Get = ruleEngine.getRuntimeRule("rule1");
        List<ActionImpl> actionsGet2 = rule1Get.getActions();
        Assert.assertNotNull("Null actions list", actionsGet2);
        Assert.assertEquals("Action was not added to the rule's list of actions", 2, actionsGet2.size());
        Assert.assertNotNull("RuleImpl action with wrong id is returned", rule1Get.getModule("actionId2"));

        actions.add((ActionImpl) ModuleBuilder.createAction().withId("actionId3").withTypeUID("typeUID3").build());
        ruleEngine.addRule(rule1); // ruleEngine.update will update the RuleImpl2.moduleMap with the new module
        rule1Get = ruleEngine.getRuntimeRule("rule1");
        List<ActionImpl> actionsGet3 = rule1Get.getActions();
        Assert.assertNotNull("Null actions list", actionsGet3);
        Assert.assertEquals("Action was not added to the rule's list of actions", 3, actionsGet3.size());
        Assert.assertNotNull("RuleImpl modules map was not updated",
                ruleEngine.getRuntimeRule("rule1").getModule("actionId3"));
    }

    /**
     * test rule triggers
     *
     */
    @Test
    public void testRuleTriggers() {
        RuleImpl rule1 = createRule();
        List<TriggerImpl> triggers = rule1.getTriggers();
        ruleEngine.addRule(rule1);
        RuleImpl rule1Get = ruleEngine.getRuntimeRule("rule1");
        List<TriggerImpl> triggersGet = rule1Get.getTriggers();
        Assert.assertNotNull("Null triggers list", triggersGet);
        Assert.assertEquals("Empty triggers list", 1, triggersGet.size());
        Assert.assertEquals("Returned triggers list should not be a copy", triggersGet, rule1Get.getTriggers());

        triggers.add((TriggerImpl) ModuleBuilder.createTrigger().withId("triggerId2").withTypeUID("typeUID2").build());
        ruleEngine.addRule(rule1); // ruleEngine.update will update the
                                   // RuleImpl2.moduleMap with the new
                                   // module
        RuleImpl rule2Get = ruleEngine.getRuntimeRule("rule1");
        List<TriggerImpl> triggersGet2 = rule2Get.getTriggers();
        Assert.assertNotNull("Null triggers list", triggersGet2);
        Assert.assertEquals("Trigger was not added to the rule's list of triggers", 2, triggersGet2.size());
        Assert.assertEquals("Returned triggers list should not be a copy", triggersGet2, rule2Get.getTriggers());
        Assert.assertNotNull("RuleImpl trigger with wrong id is returned: " + triggersGet2,
                rule2Get.getModule("triggerId2"));
    }

    /**
     * test rule condition
     */
    @Test
    public void testRuleConditions() {
        RuleImpl rule1 = createRule();
        List<ConditionImpl> conditions = rule1.getConditions();
        ruleEngine.addRule(rule1);
        RuleImpl rule1Get = ruleEngine.getRuntimeRule("rule1");
        List<ConditionImpl> conditionsGet = rule1Get.getConditions();
        Assert.assertNotNull("Null conditions list", conditionsGet);
        Assert.assertEquals("Empty conditions list", 1, conditionsGet.size());
        Assert.assertEquals("Returned conditions list should not be a copy", conditionsGet, rule1Get.getConditions());

        conditions.add(
                (ConditionImpl) ModuleBuilder.createCondition().withId("conditionId2").withTypeUID("typeUID2").build());
        ruleEngine.addRule(rule1); // ruleEngine.update will update the RuleImpl2.moduleMap with the new module
        RuleImpl rule2Get = ruleEngine.getRuntimeRule("rule1");
        List<ConditionImpl> conditionsGet2 = rule2Get.getConditions();
        Assert.assertNotNull("Null conditions list", conditionsGet2);
        Assert.assertEquals("Condition was not added to the rule's list of conditions", 2, conditionsGet2.size());
        Assert.assertEquals("Returned conditions list should not be a copy", conditionsGet2, rule2Get.getConditions());
        Assert.assertNotNull("RuleImpl condition with wrong id is returned: " + conditionsGet2,
                rule2Get.getModule("conditionId2"));
    }

    private RuleImpl createRule() {
        return (RuleImpl) RuleBuilder.create("rule1").withTriggers(createTriggers("typeUID"))
                .withConditions(createConditions("typeUID")).withActions(createActions("typeUID")).build();
    }

    private RuleImpl createAutoMapRule() {
        return (RuleImpl) RuleBuilder.create("AutoMapRule")
                .withTriggers(createTriggers(ModuleTypeRegistryMockup.TRIGGER_TYPE))
                .withConditions(createConditions(ModuleTypeRegistryMockup.CONDITION_TYPE))
                .withActions(createActions(ModuleTypeRegistryMockup.ACTION_TYPE)).build();
    }

    private List<Trigger> createTriggers(String type) {
        List<Trigger> triggers = new ArrayList<Trigger>();
        Configuration configurations = new Configuration();
        configurations.put("a", "x");
        configurations.put("b", "y");
        configurations.put("c", "z");
        triggers.add(ModuleBuilder.createTrigger().withId("triggerId").withTypeUID(type)
                .withConfiguration(configurations).build());
        return triggers;
    }

    private List<Condition> createConditions(String type) {
        List<Condition> conditions = new ArrayList<Condition>();
        Configuration configurations = new Configuration();
        configurations.put("a", "x");
        configurations.put("b", "y");
        configurations.put("c", "z");
        Map<String, String> inputs = new HashMap<String, String>(11);
        String ouputModuleId = "triggerId";
        String outputName = "triggerOutput";
        String inputName = "conditionInput";
        inputs.put(inputName, ouputModuleId + "." + outputName);
        conditions.add(ModuleBuilder.createCondition().withId("conditionId").withTypeUID(type)
                .withConfiguration(configurations).withInputs(inputs).build());
        return conditions;
    }

    private List<Action> createActions(String type) {
        List<Action> actions = new ArrayList<Action>();
        Configuration configurations = new Configuration();
        configurations.put("a", "x");
        configurations.put("b", "y");
        configurations.put("c", "z");
        Map<String, String> inputs = new HashMap<String, String>(11);
        String ouputModuleId = "triggerId";
        String outputName = "triggerOutput";
        String inputName = "actionInput";
        inputs.put(inputName, ouputModuleId + "." + outputName);
        inputs.put("in6", ouputModuleId + "." + outputName);
        actions.add(ModuleBuilder.createAction().withId("actionId").withTypeUID(type).withConfiguration(configurations)
                .withInputs(inputs).build());
        return actions;
    }

    private List<ConfigDescriptionParameter> createConfigDescriptions() {
        List<ConfigDescriptionParameter> configDescriptions = new ArrayList<ConfigDescriptionParameter>();
        List<ParameterOption> options = new ArrayList<ParameterOption>();
        options.add(new ParameterOption("1", "one"));
        options.add(new ParameterOption("2", "two"));

        String groupName = null;
        Boolean advanced = false;
        Boolean limitToOptions = true;
        Integer multipleLimit = 0;

        String label = "label1";
        String pattern = null;
        String context = "context1";
        String description = "description1";
        BigDecimal min = null;
        BigDecimal max = null;
        BigDecimal step = null;
        Boolean required = true;
        Boolean multiple = false;
        Boolean readOnly = false;

        String typeStr = ConfigDescriptionParameter.Type.INTEGER.name();
        String defValue = "3";

        List<FilterCriteria> filter = new ArrayList<FilterCriteria>();

        String configPropertyName = "config1";

        ConfigDescriptionParameter cfgDP = ConfigDescriptionParameterBuilder
                .create(configPropertyName, Type.valueOf(typeStr)).withMaximum(max).withMinimum(min).withStepSize(step)
                .withPattern(pattern).withRequired(required).withReadOnly(readOnly).withMultiple(multiple)
                .withContext(context).withDefault(defValue).withLabel(label).withDescription(description)
                .withOptions(options).withFilterCriteria(filter).withGroupName(groupName).withAdvanced(advanced)
                .withLimitToOptions(limitToOptions).withMultipleLimit(multipleLimit).build();
        configDescriptions.add(cfgDP);
        return configDescriptions;
    }

}
