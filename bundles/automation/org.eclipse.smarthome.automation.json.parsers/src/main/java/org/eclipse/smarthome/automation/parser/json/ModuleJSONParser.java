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
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.parser.Status;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 *  This class serves for creating the Module objects from JSON Objects.
 * 
 * @author Ana Dimova
 *
 */
public class ModuleJSONParser {

  /**
   * This method is used for creation of {@link Trigger}s from JSONArray.
   * @param status
   * @param moduleManager {@link ModuleTypeManager}.
   * @param automationFactory {@link AutomationFactory}.
   * @param moduleTypesStatus 
   * @param triggers is a List which has to be filled with {@link Trigger} objects created from json.
   * @param onSection is a {@link JSONArray} object containing {@link JSONObject}s representing {@link Trigger}s in json format.
   * @return
   */
  static boolean createTrigerModules(Status status, ModuleTypeManager moduleManager,
      AutomationFactory automationFactory,
      LinkedHashSet<Status> moduleTypesStatus, List<Trigger> triggers, JSONArray onSection) {
    boolean res = true;
    if (onSection == null) {
      return false;
    }
    for (int index = 0; index < onSection.length(); index++) {
      JSONObject jsonTrigger = JSONUtility.getJSONObject(JSONStructureConstants.ON, index, onSection, status);
      if (jsonTrigger == null) {
        res = false;
        continue;
      }
      String uid = JSONUtility.getString(JSONStructureConstants.TYPE, false, jsonTrigger, status);
      if (uid == null) {
        res = false;
        continue;
      }
      String triggerId = JSONUtility.getString(JSONStructureConstants.ID, false, jsonTrigger, status);
      if (triggerId == null) {
        res = false;
        continue;
      }
      ModuleType triggerT = ModuleTypeJSONParser
          .getModuleType(triggerId, uid, moduleManager, moduleTypesStatus, status);
      if (triggerT == null)
        continue;
      Set<ConfigDescriptionParameter> configDescriptions = triggerT.getConfigurationDescription();
      boolean opt = JSONUtility.isOptionalConfig(configDescriptions);
      JSONObject jsonConfig =
          JSONUtility.getJSONObject(JSONStructureConstants.CONFIG, opt, jsonTrigger, status);
      if (!opt && jsonConfig == null) {
        res = false;
        continue;
      }
      Map<String, Object> configurations = ConfigPropertyJSONParser.getConfigurationValues(jsonConfig, configDescriptions, status);
      if (configurations == null) {
        res = false;
        continue;
      }
      Trigger trigger = automationFactory.createTrigger(triggerId, uid, configurations);
      triggers.add(trigger);
    }
    return res;
  }

  /**
   * This method is used for creation of {@link Condition}s from JSONArray.
   * @param status
   * @param moduleManager {@link ModuleTypeManager}.
   * @param automationFactory {@link AutomationFactory}.
   * @param conditions is a List which has to be filled with {@link Condition} objects created from json.
   * @param ifSection is a {@link JSONArray} object containing {@link JSONObject}s representing {@link Condition}s in json format.
   * @return
   */
  static boolean createConditionModules(Status status, ModuleTypeManager moduleManager,
      AutomationFactory automationFactory, LinkedHashSet<Status> moduleTypesStatus,
      List<Condition> conditions, JSONArray ifSection) {
    boolean res = true;
    for (int index = 0; index < ifSection.length(); index++) {
      JSONObject jsonCondition = JSONUtility.getJSONObject(JSONStructureConstants.IF, index, ifSection, status);
      if (jsonCondition == null) {
        res = false;
        continue;
      }
      String uid = JSONUtility.getString(JSONStructureConstants.TYPE, false, jsonCondition, status);
      if (uid == null) {
        res = false;
        continue;
      }
      String conditionId = JSONUtility.getString(JSONStructureConstants.ID, false, jsonCondition, status);
      if (conditionId == null) {
        res = false;
        continue;
      }
      ModuleType conditionT = ModuleTypeJSONParser.getModuleType(conditionId, uid, moduleManager, moduleTypesStatus,
          status);
      if (conditionT == null)
        continue;
      Set<ConfigDescriptionParameter> configDescriptions = conditionT.getConfigurationDescription();
      boolean opt = JSONUtility.isOptionalConfig(configDescriptions);
      JSONObject jsonConfig = JSONUtility.getJSONObject(JSONStructureConstants.CONFIG, opt, jsonCondition, status);
      if (!opt && jsonConfig == null) {
        res = false;
        continue;
      }
      Map<String, Object> configurations = ConfigPropertyJSONParser.getConfigurationValues(jsonConfig, configDescriptions, status);
      if (configurations == null) {
        res = false;
        continue;
      }
      Set<Connection> connections = ModuleJSONParser.collectConnections(jsonCondition, status);
      if (connections == null) {
        res = false;
        continue;
      }
      Condition condition = automationFactory.createCondition(conditionId, uid, configurations, connections);
      conditions.add(condition);
    }
    return res;
  }

