/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This class serves for loading JSON files and parse it to the Rule objects.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class RuleJSONParser implements Parser {

    private AutomationFactory automationFactory;
    private Logger log;

    public RuleJSONParser(AutomationFactory automationFactory) {
        this.automationFactory = automationFactory;
        this.log = LoggerFactory.getLogger(RuleJSONParser.class);
    }

    /**
     * @throws JSONException
     * @see org.eclipse.smarthome.automation.parser.Parser#importData(InputStreamReader)
     */
    public Set<Status> importData(InputStreamReader reader) {
        JSONTokener tokener = new JSONTokener(reader);
        Set<Status> rulesStatus = new LinkedHashSet<Status>();
        try {
            Object json = tokener.nextValue();
            if (json != null) {
                if (json instanceof JSONArray) {
                    for (int i = 0; i < ((JSONArray) json).length(); i++) {
                        rulesStatus.add(createRule(((JSONArray) json).getJSONObject(i)));
                    }
                } else {
                    rulesStatus.add(createRule((JSONObject) json));
                }
            }
        } catch (JSONException e) {
            Status status = new Status(log, Status.TEMPLATE, null);
            status.error("JSON contains extra objects or lines.", e);
            rulesStatus.add(status);
        }
        return rulesStatus;
    }

    /**
     * @see org.eclipse.smarthome.automation.parser.Parser#exportData(Set, OutputStreamWriter)
     */
    public void exportData(Set<?> dataObjects, OutputStreamWriter writer) throws IOException {
        try {
            writeRules(dataObjects, writer);
        } catch (JSONException e) {
            throw new IOException("Export failed: " + e.toString());
        }
    }

    /**
     * This method is used for creating {@link Rule} from JSONObject.
     *
     * @param jsonRule is a json object representing the {@link Rule} in json format.
     * @return
     */
    private Status createRule(JSONObject jsonRule) {
        Status status = new Status(this.log, Status.RULE, null);
        // verify json content
        Iterator<?> i = jsonRule.keys();
        while (i.hasNext()) {
            String propertyName = (String) i.next();
            int sType = JSONUtility.checkRuleProperties(propertyName);
            if (sType == -1) {
                status.error("Unsupported property \"" + propertyName + "\" in rule : " + jsonRule,
                        new IllegalArgumentException());
            }
        }
        Map<String, Object> configurations = null;
        Rule rule = null;
        String ruleTemplateUID = JSONUtility.getString(JSONStructureConstants.TEMPLATE_UID, true, jsonRule, status);
        if (ruleTemplateUID != null) {
            JSONObject jsonConfig = JSONUtility.getJSONObject(JSONStructureConstants.CONFIG, true, jsonRule, status);
            configurations = ConfigPropertyJSONParser.getConfigurationValues(jsonConfig);
            try {
                rule = automationFactory.createRule(ruleTemplateUID, configurations);
            } catch (Exception e) {
                status.error("Failed to validate configuration of Rule! " + e.getMessage(), e);
                return status;
            }
        } else {
            List<Trigger> triggers = new ArrayList<Trigger>();
            List<Condition> conditions = new ArrayList<Condition>();
            List<Action> actions = new ArrayList<Action>();
            Set<ConfigDescriptionParameter> configDescriptions = null;
            JSONArray sectionTrigers = JSONUtility.getJSONArray(JSONStructureConstants.ON, false, jsonRule, status);
            if (sectionTrigers == null)
                return status;
            if (!ModuleJSONParser.createTrigerModules(status, automationFactory, null, triggers, sectionTrigers))
                return status;
            JSONArray sectionConditions = JSONUtility.getJSONArray(JSONStructureConstants.IF, true, jsonRule, status);
            if (sectionConditions != null) {
                if (!ModuleJSONParser.createConditionModules(status, automationFactory, null, conditions,
                        sectionConditions))
                    return status;
            }
            JSONArray sectionActions = JSONUtility.getJSONArray(JSONStructureConstants.THEN, false, jsonRule, status);
            if (sectionActions == null)
                return status;
            if (!ModuleJSONParser.createActionModules(status, automationFactory, null, actions, sectionActions))
                return status;
            configDescriptions = new LinkedHashSet<ConfigDescriptionParameter>();
            JSONObject jsonConfig = JSONUtility.getJSONObject(JSONStructureConstants.CONFIG, true, jsonRule, status);
            if (jsonConfig != null) {
                configurations = ConfigPropertyJSONParser.getConfiguration(jsonConfig, configDescriptions, status);
                if (configurations == null) {
                    return status;
                }
            }
            try {
                rule = automationFactory.createRule(triggers, conditions, actions, configDescriptions, configurations);
            } catch (Exception e) {
                status.error("Failed to validate connections of Rule! " + e.getMessage(), e);
                return status;
            }
        }
        String ruleName = JSONUtility.getString(JSONStructureConstants.NAME, true, jsonRule, status);
        if (ruleName != null)
            rule.setName(ruleName);
        else
            return status;
        JSONArray jsonTags = JSONUtility.getJSONArray(JSONStructureConstants.TAGS, true, jsonRule, status);
        if (jsonTags != null) {
            Set<String> tags = new LinkedHashSet<String>();
            for (int j = 0; j < jsonTags.length(); j++) {
                String tag = JSONUtility.getString(JSONStructureConstants.TAGS, j, jsonTags, status);
                if (tag != null)
                    tags.add(tag);
            }
            rule.setTags(tags);
        }
        status.success(rule);
        status.init(Status.RULE, rule.getUID());
        return status;
    }

    /**
     * @throws IOException
     * @throws JSONException
     * @see org.eclipse.smarthome.automation.parser.RuleParser#writeRules(org.eclipse.smarthome.automation.Rule,
     *      java.io.OutputStreamWriter)
     */
    private void writeRules(Collection<?> rules, OutputStreamWriter writer) throws IOException, JSONException {
        writer.write("[\n");
        Iterator<?> i = rules.iterator();
        while (i.hasNext()) {
            Rule rule = (Rule) i.next();
            ruleToJSON(rule, writer);
            if (i.hasNext()) {
                writer.write(",\n");
            }
        }
        writer.write("\n]");
    }

    /**
     * This method is used for reverting {@link Rule} to JSON format.
     *
     * @param rule is a {@link Rule} object to revert.
     * @param writer
     * @return JSONObject is an object representing the {@link Rule} in json format.
     * @throws IOException
     * @throws JSONException
     */
    private void ruleToJSON(Rule rule, OutputStreamWriter writer) throws IOException, JSONException {
        writer.write("  {\n");
        String uid = rule.getUID();
        if (uid != null)
            writer.write("    \"" + JSONStructureConstants.UID + "\":\"" + uid + "\",\n");

        String name = rule.getName();
        if (name != null)
            writer.write("    \"" + JSONStructureConstants.NAME + "\":\"" + name + "\",\n");

        String description = rule.getDescription();
        if (description != null)
            writer.write("    \"" + JSONStructureConstants.DESCRIPTION + "\":\"" + description + "\",\n");

        Set<String> tags = rule.getTags();
        if (tags != null && !tags.isEmpty()) {
            writer.write("    \"" + JSONStructureConstants.TAGS + "\":");
            new JSONArray(tags).write(writer);
            writer.write(",\n");
        }

        Set<ConfigDescriptionParameter> configDescriptions = rule.getConfigurationDescriptions();
        Map<String, Object> configValues = rule.getConfiguration();
        ruleConfigurationToJSON(configDescriptions, configValues, writer);

        List<Trigger> triggers = rule.getModules(Trigger.class);
        List<Condition> conditions = rule.getModules(Condition.class);
        List<Action> actions = rule.getModules(Action.class);

        writer.write("    \"" + JSONStructureConstants.ON + "\":[\n");
        ModuleJSONParser.writeModules(triggers, writer);
        writer.write("    ],\n");

        if (conditions != null && !conditions.isEmpty()) {
            writer.write("    \"" + JSONStructureConstants.IF + "\":[\n");
            ModuleJSONParser.writeModules(conditions, writer);
            writer.write("    ],\n");
        }

        writer.write("    \"" + JSONStructureConstants.THEN + "\":[\n");
        ModuleJSONParser.writeModules(actions, writer);
        writer.write("    ]\n  }");
    }

    /**
     *
     * @param configDescriptions
     * @param configValues
     * @param writer
     * @throws IOException
     * @throws JSONException
     */
    private void ruleConfigurationToJSON(Set<ConfigDescriptionParameter> configDescriptions,
            Map<String, Object> configValues, OutputStreamWriter writer) throws IOException, JSONException {
        if (configDescriptions != null && !configDescriptions.isEmpty()) {
            writer.write("    \"" + JSONStructureConstants.CONFIG + "\":{\n");
            Iterator<ConfigDescriptionParameter> i = configDescriptions.iterator();
            while (i.hasNext()) {
                ConfigDescriptionParameter configParameter = i.next();
                writer.write("      \"" + configParameter.getName() + "\":{\n");
                Object value = configValues.get(configParameter.getName());
                if (value != null)
                    ConfigPropertyJSONParser.ruleConfigPropertyToJSON(configParameter, value, writer);
                else
                    ConfigPropertyJSONParser.ruleConfigPropertyToJSON(configParameter, configParameter.getDefault(),
                            writer);
                if (i.hasNext())
                    writer.write("\n      },\n");
                else
                    writer.write("\n      }\n");
            }
            writer.write("    },\n");
        }
    }
}
