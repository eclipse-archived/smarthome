/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.FilterCriteria;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test adding, retrieving and updating rules from the RuleEngine
 *
 * @author Marin Mitev - initial version
 * @author Thomas Höfer - Added config description parameter unit
 */
public class RuleEngineTest {

    private RuleEngine createRuleEngine() {
        RuleEngine ruleEngine = new RuleEngine();
        ruleEngine.setModuleTypeRegistry(new ModuleTypeRegistryMockup());
        return ruleEngine;
    }

    /**
     * test adding and retrieving rules
     *
     */
    @Test
    public void testAddRetrieveRules() {
        RuleEngine ruleEngine = createRuleEngine();
        Rule rule0 = new Rule(ruleEngine.getUniqueId());
        ruleEngine.addRule(rule0, true);
        Collection<RuntimeRule> rules = ruleEngine.getRuntimeRules();
        Assert.assertNotNull("null returned instead of rules list", rules);
        Assert.assertEquals("empty rules list is returned", 1, rules.size());
        Assert.assertEquals("Returned rule with wrong UID", "rule_1", rules.iterator().next().getUID());
        Rule rule1 = createRule();
        ruleEngine.addRule(rule1, true);
        rules = ruleEngine.getRuntimeRules();
        Assert.assertEquals("rules list should contain 2 rules", 2, rules.size());
        RuntimeRule rule1Get = ruleEngine.getRuntimeRule("rule1");
        Assert.assertEquals("Returned rule with wrong UID", "rule1", rule1Get.getUID());
        Rule rule2 = createRule();
        ruleEngine.addRule(rule2, true);
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
        RuleEngine ruleEngine = createRuleEngine();
        Rule rule = createAutoMapRule();
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
        ruleEngine.addRule(rule, true);
        RuntimeRule ruleGet = ruleEngine.getRuntimeRule("AutoMapRule");
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
        RuleEngine ruleEngine = createRuleEngine();

        Rule rule1 = new Rule("ruleWithTag1");
        Set<String> ruleTags = new LinkedHashSet<String>();
        ruleTags.add("tag1");
        rule1.setTags(ruleTags);
        ruleEngine.addRule(rule1, true);

        Rule rule2 = new Rule("ruleWithTags12");
        ruleTags = new LinkedHashSet<String>();
        ruleTags.add("tag1");
        ruleTags.add("tag2");
        rule2.setTags(ruleTags);
        ruleEngine.addRule(rule2, true);

        RuntimeRule rule1Get = ruleEngine.getRuntimeRule("ruleWithTag1");
        Assert.assertNotNull("Cannot find rule by UID", rule1Get);
        Assert.assertNotNull("rule.getTags is null", rule1Get.getTags());
        Assert.assertEquals("rule.getTags is empty", 1, rule1Get.getTags().size());

        RuntimeRule rule2Get = ruleEngine.getRuntimeRule("ruleWithTags12");
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
        RuleEngine ruleEngine = createRuleEngine();

        Rule rule3 = new Rule("rule3");
        rule3.setTriggers(createTriggers("typeUID"));
        rule3.setConditions(createConditions("typeUID"));
        rule3.setActions(createActions("typeUID"));
        ruleEngine.addRule(rule3, true);
        RuntimeRule rule3Get = ruleEngine.getRuntimeRule("rule3");
        Assert.assertNotNull("Rule configuration is null", rule3Get.getConfiguration());
    }

    /**
     * test rule configurations with real values
     *
     */
    @Test
    public void testRuleConfigValue() {
        RuleEngine ruleEngine = createRuleEngine();

        List<ConfigDescriptionParameter> configDescriptions = createConfigDescriptions();
        Configuration configurations = new Configuration();
        configurations.put("config1", 5);

        Rule rule4 = new Rule("rule4");
        rule4.setTriggers(createTriggers("typeUID"));
        rule4.setConditions(createConditions("typeUID"));
        rule4.setActions(createActions("typeUID"));
        rule4.setConfigurationDescriptions(configDescriptions);
        rule4.setConfiguration(configurations);
        ruleEngine.addRule(rule4, true);
        RuntimeRule rule4Get = ruleEngine.getRuntimeRule("rule4");
        Configuration rule4cfg = rule4Get.getConfiguration();
        List<ConfigDescriptionParameter> rule4cfgD = rule4Get.getConfigurationDescriptions();
        Assert.assertNotNull("Rule configuration is null", rule4cfg);
        Assert.assertTrue("Missing config property in rule copy", rule4cfg.containsKey("config1"));
        Assert.assertEquals("Wrong config value", new BigDecimal(5), rule4cfg.get("config1"));

        Assert.assertNotNull("Rule configuration description is null", rule4cfgD);
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
        RuleEngine ruleEngine = createRuleEngine();

        Rule rule1 = createRule();
        List<Action> actions = rule1.getActions();
        ruleEngine.addRule(rule1, true);

        RuntimeRule rule1Get = ruleEngine.getRuntimeRule("rule1");
        List<Action> actionsGet = rule1Get.getActions();
        Assert.assertNotNull("Null actions list", actionsGet);
        Assert.assertEquals("Empty actions list", 1, actionsGet.size());
        Assert.assertEquals("Returned actions list should not be a copy", actionsGet, rule1Get.getActions());

        actions.add(new Action("actionId2", "typeUID2", null, null));
        ruleEngine.updateRule(rule1, true);
        rule1Get = ruleEngine.getRuntimeRule("rule1");
        List<Action> actionsGet2 = rule1Get.getActions();
        Assert.assertNotNull("Null actions list", actionsGet2);
        Assert.assertEquals("Action was not added to the rule's list of actions", 2, actionsGet2.size());
        Assert.assertNotNull("Rule action with wrong id is returned", rule1Get.getModule("actionId2"));

        actions.add(new Action("actionId3", "typeUID3", null, null));
        ruleEngine.updateRule(rule1, true);// ruleEngine.update will update the RuntimeRule.moduleMap with the new
        // module
        rule1Get = ruleEngine.getRuntimeRule("rule1");
        List<Action> actionsGet3 = rule1Get.getActions();
        Assert.assertNotNull("Null actions list", actionsGet3);
        Assert.assertEquals("Action was not added to the rule's list of actions", 3, actionsGet3.size());
        Assert.assertNotNull("Rule modules map was not updated",
                ruleEngine.getRuntimeRule("rule1").getModule("actionId3"));
    }

