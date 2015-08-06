/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.json;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.json.JSONException;

/**
 * Parser for ActionTypes.
 * 
 * @author Ana Dimova - Initial Contribution
 *
 */
public class ActionTypeJSONParser {

    /**
     * This method is used for reversion of {@link ActionType} to JSON format.
     *
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
                Input input = inputsI.next();
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
                Output output = outputsI.next();
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
     *
     * @param cActionType is a {@link CompositeActionType} object to revert.
     * @return JSONObject is an object representing the {@link CompositeActionType} in json format.
     * @throws IOException
     * @throws JSONException
     */
    static void compositeActionTypeToJSON(CompositeActionType cActionType, OutputStreamWriter writer)
            throws IOException, JSONException {
        ActionTypeJSONParser.actionTypeToJSON(cActionType, writer);
        List<Action> actions = cActionType.getModules();
        Set<ConfigDescriptionParameter> configDescriptions = cActionType.getConfigurationDescription();
        if (configDescriptions != null && !configDescriptions.isEmpty())
            writer.write(",\n");
        writer.write("    " + JSONStructureConstants.ACTIONS + ":{\n");
        ModuleJSONParser.writeModules(actions, writer);
        writer.write("    }");
    }

    /**
     * Writes ActionTypes to the provided OutputStreamWriter.
     *
     * @param actions ActionTypes to be written
     * @param conditions ConditionTypes
     * @param triggers TriggerTypes
     * @param writer where ActionTypes will be written
     * @throws IOException
     * @throws JSONException
     */
    static void writeActionTypes(Map<String, ActionType> actions, Map<String, ConditionType> conditions,
            Map<String, TriggerType> triggers, OutputStreamWriter writer) throws IOException, JSONException {
        if (!actions.isEmpty()) {
            if (triggers.isEmpty() && conditions.isEmpty())
                writer.write(" " + JSONStructureConstants.ACTIONS + ":{\n");
            else
                writer.write(",\n " + JSONStructureConstants.ACTIONS + ":{\n");
            Iterator<String> actionsI = actions.keySet().iterator();
            while (actionsI.hasNext()) {
                String actionUID = actionsI.next();
                ActionType action = actions.get(actionUID);
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
