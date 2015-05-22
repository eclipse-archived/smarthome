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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.osgi.framework.BundleContext;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.parser.Status;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * @author Ana Dimova
 *
 */
public class ConditionTypeJSONParser {

  /**
   * This method is used for reversion of {@link ConditionType} to JSON format. 
   * @param conditionType is a {@link ConditionType} object to revert.
   * @return JSONObject is an object representing the {@link ConditionType} in json format.
   * @throws IOException 
 * @throws JSONException 
   */
  static void conditionTypeToJSON(ConditionType conditionType, OutputStreamWriter writer) throws IOException, JSONException {
    ModuleTypeJSONParser.moduleTypeToJSON(conditionType, writer);
    Set<Input> inputs = conditionType.getInputs();
    if (inputs != null && !inputs.isEmpty()) {
      Set<ConfigDescriptionParameter> configDescriptions = conditionType.getConfigurationDescription();
      if (configDescriptions != null && !configDescriptions.isEmpty())
        writer.write(",\n    \"" + JSONStructureConstants.INPUT + "\":{\n");
      Iterator<Input> inputsI = inputs.iterator();
      while (inputsI.hasNext()) {
        Input input = (Input) inputsI.next();
        writer.write("      \"" + input.getName() + "\":{\n");
        InputJSONParser.inputToJSON(input, writer);
        if (inputsI.hasNext())
          writer.write("\n      },");
        else
          writer.write("\n      }");
      }
      writer.write("\n    }");
    }
  }

  /**
   * This method is used for reversion of {@link CompositeConditionType} to JSON format.
   * @param cConditionType is a {@link CompositeConditionType} object to revert.
   * @return JSONObject is an object representing the {@link CompositeConditionType} in json format.
   * @throws IOException 
 * @throws JSONException 
   */
  static void compositeConditionTypeToJSON(CompositeConditionType cConditionType, OutputStreamWriter writer,
      ModuleTypeManager moduleManager) throws IOException, JSONException {
    conditionTypeToJSON(cConditionType, writer);
    List<Condition> conditions = cConditionType.getModules();
    Set<ConfigDescriptionParameter> configDescriptions = cConditionType.getConfigurationDescription();
    if (configDescriptions != null && !configDescriptions.isEmpty())
      writer.write(",\n");
    writer.write("    " + JSONStructureConstants.CONDITIONS + ":{\n");
    ModuleJSONParser.writeModules(moduleManager, conditions, writer);
    writer.write("    }");
  }

  /**
   * 
   * @param connections
   * @param condition
   * @param triggersMap
   * @param moduleManager
   * @throws ClassNotFoundException 
   * @throws IllegalArgumentException 
   */
  static boolean validateConditionConnections(BundleContext bc, Set<Connection> connections, Condition condition,
      Map<String, Trigger> triggersMap, ModuleTypeManager moduleManager, Status status) {
    boolean res = true;
    ConditionType conditionType = moduleManager.getType(condition.getTypeUID());
    Map<String, Input> inputs = InputJSONParser.convertInputToMap(conditionType.getInputs());
    Iterator<Connection> connectionsI = connections.iterator();
    while (connectionsI.hasNext()) {
      Connection connection = (Connection) connectionsI.next();
      String moduleId = connection.getOuputModuleId();
      String inputName = connection.getInputName();
      String outputName = connection.getOutputName();
      String msg = "Connection \"" + inputName + ":" + moduleId + "." + outputName
          + "\" in the Condition with ID \"" + condition.getId() + "\" is invalid!";
      String inputType = ((Input) inputs.get(inputName)).getType();
      Trigger trigger = (Trigger) triggersMap.get(moduleId);
      if (trigger == null) {
        status.error(msg + " Trigger with ID \"" + moduleId + "\" not exists!", new IllegalArgumentException());
        res = false;
        continue;
      }
      String triggerTypeUID = trigger.getTypeUID();
      TriggerType triggerType = moduleManager.getType(triggerTypeUID);
      if (triggerType == null) {
        status.error(msg + " Trigger Type with UID \"" + triggerTypeUID + "\" not exists!",
            new IllegalArgumentException());
        res = false;
        continue;
      }
      Map<String, Output> outputs = OutputJSONParser.convertOutputToMap(triggerType.getOutputs());
      if (outputs != null) {
        Output output = outputs.get(outputName);
        if (output != null)
          try {
            JSONUtility.verifyType(bc, output.getType(), inputType);
            continue;
          } catch (IllegalArgumentException e) {
            status.error(msg, e);
            res = false;
            continue;
          } catch (ClassNotFoundException e) {
            status.error(msg, e);
            res = false;
            continue;
          }
      }
      status.error(msg + " Output with name \"" + outputName + "\" not exists in the Trigger with ID \""
          + moduleId + "\"", new IllegalArgumentException());
      res = false;
    }
    return res;
  }

  /**
   * @param conditions
   * @param triggers
   * @param writer
   * @throws IOException 
   * @throws JSONException 
   */
  static void writeConditionTypes(Map<String, ConditionType> conditions, Map<String, TriggerType> triggers, OutputStreamWriter writer) throws IOException, JSONException {
    if (!conditions.isEmpty()) {
      if (triggers.isEmpty())
        writer.write(" " + JSONStructureConstants.CONDITIONS + ":{\n");
      else
        writer.write(",\n " + JSONStructureConstants.CONDITIONS + ":{\n");
      Iterator<String> conditionsI = conditions.keySet().iterator();
      while (conditionsI.hasNext()) {
        String conditionUID = (String) conditionsI.next();
        ConditionType condition = (ConditionType) conditions.get(conditionUID);
        writer.write("  \"" + conditionUID + "\":{\n");
        ConditionTypeJSONParser.conditionTypeToJSON(condition, writer);
        if (conditionsI.hasNext())
          writer.write("\n  },\n");
        else
          writer.write("\n  }\n");
      }
      writer.write(" }");
    }
  }

}
