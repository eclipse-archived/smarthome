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
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.ParsingException;
import org.eclipse.smarthome.automation.parser.ParsingNestedException;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template.Visibility;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class serves for loading JSON files and parse it to the Rule Template objects.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class TemplateJSONParser implements Parser<RuleTemplate> {

    private Logger log;

    /**
     * Constructs the TemplateJSONParser
     */
    public TemplateJSONParser() {
        this.log = LoggerFactory.getLogger(TemplateJSONParser.class);
    }

    @Override
    public Set<RuleTemplate> parse(InputStreamReader reader) throws ParsingException {
        Set<RuleTemplate> ruleTemplates = new HashSet<RuleTemplate>();
        JSONTokener tokener = new JSONTokener(reader);
        List<ParsingNestedException> exceptions = new ArrayList<ParsingNestedException>();
        try {
            Object jsonRuleTemplates = tokener.nextValue();
            if (jsonRuleTemplates != null) {
                if (jsonRuleTemplates instanceof JSONArray) {
                    for (int j = 0; j < ((JSONArray) jsonRuleTemplates).length(); j++) {
                        JSONObject jsonRT = JSONUtility.getJSONObject(ParsingNestedException.TEMPLATE, null, exceptions,
                                JSONStructureConstants.RULE_TEMPLATES, j, (JSONArray) jsonRuleTemplates, log);
                        if (jsonRT != null) {
                            RuleTemplate ruleTemplate = createRuleTemplate(jsonRT, exceptions);
                            ruleTemplates.add(ruleTemplate);
                        }
                    }
                } else {
                    RuleTemplate ruleTemplate = createRuleTemplate((JSONObject) jsonRuleTemplates, exceptions);
                    ruleTemplates.add(ruleTemplate);
                }
            }
        } catch (JSONException e) {
            JSONUtility.catchParsingException(ParsingNestedException.TEMPLATE, null, exceptions,
                    new IllegalArgumentException("JSON contains extra objects or lines.", e), log);
        }
        if (exceptions.isEmpty())
            return ruleTemplates;
        throw new ParsingException(exceptions);
    }

    @Override
    public void serialize(Set<RuleTemplate> dataObjects, OutputStreamWriter writer) throws Exception {
        try {
            writeRuleTemplates(dataObjects, writer);
        } catch (JSONException e) {
            throw new Exception("Export failed: " + e.toString());
        }
    }

    /**
     * This method serializes {@link RuleTemplate}s to JSON format.
     *
     * @param ruleTemplates for serialization
     * @param writer where RuleTemplates will be written
     * @throws JSONException when generating of the text fails for some reasons.
     */
    private void writeRuleTemplates(Set<RuleTemplate> ruleTemplates, OutputStreamWriter writer) throws JSONException {
        try {
            writer.write("[\n");
            Iterator<RuleTemplate> i = ruleTemplates.iterator();
            while (i.hasNext()) {
                RuleTemplate ruleTemplate = i.next();
                ruleTemplateToJSON(ruleTemplate, writer);
                if (i.hasNext()) {
                    writer.write(",\n");
                }
            }
            writer.write("\n]");
        } catch (IOException ioe) {
            log.error("Export of data failed!", ioe);
        }
    }

    /**
     * This method is used for reversion of {@link RuleTemplate} to JSON format.
     *
     * @param ruleTemplate is an {@link RuleTemplate} object to revert.
     * @param writer where RuleTemplate will be written
     * @return JSONObject is an object representing the {@link RuleTemplate} in json format.
     * @throws JSONException is thrown when the parameter for the constructor of the JSON not amiss.
     * @throws IOException when I/O operation has failed or has been interrupted
     */
    private void ruleTemplateToJSON(RuleTemplate ruleTemplate, OutputStreamWriter writer)
            throws JSONException, IOException {
        writer.write("  {\n");
        String uid = ruleTemplate.getUID();
        if (uid != null)
            writer.write("    \"" + JSONStructureConstants.UID + "\":\"" + uid + "\",\n");

        String name = ruleTemplate.getLabel();
        if (name != null)
            writer.write("    \"" + JSONStructureConstants.NAME + "\":\"" + name + "\",\n");

        String description = ruleTemplate.getDescription();
        if (description != null)
            writer.write("    \"" + JSONStructureConstants.DESCRIPTION + "\":\"" + description + "\",\n");

        Visibility visibility = ruleTemplate.getVisibility();
        writer.write(
                "    \"" + JSONStructureConstants.VISIBILITY + "\":\"" + visibility.toString().toLowerCase() + "\",\n");

        Set<String> tags = ruleTemplate.getTags();
        if (tags != null && !tags.isEmpty()) {
            writer.write("    \"" + JSONStructureConstants.TAGS + "\":");
            new JSONArray(tags).write(writer);
            writer.write(",\n");
        }

        Set<ConfigDescriptionParameter> configDescriptions = ruleTemplate.getConfigurationDescription();
        ruleTemplateConfigurationToJSON(configDescriptions, writer);

        List<Trigger> triggers = ruleTemplate.getTriggers();
        List<Condition> conditions = ruleTemplate.getConditions();
        List<Action> actions = ruleTemplate.getActions();

        writer.write("    \"" + JSONStructureConstants.ON + "\":[\n");
        ModuleJSONParser.writeModules(triggers, writer);
        writer.write("    ],\n");

        if (!conditions.isEmpty()) {
            writer.write("    \"" + JSONStructureConstants.IF + "\":[\n");
            ModuleJSONParser.writeModules(conditions, writer);
            writer.write("    ],\n");
        }

        writer.write("    \"" + JSONStructureConstants.THEN + "\":[\n");
        ModuleJSONParser.writeModules(actions, writer);
        writer.write("    ]\n  }");
    }

    /**
     * This method is used for reversion of {@link RuleTemplate}'s Configuration to JSON format.
     *
     * @param configDescriptions Configuration of the RuleTemplate
     * @param writer where RuleTemplate's Configuration will be written
     * @throws when I/O operation has failed or has been interrupted
     * @throws JSONException when generating of the text fails for some reasons.
     */
    private void ruleTemplateConfigurationToJSON(Set<ConfigDescriptionParameter> configDescriptions,
            OutputStreamWriter writer) throws IOException, JSONException {
        if (configDescriptions != null && !configDescriptions.isEmpty()) {
            writer.write("    \"" + JSONStructureConstants.CONFIG + "\":{\n");
            Iterator<ConfigDescriptionParameter> configI = configDescriptions.iterator();
            while (configI.hasNext()) {
                ConfigDescriptionParameter configParameter = configI.next();
                writer.write("      \"" + configParameter.getName() + "\":{\n");
                ConfigPropertyJSONParser.configPropertyToJSON(configParameter, writer);
                if (configI.hasNext())
                    writer.write("\n      },\n");
                else
                    writer.write("\n      }\n");
            }
            writer.write("    },\n");
        }
    }

    /**
     * This method is used for creation of {@link RuleTemplate}s from {@link JSONObject}s.
     *
     * @param jsonRuleTemplate is the JSON representation of {@link RuleTemplate}.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link RuleTemplate} creation.
     * @return a {@link RuleTemplate} created from json object.
     * @throws IllegalArgumentException is thrown when the json content is incorrect or insufficient.
     * @throws ClassNotFoundException
     */
    private RuleTemplate createRuleTemplate(JSONObject jsonRuleTemplate, List<ParsingNestedException> exceptions) {
        // verify json content
        Iterator<?> i = jsonRuleTemplate.keys();
        while (i.hasNext()) {
            String propertyName = (String) i.next();
            if (JSONUtility.checkTemplateProperties(propertyName) == -1)
                JSONUtility.catchParsingException(ParsingNestedException.TEMPLATE, null, exceptions,
                        new IllegalArgumentException(
                                "Unsupported property \"" + propertyName + "\" in rule template : " + jsonRuleTemplate),
                        log);
        }
        // get rule template UID
        String ruleTemplateUID = JSONUtility.getString(ParsingNestedException.TEMPLATE, null, exceptions,
                JSONStructureConstants.UID, false, jsonRuleTemplate, log);

        // create modules of rule template
        JSONArray sectionTrigers = JSONUtility.getJSONArray(ParsingNestedException.TEMPLATE, ruleTemplateUID,
                exceptions, JSONStructureConstants.ON, false, jsonRuleTemplate, log);
        List<Trigger> triggers = ModuleJSONParser.createTriggerModules(ParsingNestedException.TEMPLATE, ruleTemplateUID,
                JSONStructureConstants.ON, sectionTrigers, exceptions, log);

        JSONArray sectionConditions = JSONUtility.getJSONArray(ParsingNestedException.TEMPLATE, ruleTemplateUID,
                exceptions, JSONStructureConstants.IF, true, jsonRuleTemplate, log);
        List<Condition> conditions = ModuleJSONParser.createConditionModules(ParsingNestedException.TEMPLATE,
                ruleTemplateUID, JSONStructureConstants.IF, sectionConditions, exceptions, log);

        JSONArray sectionActions = JSONUtility.getJSONArray(ParsingNestedException.TEMPLATE, ruleTemplateUID,
                exceptions, JSONStructureConstants.THEN, false, jsonRuleTemplate, log);
        List<Action> actions = ModuleJSONParser.createActionModules(ParsingNestedException.TEMPLATE, ruleTemplateUID,
                JSONStructureConstants.THEN, sectionActions, exceptions, log);

        // get configuration description of rule template
        Set<ConfigDescriptionParameter> configDescriptions = null;
        JSONObject config = JSONUtility.getJSONObject(ParsingNestedException.TEMPLATE, ruleTemplateUID, exceptions,
                JSONStructureConstants.CONFIG, false, jsonRuleTemplate, log);
        if (config != null) {
            configDescriptions = new HashSet<ConfigDescriptionParameter>();
            Iterator<?> configI = config.keys();
            while (configI.hasNext()) {
                String configPropertyName = (String) configI.next();
                JSONObject configPropertyInfo = JSONUtility.getJSONObject(ParsingNestedException.TEMPLATE,
                        ruleTemplateUID, exceptions, configPropertyName, false, config, log);
                if (configPropertyInfo == null) {
                    continue;
                }
                ConfigDescriptionParameter configProperty = ConfigPropertyJSONParser.createConfigPropertyDescription(
                        ParsingNestedException.TEMPLATE, ruleTemplateUID, exceptions, configPropertyName,
                        configPropertyInfo, log);
                if (configProperty != null)
                    configDescriptions.add(configProperty);
            }
        }
        // get visibility of rule template
        Visibility visibility = getVisibility(ruleTemplateUID, jsonRuleTemplate, exceptions);

        // get tags of rule template
        Set<String> tags = getTags(ruleTemplateUID, jsonRuleTemplate, exceptions);

        // get description of rule template
        String description = JSONUtility.getString(ParsingNestedException.TEMPLATE, null, exceptions,
                JSONStructureConstants.DESCRIPTION, true, jsonRuleTemplate, log);

        // get label of rule template
        String label = JSONUtility.getString(ParsingNestedException.TEMPLATE, null, exceptions,
                JSONStructureConstants.LABEL, true, jsonRuleTemplate, log);

        // create rule template
        return new RuleTemplate(ruleTemplateUID, label, description, tags, triggers, conditions, actions,
                configDescriptions, visibility);
    }

    /**
     * This method parses Visibility of the RuleTemplate.
     *
     * @param UID is the unique identifier of the RuleTemplate.
     * @param json is the representation of the Visibility of RuleTemplate in json format.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link Visibility} creation.
     * @return parsed Visibility of the RuleTemplate.
     */
    private Visibility getVisibility(String UID, JSONObject json, List<ParsingNestedException> exceptions) {
        String visibility = JSONUtility.getString(ParsingNestedException.TEMPLATE, UID, exceptions,
                JSONStructureConstants.VISIBILITY, true, json, log);
        Visibility v = null;
        if (visibility == null) {
            v = Visibility.PUBLIC;
        } else {
            try {
                v = Visibility.valueOf(visibility.toUpperCase());
            } catch (IllegalArgumentException ie) {
                Throwable t = new Throwable("Incorrect value for property \"" + JSONStructureConstants.VISIBILITY
                        + "\" : \"" + json + "\".", ie);
                JSONUtility.catchParsingException(ParsingNestedException.TEMPLATE, UID, exceptions, t, log);
            }
        }
        return v;
    }

    /**
     * This method parses Tags of the RuleTemplate.
     *
     * @param UID is the unique identifier of the RuleTemplate.
     * @param json is the representation of the Tags of RuleTemplate in json format.
     * @param exceptions is a list used for collecting the exceptions occurred during tags creation.
     * @return parsed Tags of the RuleTemplate.
     */
    private Set<String> getTags(String UID, JSONObject json, List<ParsingNestedException> exceptions) {
        JSONArray jsonTags = JSONUtility.getJSONArray(ParsingNestedException.TEMPLATE, UID, exceptions,
                JSONStructureConstants.TAGS, true, json, log);
        Set<String> tags = null;
        if (jsonTags != null) {
            tags = new HashSet<String>();
            for (int j = 0; j < jsonTags.length(); j++) {
                String tag = JSONUtility.getString(ParsingNestedException.TEMPLATE, UID, exceptions,
                        JSONStructureConstants.TAGS, j, jsonTags, log);
                if (tag != null)
                    tags.add(tag);
            }
        }
        return tags;
    }

}