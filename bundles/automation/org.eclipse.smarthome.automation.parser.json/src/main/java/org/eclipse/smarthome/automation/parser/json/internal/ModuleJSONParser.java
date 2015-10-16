/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.json.internal;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.parser.ParsingNestedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 * This class serves for creating the Module objects from JSON Objects.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class ModuleJSONParser {

    /**
     * This method is used for creation of {@link Trigger}s from JSONArray.
     *
     * @param jsonTriggers is a {@link JSONArray} object containing {@link JSONObject}s representing {@link Trigger}s in
     *            json format.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link Module}s creation.
     * @param log is used for logging of exceptions.
     * @return a List which is filled with {@link Trigger} objects created from json.
     */
    static List<Trigger> createTriggerModules(int type, String UID, String key, JSONArray jsonTriggers,
            List<ParsingNestedException> exceptions, Logger log) {
        if (jsonTriggers == null) {
            return null;
        }
        List<Trigger> triggerModules = new ArrayList<Trigger>();
        for (int index = 0; index < jsonTriggers.length(); index++) {
            JSONObject jsonTrigger = JSONUtility.getJSONObject(type, UID, exceptions, key, index, jsonTriggers, log);
            if (jsonTrigger == null) {
                continue;
            }
            Trigger trigger = createTrigger(type, key, jsonTrigger, exceptions, log);
            triggerModules.add(trigger);
        }
        return triggerModules;
    }

    /**
     * This method is used for creation of {@link Trigger} from JSONObject.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param UID is the unique identifier of the automation object - module type, rule or rule template.
     * @param jsonTrigger is a JSONObject representing the module.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link Module}s creation.
     * @param log is used for logging of exceptions.
     * @return a {@link Trigger} object created from json.
     */
    private static Trigger createTrigger(int type, String UID, JSONObject jsonTrigger,
            List<ParsingNestedException> exceptions, Logger log) {
        String uid = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.TYPE, false, jsonTrigger, log);
        String triggerId = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.ID, false, jsonTrigger,
                log);
        JSONObject jsonConfig = JSONUtility.getJSONObject(type, UID, exceptions, JSONStructureConstants.CONFIG, true,
                jsonTrigger, log);
        Map<String, Object> configurations = ConfigPropertyJSONParser.getConfigurationValues(type, UID, exceptions,
                jsonConfig, log);
        Trigger trigger = new Trigger(triggerId, uid, configurations);
        String label = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.LABEL, true, jsonTrigger,
                log);
        if (label != null)
            trigger.setLabel(label);
        String description = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.DESCRIPTION, true,
                jsonTrigger, log);
        if (description != null)
            trigger.setDescription(description);
        return trigger;
    }

    /**
     * This method is used for creation of {@link Condition}s from JSONArray.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param UID is the unique identifier of the automation object - module type, rule or rule template.
     * @param key is a json property for getting {@link Condition}s.
     * @param jsonConditions is a {@link JSONArray} containing {@link JSONObject}s representing {@link Condition}s
     *            in json format.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link Module}s creation.
     * @param log is used for logging of exceptions.
     * @return a List which is filled with {@link Condition} objects created from json.
     */
    static List<Condition> createConditionModules(int type, String UID, String key, JSONArray jsonConditions,
            List<ParsingNestedException> exceptions, Logger log) {
        if (jsonConditions != null) {
            List<Condition> conditions = new ArrayList<Condition>();
            for (int index = 0; index < jsonConditions.length(); index++) {
                JSONObject jsonCondition = JSONUtility.getJSONObject(type, UID, exceptions, key, index, jsonConditions,
                        log);
                if (jsonCondition == null) {
                    continue;
                }
                Condition condition = createCondition(type, UID, jsonCondition, exceptions, log);
                conditions.add(condition);
            }
            return conditions;
        }
        return null;
    }

    /**
     * This method is used for creation of {@link Condition} from JSONObject.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param UID is the unique identifier of the automation object - module type, rule or rule template.
     * @param jsonCondition is a JSONObject representing the module.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link Module}s creation.
     * @param log is used for logging of exceptions.
     * @return a {@link Condition} object created from json.
     */
    private static Condition createCondition(int type, String UID, JSONObject jsonCondition,
            List<ParsingNestedException> exceptions, Logger log) {
        String uid = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.TYPE, false, jsonCondition,
                log);
        String conditionId = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.ID, false,
                jsonCondition, log);
        JSONObject jsonConfig = JSONUtility.getJSONObject(type, UID, exceptions, JSONStructureConstants.CONFIG, true,
                jsonCondition, log);
        Map<String, Object> configurations = ConfigPropertyJSONParser.getConfigurationValues(type, UID, exceptions,
                jsonConfig, log);
        JSONObject jsonInput = JSONUtility.getJSONObject(type, UID, exceptions, JSONStructureConstants.INPUT, true,
                jsonCondition, log);
        Map<String, String> inputs = ModuleJSONParser.getInputs(type, UID, exceptions, jsonInput, log);
        Condition condition = new Condition(conditionId, uid, configurations, inputs);
        String label = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.LABEL, true, jsonCondition,
                log);
        if (label != null)
            condition.setLabel(label);
        String description = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.DESCRIPTION, true,
                jsonCondition, log);
        if (description != null)
            condition.setDescription(description);
        return condition;
    }

    /**
     * This method is used for creation of {@link Action}s from JSONArray.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param UID is the unique identifier of the automation object - module type, rule or rule template.
     * @param key is a json property for getting {@link Action}s.
     * @param jsonActions is a {@link JSONArray} object containing {@link JSONObject}s representing {@link Action}s in
     *            json format.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link Module}s creation.
     * @param log is used for logging of exceptions.
     * @return a List which is filled with {@link Action} objects created from json.
     */
    static List<Action> createActionModules(int type, String UID, String key, JSONArray jsonActions,
            List<ParsingNestedException> exceptions, Logger log) {
        if (jsonActions == null) {
            return null;
        }
        List<Action> actions = new ArrayList<Action>();
        for (int index = 0; index < jsonActions.length(); index++) {
            JSONObject jsonAction = JSONUtility.getJSONObject(type, UID, exceptions, key, index, jsonActions, log);
            if (jsonAction == null) {
                continue;
            }
            Action action = createAction(type, UID, jsonAction, exceptions, log);
            actions.add(action);
        }
        return actions;
    }

    /**
     * This method is used for creation of {@link Action} from JSONObject.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param UID is the unique identifier of the automation object - module type, rule or rule template.
     * @param jsonAction is a JSONObject representing the module.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link Module}s creation.
     * @param log is used for logging of exceptions.
     * @return an {@link Action} object created from json.
     */
    static Action createAction(int type, String UID, JSONObject jsonAction, List<ParsingNestedException> exceptions,
            Logger log) {
        String uid = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.TYPE, false, jsonAction, log);
        String actionId = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.ID, false, jsonAction,
                log);
        JSONObject jsonConfig = JSONUtility.getJSONObject(type, UID, exceptions, JSONStructureConstants.CONFIG, true,
                jsonAction, log);
        Map<String, Object> configurations = ConfigPropertyJSONParser.getConfigurationValues(type, UID, exceptions,
                jsonConfig, log);
        JSONObject jsonInput = JSONUtility.getJSONObject(type, UID, exceptions, JSONStructureConstants.INPUT, true,
                jsonAction, log);
        Map<String, String> inputs = ModuleJSONParser.getInputs(type, UID, exceptions, jsonInput, log);
        Action action = new Action(actionId, uid, configurations, inputs);
        String label = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.LABEL, true, jsonAction,
                log);
        if (label != null)
            action.setLabel(label);
        String description = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.DESCRIPTION, true,
                jsonAction, log);
        if (description != null)
            action.setDescription(description);
        return action;
    }

    /**
     * This method is used for reversion of {@link Action} to JSON format.
     *
     * @param action is an {@link Action} object to revert.
     * @param writer is the {@link OutputStreamWriter} used for exporting the rules.
     * @throws IOException is thrown when the I/O operations are failed or interrupted.
     */
    static void actionToJSON(Action action, OutputStreamWriter writer) throws IOException {
        moduleToJSON(action, writer);
        Map<String, String> inputs = action.getInputs();
        if (inputs != null && !inputs.isEmpty()) {
            Map<String, ?> configValues = action.getConfiguration();
            if (configValues != null && !configValues.isEmpty())
                writer.write(",\n");
            writer.write("        \"" + JSONStructureConstants.INPUT + "\":{\n");
            Iterator<Entry<String, String>> connectionsI = inputs.entrySet().iterator();
            while (connectionsI.hasNext()) {
                Entry<String, String> input = connectionsI.next();
                if (connectionsI.hasNext()) {
                    writer.write("          \"" + input.getKey() + "\":\"" + input.getValue() + "\",");
                } else {
                    writer.write("          \"" + input.getKey() + "\":\"" + input.getValue() + "\"");
                }
                writer.write("\n        }");
            }
        }
    }

    /**
     * This method is used for reversion of {@link Condition} to JSON format.
     *
     * @param condition is an {@link Condition} object to revert.
     * @param writer is the {@link OutputStreamWriter} used for exporting the rules.
     * @throws IOException is thrown when the I/O operations are failed or interrupted.
     */
    static void conditionToJSON(Condition condition, OutputStreamWriter writer) throws IOException {
        moduleToJSON(condition, writer);
        Map<String, String> connections = condition.getInputs();
        if (connections != null && !connections.isEmpty()) {
            Map<String, ?> configValues = condition.getConfiguration();
            if (configValues != null && !configValues.isEmpty())
                writer.write(",\n");
            writer.write("        \"" + JSONStructureConstants.INPUT + "\":{\n");
            Iterator<Entry<String, String>> connectionsI = connections.entrySet().iterator();
            while (connectionsI.hasNext()) {
                Entry<String, String> connection = connectionsI.next();
                if (connectionsI.hasNext()) {
                    writer.write("          \"" + connection.getKey() + "\":\"" + connection.getValue() + "." + "\",");
                } else {
                    writer.write("          \"" + connection.getKey() + "\":\"" + connection.getValue() + "\"");
                }
                writer.write("\n        }");
            }
        }
    }

    /**
     * This method is used for reversion of {@link Trigger} to JSON format.
     *
     * @param trigger is an {@link Trigger} object to revert.
     * @param writer is the {@link OutputStreamWriter} used for exporting the rules.
     * @throws IOException is thrown when the I/O operations are failed or interrupted.
     */
    static void triggerToJSON(Trigger trigger, OutputStreamWriter writer) throws IOException {
        moduleToJSON(trigger, writer);
    }

    /**
     * This method is used for reversion of {@link Module} to JSON format.
     *
     * @param module is an {@link Module} object to revert.
     * @param writer is the {@link OutputStreamWriter} used for exporting the rules.
     * @throws IOException is thrown when the I/O operations are failed or interrupted.
     */
    private static void moduleToJSON(Module module, OutputStreamWriter writer) throws IOException {
        writer.write("        \"" + JSONStructureConstants.TYPE + "\":\"" + module.getTypeUID());
        String label = module.getLabel();
        if (label != null)
            writer.write(",\n        \"" + JSONStructureConstants.LABEL + "\":\"" + label);
        String description = module.getLabel();
        if (description != null) {
            writer.write(",\n        \"" + JSONStructureConstants.DESCRIPTION + "\":\"" + description);
        }
        Map<String, ?> configValues = module.getConfiguration();
        if (configValues != null && !configValues.isEmpty()) {
            writer.write(",\n        \"" + JSONStructureConstants.CONFIG + "\":{\n");
            Iterator<String> i = configValues.keySet().iterator();
            while (i.hasNext()) {
                String paramName = i.next();
                writer.write("          \"" + paramName + "\":\"");
                writer.write(configValues.get(paramName).toString());
                if (i.hasNext())
                    writer.write("\",\n");
                else
                    writer.write("\"\n");
            }
            writer.write("        }");
        }
    }

    /**
     * This method is used for reversion of {@link Module}s to JSON format.
     *
     * @param modules {@link Module}s to revert
     * @param writer is the {@link OutputStreamWriter} used for exporting the rules.
     * @throws IOException is thrown when the I/O operations are failed or interrupted.
     */
    static <T extends Module> void writeModules(List<T> modules, OutputStreamWriter writer) throws IOException {
        if (!modules.isEmpty()) {
            Iterator<T> i = modules.iterator();
            while (i.hasNext()) {
                T module = i.next();
                writer.write("      \"" + module.getId() + "\":{\n");
                if (module instanceof Trigger)
                    ModuleJSONParser.triggerToJSON((Trigger) module, writer);
                if (module instanceof Condition)
                    ModuleJSONParser.conditionToJSON((Condition) module, writer);
                if (module instanceof Action)
                    ModuleJSONParser.actionToJSON((Action) module, writer);
                if (i.hasNext())
                    writer.write("\n      },\n");
                else
                    writer.write("\n      }\n");
            }
        }
    }

    static Map<String, String> getInputs(int type, String UID, List<ParsingNestedException> exceptions,
            JSONObject jsonConfig, Logger log) {
        Map<String, String> inputs = new HashMap<String, String>();
        if (jsonConfig == null) {
            return inputs;
        }
        Iterator<?> i = jsonConfig.keys();
        while (i.hasNext()) {
            String inputName = (String) i.next();
            try {
                inputs.put(inputName, (String) jsonConfig.get(inputName));
            } catch (JSONException e) {
                JSONUtility.catchParsingException(type, UID, exceptions,
                        new Throwable("Failed to get the value for the input: " + inputName, e), log);
                return null;
            }
        }
        return inputs;
    }

}
