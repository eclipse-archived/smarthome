/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.internal.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.ParsingException;
import org.eclipse.smarthome.automation.parser.ParsingNestedException;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class serves for loading JSON files and parse it to the Module Type objects.
 *
 * @author Ana Dimova - Initial Contribution
 * @author Ana Dimova - refactor Parser interface.
 *
 */
public class ModuleTypeJSONParser implements Parser<ModuleType> {

    private BundleContext bc;
    private Logger log;

    /**
     * Constructs the ModuleTypeJSONParser
     *
     * @param bc bundleContext
     */
    public ModuleTypeJSONParser(BundleContext bc) {
        this.bc = bc;
        this.log = LoggerFactory.getLogger(ModuleTypeJSONParser.class);
    }

    @Override
    public Set<ModuleType> parse(InputStreamReader reader) throws ParsingException {
        Set<ModuleType> moduleTypes = new HashSet<ModuleType>();
        JSONTokener tokener = new JSONTokener(reader);
        List<ParsingNestedException> exceptions = new ArrayList<>();
        try {
            JSONObject json = (JSONObject) tokener.nextValue();
            Iterator<?> i = json.keys();
            while (i.hasNext()) {
                String moduleTypeProperty = (String) i.next();
                int type = JSONUtility.checkModuleTypeProperties(moduleTypeProperty);
                if (type == -1) {
                    JSONUtility.catchParsingException(ParsingNestedException.MODULE_TYPE, null, exceptions,
                            new IllegalArgumentException("Unsupported Automation Module Type Property \""
                                    + moduleTypeProperty + "\"! Expected \"" + JSONStructureConstants.TRIGGERS
                                    + "\" or \"" + JSONStructureConstants.CONDITIONS + "\" or \""
                                    + JSONStructureConstants.ACTIONS + "\"."),
                            log);
                }
            }
            JSONObject jsonTRIGGERS = JSONUtility.getJSONObject(ParsingNestedException.MODULE_TYPE, null, exceptions,
                    JSONStructureConstants.TRIGGERS, true, json, log);
            if (jsonTRIGGERS != null)
                createModuleTypes(JSONUtility.TRIGGERS, jsonTRIGGERS, moduleTypes, exceptions);

            JSONObject jsonCONDITIONS = JSONUtility.getJSONObject(ParsingNestedException.MODULE_TYPE, null, exceptions,
                    JSONStructureConstants.CONDITIONS, true, json, log);
            if (jsonCONDITIONS != null)
                createModuleTypes(JSONUtility.CONDITIONS, jsonCONDITIONS, moduleTypes, exceptions);

            JSONObject jsonACTIONS = JSONUtility.getJSONObject(ParsingNestedException.MODULE_TYPE, null, exceptions,
                    JSONStructureConstants.ACTIONS, true, json, log);
            if (jsonACTIONS != null)
                createModuleTypes(JSONUtility.ACTIONS, jsonACTIONS, moduleTypes, exceptions);

        } catch (JSONException e) {
            JSONUtility.catchParsingException(ParsingNestedException.MODULE_TYPE, null, exceptions,
                    new IllegalArgumentException("JSON contains extra objects or lines.", e), log);
        }
        if (exceptions.isEmpty())
            return moduleTypes;
        throw new ParsingException(exceptions);
    }

    @Override
    public void serialize(Set<ModuleType> dataObjects, OutputStreamWriter writer) throws Exception {
        try {
            writer.write("{\n");

            Map<String, TriggerType> triggers = new HashMap<String, TriggerType>();
            Map<String, ConditionType> conditions = new HashMap<String, ConditionType>();
            Map<String, ActionType> actions = new HashMap<String, ActionType>();

            sortModuleTypesByTypes(dataObjects, triggers, conditions, actions);

            TriggerTypeJSONParser.writeTriggerTypes(triggers, writer);
            ConditionTypeJSONParser.writeConditionTypes(conditions, triggers, writer);
            ActionTypeJSONParser.writeActionTypes(actions, conditions, triggers, writer);

            writer.write("\n}");
        } catch (JSONException e) {
            throw new Exception("Export failed: " + e.toString());
        }
    }

