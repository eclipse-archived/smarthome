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

import org.eclipse.smarthome.automation.dto.ActionDTO;
import org.eclipse.smarthome.automation.dto.ConditionDTO;
import org.eclipse.smarthome.automation.dto.TriggerDTO;
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
import org.eclipse.smarthome.automation.type.dto.CompositeActionTypeDTO;
import org.eclipse.smarthome.automation.type.dto.CompositeConditionTypeDTO;
import org.eclipse.smarthome.automation.type.dto.CompositeTriggerTypeDTO;
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
                    JSONUtility.getJSONObject(JSONStructureConstants.CONDITIONS, true, json, status),
                    moduleTypesStatus);
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

    @Override
    public void exportData(Set<ModuleType> dataObjects, OutputStreamWriter writer) throws IOException {
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
     *
     * @param moduleTypes
     * @param triggers
     * @param conditions
     * @param actions
     * @param composites
     */
    private void sortModuleTypesByTypes(Set<ModuleType> moduleTypes, Map<String, TriggerType> triggers,
            Map<String, ConditionType> conditions, Map<String, ActionType> actions,
            Map<String, ModuleType> composites) {
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
     *
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
            Object moduleType = null;
            JSONObject jsonModuleType = JSONUtility.getJSONObject(moduleTypeUID, false, jsonModuleTypes, status);
            if (jsonModuleType == null) {
                moduleTypesStatus.add(status);
                continue;
            }
            switch (type) {
                case JSONUtility.TRIGGERS:
                    moduleType = createTriggerType(status, moduleTypeUID, jsonModuleType);
                    break;
                case JSONUtility.CONDITIONS:
                    moduleType = createConditionType(status, moduleTypeUID, jsonModuleType);
                    break;
                case JSONUtility.ACTIONS:
                    moduleType = createActionType(status, moduleTypeUID, jsonModuleType);
                    break;
                case JSONUtility.COMPOSITE:
                    moduleType = createCompositeType(status, moduleTypeUID, jsonModuleType);
                    break;
            }
            status.success(moduleType);
            moduleTypesStatus.add(status);
        }
    }

    /**
     *
     * @param status
     * @param moduleTypeUID
     * @param jsonModuleType
     * @return
     */
    private Object createActionType(Status status, String moduleTypeUID, JSONObject jsonModuleType) {

        String label = JSONUtility.getString(JSONStructureConstants.LABEL, true, jsonModuleType, status);
        String description = JSONUtility.getString(JSONStructureConstants.DESCRIPTION, true, jsonModuleType, status);

        LinkedHashSet<ConfigDescriptionParameter> configDescriptions = ConfigPropertyJSONParser
                .initializeConfigDescriptions(jsonModuleType, status);
        if (configDescriptions == null)
            return null;

        Visibility v = getVisibility(status, jsonModuleType);
        if (v == null)
            return null;

        Set<String> tags = getTags(status, jsonModuleType);

        JSONObject jsonActionInputs = JSONUtility.getJSONObject(JSONStructureConstants.INPUT, true, jsonModuleType,
                status);
        if (jsonActionInputs != null) {
            LinkedHashSet<Input> inputs = new LinkedHashSet<Input>();
            LinkedHashSet<Output> outputs = new LinkedHashSet<Output>();
            if (InputJSONParser.collectInputs(bc, jsonActionInputs, inputs, status)) {
                JSONObject jsonActionOutputs = JSONUtility.getJSONObject(JSONStructureConstants.OUTPUT, true,
                        jsonModuleType, status);
                if (jsonActionOutputs != null) {
                    if (OutputJSONParser.collectOutputs(bc, jsonActionOutputs, outputs, status))
                        return new ActionType(moduleTypeUID, configDescriptions, label, description, tags, v, inputs,
                                outputs);
                } else
                    return new ActionType(moduleTypeUID, configDescriptions, label, description, tags, v, inputs, null);
            }
        } else
            return new ActionType(moduleTypeUID, configDescriptions, null);
        return null;
    }

    /**
     *
     * @param status
     * @param moduleTypeUID
     * @param jsonModuleType
     * @return
     */
    private Object createConditionType(Status status, String moduleTypeUID, JSONObject jsonModuleType) {

        String label = JSONUtility.getString(JSONStructureConstants.LABEL, true, jsonModuleType, status);
        String description = JSONUtility.getString(JSONStructureConstants.DESCRIPTION, true, jsonModuleType, status);

        LinkedHashSet<ConfigDescriptionParameter> configDescriptions = ConfigPropertyJSONParser
                .initializeConfigDescriptions(jsonModuleType, status);
        if (configDescriptions == null)
            return null;

        Visibility v = getVisibility(status, jsonModuleType);
        if (v == null)
            return null;

        Set<String> tags = getTags(status, jsonModuleType);

        JSONObject jsonConditionInputs = JSONUtility.getJSONObject(JSONStructureConstants.INPUT, true, jsonModuleType,
                status);
        if (jsonConditionInputs != null) {
            LinkedHashSet<Input> inputs = new LinkedHashSet<Input>();
            if (InputJSONParser.collectInputs(bc, jsonConditionInputs, inputs, status))
                return new ConditionType(moduleTypeUID, configDescriptions, label, description, tags, v, inputs);
            else
                return null;
        } else
            return new ConditionType(moduleTypeUID, configDescriptions, label, description, tags, v, null);
    }

    /**
     *
     * @param status
     * @param moduleTypeUID
     * @param jsonModuleType
     * @return
     */
    private Object createTriggerType(Status status, String moduleTypeUID, JSONObject jsonModuleType) {

        String label = JSONUtility.getString(JSONStructureConstants.LABEL, true, jsonModuleType, status);
        String description = JSONUtility.getString(JSONStructureConstants.DESCRIPTION, true, jsonModuleType, status);

        LinkedHashSet<ConfigDescriptionParameter> configDescriptions = ConfigPropertyJSONParser
                .initializeConfigDescriptions(jsonModuleType, status);
        if (configDescriptions == null)
            return null;

        Visibility v = getVisibility(status, jsonModuleType);
        if (v == null)
            return null;

        Set<String> tags = getTags(status, jsonModuleType);

        Set<Output> outputs = getOutputs(status, jsonModuleType);
        if (jsonModuleType.has(JSONStructureConstants.OUTPUT) && outputs != null)
            return new TriggerType(moduleTypeUID, configDescriptions, label, description, tags, v, outputs);
        return null;
    }

    /**
     *
     * @param status
     * @param moduleTypeUID
     * @param jsonModuleType
     * @return
     */
    private Object createCompositeType(Status status, String moduleTypeUID, JSONObject jsonModuleType) {

        JSONArray jsonTriggers = JSONUtility.getJSONArray(JSONStructureConstants.TRIGGERS, true, jsonModuleType,
                status);
        JSONArray jsonConditions = JSONUtility.getJSONArray(JSONStructureConstants.CONDITIONS, true, jsonModuleType,
                status);
        JSONArray jsonActions = JSONUtility.getJSONArray(JSONStructureConstants.ACTIONS, true, jsonModuleType, status);

        if (jsonTriggers == null && jsonConditions == null && jsonActions == null) {
            status.error("At least one property of \"triggers\", \"conditions\" or \"actions\" must be present!",
                    new IllegalArgumentException());
            return null;
        } else if (jsonTriggers != null && jsonConditions == null && jsonActions == null) {
            return createCompositeTriggerTypeDTO(status, moduleTypeUID, jsonTriggers, jsonModuleType);
        } else if (jsonConditions != null && jsonTriggers == null && jsonActions == null) {
            return createCompositeConditionTypeDTO(status, moduleTypeUID, jsonConditions, jsonModuleType);
        } else if (jsonActions != null && jsonConditions == null && jsonTriggers == null) {
            return createCompositeActionTypeDTO(status, moduleTypeUID, jsonActions, jsonModuleType);
        } else {
            status.error("Only one of properties \"triggers\", \"conditions\" and \"actions\" must be present!",
                    new IllegalArgumentException());
            return null;
        }
    }

    /**
     *
     * @param status
     * @param moduleTypeUID
     * @param jsonActions
     * @param jsonModuleType
     * @return
     */
    private Object createCompositeActionTypeDTO(Status status, String moduleTypeUID, JSONArray jsonActions,
            JSONObject jsonModuleType) {

        String label = JSONUtility.getString(JSONStructureConstants.LABEL, true, jsonModuleType, status);
        String description = JSONUtility.getString(JSONStructureConstants.DESCRIPTION, true, jsonModuleType, status);

        LinkedHashSet<ConfigDescriptionParameter> configDescriptions = ConfigPropertyJSONParser
                .initializeConfigDescriptions(jsonModuleType, status);
        if (configDescriptions == null)
            return null;

        Visibility v = getVisibility(status, jsonModuleType);
        if (v == null)
            return null;

        Set<String> tags = getTags(status, jsonModuleType);

        List<ActionDTO> actionModules = createActionModulesDTO(status, jsonActions, jsonModuleType);
        if (actionModules != null) {
            Set<Output> outputs = getOutputs(status, jsonModuleType);
            Set<Input> inputs = getInputs(status, jsonModuleType);
            if (jsonModuleType.has(JSONStructureConstants.OUTPUT) && outputs == null
                    || jsonModuleType.has(JSONStructureConstants.INPUT) && inputs == null)
                return null;
            return new CompositeActionTypeDTO(moduleTypeUID, configDescriptions, label, description, tags, v, inputs,
                    outputs, actionModules);
        }
        return null;
    }

    /**
     *
     * @param status
     * @param moduleTypeUID
     * @param jsonConditions
     * @param jsonModuleType
     * @return
     */
    private Object createCompositeConditionTypeDTO(Status status, String moduleTypeUID, JSONArray jsonConditions,
            JSONObject jsonModuleType) {

        String label = JSONUtility.getString(JSONStructureConstants.LABEL, true, jsonModuleType, status);
        String description = JSONUtility.getString(JSONStructureConstants.DESCRIPTION, true, jsonModuleType, status);

        LinkedHashSet<ConfigDescriptionParameter> configDescriptions = ConfigPropertyJSONParser
                .initializeConfigDescriptions(jsonModuleType, status);
        if (configDescriptions == null)
            return null;

        Visibility v = getVisibility(status, jsonModuleType);
        if (v == null)
            return null;

        Set<String> tags = getTags(status, jsonModuleType);

        List<ConditionDTO> conditionModules = createConditionModulesDTO(status, jsonConditions, jsonModuleType);
        if (conditionModules != null) {
            Set<Input> inputs = getInputs(status, jsonModuleType);
            if (jsonModuleType.has(JSONStructureConstants.INPUT) && inputs != null
                    || !jsonModuleType.has(JSONStructureConstants.INPUT))
                return new CompositeConditionTypeDTO(moduleTypeUID, configDescriptions, label, description, tags, v,
                        inputs, conditionModules);
        }
        return null;
    }

    /**
     *
     * @param status
     * @param moduleTypeUID
     * @param jsonTriggers
     * @param jsonModuleType
     * @return
     */
    private Object createCompositeTriggerTypeDTO(Status status, String moduleTypeUID, JSONArray jsonTriggers,
            JSONObject jsonModuleType) {

        String label = JSONUtility.getString(JSONStructureConstants.LABEL, true, jsonModuleType, status);
        String description = JSONUtility.getString(JSONStructureConstants.DESCRIPTION, true, jsonModuleType, status);

        LinkedHashSet<ConfigDescriptionParameter> configDescriptions = ConfigPropertyJSONParser
                .initializeConfigDescriptions(jsonModuleType, status);
        if (configDescriptions == null)
            return null;

        Visibility v = getVisibility(status, jsonModuleType);
        if (v == null)
            return null;

        Set<String> tags = getTags(status, jsonModuleType);

        List<TriggerDTO> triggerModules = createTriggerModulesDTO(status, jsonTriggers, jsonModuleType);
        if (triggerModules != null) {
            Set<Output> outputs = getOutputs(status, jsonModuleType);
            if (jsonModuleType.has(JSONStructureConstants.OUTPUT) && outputs != null
                    || !jsonModuleType.has(JSONStructureConstants.OUTPUT))
                return new CompositeTriggerTypeDTO(moduleTypeUID, configDescriptions, label, description, tags, v,
                        outputs, triggerModules);
        }
        return null;
    }

    /**
     *
     * @param status
     * @param jsonActions
     * @param jsonModuleType
     * @return
     */
    private List<ActionDTO> createActionModulesDTO(Status status, JSONArray jsonActions, JSONObject jsonModuleType) {
        List<ActionDTO> actionModules = new ArrayList<ActionDTO>();
        if (ModuleJSONParser.createActionModules(status, actionModules, jsonActions))
            return actionModules;
        return null;
    }

    /**
     *
     * @param status
     * @param jsonConditions
     * @param jsonModuleType
     * @return
     */
    private List<ConditionDTO> createConditionModulesDTO(Status status, JSONArray jsonConditions,
            JSONObject jsonModuleType) {
        List<ConditionDTO> conditionModules = new ArrayList<ConditionDTO>();
        if (ModuleJSONParser.createConditionModules(status, conditionModules, jsonConditions))
            return conditionModules;
        return null;
    }

    /**
     *
     * @param status
     * @param jsonTriggers
     * @param jsonModuleType
     * @return
     */
    private List<TriggerDTO> createTriggerModulesDTO(Status status, JSONArray jsonTriggers, JSONObject jsonModuleType) {
        List<TriggerDTO> triggerModules = new ArrayList<TriggerDTO>();
        if (ModuleJSONParser.createTrigerModules(status, triggerModules, jsonTriggers))
            return triggerModules;
        return null;
    }

    /**
     *
     * @param status
     * @param jsonModuleType
     * @return
     */
    private Set<Output> getOutputs(Status status, JSONObject jsonModuleType) {
        JSONObject jsonOutputs = JSONUtility.getJSONObject(JSONStructureConstants.OUTPUT, true, jsonModuleType, status);
        if (jsonOutputs != null) {
            Set<Output> outputs = new LinkedHashSet<Output>();
            if (!OutputJSONParser.collectOutputs(bc, jsonOutputs, outputs, status))
                return null;
            else
                return outputs;
        }
        return null;
    }

    /**
     *
     * @param status
     * @param jsonModuleType
     * @return
     */
    private Set<Input> getInputs(Status status, JSONObject jsonModuleType) {
        JSONObject jsonInputs = JSONUtility.getJSONObject(JSONStructureConstants.INPUT, true, jsonModuleType, status);
        if (jsonInputs != null) {
            Set<Input> inputs = new LinkedHashSet<Input>();
            if (!InputJSONParser.collectInputs(bc, jsonInputs, inputs, status))
                return null;
            else
                return inputs;
        }
        return null;
    }

    /**
     *
     * @param status
     * @param jsonModuleType
     * @return
     */
    private Set<String> getTags(Status status, JSONObject jsonModuleType) {
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
        return tags;
    }

    /**
     *
     * @param status
     * @param jsonModuleType
     * @return
     */
    private Visibility getVisibility(Status status, JSONObject jsonModuleType) {
        String visibility = JSONUtility.getString(JSONStructureConstants.VISIBILITY, true, jsonModuleType, status);
        Visibility v = null;
        if (visibility == null) {
            v = Visibility.PUBLIC;
        } else {
            try {
                v = Visibility.valueOf(visibility.toUpperCase());
            } catch (IllegalArgumentException ie) {
                status.error("Incorrect value for property \"" + JSONStructureConstants.VISIBILITY + "\" : \""
                        + jsonModuleType + "\".", ie);
                return null;
            }
        }
        return v;
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
