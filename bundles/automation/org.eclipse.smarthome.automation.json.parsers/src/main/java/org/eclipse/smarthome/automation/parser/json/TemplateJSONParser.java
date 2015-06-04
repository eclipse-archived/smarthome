/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.parser.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.parser.Parser;
import org.eclipse.smarthome.automation.handler.parser.Status;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template.Visibility;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class serves for loading JSON files and parse it to the Rule Template objects.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class TemplateJSONParser implements Parser {

    private BundleContext bc;
    private AutomationFactory automationFactory;
    private TemplateManager templateManager;
    private ModuleTypeManager moduleManager;
    private Logger log;

    public TemplateJSONParser(BundleContext bc, ModuleTypeManager moduleManager, TemplateManager templateManager,
            AutomationFactory automationFactory) {
        this.bc = bc;
        this.moduleManager = moduleManager;
        this.templateManager = templateManager;
        this.automationFactory = automationFactory;
        this.log = LoggerFactory.getLogger(TemplateJSONParser.class);
    }

    /**
     * @see org.eclipse.smarthome.automation.handler.parser.Parser#importData(InputStreamReader)
     */
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
                    ruleTemplatesStatus.add(createRuleTemplate((JSONObject) jsonRuleTemplates, new Status(log,
                            Status.TEMPLATE, null)));
                }
            }
        } catch (JSONException e) {
            Status status = new Status(log, Status.TEMPLATE, null);
            status.error("JSON contains extra objects or lines.", e);
            ruleTemplatesStatus.add(status);
        }
        return ruleTemplatesStatus;
    }

    /**
     * @see org.eclipse.smarthome.automation.handler.parser.Parser#exportData(Set, OutputStreamWriter)
     */
    @Override
    public void exportData(Set<?> dataObjects, OutputStreamWriter writer) throws IOException {
        try {
            writeRuleTemplates(dataObjects, writer);
        } catch (JSONException e) {
            throw new IOException("Export failed: " + e.toString());
        }
    }

    /**
     * @throws JSONException
     * @see org.eclipse.smarthome.automation.handler.parser.TemplateParser#writeRuleTemplates(org.eclipse.smarthome.automation.template.RuleTemplate,
     *      java.io.OutputStreamWriter)
     */
    private void writeRuleTemplates(Collection<?> ruleTemplates, OutputStreamWriter writer) throws JSONException {
        try {
            writer.write("[\n");
            Iterator<?> i = ruleTemplates.iterator();
            while (i.hasNext()) {
                RuleTemplate ruleTemplate = (RuleTemplate) i.next();
                ruleTepmlateToJSON(ruleTemplate, writer);
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
    private void ruleTepmlateToJSON(RuleTemplate ruleTemplate, OutputStreamWriter writer) throws JSONException,
            IOException {
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
        writer.write("    \"" + JSONStructureConstants.VISIBILITY + "\":\"" + visibility.toString().toLowerCase()
                + "\",\n");

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
        ModuleJSONParser.writeModules(moduleManager, triggers, writer);
        writer.write("    ],\n");

        if (conditions != null && !conditions.isEmpty()) {
            writer.write("    \"" + JSONStructureConstants.IF + "\":[\n");
            ModuleJSONParser.writeModules(moduleManager, conditions, writer);
            writer.write("    ],\n");
        }

        writer.write("    \"" + JSONStructureConstants.THEN + "\":[\n");
        ModuleJSONParser.writeModules(moduleManager, actions, writer);
        writer.write("    ]\n  }");
    }

    /**
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
        // get rule template UID
        String ruleTemplateUID = JSONUtility.getString(JSONStructureConstants.UID, false, jsonRuleTemplate, status);
        if (ruleTemplateUID == null)
            return status;
        status.init(Status.TEMPLATE, ruleTemplateUID);
        if (templateManager.getTemplate(ruleTemplateUID) != null) {
            status.error("Template with UID \"" + ruleTemplateUID + "\" already exists! Creation failed!",
                    new IllegalArgumentException());
            return status;
        }
        // create modules of rule template
        List<Trigger> triggers = new ArrayList<Trigger>();
        List<Condition> conditions = new ArrayList<Condition>();
        List<Action> actions = new ArrayList<Action>();
        JSONArray sectionTrigers = JSONUtility.getJSONArray(JSONStructureConstants.ON, false, jsonRuleTemplate, status);
        if (sectionTrigers == null)
            return status;
        JSONArray sectionConditions = JSONUtility.getJSONArray(JSONStructureConstants.IF, true, jsonRuleTemplate,
                status);
        JSONArray sectionActions = JSONUtility.getJSONArray(JSONStructureConstants.THEN, false, jsonRuleTemplate,
                status);
        if (sectionActions == null)
            return status;
        if (!ModuleJSONParser.createTrigerModules(status, moduleManager, automationFactory, null, triggers,
                sectionTrigers))
            return status;
        ModuleJSONParser.createConditionModules(status, moduleManager, automationFactory, null, conditions,
                sectionConditions);
        if (!ModuleJSONParser.createActionModules(status, moduleManager, automationFactory, null, actions,
                sectionActions))
            return status;
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
                ConfigDescriptionParameter configProperty = ConfigPropertyJSONParser.createConfigPropertyDescription(
                        configPropertyName, configPropertyInfo, status);
                if (configProperty != null)
                    configDescriptions.add(configProperty);
                else
                    fail = true;
            }
            if (fail)
                return status;
        }
        // get visibility of rule template
        String visibilityString = JSONUtility.getString(JSONStructureConstants.VISIBILITY, true, jsonRuleTemplate,
                status);
        Visibility visibility = null;
        if (visibilityString == null || visibilityString.equalsIgnoreCase(Visibility.PUBLIC.toString()))
            visibility = Visibility.PUBLIC;
        else if (visibilityString.equalsIgnoreCase(Visibility.PRIVATE.toString()))
            visibility = Visibility.PRIVATE;
        else {
            status.error("Incorrect value for property \"" + JSONStructureConstants.VISIBILITY + "\" : \"" + visibility
                    + "\".", new IllegalArgumentException());
            return status;
        }
        // create rule template
        RuleTemplate template = new RuleTemplate(ruleTemplateUID, triggers, conditions, actions, configDescriptions,
                visibility);
        if (ModuleJSONParser.validateConnections(bc, moduleManager, template, status)) {
            // get tags of rule template
            JSONArray jsonTags = JSONUtility.getJSONArray(JSONStructureConstants.TAGS, true, jsonRuleTemplate, status);
            if (jsonTags != null) {
                Set<String> tags = new LinkedHashSet<String>();
                for (int j = 0; j < jsonTags.length(); j++) {
                    String tag = JSONUtility.getString(JSONStructureConstants.TAGS, j, jsonTags, status);
                    if (tag != null)
                        tags.add(tag);
                }
                template.setTags(tags);
            }
            // get description of rule template
            String description = JSONUtility.getString(JSONStructureConstants.DESCRIPTION, true, jsonRuleTemplate,
                    status);
            template.setDescription(description);
            status.success(template);
        }
        return status;
    }
}
