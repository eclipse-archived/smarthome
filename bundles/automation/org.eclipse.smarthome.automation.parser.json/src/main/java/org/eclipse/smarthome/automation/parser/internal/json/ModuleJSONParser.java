/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.internal.json;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.parser.Status;
import org.json.JSONArray;
import org.json.JSONObject;

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
     * @param status
     * @param triggerModules is a List which has to be filled with {@link Trigger} objects created from json.
     * @param onSection is a {@link JSONArray} object containing {@link JSONObject}s representing {@link Trigger}s in
     *            json format.
     * @return
     */
    static boolean createTrigerModules(Status status, List<Trigger> triggerModules, JSONArray onSection) {
        if (onSection == null) {
            return false;
        }
        for (int index = 0; index < onSection.length(); index++) {
            JSONObject jsonTrigger = JSONUtility.getJSONObject(JSONStructureConstants.ON, index, onSection, status);
            if (jsonTrigger == null) {
                continue;
            }
            String uid = JSONUtility.getString(JSONStructureConstants.TYPE, false, jsonTrigger, status);
            String triggerId = JSONUtility.getString(JSONStructureConstants.ID, false, jsonTrigger, status);
            JSONObject jsonConfig = JSONUtility.getJSONObject(JSONStructureConstants.CONFIG, true, jsonTrigger, status);
            Map<String, Object> configurations = ConfigPropertyJSONParser.getConfigurationValues(jsonConfig, status);
            Trigger trigger = new Trigger(triggerId, uid, configurations);
            String label = JSONUtility.getString(JSONStructureConstants.LABEL, true, jsonTrigger, status);
            if (label != null)
                trigger.setLabel(label);
            String description = JSONUtility.getString(JSONStructureConstants.DESCRIPTION, true, jsonTrigger, status);
            if (description != null)
                trigger.setDescription(description);
            triggerModules.add(trigger);
        }
        if (status.hasErrors()) {
            return false;
        }
        return true;
    }

    /**
     * This method is used for creation of {@link Condition}s from JSONArray.
     *
     * @param status
     * @param conditions is a List which has to be filled with {@link Condition} objects created from json.
     * @param ifSection is a {@link JSONArray} object containing {@link JSONObject}s representing {@link Condition}s in
     *            json format.
     * @return
     */
    static boolean createConditionModules(Status status, List<Condition> conditions, JSONArray ifSection) {
        for (int index = 0; index < ifSection.length(); index++) {
            JSONObject jsonCondition = JSONUtility.getJSONObject(JSONStructureConstants.IF, index, ifSection, status);
            if (jsonCondition == null) {
                continue;
            }
            String uid = JSONUtility.getString(JSONStructureConstants.TYPE, false, jsonCondition, status);
            String conditionId = JSONUtility.getString(JSONStructureConstants.ID, false, jsonCondition, status);
            JSONObject jsonConfig = JSONUtility.getJSONObject(JSONStructureConstants.CONFIG, true, jsonCondition,
                    status);
            Map<String, Object> configurations = ConfigPropertyJSONParser.getConfigurationValues(jsonConfig, status);
            Set<Connection> connections = ModuleJSONParser.collectConnections(jsonCondition, status);
            Condition condition = new Condition(conditionId, uid, configurations, connections);
            String label = JSONUtility.getString(JSONStructureConstants.LABEL, true, jsonCondition, status);
            if (label != null)
                condition.setLabel(label);
            String description = JSONUtility.getString(JSONStructureConstants.DESCRIPTION, true, jsonCondition, status);
            if (description != null)
                condition.setDescription(description);
            conditions.add(condition);
        }
        if (status.hasErrors()) {
            return false;
        }
        return true;
    }

    /**
     * This method is used for creation of {@link Action}s from JSONArray.
     *
     * @param status
     * @param actions is a List which has to be filled with {@link Action} objects created from json.
     * @param thenSection is a {@link JSONArray} object containing {@link JSONObject}s representing {@link Action}s in
     *            json format.
     * @return
     */
    static boolean createActionModules(Status status, List<Action> actions, JSONArray thenSection) {
        if (thenSection == null) {
            return false;
        }
        for (int index = 0; index < thenSection.length(); index++) {
            JSONObject jsonAction = JSONUtility.getJSONObject(JSONStructureConstants.THEN, index, thenSection, status);
            if (jsonAction == null) {
                continue;
            }
            String uid = JSONUtility.getString(JSONStructureConstants.TYPE, false, jsonAction, status);
            String actionId = JSONUtility.getString(JSONStructureConstants.ID, false, jsonAction, status);
            JSONObject jsonConfig = JSONUtility.getJSONObject(JSONStructureConstants.CONFIG, true, jsonAction, status);
            Map<String, Object> configurations = ConfigPropertyJSONParser.getConfigurationValues(jsonConfig, status);
            Set<Connection> connections = ModuleJSONParser.collectConnections(jsonAction, status);
            Action action = new Action(actionId, uid, configurations, connections);
            String label = JSONUtility.getString(JSONStructureConstants.LABEL, true, jsonAction, status);
            if (label != null)
                action.setLabel(label);
            String description = JSONUtility.getString(JSONStructureConstants.DESCRIPTION, true, jsonAction, status);
            if (description != null)
                action.setDescription(description);
            actions.add(action);
        }
        if (status.hasErrors()) {
            return false;
        }
        return true;
    }

    /**
     * This method is used for reversion of {@link Action} to JSON format.
     *
     * @param action is an {@link Action} object to revert.
     * @param writer
     * @throws IOException
     */
    static void actionToJSON(Action action, OutputStreamWriter writer) throws IOException {
        moduleToJSON(action, writer);
        Set<Connection> connections = action.getConnections();
        if (connections != null && !connections.isEmpty()) {
            Map<String, ?> configValues = action.getConfiguration();
            if (configValues != null && !configValues.isEmpty())
                writer.write(",\n");
            writer.write("        \"" + JSONStructureConstants.INPUT + "\":{\n");
            Iterator<Connection> connectionsI = connections.iterator();
            while (connectionsI.hasNext()) {
                Connection connection = connectionsI.next();
                if (connectionsI.hasNext()) {
                    writer.write("          \"" + connection.getInputName() + "\":\"" + connection.getOuputModuleId()
                            + "." + connection.getOutputName() + "\",");
                } else {
                    writer.write("          \"" + connection.getInputName() + "\":\"" + connection.getOuputModuleId()
                            + "." + connection.getOutputName() + "\"");
                }
                writer.write("\n        }");
            }
        }
    }

    /**
     * This method is used for reversion of {@link Condition} to JSON format.
     *
     * @param condition is an {@link Condition} object to revert.
     * @param writer
     * @throws IOException
     */
    static void conditionToJSON(Condition condition, OutputStreamWriter writer) throws IOException {
        moduleToJSON(condition, writer);
        Set<Connection> connections = condition.getConnections();
        if (connections != null && !connections.isEmpty()) {
            Map<String, ?> configValues = condition.getConfiguration();
            if (configValues != null && !configValues.isEmpty())
                writer.write(",\n");
            writer.write("        \"" + JSONStructureConstants.INPUT + "\":{\n");
            Iterator<Connection> connectionsI = connections.iterator();
            while (connectionsI.hasNext()) {
                Connection connection = connectionsI.next();
                if (connectionsI.hasNext()) {
                    writer.write("          \"" + connection.getInputName() + "\":\"" + connection.getOuputModuleId()
                            + "." + connection.getOutputName() + "\",");
                } else {
                    writer.write("          \"" + connection.getInputName() + "\":\"" + connection.getOuputModuleId()
                            + "." + connection.getOutputName() + "\"");
                }
                writer.write("\n        }");
            }
        }
    }

    /**
     * This method is used for reversion of {@link Trigger} to JSON format.
     *
     * @param trigger is an {@link Trigger} object to revert.
     * @param writer
     * @throws IOException
     */
    static void triggerToJSON(Trigger trigger, OutputStreamWriter writer) throws IOException {
        moduleToJSON(trigger, writer);
    }

    /**
     * This method is used for reversion of {@link Module} to JSON format.
     *
     * @param module is an {@link Module} object to revert.
     * @param writer
     * @throws IOException
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
     *
     * @param <T>
     * @param moduleManager
     * @param modules
     * @param writer
     * @throws IOException
     */
    static <T extends Module> void writeModules(List<T> modules, OutputStreamWriter writer) throws IOException {
        if (modules != null && !modules.isEmpty()) {
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

    /**
     *
     * @param jsonModule
     * @param status
     * @return
     */
    static Set<Connection> collectConnections(JSONObject jsonModule, Status status) {
        LinkedHashSet<Connection> connections = new LinkedHashSet<Connection>();
        boolean res = true;
        JSONObject jsonInputs = JSONUtility.getJSONObject(JSONStructureConstants.INPUT, true, jsonModule, status);
        if (jsonInputs != null) {
            Iterator<?> i = jsonInputs.keys();
            while (i.hasNext()) {
                String inputName = (String) i.next();
                String jsonOutput = JSONUtility.getString(inputName, false, jsonInputs, status);
                if (jsonOutput == null) {
                    res = false;
                    continue;
                }
                int index = jsonOutput.indexOf('.');
                if (index == -1) {
                    status.error("Wrong format of Output : " + jsonOutput + ". Should be as \"Module_Id.Output_Id\".",
                            new IllegalArgumentException());
                    res = false;
                    continue;
                }
                String ouputModuleId = jsonOutput.substring(0, index);
                String outputName = jsonOutput.substring(index + 1);
                Connection connection = new Connection(inputName, ouputModuleId, outputName);
                connections.add(connection);
            }
        }
        if (res)
            return connections;
        return null;
    }

}