    /**
     * test rule triggers
     *
     */
    @Test
    public void testRuleTriggers() {
        RuleEngine ruleEngine = createRuleEngine();

        Rule rule1 = createRule();
        List<Trigger> triggers = rule1.getTriggers();
        ruleEngine.addRule(rule1, true);
        RuntimeRule rule1Get = ruleEngine.getRuntimeRule("rule1");
        List<Trigger> triggersGet = rule1Get.getTriggers();
        Assert.assertNotNull("Null triggers list", triggersGet);
        Assert.assertEquals("Empty triggers list", 1, triggersGet.size());
        Assert.assertEquals("Returned triggers list should not be a copy", triggersGet, rule1Get.getTriggers());

        triggers.add(new Trigger("triggerId2", "typeUID2", null));
        ruleEngine.updateRule(rule1, true);// ruleEngine.update will update the RuntimeRule.moduleMap with the new
                                           // module
        RuntimeRule rule2Get = ruleEngine.getRuntimeRule("rule1");
        List<Trigger> triggersGet2 = rule2Get.getTriggers();
        Assert.assertNotNull("Null triggers list", triggersGet2);
        Assert.assertEquals("Trigger was not added to the rule's list of triggers", 2, triggersGet2.size());
        Assert.assertEquals("Returned triggers list should not be a copy", triggersGet2, rule2Get.getTriggers());
        Assert.assertNotNull("Rule trigger with wrong id is returned: " + triggersGet2,
                rule2Get.getModule("triggerId2"));
    }

    /**
     * test rule condition
     */
    @Test
    public void testRuleConditions() {
        RuleEngine ruleEngine = createRuleEngine();

        Rule rule1 = createRule();
        List<Condition> conditions = rule1.getConditions();
        ruleEngine.addRule(rule1, true);
        RuntimeRule rule1Get = ruleEngine.getRuntimeRule("rule1");
        List<Condition> conditionsGet = rule1Get.getConditions();
        Assert.assertNotNull("Null conditions list", conditionsGet);
        Assert.assertEquals("Empty conditions list", 1, conditionsGet.size());
        Assert.assertEquals("Returned conditions list should not be a copy", conditionsGet, rule1Get.getConditions());

        conditions.add(new Condition("conditionId2", "typeUID2", null, null));
        ruleEngine.updateRule(rule1, true);// ruleEngine.update will update the RuntimeRule.moduleMap with the new
        // module
        RuntimeRule rule2Get = ruleEngine.getRuntimeRule("rule1");
        List<Condition> conditionsGet2 = rule2Get.getConditions();
        Assert.assertNotNull("Null conditions list", conditionsGet2);
        Assert.assertEquals("Condition was not added to the rule's list of conditions", 2, conditionsGet2.size());
        Assert.assertEquals("Returned conditions list should not be a copy", conditionsGet2, rule2Get.getConditions());
        Assert.assertNotNull("Rule condition with wrong id is returned: " + conditionsGet2,
                rule2Get.getModule("conditionId2"));
    }

    private Rule createRule() {
        Rule rule = new Rule("rule1");
        rule.setTriggers(createTriggers("typeUID"));
        rule.setConditions(createConditions("typeUID"));
        rule.setActions(createActions("typeUID"));
        return rule;
    }

    private Rule createAutoMapRule() {
        Rule rule = new Rule("AutoMapRule");
        rule.setTriggers(createTriggers(ModuleTypeRegistryMockup.TRIGGER_TYPE));
        rule.setConditions(createConditions(ModuleTypeRegistryMockup.CONDITION_TYPE));
        rule.setActions(createActions(ModuleTypeRegistryMockup.ACTION_TYPE));
        return rule;

    }

    private List<Trigger> createTriggers(String type) {
        List<Trigger> triggers = new ArrayList<Trigger>();
        Configuration configurations = new Configuration();
        configurations.put("a", "x");
        configurations.put("b", "y");
        configurations.put("c", "z");
        triggers.add(new Trigger("triggerId", type, configurations));
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
        conditions.add(new Condition("conditionId", type, configurations, inputs));
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
        actions.add(new Action("actionId", type, configurations, inputs));
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
