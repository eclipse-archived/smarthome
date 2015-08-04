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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleType.Visibility;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This class serves for loading JSON files and parse it to the Module Type objects.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class ModuleTypeJSONParser implements Parser {

    private BundleContext bc;
    private AutomationFactory automationFactory;
    private Logger log;

    public ModuleTypeJSONParser(BundleContext bc, AutomationFactory automationFactory) {
        this.bc = bc;
        this.automationFactory = automationFactory;
        this.log = LoggerFactory.getLogger(ModuleTypeJSONParser.class);
    }

    /**
     * @see org.eclipse.smarthome.automation.parser.Parser#importData(InputStreamReader)
     */
    public Set<Status> importData(InputStreamReader reader) {
        LinkedHashSet<Status> moduleTypesStatus = new LinkedHashSet<Status>();
        JSONTokener tokener = new JSONTokener(reader);
        Status status = new Status(this.log, Status.MODULE_TYPE, null);
        try {
            JSONObject json = (JSONObject) tokener.nextValue();
            Iterator<?> i = json.keys();
            while (i.hasNext()) {
                String moduleType = (String) i.next();
                int type = JSONUtility.checkModuleTypeProperties(moduleType);
                if (type == -1) {
                    status.error("Unsupported Automation Type \"" + moduleType + "\"! Expected \""
                            + JSONStructureConstants.TRIGGERS + "\" or \"" + JSONStructureConstants.CONDITIONS
                            + "\" or \"" + JSONStructureConstants.ACTIONS + "\" or \""
                            + JSONStructureConstants.COMPOSITE + "\".", new IllegalArgumentException());
                }
            }
            createModuleTypes(JSONUtility.TRIGGERS,
                    JSONUtility.getJSONObject(JSONStructureConstants.TRIGGERS, true, json, status), moduleTypesStatus);
            createModuleTypes(JSONUtility.CONDITIONS,
                    JSONUtility.getJSONObject(JSONStructureConstants.CONDITIONS, true, json, status), moduleTypesStatus);
            createModuleTypes(JSONUtility.ACTIONS,
                    JSONUtility.getJSONObject(JSONStructureConstants.ACTIONS, true, json, status), moduleTypesStatus);
            createModuleTypes(JSONUtility.COMPOSITE,
                    JSONUtility.getJSONObject(JSONStructureConstants.COMPOSITE, true, json, status), moduleTypesStatus);
            if (status.hasErrors()) {
                moduleTypesStatus.add(status);
            }

        } catch (JSONException e) {
            status.error("JSON contains extra objects or lines.", e);
            moduleTypesStatus.add(status);
        }
        return moduleTypesStatus;
    }

    /**
     * @throws IOException
     * @see org.eclipse.smarthome.automation.parser.Parser#exportData(Set, OutputStreamWriter)
     */
    public void exportData(Set<?> dataObjects, OutputStreamWriter writer) throws IOException {
        try {
            writer.write("{\n");

            Map<String, TriggerType> triggers = new HashMap<String, TriggerType>();
            Map<String, ConditionType> conditions = new HashMap<String, ConditionType>();
            Map<String, ActionType> actions = new HashMap<String, ActionType>();
            Map<String, ModuleType> composites = new HashMap<String, ModuleType>();

            sortModuleTypesByTypes(dataObjects, triggers, conditions, actions, composites);

            TriggerTypeJSONParser.writeTriggerTypes(triggers, writer);
            ConditionTypeJSONParser.writeConditionTypes(conditions, triggers, writer);

            ActionTypeJSONParser.writeActionTypes(actions, conditions, triggers, writer);
            writeCompositeTypes(composites, actions, conditions, triggers, writer);

            writer.write("\n}");
        } catch (JSONException e) {
            throw new IOException("Export failed: " + e.toString());
        }
    }

    /**
     * @see org.eclipse.smarthome.automation.parser.ModuleTypeParser#writeModuleTypes(org.eclipse.smarthome.automation.type.ModuleType,
     *      java.io.OutputStreamWriter)
     */
    private void sortModuleTypesByTypes(Set<?> moduleTypes, Map<String, TriggerType> triggers,
            Map<String, ConditionType> conditions, Map<String, ActionType> actions, Map<String, ModuleType> composites) {
        Iterator<?> i = moduleTypes.iterator();
        while (i.hasNext()) {
            ModuleType moduleType = (ModuleType) i.next();
            if (moduleType.getClass().getName().equals(CompositeTriggerType.class.getName())) {
                composites.put(moduleType.getUID(), moduleType);
                continue;
            }
            if (moduleType.getClass().getName().equals(TriggerType.class.getName())) {
                triggers.put(moduleType.getUID(), (TriggerType) moduleType);
                continue;
            }
            if (moduleType.getClass().getName().equals(CompositeConditionType.class.getName())) {
                composites.put(moduleType.getUID(), moduleType);
                continue;
            }
            if (moduleType.getClass().getName().equals(ConditionType.class.getName())) {
                conditions.put(moduleType.getUID(), (ConditionType) moduleType);
                continue;
            }
            if (moduleType.getClass().getName().equals(CompositeActionType.class.getName())) {
                composites.put(moduleType.getUID(), moduleType);
                continue;
            }
            if (moduleType.getClass().getName().equals(ActionType.class.getName())) {
                actions.put(moduleType.getUID(), (ActionType) moduleType);
            }
        }
    }

    /**
     * @param composites
     * @param actions
     * @param conditions
     * @param triggers
     * @param writer
     * @throws IOException
     * @throws JSONException
     */
    private void writeCompositeTypes(Map<String, ModuleType> composites, Map<String, ActionType> actions,
            Map<String, ConditionType> conditions, Map<String, TriggerType> triggers, OutputStreamWriter writer)
            throws IOException, JSONException {
        if (!composites.isEmpty()) {
            if (triggers.isEmpty() && conditions.isEmpty() && actions.isEmpty())
                writer.write(" " + JSONStructureConstants.COMPOSITE + ":{\n");
            else
                writer.write(",\n " + JSONStructureConstants.COMPOSITE + ":{\n");
            Iterator<String> compositesI = composites.keySet().iterator();
            while (compositesI.hasNext()) {
                String compositeUID = compositesI.next();
                writer.write("  \"" + compositeUID + "\":{\n");
                Object composite = composites.get(compositeUID);
                if (composite instanceof CompositeTriggerType)
                    TriggerTypeJSONParser.compositeTriggerTypeToJSON((CompositeTriggerType) composite, writer);
                if (composite instanceof CompositeConditionType)
                    ConditionTypeJSONParser.compositeConditionTypeToJSON((CompositeConditionType) composite, writer);
                if (composite instanceof CompositeActionType)
                    ActionTypeJSONParser.compositeActionTypeToJSON((CompositeActionType) composite, writer);
                if (compositesI.hasNext())
                    writer.write("\n  },\n");
                else
                    writer.write("\n  }\n");
            }
            writer.write(" }");
        }
    }

    /**
     * This method is used for creation of Module Types : {@link TriggerType}s, {@link ConditionType}s,
     * {@link ActionType}s, {@link CompositeTriggerType}s, {@link CompositeConditionType}s or
     * {@link CompositeActionType}s.
     *
     * @param jsonModuleTypes JSONObject representing the module types.
     * @param moduleTypesStatus
     * @return a set of {@link ModuleType}s created form json objects.
     */
    private void createModuleTypes(int type, JSONObject jsonModuleTypes, LinkedHashSet<Status> moduleTypesStatus) {
        if (jsonModuleTypes == null) {
            return;
        }
        Iterator<?> jsonModulesIds = jsonModuleTypes.keys();
        while (jsonModulesIds.hasNext()) {
            String moduleTypeUID = (String) jsonModulesIds.next();
            Status status = new Status(this.log, Status.MODULE_TYPE, moduleTypeUID);
            ModuleType moduleType = null;
            JSONObject jsonModuleType = JSONUtility.getJSONObject(moduleTypeUID, false, jsonModuleTypes, status);
            if (jsonModuleType == null) {
                moduleTypesStatus.add(status);
                continue;
            }
            LinkedHashSet<ConfigDescriptionParameter> configDescriptions = ConfigPropertyJSONParser
                    .initializeConfigDescriptions(jsonModuleType, status);
            if (configDescriptions == null) {
                moduleTypesStatus.add(status);
                continue;
            }
            String visibility = JSONUtility.getString(JSONStructureConstants.VISIBILITY, true, jsonModuleType, status);
            Visibility v;
            if (visibility == null) {
                v = Visibility.PUBLIC;
            } else {
                try {
                    v = Visibility.valueOf(visibility.toUpperCase());
                } catch (IllegalArgumentException ie) {
                    status.error("Incorrect value for property \"" + JSONStructureConstants.VISIBILITY + "\" : \""
                            + visibility + "\".", ie);
                    continue;
                }
            }

            JSONArray jsonTags = JSONUtility.getJSONArray(JSONStructureConstants.TAGS, true, jsonModuleType, status);
            Set<String> tags = null;
            if (jsonTags != null) {
                tags = new HashSet<String>();
                for (int j = 0; j < jsonTags.length(); j++) {
                    String tag = JSONUtility.getString(JSONStructureConstants.TAGS, j, jsonTags, status);
                    if (tag != null)
                        tags.add(tag);
                }
            }
            String label = JSONUtility.getString(JSONStructureConstants.LABEL, true, jsonModuleType, status);
            String description = JSONUtility
                    .getString(JSONStructureConstants.DESCRIPTION, true, jsonModuleType, status);
            LinkedHashSet<Input> inputs = new LinkedHashSet<Input>();
            LinkedHashSet<Output> outputs = new LinkedHashSet<Output>();
            switch (type) {
                case JSONUtility.TRIGGERS:
                    JSONObject jsonTriggerOutputs = JSONUtility.getJSONObject(JSONStructureConstants.OUTPUT, true,
                            jsonModuleType, status);
                    if (jsonTriggerOutputs != null) {
                        if (OutputJSONParser.collectOutputs(bc, jsonTriggerOutputs, outputs, status))
                            moduleType = new TriggerType(moduleTypeUID, configDescriptions, label, description, tags,
                                    v, outputs);
                    } else
                        moduleType = new TriggerType(moduleTypeUID, configDescriptions, label, description, tags, v,
                                null);
                    break;
                case JSONUtility.CONDITIONS:
                    JSONObject jsonConditionInputs = JSONUtility.getJSONObject(JSONStructureConstants.INPUT, true,
                            jsonModuleType, status);
                    if (jsonConditionInputs != null) {
                        if (InputJSONParser.collectInputs(bc, jsonConditionInputs, inputs, status))
                            moduleType = new ConditionType(moduleTypeUID, configDescriptions, label, description, tags,
                                    v, inputs);
                    } else
                        moduleType = new ConditionType(moduleTypeUID, configDescriptions, label, description, tags, v,
                                null);
                    break;
                case JSONUtility.ACTIONS:
                    JSONObject jsonActionInputs = JSONUtility.getJSONObject(JSONStructureConstants.INPUT, true,
                            jsonModuleType, status);
                    if (jsonActionInputs != null) {
                        if (InputJSONParser.collectInputs(bc, jsonActionInputs, inputs, status)) {
                            JSONObject jsonActionOutputs = JSONUtility.getJSONObject(JSONStructureConstants.OUTPUT,
                                    true, jsonModuleType, status);
                            if (jsonActionOutputs != null) {
                                if (OutputJSONParser.collectOutputs(bc, jsonActionOutputs, outputs, status))
                                    moduleType = new ActionType(moduleTypeUID, configDescriptions, label, description,
                                            tags, v, inputs, outputs);
                            } else
                                moduleType = new ActionType(moduleTypeUID, configDescriptions, label, description,
                                        tags, v, inputs, null);
                        }
                    } else
                        moduleType = new ActionType(moduleTypeUID, configDescriptions, null);
                    break;
                case JSONUtility.COMPOSITE:
                    moduleType = createCompositeTypes(moduleTypeUID, configDescriptions, jsonModuleType,
                            moduleTypesStatus, status, label, description, tags, v);
                    break;
            }
            if (moduleType != null) {
                status.success(moduleType);
            }
            moduleTypesStatus.add(status);
        }
    }

    /**
     * @param moduleTypeUID
     * @param configDescriptions
     * @param moduleType
     * @param jsonModuleType
     * @param moduleTypesStatus
     * @param v
     * @param tags
     * @param description
     * @param label
     */
    private ModuleType createCompositeTypes(String moduleTypeUID,
            LinkedHashSet<ConfigDescriptionParameter> configDescriptions, JSONObject jsonModuleType,
            LinkedHashSet<Status> moduleTypesStatus, Status status, String label, String description, Set<String> tags,
            Visibility v) {
        Set<Input> inputs = null;
        Set<Output> outputs = null;
        JSONArray jsonTriggers = JSONUtility
                .getJSONArray(JSONStructureConstants.TRIGGERS, true, jsonModuleType, status);
        if (jsonTriggers != null) {
            JSONObject jsonTriggerOutputs = JSONUtility.getJSONObject(JSONStructureConstants.OUTPUT, true,
                    jsonModuleType, status);
            if (jsonTriggerOutputs != null) {
                outputs = new LinkedHashSet<Output>();
                if (!OutputJSONParser.collectOutputs(bc, jsonTriggerOutputs, outputs, status))
                    return null;
            }
            List<Trigger> triggerModules = new ArrayList<Trigger>();
            if (ModuleJSONParser.createTrigerModules(status, automationFactory, moduleTypesStatus,
                    triggerModules, jsonTriggers))
                return new CompositeTriggerType(moduleTypeUID, configDescriptions, label, description, tags, v,
                        outputs, triggerModules);
            return null;
        }
        JSONArray jsonConditions = JSONUtility.getJSONArray(JSONStructureConstants.CONDITIONS, true, jsonModuleType,
                status);
        if (jsonConditions != null) {
            JSONObject jsonConditionInputs = JSONUtility.getJSONObject(JSONStructureConstants.INPUT, true,
                    jsonModuleType, status);
            if (jsonConditionInputs != null) {
                inputs = new LinkedHashSet<Input>();
                if (!InputJSONParser.collectInputs(bc, jsonConditionInputs, inputs, status))
                    return null;
            }
            List<Condition> conditionModules = new ArrayList<Condition>();
            if (ModuleJSONParser.createConditionModules(status, automationFactory, moduleTypesStatus,
                    conditionModules, jsonConditions))
                return new CompositeConditionType(moduleTypeUID, configDescriptions, label, description, tags, v,
                        inputs, conditionModules);
            return null;
        }
        JSONArray jsonActions = JSONUtility.getJSONArray(JSONStructureConstants.ACTIONS, true, jsonModuleType, status);
        if (jsonActions == null) {
            status.error("At least one property of \"triggers\", \"conditions\" or \"actions\" must be present!",
                    new IllegalArgumentException());
            return null;
        }
        JSONObject jsonActionInputs = JSONUtility.getJSONObject(JSONStructureConstants.INPUT, true, jsonModuleType,
                status);
        if (jsonActionInputs != null) {
            inputs = new LinkedHashSet<Input>();
            if (!InputJSONParser.collectInputs(bc, jsonActionInputs, inputs, status))
                return null;
        }
        JSONObject jsonActionOutputs = JSONUtility.getJSONObject(JSONStructureConstants.OUTPUT, true, jsonModuleType,
                status);
        if (jsonActionOutputs != null) {
            outputs = new LinkedHashSet<Output>();
            if (!OutputJSONParser.collectOutputs(bc, jsonActionOutputs, outputs, status))
                return null;
        }
        List<Action> actionModules = new ArrayList<Action>();
        if (ModuleJSONParser.createActionModules(status, automationFactory, moduleTypesStatus,
                actionModules, jsonActions))
            return new CompositeActionType(moduleTypeUID, configDescriptions, label, description, tags, v, inputs,
                    outputs, actionModules);
        return null;
    }

    /**
     * This method is used for reversion of {@link ModuleType} to JSON format.
     *
     * @param moduleType is a {@link ModuleType} object to revert.
     * @param writer
     * @throws IOException
     * @throws JSONException
     */
    static void moduleTypeToJSON(ModuleType moduleType, OutputStreamWriter writer) throws IOException, JSONException {
        String label = moduleType.getLabel();
        if (label != null)
            writer.write("    \"" + JSONStructureConstants.LABEL + "\":\"" + label + "\",\n");

        String description = moduleType.getLabel();
        if (description != null)
            writer.write("    \"" + JSONStructureConstants.DESCRIPTION + "\":\"" + description + "\",\n");

        Visibility visibility = moduleType.getVisibility();
        writer.write("    \"" + JSONStructureConstants.VISIBILITY + "\":\"" + visibility.toString().toLowerCase()
                + "\",\n");

        Set<String> tags = moduleType.getTags();
        if (tags != null) {
            writer.write("    \"" + JSONStructureConstants.TAGS + "\":");
            new JSONArray(tags).write(writer);
            writer.write(",\n");
        }

        Set<ConfigDescriptionParameter> configDescriptions = moduleType.getConfigurationDescription();
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
            writer.write("    }");
        }
    }

}
