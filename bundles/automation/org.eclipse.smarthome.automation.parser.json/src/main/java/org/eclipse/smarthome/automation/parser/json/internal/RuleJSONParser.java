/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.json.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.ParsingException;
import org.eclipse.smarthome.automation.parser.ParsingNestedException;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class serves for loading JSON files and parse it to the Rule objects.
 *
 * @author Ana Dimova - Initial Contribution
 * @author Ana Dimova - refactor Parser interface.
 *
 */
public class RuleJSONParser implements Parser<Rule> {

    /**
     * This field is used for logging
     */
    private Logger log;

    /**
     * Constructs RuleJSONParser
     *
     * @param automationFactory the AutomationFactory
     */
    public RuleJSONParser() {
        this.log = LoggerFactory.getLogger(RuleJSONParser.class);
    }

    @Override
    public Set<Rule> parse(InputStreamReader reader) throws ParsingException {
        JSONTokener tokener = new JSONTokener(reader);
        Set<Rule> rules = new HashSet<Rule>();
        List<ParsingNestedException> exceptions = new ArrayList<ParsingNestedException>();
        try {
            Object json = tokener.nextValue();
            if (json != null) {
                if (json instanceof JSONArray) {
                    for (int i = 0; i < ((JSONArray) json).length(); i++) {
                        rules.add(createRule(((JSONArray) json).getJSONObject(i), exceptions));
                    }
                } else {
                    rules.add(createRule((JSONObject) json, exceptions));
                }
            }
        } catch (JSONException e) {
            JSONUtility.catchParsingException(ParsingNestedException.RULE, null, exceptions,
                    new IllegalArgumentException("JSON contains extra objects or lines.", e), log);
        }
        if (exceptions.isEmpty())
            return rules;
        else
            throw new ParsingException(exceptions);

    }

    @Override
    public void serialize(Set<Rule> dataObjects, OutputStreamWriter writer) throws Exception {
        try {
            writeRules(dataObjects, writer);
        } catch (JSONException e) {
            throw new Exception("Export failed: " + e.toString());
        }
    }

