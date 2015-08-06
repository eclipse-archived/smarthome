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

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.json.JSONException;

/**
 * Parser for ConditionTypes.
 * 
 * @author Ana Dimova - Initial Contribution
 *
 */
public class ConditionTypeJSONParser {

    /**
     * This method is used for reversion of {@link ConditionType} to JSON format.
     *
     * @param conditionType is a {@link ConditionType} object to revert.
     * @return JSONObject is an object representing the {@link ConditionType} in json format.
     * @throws IOException
     * @throws JSONException
     */
    static void conditionTypeToJSON(ConditionType conditionType, OutputStreamWriter writer)
            throws IOException, JSONException {
        ModuleTypeJSONParser.moduleTypeToJSON(conditionType, writer);
        Set<Input> inputs = conditionType.getInputs();
        if (inputs != null && !inputs.isEmpty()) {
            Set<ConfigDescriptionParameter> configDescriptions = conditionType.getConfigurationDescription();
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
    }

    /**
     * This method is used for reversion of {@link CompositeConditionType} to JSON format.
     *
     * @param cConditionType is a {@link CompositeConditionType} object to revert.
     * @return JSONObject is an object representing the {@link CompositeConditionType} in json format.
     * @throws IOException
     * @throws JSONException
     */
    static void compositeConditionTypeToJSON(CompositeConditionType cConditionType, OutputStreamWriter writer)
            throws IOException, JSONException {
        conditionTypeToJSON(cConditionType, writer);
        List<Condition> conditions = cConditionType.getModules();
        Set<ConfigDescriptionParameter> configDescriptions = cConditionType.getConfigurationDescription();
        if (configDescriptions != null && !configDescriptions.isEmpty())
            writer.write(",\n");
        writer.write("    " + JSONStructureConstants.CONDITIONS + ":{\n");
        ModuleJSONParser.writeModules(conditions, writer);
        writer.write("    }");
    }

    /**
     * Writes ConditionTypes to the provided OutputStreamWriter.
     *
     * @param conditions conditionTypes that will be written
     * @param triggers triggerTypes
     * @param writer where ConditionTypes will be written
     * @throws IOException
     * @throws JSONException
     */
    static void writeConditionTypes(Map<String, ConditionType> conditions, Map<String, TriggerType> triggers,
            OutputStreamWriter writer) throws IOException, JSONException {
        if (!conditions.isEmpty()) {
            if (triggers.isEmpty())
                writer.write(" " + JSONStructureConstants.CONDITIONS + ":{\n");
            else
                writer.write(",\n " + JSONStructureConstants.CONDITIONS + ":{\n");
            Iterator<String> conditionsI = conditions.keySet().iterator();
            while (conditionsI.hasNext()) {
                String conditionUID = conditionsI.next();
                ConditionType condition = conditions.get(conditionUID);
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