    /**
     * Utility method for sorting the module types by class types - {@link TriggerType}, {@link ConditionType},
     * {@link ActionType}.
     *
     * @param moduleTypes the set of module types for sorting.
     * @param triggers is a map fulfilled with {@link TriggerType} objects.
     * @param conditions is a map fulfilled with {@link ConditionType} objects.
     * @param actions is a map fulfilled with {@link ActionType} objects.
     */
    private void sortModuleTypesByTypes(Set<ModuleType> moduleTypes, Map<String, TriggerType> triggers,
            Map<String, ConditionType> conditions, Map<String, ActionType> actions) {
        Iterator<?> i = moduleTypes.iterator();
        while (i.hasNext()) {
            ModuleType moduleType = (ModuleType) i.next();
            if (moduleType instanceof TriggerType) {
                triggers.put(moduleType.getUID(), (TriggerType) moduleType);
                continue;
            }
            if (moduleType instanceof ConditionType) {
                conditions.put(moduleType.getUID(), (ConditionType) moduleType);
                continue;
            }
            if (moduleType instanceof ActionType) {
                actions.put(moduleType.getUID(), (ActionType) moduleType);
            }
        }
    }

    /**
     * This method is used for creation of Module Types : {@link TriggerType}s, {@link ConditionType}s,
     * {@link ActionType}s, {@link CompositeTriggerType}s, {@link CompositeConditionType}s or
     * {@link CompositeActionType}s.
     *
     * @param jsonModuleTypes is a JSONObject representing the module types.
     * @param moduleTypes is a set fulfilled with {@link ModuleType} objects created form json objects.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link ModuleType}s creation.
     */
    private void createModuleTypes(int type, JSONObject jsonModuleTypes, Set<ModuleType> moduleTypes,
            List<ParsingNestedException> exceptions) {
        if (jsonModuleTypes == null) {
            return;
        }
        Iterator<?> jsonModulesIds = jsonModuleTypes.keys();
        while (jsonModulesIds.hasNext()) {
            String moduleTypeUID = (String) jsonModulesIds.next();
            ModuleType moduleType = null;
            JSONObject jsonModuleType = JSONUtility.getJSONObject(ParsingNestedException.MODULE_TYPE, moduleTypeUID,
                    exceptions, moduleTypeUID, false, jsonModuleTypes, log);
            switch (type) {
                case JSONUtility.TRIGGERS:
                    moduleType = createTriggerType(moduleTypeUID, jsonModuleType, exceptions);
                    break;
                case JSONUtility.CONDITIONS:
                    moduleType = createConditionType(moduleTypeUID, jsonModuleType, exceptions);
                    break;
                case JSONUtility.ACTIONS:
                    moduleType = createActionType(moduleTypeUID, jsonModuleType, exceptions);
                    break;
            }
            moduleTypes.add(moduleType);
        }
    }

    /**
     * This method is used for creation of ActionType.
     *
     * @param moduleTypeUID is the unique identifier of the ActionType.
     * @param jsonModuleType is a JSONObject representing the action type.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link ActionType}s creation.
     * @return the newly created ActionType.
     */
    private ActionType createActionType(String moduleTypeUID, JSONObject jsonModuleType,
            List<ParsingNestedException> exceptions) {

        String label = JSONUtility.getString(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.LABEL, true, jsonModuleType, log);
        String description = JSONUtility.getString(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.DESCRIPTION, true, jsonModuleType, log);

        Set<ConfigDescriptionParameter> configDescriptions = ConfigPropertyJSONParser.initializeConfigDescriptions(
                ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions, jsonModuleType, log);

        Visibility v = null;
        v = getVisibility(moduleTypeUID, jsonModuleType, exceptions);

        Set<String> tags = null;
        tags = getTags(moduleTypeUID, jsonModuleType, exceptions);

        Set<Input> inputs = getInputs(moduleTypeUID, jsonModuleType, exceptions);
        Set<Output> outputs = getOutputs(moduleTypeUID, jsonModuleType, exceptions);

        JSONArray jsonActions = JSONUtility.getJSONArray(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.ACTIONS, true, jsonModuleType, log);
        List<Action> actionModules = null;
        if (jsonActions != null) {
            actionModules = ModuleJSONParser.createActionModules(ParsingNestedException.MODULE_TYPE, moduleTypeUID,
                    JSONStructureConstants.ACTIONS, jsonActions, exceptions, log);
        }
        ActionType actionType = null;
        if (actionModules != null) {
            actionType = new CompositeActionType(moduleTypeUID, configDescriptions, label, description, tags, v, inputs,
                    outputs, actionModules);
        }
        actionType = new ActionType(moduleTypeUID, configDescriptions, label, description, tags, v, inputs, outputs);
        return actionType;
    }