    /**
     * This method is used for creating {@link Rule} from JSONObject.
     *
     * @param jsonRule is a json object representing the {@link Rule} in json format.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link Rule}'s creation.
     * @return the newly created {@link Rule} or {@code null};
     */
    private Rule createRule(JSONObject jsonRule, List<ParsingNestedException> exceptions) {
        // verify json content
        Iterator<?> i = jsonRule.keys();
        while (i.hasNext()) {
            String propertyName = (String) i.next();
            int sType = JSONUtility.checkRuleProperties(propertyName);
            if (sType == -1) {
                JSONUtility.catchParsingException(ParsingNestedException.RULE, null, exceptions,
                        new IllegalArgumentException(
                                "Unsupported property \"" + propertyName + "\" in rule : " + jsonRule),
                        log);
            }
        }
        Map<String, Object> configurations = null;
        Rule rule = null;
        String uid = JSONUtility.getString(ParsingNestedException.RULE, null, exceptions, JSONStructureConstants.UID,
                true, jsonRule, log);
        String ruleTemplateUID = JSONUtility.getString(ParsingNestedException.RULE, uid, exceptions,
                JSONStructureConstants.TEMPLATE_UID, true, jsonRule, log);
        if (ruleTemplateUID != null) {
            JSONObject jsonConfig = JSONUtility.getJSONObject(ParsingNestedException.RULE, uid, exceptions,
                    JSONStructureConstants.CONFIG, true, jsonRule, log);
            configurations = ConfigPropertyJSONParser.getConfigurationValues(ParsingNestedException.RULE, uid,
                    exceptions, jsonConfig, log);
            if (uid != null)
                rule = new Rule(uid, ruleTemplateUID, configurations);
            else
                rule = new Rule(ruleTemplateUID, configurations);
        } else {
            JSONArray sectionTriggers = JSONUtility.getJSONArray(ParsingNestedException.RULE, uid, exceptions,
                    JSONStructureConstants.ON, false, jsonRule, log);
            List<Trigger> triggers = ModuleJSONParser.createTriggerModules(ParsingNestedException.RULE, uid,
                    JSONStructureConstants.ON, sectionTriggers, exceptions, log);

            JSONArray sectionConditions = JSONUtility.getJSONArray(ParsingNestedException.RULE, uid, exceptions,
                    JSONStructureConstants.IF, true, jsonRule, log);
            List<Condition> conditions = ModuleJSONParser.createConditionModules(ParsingNestedException.RULE, uid,
                    JSONStructureConstants.IF, sectionConditions, exceptions, log);

            JSONArray sectionActions = JSONUtility.getJSONArray(ParsingNestedException.RULE, uid, exceptions,
                    JSONStructureConstants.THEN, false, jsonRule, log);
            List<Action> actions = ModuleJSONParser.createActionModules(ParsingNestedException.RULE, uid,
                    JSONStructureConstants.THEN, sectionActions, exceptions, log);

            JSONObject jsonConfig = JSONUtility.getJSONObject(ParsingNestedException.RULE, uid, exceptions,
                    JSONStructureConstants.CONFIG, true, jsonRule, log);
            Set<ConfigDescriptionParameter> configDescriptions = null;
            if (jsonConfig != null) {
                configDescriptions = new LinkedHashSet<ConfigDescriptionParameter>();
                configurations = ConfigPropertyJSONParser.getConfiguration(ParsingNestedException.RULE, uid, exceptions,
                        jsonConfig, configDescriptions, log);
            }
            if (uid != null)
                rule = new Rule(uid, triggers, conditions, actions, configDescriptions, configurations);
            else
                rule = new Rule(triggers, conditions, actions, configDescriptions, configurations);
        }
        String ruleName = JSONUtility.getString(ParsingNestedException.RULE, uid, exceptions,
                JSONStructureConstants.NAME, true, jsonRule, log);

        String description = JSONUtility.getString(ParsingNestedException.RULE, uid, exceptions,
                JSONStructureConstants.DESCRIPTION, true, jsonRule, log);

        JSONArray jsonTags = JSONUtility.getJSONArray(ParsingNestedException.RULE, uid, exceptions,
                JSONStructureConstants.TAGS, true, jsonRule, log);
        Set<String> tags = null;
        if (jsonTags != null) {
            tags = new HashSet<String>();
            for (int j = 0; j < jsonTags.length(); j++) {
                String tag = JSONUtility.getString(ParsingNestedException.RULE, uid, exceptions,
                        JSONStructureConstants.TAGS, j, jsonTags, log);
                if (tag != null)
                    tags.add(tag);
            }
        }
        if (rule != null) {
            rule.setName(ruleName);
            rule.setDescription(description);
            rule.setTags(tags);
        }
        return rule;
    }

    /**
     * This method is used to export the set of {@link Rule}s reverting them to JSON format.
     *
     * @param rules are the {@link Rule} objects to revert.
     * @param writer is the {@link OutputStreamWriter} used for exporting the rules.
     * @throws IOException is thrown when the I/O operations are failed or interrupted.
     * @throws JSONException is thrown by the JSON.org classes when things are amiss.
     * @see org.eclipse.smarthome.automation.parser.RuleParser#writeRules(org.eclipse.smarthome.automation.Rule,
     *      java.io.OutputStreamWriter)
     */
    private void writeRules(Set<Rule> rules, OutputStreamWriter writer) throws IOException, JSONException {
        writer.write("[\n");
        Iterator<Rule> i = rules.iterator();
        while (i.hasNext()) {
            Rule rule = i.next();
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
     * @param writer is the {@link OutputStreamWriter} used for exporting the rule.
     * @return JSONObject is an object representing the {@link Rule} in json format.
     * @throws IOException is thrown when the I/O operations are failed or interrupted.
     * @throws JSONException is thrown by the JSON.org classes when things are amiss.
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
        Map<String, ?> configValues = rule.getConfiguration();
        ruleConfigurationToJSON(configDescriptions, configValues, writer);

        List<Trigger> triggers = rule.getTriggers();
        List<Condition> conditions = rule.getConditions();
        List<Action> actions = rule.getActions();

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
     * This method is used for reverting the {@link Rule}'s configuration description to JSON format.
     *
     * @param configDescriptions is the {@link Rule}'s configuration description to convert.
     * @param configValues is the {@link Rule}'s configuration values.
     * @param writer is the {@link OutputStreamWriter} used for exporting the rule.
     * @throws IOException is thrown when the I/O operations are failed or interrupted.
     * @throws JSONException is thrown by the JSON.org classes when things are amiss.
     */
    private void ruleConfigurationToJSON(Set<ConfigDescriptionParameter> configDescriptions,
            Map<String, ?> configValues, OutputStreamWriter writer) throws IOException, JSONException {
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
