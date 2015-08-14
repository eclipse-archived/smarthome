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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.dto.ActionDTO;
import org.eclipse.smarthome.automation.dto.ConditionDTO;
import org.eclipse.smarthome.automation.dto.TriggerDTO;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template.Visibility;
import org.eclipse.smarthome.automation.template.dto.RuleTemplateDTO;
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
    public Set<Status> importData(InputStreamReader reader) {
        LinkedHashSet<Status> ruleTemplatesStatus = new LinkedHashSet<Status>();
        JSONTokener tokener = new JSONTokener(reader);
        try {
            Object jsonRuleTemplates = tokener.nextValue();
            if (jsonRuleTemplates != null) {
                if (jsonRuleTemplates instanceof JSONArray) {
                    for (int j = 0; j < ((JSONArray) jsonRuleTemplates).length(); j++) {
                        Status statusPerTemplate = new Status(log, Status.TEMPLATE, null);
                        JSONObject jsonRT = JSONUtility.getJSONObject(JSONStructureConstants.RULE_TEMPLATES, j,
                                (JSONArray) jsonRuleTemplates, statusPerTemplate);
                        if (jsonRT == null)
                            ruleTemplatesStatus.add(statusPerTemplate);
                        else
                            ruleTemplatesStatus.add(createRuleTemplate(jsonRT, statusPerTemplate));
                    }
                } else {
                    ruleTemplatesStatus.add(
                            createRuleTemplate((JSONObject) jsonRuleTemplates, new Status(log, Status.TEMPLATE, null)));
                }
            }
        } catch (JSONException e) {
            Status status = new Status(log, Status.TEMPLATE, null);
            status.error("JSON contains extra objects or lines.", e);
            ruleTemplatesStatus.add(status);
        }
        return ruleTemplatesStatus;
    }

    @Override
    public void exportData(Set<RuleTemplate> dataObjects, OutputStreamWriter writer) throws IOException {
        try {
            writeRuleTemplates(dataObjects, writer);
        } catch (JSONException e) {
            throw new IOException("Export failed: " + e.toString());
        }
    }

    /**
     *
     * @param ruleTemplates
     * @param writer
     * @throws JSONException
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
     * @param writer
     * @return JSONObject is an object representing the {@link RuleTemplate} in json format.
     * @throws JSONException is thrown when the parameter for the constructor of the JSON not amiss.
     * @throws IOException
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

        List<Trigger> triggers = ruleTemplate.getModules(Trigger.class);
        List<Condition> conditions = ruleTemplate.getModules(Condition.class);
        List<Action> actions = ruleTemplate.getModules(Action.class);

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
     * @param writer
     * @throws IOException
     * @throws JSONException
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
     * @return a {@link RuleTemplate} created from json object.
     * @throws IllegalArgumentException is thrown when the json content is incorrect or insufficient.
     * @throws ClassNotFoundException
     */
    private Status createRuleTemplate(JSONObject jsonRuleTemplate, Status status) {
        // verify json content
        Iterator<?> i = jsonRuleTemplate.keys();
        while (i.hasNext()) {
            String propertyName = (String) i.next();
            if (JSONUtility.checkTemplateProperties(propertyName) == -1)
                status.error("Unsupported property \"" + propertyName + "\" in rule template : " + jsonRuleTemplate,
                        new IllegalArgumentException());
        }
        // create rule template
        RuleTemplateDTO template = new RuleTemplateDTO();
        // get rule template UID
        String ruleTemplateUID = JSONUtility.getString(JSONStructureConstants.UID, false, jsonRuleTemplate, status);
        if (ruleTemplateUID == null)
            return status;
        template.uid = ruleTemplateUID;
        status.init(Status.TEMPLATE, ruleTemplateUID);
        // create modules of rule template
        List<TriggerDTO> triggers = new ArrayList<TriggerDTO>();
        List<ConditionDTO> conditions = new ArrayList<ConditionDTO>();
        List<ActionDTO> actions = new ArrayList<ActionDTO>();
        JSONArray sectionTrigers = JSONUtility.getJSONArray(JSONStructureConstants.ON, false, jsonRuleTemplate, status);
        if (sectionTrigers == null)
            return status;
        JSONArray sectionConditions = JSONUtility.getJSONArray(JSONStructureConstants.IF, true, jsonRuleTemplate,
                status);
        JSONArray sectionActions = JSONUtility.getJSONArray(JSONStructureConstants.THEN, false, jsonRuleTemplate,
                status);
        if (sectionActions == null)
            return status;
        if (!ModuleJSONParser.createTrigerModules(status, triggers, sectionTrigers))
            return status;
        template.triggers = triggers;
        if (ModuleJSONParser.createConditionModules(status, conditions, sectionConditions))
            template.conditions = conditions;
        if (!ModuleJSONParser.createActionModules(status, actions, sectionActions))
            return status;
        template.actions = actions;
        // get configuration description of rule template
        LinkedHashSet<ConfigDescriptionParameter> configDescriptions = null;
        JSONObject config = JSONUtility.getJSONObject(JSONStructureConstants.CONFIG, false, jsonRuleTemplate, status);
        if (config != null) {
            configDescriptions = new LinkedHashSet<ConfigDescriptionParameter>();
            boolean fail = false;
            Iterator<?> configI = config.keys();
            while (configI.hasNext()) {
                String configPropertyName = (String) configI.next();
                JSONObject configPropertyInfo = JSONUtility.getJSONObject(configPropertyName, false, config, status);
                if (configPropertyInfo == null) {
                    fail = true;
                    continue;
                }
                ConfigDescriptionParameter configProperty = ConfigPropertyJSONParser
                        .createConfigPropertyDescription(configPropertyName, configPropertyInfo, status);
                if (configProperty != null)
                    configDescriptions.add(configProperty);
                else
                    fail = true;
            }
            if (fail)
                return status;
            template.configDescriptions = configDescriptions;
        }
        // get visibility of rule template
        String visibilityString = JSONUtility.getString(JSONStructureConstants.VISIBILITY, true, jsonRuleTemplate,
                status);
        Visibility visibility;
        if (visibilityString == null) {
            visibility = Visibility.PUBLIC;
        } else {
            try {
                visibility = Visibility.valueOf(visibilityString.toUpperCase());
            } catch (IllegalArgumentException ie) {
                status.error("Incorrect value for property \"" + JSONStructureConstants.VISIBILITY + "\" : \""
                        + visibilityString + "\".", ie);
                return status;
            }
        }
        template.visibility = visibility;
        // get tags of rule template
        JSONArray jsonTags = JSONUtility.getJSONArray(JSONStructureConstants.TAGS, true, jsonRuleTemplate, status);
        Set<String> tags = null;
        if (jsonTags != null) {
            tags = new LinkedHashSet<String>();
            for (int j = 0; j < jsonTags.length(); j++) {
                String tag = JSONUtility.getString(JSONStructureConstants.TAGS, j, jsonTags, status);
                if (tag != null)
                    tags.add(tag);
            }
            template.tags = tags;
        }
        // get description of rule template
        String description = JSONUtility.getString(JSONStructureConstants.DESCRIPTION, true, jsonRuleTemplate, status);
        template.description = description;
        // get label of rule template
        String label = JSONUtility.getString(JSONStructureConstants.LABEL, true, jsonRuleTemplate, status);
        template.label = label;
        status.success(template);
        return status;
    }

}