  /**
   * This method is used for creation of {@link Action}s from JSONArray.
   * @param status
   * @param moduleManager {@link ModuleTypeManager}.
   * @param automationFactory {@link AutomationFactory}.
   * @param actions is a List which has to be filled with {@link Action} objects created from json.
   * @param thenSection is a {@link JSONArray} object containing {@link JSONObject}s representing {@link Action}s in json format.
   * @return
   */
  static boolean createActionModules(Status status, ModuleTypeManager moduleManager,
      AutomationFactory automationFactory,
      LinkedHashSet<Status> moduleTypesStatus, List<Action> actions, JSONArray thenSection) {
    boolean res = true;
    if (thenSection == null) {
      return false;
    }
    for (int index = 0; index < thenSection.length(); index++) {
      JSONObject jsonAction = JSONUtility.getJSONObject(JSONStructureConstants.THEN, index, thenSection, status);
      if (jsonAction == null) {
        res = false;
        continue;
      }
      String uid = JSONUtility.getString(JSONStructureConstants.TYPE, false, jsonAction, status);
      if (uid == null) {
        res = false;
        continue;
      }
      String actionId = JSONUtility.getString(JSONStructureConstants.ID, false, jsonAction, status);
      if (actionId == null) {
        res = false;
        continue;
      }
      ModuleType actionT = ModuleTypeJSONParser.getModuleType(actionId, uid, moduleManager, moduleTypesStatus, status);
      if (actionT == null)
        continue;
      Set<ConfigDescriptionParameter> configDescriptions = actionT.getConfigurationDescription();
      boolean opt = JSONUtility.isOptionalConfig(configDescriptions);
      JSONObject jsonConfig = JSONUtility.getJSONObject(JSONStructureConstants.CONFIG, opt, jsonAction, status);
      if (!opt && jsonConfig == null) {
        res = false;
        continue;
      }
      Map<String, Object> configurations = ConfigPropertyJSONParser.getConfigurationValues(jsonConfig, configDescriptions, status);
      if (configurations == null) {
        res = false;
        continue;
      }
      Set<Connection> connections = ModuleJSONParser.collectConnections(jsonAction, status);
      if (connections == null) {
        res = false;
        continue;
      }
      Action action = automationFactory.createAction(actionId, uid, configurations, connections);
      actions.add(action);
    }
    return res;
  }

  /**
   * This method is used for reversion of {@link Action} to JSON format.
   * @param moduleManager {@link ModuleTypeManager}.
   * @param action is an {@link Action} object to revert.
   * @param writer 
   * @throws IOException 
   */
  static void actionToJSON(ModuleTypeManager moduleManager, Action action, OutputStreamWriter writer)
      throws IOException {
    moduleToJSON(action, writer);
    Set<Connection> connections = action.getConnections();
    if (connections != null && !connections.isEmpty()) {
      Map<String, Object> configValues = action.getConfiguration();
      if (configValues != null && !configValues.isEmpty())
        writer.write(",\n");
      writer.write("        \"" + JSONStructureConstants.INPUT + "\":{\n");
      Iterator<Connection> connectionsI = connections.iterator();
      while (connectionsI.hasNext()) {
        Connection connection = (Connection) connectionsI.next();
        if (connectionsI.hasNext()) {
          writer.write("          \"" + connection.getInputName() + "\":\"" + connection.getOuputModuleId() + "."
              + connection.getOutputName() + "\",");
        } else {
          writer.write("          \"" + connection.getInputName() + "\":\"" + connection.getOuputModuleId() + "."
              + connection.getOutputName() + "\"");
        }
        writer.write("\n        }");
      }
    }
    ActionType moduleType = moduleManager.getType(action.getTypeUID());
    Set<Output> outputs = moduleType.getOutputs();
    if (outputs != null && !outputs.isEmpty()) {
      if (connections != null && !connections.isEmpty())
        writer.write(",\n");
      writer.write("        " + JSONStructureConstants.OUTPUT + ":{\n");
      Iterator<Output> outputsI = outputs.iterator();
      while (outputsI.hasNext()) {
        Output output = (Output) outputsI.next();
        if (outputsI.hasNext()) {
          writer.write("          \"" + output.getName() + "\":\"" + output.getReference() + "\",");
        } else {
          writer.write("          \"" + output.getName() + "\":\"" + output.getReference() + "\"");
        }
        writer.write("\n        }");
      }
    }
  }

