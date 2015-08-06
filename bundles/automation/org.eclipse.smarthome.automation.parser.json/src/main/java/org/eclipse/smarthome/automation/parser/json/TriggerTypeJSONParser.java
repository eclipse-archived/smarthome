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

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.json.JSONException;

/**
 * Parser for TriggerTypes.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
class TriggerTypeJSONParser {

    /**
     * This method is used for reversion of {@link TriggerType} to JSON format.
     *
     * @param triggerType is a {@link TriggerType} object to revert.
     * @return JSONObject is an object representing the {@link TriggerType} in json format.
     * @throws IOException
     * @throws JSONException
     */
    static void triggerTypeToJSON(TriggerType triggerType, OutputStreamWriter writer)
            throws IOException, JSONException {
        ModuleTypeJSONParser.moduleTypeToJSON(triggerType, writer);
        Set<Output> outputs = triggerType.getOutputs();
        if (outputs != null && !outputs.isEmpty()) {
            Set<ConfigDescriptionParameter> configDescriptions = triggerType.getConfigurationDescription();
            if (configDescriptions != null && !configDescriptions.isEmpty())
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
     * This method is used for reversion of {@link CompositeTriggerType} to JSON format.
     *
     * @param cTriggerType is a {@link CompositeTriggerType} object to revert.
     * @return JSONObject is an object representing the {@link CompositeTriggerType} in json format.
     * @throws IOException
     * @throws JSONException
     */
    static void compositeTriggerTypeToJSON(CompositeTriggerType cTriggerType, OutputStreamWriter writer)
            throws IOException, JSONException {
        triggerTypeToJSON(cTriggerType, writer);
        List<Trigger> triggers = cTriggerType.getModules();
        Set<ConfigDescriptionParameter> configDescriptions = cTriggerType.getConfigurationDescription();
        if (configDescriptions != null && !configDescriptions.isEmpty())
            writer.write(",\n");
        writer.write("    " + JSONStructureConstants.TRIGGERS + ":{\n");
        ModuleJSONParser.writeModules(triggers, writer);
        writer.write("    }");
    }

    /**
     * @param triggers
     * @param writer
     * @throws IOException
     * @throws JSONException
     */
    static void writeTriggerTypes(Map<String, TriggerType> triggers, OutputStreamWriter writer)
            throws IOException, JSONException {
        if (!triggers.isEmpty()) {
            writer.write(" " + JSONStructureConstants.TRIGGERS + ":{\n");
            Iterator<String> triggersI = triggers.keySet().iterator();
            while (triggersI.hasNext()) {
                String triggerUID = triggersI.next();
                TriggerType trigger = triggers.get(triggerUID);
                writer.write("  \"" + triggerUID + "\":{\n");
                TriggerTypeJSONParser.triggerTypeToJSON(trigger, writer);
                if (triggersI.hasNext())
                    writer.write("\n  },\n");
                else
                    writer.write("\n  }\n");
            }
            writer.write(" }");
        }
    }

}