    /**
     * This method is used for creation of ConditionType.
     *
     * @param moduleTypeUID is the unique identifier of the ConditionType.
     * @param jsonModuleType is a JSONObject representing the ConditionType.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link ConditionType}s creation.
     * @return parsed ConditionType.
     */
    private ConditionType createConditionType(String moduleTypeUID, JSONObject jsonModuleType,
            List<ParsingNestedException> exceptions) {
        String label = JSONUtility.getString(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.LABEL, true, jsonModuleType, log);
        String description = JSONUtility.getString(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.DESCRIPTION, true, jsonModuleType, log);

        Set<ConfigDescriptionParameter> configDescriptions = ConfigPropertyJSONParser.initializeConfigDescriptions(
                ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions, jsonModuleType, log);

        Visibility v = getVisibility(moduleTypeUID, jsonModuleType, exceptions);
        Set<String> tags = getTags(moduleTypeUID, jsonModuleType, exceptions);

        Set<Input> inputs = getInputs(moduleTypeUID, jsonModuleType, exceptions);
        JSONArray jsonConditions = JSONUtility.getJSONArray(ParsingNestedException.MODULE_TYPE, moduleTypeUID,
                exceptions, JSONStructureConstants.CONDITIONS, true, jsonModuleType, log);
        ConditionType conditionType = null;
        if (jsonConditions != null) {
            List<Condition> conditionModules = ModuleJSONParser.createConditionModules(
                    ParsingNestedException.MODULE_TYPE, moduleTypeUID, JSONStructureConstants.CONDITIONS,
                    jsonConditions, exceptions, log);
            if (conditionModules != null) {
                conditionType = new CompositeConditionType(moduleTypeUID, configDescriptions, label, description, tags,
                        v, inputs, conditionModules);
            }
        } else
            conditionType = new ConditionType(moduleTypeUID, configDescriptions, label, description, tags, v, inputs);
        return conditionType;
    }

    /**
     * This method is used for creation of TriggerType.
     *
     * @param moduleTypeUID is the unique identifier of the TriggerType.
     * @param jsonModuleType is a JSONObject representing the TriggerType.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link TriggerType}s creation.
     * @return parsed TriggerType.
     */
    private TriggerType createTriggerType(String moduleTypeUID, JSONObject jsonModuleType,
            List<ParsingNestedException> exceptions) {
        String label = JSONUtility.getString(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.LABEL, true, jsonModuleType, log);

        String description = JSONUtility.getString(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.DESCRIPTION, true, jsonModuleType, log);

        Set<ConfigDescriptionParameter> configDescriptions = ConfigPropertyJSONParser.initializeConfigDescriptions(
                ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions, jsonModuleType, log);

        Visibility v = null;
        v = getVisibility(moduleTypeUID, jsonModuleType, exceptions);

        Set<String> tags = getTags(moduleTypeUID, jsonModuleType, exceptions);

        Set<Output> outputs = getOutputs(moduleTypeUID, jsonModuleType, exceptions);

        JSONArray jsonTriggers = JSONUtility.getJSONArray(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.TRIGGERS, true, jsonModuleType, log);

        TriggerType triggerType = null;
        if (jsonTriggers != null) {
            List<Trigger> triggerModules = ModuleJSONParser.createTriggerModules(ParsingNestedException.MODULE_TYPE,
                    moduleTypeUID, JSONStructureConstants.TRIGGERS, jsonTriggers, exceptions, log);
            if (triggerModules != null) {
                triggerType = new CompositeTriggerType(moduleTypeUID, configDescriptions, label, description, tags, v,
                        outputs, triggerModules);
            }
        } else
            triggerType = new TriggerType(moduleTypeUID, configDescriptions, label, description, tags, v, outputs);
        return triggerType;
    }

