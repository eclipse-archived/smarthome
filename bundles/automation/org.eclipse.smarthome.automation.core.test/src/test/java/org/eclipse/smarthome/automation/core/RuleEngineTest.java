/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

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
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.FilterCriteria;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test adding, retrieving and updating rules from the RuleEngine
 */
public class RuleEngineTest {

    Logger log = LoggerFactory.getLogger(RuleEngineTest.class);

    /**
     * test adding and retrieving rules
     */
    @Test
    public void testAddRetrieveRules() {
        RuleEngine ruleEngine = new RuleEngine(new BundleContextMockup());
        Rule rule0 = new Rule();
        ruleEngine.addRule(rule0);
        Collection<Rule> rules = ruleEngine.getRules();
        Assert.assertNotNull("null returned instead of rules list", rules);
        Assert.assertEquals("empty rules list is returned", 1, rules.size());
        rules.add(new Rule());
        Assert.assertNotEquals("new copy of rules list should be returned", rules.hashCode(),
                ruleEngine.getRules().hashCode());

        Rule rule1 = createRule();
        ruleEngine.addRule(rule1);
        Rule[] array = new Rule[2];
        array = ruleEngine.getRules().toArray(array);
        Assert.assertEquals("Expected list with 2 rules", 2, array.length);
        Assert.assertNotEquals("Returned rule [0] is not a copy", rule1, array[0]);// returned rule should be a copy
        Assert.assertNotEquals("Returned rule [1] is not a copy", rule1, array[1]);// returned rule should be a copy

        Rule rule1Get = ruleEngine.getRule("rule1");
        Assert.assertNotNull("Cannot find rule by UID", rule1Get);
        Assert.assertNotEquals("Returned rule by UID is not a copy", rule1, rule1Get);// returned rule should be a
                                                                                      // copy
        Assert.assertEquals("Returned rule with wrong UID", "rule1", rule1Get.getUID());
    }

    /**
     * test editing rule tags
     */
    @Test
    public void testRuleTags() {
        RuleEngine ruleEngine = new RuleEngine(new BundleContextMockup());
        Rule rule2 = new Rule("rule2", null, null, null, null, null);
        Set<String> ruleTags = new LinkedHashSet<String>();
        ruleTags.add("tag1");
        ruleTags.add("tag2");
        rule2.setTags(ruleTags);
        ruleEngine.addRule(rule2);

        Rule rule2Get = ruleEngine.getRule("rule2");
        Assert.assertNotNull("Cannot find rule by UID", rule2Get);
        Assert.assertNotNull("rule.getTags is null", rule2Get.getTags());
        Assert.assertEquals("rule.getTags is empty", 2, rule2Get.getTags().size());

        Collection<Rule> rules = ruleEngine.getRulesByTag("tag1");
        Assert.assertNotNull("getRulesByTag returned null", rules);
        Assert.assertEquals("getRulesByTag returned empty list", 1, rules.size());
        Rule[] array = new Rule[1];
        array = rules.toArray(array);
        Set<String> rule2GetTags = array[0].getTags();
        Assert.assertNotNull("rule.getTags is null", rule2GetTags);
        Assert.assertEquals("rule.getTags is empty", 2, rule2GetTags.size());
        Assert.assertTrue("Missing tag in rule", rule2GetTags.contains("tag1"));
        Assert.assertTrue("Missing tag in rule", rule2GetTags.contains("tag2"));
    }

    /**
     * test rule configurations with null
     */
    @Test
    public void testRuleConfigNull() {
        RuleEngine ruleEngine = new RuleEngine(new BundleContextMockup());
        Rule rule3 = new Rule("rule3", createTriggers(), createConditions(), createActions(), null, null);
        ruleEngine.addRule(rule3);
        Rule rule3Get = ruleEngine.getRule("rule3");
        Assert.assertNotNull("Rule configuration description is null", rule3Get.getConfigurationDescriptions());
        Assert.assertNotNull("Rule configuration is null", rule3Get.getConfiguration());
    }