  /**
   * This method is used for reversion of {@link Condition} to JSON format.
   * @param condition is an {@link Condition} object to revert.
   * @param writer 
   * @throws IOException 
   */
  static void conditionToJSON(Condition condition, OutputStreamWriter writer) throws IOException {
    moduleToJSON(condition, writer);
    Set<Connection> connections = condition.getConnections();
    if (connections != null && !connections.isEmpty()) {
      Map<String, Object> configValues = condition.getConfiguration();
      if (configValues != null && !configValues.isEmpty())
        writer.write(",\n");
      writer.write("        \"" + JSONStructureConstants.INPUT + "\":{\n");
      Iterator<Connection> connectionsI = connections.iterator();
      while (connectionsI.hasNext()) {
        Connection connection = (Connection) connectionsI.next();
        if (connectionsI.hasNext()) {
          writer.write("          \"" + connection.getInputName() + "\":\"" + connection.getOuputModuleId() + "."
              + connection.getOutputName() + "\",");
        } else {
          writer.write("          \"" + connection.getInputName() + "\":\"" + connection.getOuputModuleId() + "."
              + connection.getOutputName() + "\"");
        }
        writer.write("\n        }");
      }
    }
  }

  /**
   * This method is used for reversion of {@link Trigger} to JSON format.
   * @param trigger is an {@link Trigger} object to revert.
   * @param writer 
   * @throws IOException 
   */
  static void triggerToJSON(Trigger trigger, OutputStreamWriter writer)
      throws IOException {
    moduleToJSON(trigger, writer);
  }

  /**
   * This method is used for reversion of {@link Module} to JSON format.
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
    Map<String, Object> configValues = module.getConfiguration();
    if (configValues != null && !configValues.isEmpty()) {
      writer.write(",\n        \"" + JSONStructureConstants.CONFIG + "\":{\n");
      Iterator<String> i = configValues.keySet().iterator();
      while (i.hasNext()) {
        String paramName = (String) i.next();
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
  static <T extends Module> void writeModules(ModuleTypeManager moduleManager, List<T> modules, OutputStreamWriter writer)
      throws IOException {
    if (modules != null && !modules.isEmpty()) {
      Iterator<T> i = modules.iterator();
      while (i.hasNext()) {
        T module = (T) i.next();
        writer.write("      \"" + module.getId() + "\":{\n");
        if (module instanceof Trigger)
          ModuleJSONParser.triggerToJSON((Trigger) module, writer);
        if (module instanceof Condition)
          ModuleJSONParser.conditionToJSON((Condition) module, writer);
        if (module instanceof Action)
          ModuleJSONParser.actionToJSON(moduleManager, (Action) module, writer);
        if (i.hasNext())
          writer.write("\n      },\n");
        else
          writer.write("\n      }\n");
      }
    }
  }

  /**
   * 
   * @param bc
   * @param moduleManager
   * @param rule
   * @param status
   * @return
   */
  static boolean validateConnections(BundleContext bc, ModuleTypeManager moduleManager, Object rule, Status status) {

    boolean res = true;

    Map<String, Condition> conditionsMap = convertListToMap(getModules(rule, Condition.class));
    Map<String, Action> actionsMap = convertListToMap(getModules(rule, Action.class));
    Map<String, Trigger> triggersMap = convertListToMap(getModules(rule, Trigger.class));

    if (conditionsMap != null) {
      Iterator<Condition> conditionsI = conditionsMap.values().iterator();
      while (conditionsI.hasNext()) {
        Condition condition = (Condition) conditionsI.next();
        Set<Connection> connections = condition.getConnections();
        res = ConditionTypeJSONParser.validateConditionConnections(bc, connections, condition, triggersMap,
            moduleManager, status);
      }
    }
    if (actionsMap != null) {
      Iterator<Action> actionsI = actionsMap.values().iterator();
      while (actionsI.hasNext()) {
        Action action = (Action) actionsI.next();
        Set<Connection> connections = action.getConnections();
        res = ActionTypeJSONParser.validateActionConnections(bc, connections, action, triggersMap, actionsMap,
            moduleManager, status);
      }
    }
    return res;
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

  /**
   * 
   * @param modules
   * @return
   */
  private static <T extends Module> Map<String, T> convertListToMap(List<T> modules) {
    if (modules == null) {
      return null;
    }
    Iterator<T> modulesI = modules.iterator();
    Map<String, T> modulesMap = new HashMap<String, T>();
    while (modulesI.hasNext()) {
      T module = (T) modulesI.next();
      modulesMap.put(module.getId(), module);
    }
    return modulesMap;
  }

  /**
   * 
   * @param <T>
   * @param rule
   * @param triggers
   * @param conditions
   * @param actions
   */
  private static <T extends Module> List<T> getModules(Object rule, Class<T> moduleClazz) {
    if (rule instanceof Rule)
      return ((Rule) rule).getModules(moduleClazz);
    return ((RuleTemplate) rule).getModules(moduleClazz);
  }

}