    /**
     * This method is used for creation of Outputs of the ModuleType.
     *
     * @param moduleTypeUID is the unique identifier of the ModuleType.
     * @param jsonModuleType is a JSONObject representing the ModuleType.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link Output}s creation.
     * @return a set of parsed Outputs.
     */
    private Set<Output> getOutputs(String moduleTypeUID, JSONObject jsonModuleType,
            List<ParsingNestedException> exceptions) {
        JSONObject jsonOutputs = JSONUtility.getJSONObject(ParsingNestedException.MODULE_TYPE, moduleTypeUID,
                exceptions, JSONStructureConstants.OUTPUT, true, jsonModuleType, log);
        Set<Output> outputs = null;
        if (jsonOutputs != null) {
            outputs = OutputJSONParser.collectOutputs(bc, moduleTypeUID, jsonOutputs, exceptions, log);
        }
        return outputs;
    }

    /**
     * This method is used for creation of Inputs of the ModuleType.
     *
     * @param moduleTypeUID is the unique identifier of the ModuleType.
     * @param jsonModuleType is a JSONObject representing the ModuleType.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link Input}s creation.
     * @return a set of parsed Inputs.
     */
    private Set<Input> getInputs(String moduleTypeUID, JSONObject jsonModuleType,
            List<ParsingNestedException> exceptions) {
        JSONObject jsonInputs = null;
        Set<Input> inputs = null;
        jsonInputs = JSONUtility.getJSONObject(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.INPUT, true, jsonModuleType, log);
        if (jsonInputs != null) {
            inputs = InputJSONParser.collectInputs(bc, moduleTypeUID, jsonInputs, exceptions, log);
        }
        return inputs;
    }

    /**
     * This method is used for creation of Tags of the ModuleType.
     *
     * @param moduleTypeUID is the unique identifier of the ModuleType.
     * @param jsonModuleType is a JSONObject representing the ModuleType.
     * @param exceptions is a list used for collecting the exceptions occurred during tags creation.
     * @return a set of parsed Tags
     */
    private Set<String> getTags(String moduleTypeUID, JSONObject jsonModuleType,
            List<ParsingNestedException> exceptions) {
        JSONArray jsonTags = null;
        jsonTags = JSONUtility.getJSONArray(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.TAGS, true, jsonModuleType, log);
        Set<String> tags = null;
        if (jsonTags != null) {
            tags = new HashSet<String>();
            for (int j = 0; j < jsonTags.length(); j++) {
                String tag = JSONUtility.getString(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                        JSONStructureConstants.TAGS, j, jsonTags, log);
                if (tag != null)
                    tags.add(tag);
            }
        }
        return tags;
    }

    /**
     * This method is used for creation of Visibility of the ModuleType.
     *
     * @param UID is the unique identifier of the ModuleType.
     * @param json is a JSONObject representing the ModuleType.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link Visibility} creation.
     * @return a Visibility of the ModuleType.
     */
    private Visibility getVisibility(String UID, JSONObject json, List<ParsingNestedException> exceptions) {
        String visibility = JSONUtility.getString(ParsingNestedException.MODULE_TYPE, UID, exceptions,
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
                JSONUtility.catchParsingException(ParsingNestedException.MODULE_TYPE, UID, exceptions, t, log);
            }
        }
        return v;
    }

    /**
     * This method is used for reversion of {@link ModuleType} to JSON format.
     *
     * @param moduleType is a {@link ModuleType} object to revert.
     * @param writer is the {@link OutputStreamWriter} used for exporting the module types.
     * @throws IOException is thrown when the I/O operations are failed or interrupted.
     * @throws JSONException is thrown by the JSON.org classes when things are amiss.
     */
    static void moduleTypeToJSON(ModuleType moduleType, OutputStreamWriter writer) throws IOException, JSONException {
        String label = moduleType.getLabel();
        if (label != null)
            writer.write("    \"" + JSONStructureConstants.LABEL + "\":\"" + label + "\",\n");

        String description = moduleType.getLabel();
        if (description != null)
            writer.write("    \"" + JSONStructureConstants.DESCRIPTION + "\":\"" + description + "\",\n");

        Visibility visibility = moduleType.getVisibility();
        writer.write(
                "    \"" + JSONStructureConstants.VISIBILITY + "\":\"" + visibility.toString().toLowerCase() + "\",\n");

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