    /**
     * test rule configurations with real values
     */
    @Test
    public void testRuleConfigValue() {
        RuleEngine ruleEngine = new RuleEngine(new BundleContextMockup());
        Set<ConfigDescriptionParameter> configDescriptions = createConfigDescriptinos();
        Map<String, Object> configurations = new HashMap<String, Object>();
        configurations.put("config1", 5);

        Rule rule4 = new Rule("rule4", createTriggers(), createConditions(), createActions(), configDescriptions,
                configurations);
        ruleEngine.addRule(rule4);
        Rule rule4Get = ruleEngine.getRule("rule4");
        Map<String, ?> rule4cfg = rule4Get.getConfiguration();
        Set<ConfigDescriptionParameter> rule4cfgD = rule4Get.getConfigurationDescriptions();
        Assert.assertNotNull("Rule configuration is null", rule4cfg);
        Assert.assertTrue("Missing config property in rule copy", rule4cfg.containsKey("config1"));
        Assert.assertEquals("Wrong config value", 5, rule4cfg.get("config1"));

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
     */
    @Test
    public void testRuleActions() {
        RuleEngine ruleEngine = new RuleEngine(new BundleContextMockup());
        Rule rule1 = createRule();
        ruleEngine.addRule(rule1);
        Rule rule1Get = ruleEngine.getRule("rule1");
        List<Action> actionsGet = rule1Get.getActions();
        Assert.assertNotNull("Null actions list", actionsGet);
        Assert.assertEquals("Empty actions list", 1, actionsGet.size());
        Assert.assertEquals("Returned actions list should not be a copy", actionsGet, rule1Get.getActions());

        actionsGet.add(new Action("actionId2", "typeUID2", null, null));
        List<Action> actionsGet2 = rule1Get.getActions();
        Assert.assertNotNull("Null actions list", actionsGet2);
        Assert.assertEquals("Action was not added to the rule's list of actions", 2, actionsGet2.size());
        Assert.assertNotNull("Rule action with wrong id is returned", rule1Get.getModule("actionId2"));

        actionsGet.add(new Action("actionId3", "typeUID3", null, null));
        List<Action> actionsGet3 = rule1Get.getActions();
        Assert.assertNotNull("Null actions list", actionsGet3);
        Assert.assertEquals("Action was not added to the rule's list of actions", 3, actionsGet3.size());
        Assert.assertNull("Rule modules are not cached", rule1Get.getModule("actionId3"));
        ruleEngine.updateRule(rule1Get);// ruleEngine.update will update the RuntimeRule.moduleMap with the new
        // module
        Assert.assertNotNull("Rule modules map was not updated", ruleEngine.getRule("rule1").getModule("actionId3"));
    }

    /**
     * test rule triggers
     */
    @Test
    public void testRuleTriggers() {
        RuleEngine ruleEngine = new RuleEngine(new BundleContextMockup());
        Rule rule1 = createRule();
        ruleEngine.addRule(rule1);
        Rule rule1Get = ruleEngine.getRule("rule1");
        List<Trigger> triggersGet = rule1Get.getTriggers();
        Assert.assertNotNull("Null triggers list", triggersGet);
        Assert.assertEquals("Empty triggers list", 1, triggersGet.size());
        Assert.assertEquals("Returned triggers list should not be a copy", triggersGet, rule1Get.getTriggers());

        triggersGet.add(new Trigger("triggerId2", "typeUID2", null));
        ruleEngine.updateRule(rule1Get);// ruleEngine.update will update the RuntimeRule.moduleMap with the new
                                        // module
        Rule rule2Get = ruleEngine.getRule("rule1");
        List<Trigger> triggersGet2 = rule2Get.getTriggers();
        Assert.assertNotNull("Null triggers list", triggersGet2);
        Assert.assertEquals("Trigger was not added to the rule's list of triggers", 2, triggersGet2.size());
        Assert.assertEquals("Returned triggers list should not be a copy", triggersGet2, rule2Get.getTriggers());
        Assert.assertNotNull("Rule trigger with wrong id is returned: " + triggersGet2,
                rule2Get.getModule("triggerId2"));
    }

    /**
     * test rule conditions
     */
    @Test
    public void testRuleConditions() {
        RuleEngine ruleEngine = new RuleEngine(new BundleContextMockup());
        Rule rule1 = createRule();
        ruleEngine.addRule(rule1);
        Rule rule1Get = ruleEngine.getRule("rule1");
        List<Condition> conditionsGet = rule1Get.getConditions();
        Assert.assertNotNull("Null conditions list", conditionsGet);
        Assert.assertEquals("Empty conditions list", 1, conditionsGet.size());
        Assert.assertEquals("Returned conditions list should not be a copy", conditionsGet, rule1Get.getConditions());

        conditionsGet.add(new Condition("conditionId2", "typeUID2", null, null));
        ruleEngine.updateRule(rule1Get);// ruleEngine.update will update the RuntimeRule.moduleMap with the new
                                        // module
        Rule rule2Get = ruleEngine.getRule("rule1");
        List<Condition> conditionsGet2 = rule2Get.getConditions();
        Assert.assertNotNull("Null conditions list", conditionsGet2);
        Assert.assertEquals("Condition was not added to the rule's list of conditions", 2, conditionsGet2.size());
        Assert.assertEquals("Returned conditions list should not be a copy", conditionsGet2, rule2Get.getConditions());
        Assert.assertNotNull("Rule condition with wrong id is returned: " + conditionsGet2,
                rule2Get.getModule("conditionId2"));
    }

    private Rule createRule() {
        Set<ConfigDescriptionParameter> configDescriptions = null;// new LinkedHashSet<ConfigDescriptionParameter>();
        Map<String, Object> configurations = null;// new HashMap<String, Object>();
        return new Rule("rule1", createTriggers(), createConditions(), createActions(), configDescriptions,
                configurations);
    }

    private List<Trigger> createTriggers() {
        List<Trigger> triggers = new ArrayList<Trigger>();
        Map<String, Object> configurations = new HashMap<String, Object>();
        configurations.put("a", "x");
        configurations.put("b", "y");
        configurations.put("c", "z");
        triggers.add(new Trigger("triggerId", "typeUID", configurations));
        return triggers;
    }

    private List<Condition> createConditions() {
        List<Condition> conditions = new ArrayList<Condition>();
        Map<String, Object> configurations = new HashMap<String, Object>();
        configurations.put("a", "x");
        configurations.put("b", "y");
        configurations.put("c", "z");
        LinkedHashSet<Connection> connections = new LinkedHashSet<Connection>();
        String ouputModuleId = "triggerId";
        String outputName = "triggerOutput";
        String inputName = "conditionInput";
        Connection connection = new Connection(inputName, ouputModuleId, outputName);
        connections.add(connection);
        conditions.add(new Condition("conditionId", "typeUID", configurations, connections));
        return conditions;
    }

    private List<Action> createActions() {
        List<Action> actions = new ArrayList<Action>();
        Map<String, Object> configurations = new HashMap<String, Object>();
        configurations.put("a", "x");
        configurations.put("b", "y");
        configurations.put("c", "z");
        LinkedHashSet<Connection> connections = new LinkedHashSet<Connection>();
        String ouputModuleId = "triggerId";
        String outputName = "triggerOutput";
        String inputName = "conditionInput";
        Connection connection = new Connection(inputName, ouputModuleId, outputName);
        connections.add(connection);
        actions.add(new Action("actionId", "typeUID", configurations, connections));
        return actions;
    }

    private Set<ConfigDescriptionParameter> createConfigDescriptinos() {
        Set<ConfigDescriptionParameter> configDescriptions = new LinkedHashSet<ConfigDescriptionParameter>();
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

        ConfigDescriptionParameter cfgDP = new ConfigDescriptionParameter(configPropertyName, Type.valueOf(typeStr),
                max, min, step, pattern, required, readOnly, multiple, context, defValue, label, description, options,
                filter, groupName, advanced, limitToOptions, multipleLimit);
        configDescriptions.add(cfgDP);
        return configDescriptions;
    }

}
