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

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.parser.Status;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * @author Ana Dimova
 *
 */
public class ActionTypeJSONParser {

  /**
   * This method is used for reversion of {@link ActionType} to JSON format.
   * @param actionType is an {@link ActionType} object to revert.
   * @return JSONObject is an object representing the {@link ActionType} in json format.
   * @throws IOException 
 * @throws JSONException 
   */
  static void actionTypeToJSON(ActionType actionType, OutputStreamWriter writer) throws IOException, JSONException {
    ModuleTypeJSONParser.moduleTypeToJSON(actionType, writer);
    Set<Input> inputs = actionType.getInputs();
    if (inputs != null && !inputs.isEmpty()) {
      Set<ConfigDescriptionParameter> configDescriptions = actionType.getConfigurationDescription();
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
    Set<Output> outputs = actionType.getOutputs();
    if (outputs != null && !outputs.isEmpty()) {
      if (inputs != null && !inputs.isEmpty())
        writer.write(",\n    \"" + JSONStructureConstants.OUTPUT + "\":{\n");
      Iterator<Output> outputsI = outputs.iterator();
      while (outputsI.hasNext()) {
        Output output = (Output) outputsI.next();
        writer.write("      \"" + output.getName() + "\":{\n");
        OutputJSONParser.outputToJSON(output, writer);
        if (outputsI.hasNext())
          writer.write("\n      },");
        else
          writer.write("\n      }");
      }
      writer.write("\n    }");
    }
  }

  /**
   * This method is used for reversion of {@link CompositeActionType} to JSON format.
   * @param cActionType is a {@link CompositeActionType} object to revert.
   * @return JSONObject is an object representing the {@link CompositeActionType} in json format.
   * @throws IOException 
 * @throws JSONException 
   */
  static void compositeActionTypeToJSON(CompositeActionType cActionType, OutputStreamWriter writer,
      ModuleTypeManager moduleManager) throws IOException, JSONException {
    ActionTypeJSONParser.actionTypeToJSON(cActionType, writer);
    List<Action> actions = cActionType.getModules();
    Set<ConfigDescriptionParameter> configDescriptions = cActionType.getConfigurationDescription();
    if (configDescriptions != null && !configDescriptions.isEmpty())
      writer.write(",\n");
    writer.write("    " + JSONStructureConstants.ACTIONS + ":{\n");
    ModuleJSONParser.writeModules(moduleManager, actions, writer);
    writer.write("    }");
  }

  /**
   * 
   * @param bc 
   * @param connections
   * @param action
   * @param triggersMap
   * @param actionsMap
   * @param moduleManager
   * @throws ClassNotFoundException 
   */
  static boolean validateActionConnections(BundleContext bc, Set<Connection> connections, Action action,
      Map<String, Trigger> triggersMap, Map<String, Action> actionsMap, ModuleTypeManager moduleManager, Status status) {
    boolean res = true;
    ActionType actionModule = moduleManager.getType(action.getTypeUID());
    Map<String, Input> inputs = InputJSONParser.convertInputToMap(actionModule.getInputs());
    Iterator<Connection> connectionsI = connections.iterator();
    while (connectionsI.hasNext()) {
      Connection connection = (Connection) connectionsI.next();
      String inputType = ((Input) inputs.get(connection.getInputName())).getType();
      String moduleId = connection.getOuputModuleId();
      String inputName = connection.getInputName();
      String outputName = connection.getOutputName();
      String msg = "Connection \"" + inputName + ":" + moduleId + "." + outputName
          + "\" in the Action with ID \"" + action.getId() + "\" is invalid!";
      Trigger trigger = (Trigger) triggersMap.get(moduleId);
      Map<String, Output> outputs;
      if (trigger != null) {
        String triggerTypeUID = trigger.getTypeUID();
        TriggerType triggerType = moduleManager.getType(triggerTypeUID);
        if (triggerType == null) {
          status.error(msg + " Trigger Type with UID \"" + triggerTypeUID + "\" not exists!",
              new IllegalArgumentException());
          res = false;
          continue;
        }
        outputs = OutputJSONParser.convertOutputToMap(triggerType.getOutputs());
      } else {
        Action processor = (Action) actionsMap.get(moduleId);
        if (processor == null)
          status.error(msg + " Action " + moduleId + " not exists!", new IllegalArgumentException());
        String processorTypeUID = processor.getTypeUID();
        ActionType processorType = moduleManager.getType(processorTypeUID);
        if (processorType == null) {
          status.error(msg + " Action Type with UID \"" + processorTypeUID + "\" not exists!",
              new IllegalArgumentException());
          res = false;
          continue;
        }
        outputs = OutputJSONParser.convertOutputToMap(processorType.getOutputs());
      }
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
      status.error(msg + " Output with name \"" + outputName + "\" not exists in the Module with ID \""
          + moduleId + "\"", new IllegalArgumentException());
      res = false;
    }
    return res;
  }

  /**
   * @param actions
   * @param conditions
   * @param triggers
   * @param writer
   * @throws IOException 
 * @throws JSONException 
   */
  static void writeActionTypes(Map<String, ActionType> actions,
      Map<String, ConditionType> conditions,
      Map<String, TriggerType> triggers, OutputStreamWriter writer)
      throws IOException, JSONException {
  if (!actions.isEmpty()) {
      if (triggers.isEmpty() && conditions.isEmpty())
        writer.write(" " + JSONStructureConstants.ACTIONS + ":{\n");
      else
        writer.write(",\n " + JSONStructureConstants.ACTIONS + ":{\n");
      Iterator<String> actionsI = actions.keySet().iterator();
      while (actionsI.hasNext()) {
        String actionUID = (String) actionsI.next();
        ActionType action = (ActionType) actions.get(actionUID);
        writer.write("  \"" + actionUID + "\":{\n");
        ActionTypeJSONParser.actionTypeToJSON(action, writer);
        if (actionsI.hasNext())
          writer.write("\n  },\n");
        else
          writer.write("\n  }\n");
      }
      writer.write(" }");
    }
  }

}
