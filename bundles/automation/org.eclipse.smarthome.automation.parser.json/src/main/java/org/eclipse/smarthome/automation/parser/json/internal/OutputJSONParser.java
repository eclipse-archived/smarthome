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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.automation.parser.ParsingNestedException;
import org.eclipse.smarthome.automation.type.Output;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;

/**
 * This class is responsible for parsing the Outputs.
 *
 * @author Ana Dimova - Initial Contribution
 * @author Ana Dimova - refactor Parser interface.
 *
 */
public class OutputJSONParser {

    /**
     * This method collects Outputs from JSON.
     *
     * @param bc BundleContext
     * @param moduleTypeUID is the unique identifier of the ModuleType.
     * @param jsonOutputs Outputs in JSON format
     * @param exceptions is a list used for collecting the exceptions occurred during {@link Output}s creation.
     * @param log is used for logging the exceptions.
     * @return the collected Outputs.
     */
    static Set<Output> collectOutputs(BundleContext bc, String moduleTypeUID, JSONObject jsonOutputs,
            List<ParsingNestedException> exceptions, Logger log) {
        Set<Output> outputs = new HashSet<Output>();
        Iterator<?> joutputs = jsonOutputs.keys();
        while (joutputs.hasNext()) {
            String outputName = (String) joutputs.next();
            JSONObject outputInfo = JSONUtility.getJSONObject(ParsingNestedException.MODULE_TYPE, moduleTypeUID,
                    exceptions, outputName, false, jsonOutputs, log);
            if (outputInfo == null) {
                continue;
            }
            Output output = OutputJSONParser.createOutput(bc, moduleTypeUID, outputName, outputInfo, exceptions, log);
            if (output != null)
                outputs.add(output);
        }
        return outputs;
    }

    /**
     * This method is used for creation of {@link Output} objects based on its JSON description.
     *
     * @param bc BundleContext
     * @param moduleTypeUID is the unique identifier of the ModuleType.
     * @param outputName is a string representing the name of the {@link Output}.
     * @param jsonOutput is a JSON object describing the {@link Output}.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link Output}'s creation.
     * @param log is used for logging the exceptions.
     * @return an object representing the {@link Output} or <code>null</code>.
     */
    static Output createOutput(BundleContext bc, String moduleTypeUID, String outputName, JSONObject jsonOutput,
            List<ParsingNestedException> exceptions, Logger log) {
        String type = JSONUtility.getString(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.TYPE, false, jsonOutput, log);
        if (type == null)
            return null;
        String label = JSONUtility.getString(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.LABEL, true, jsonOutput, log);
        String description = JSONUtility.getString(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.DESCRIPTION, true, jsonOutput, log);
        String reference = JSONUtility.getString(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.REFERENCE, false, jsonOutput, log);
        if (reference == null)
            return null;
        Object defaultValue = jsonOutput.has(JSONStructureConstants.DEFAULT_VALUE)
                ? jsonOutput.opt(JSONStructureConstants.DEFAULT_VALUE) : null;
        Object convDefaultValue = null;
        if (defaultValue != null) {
            try {
                convDefaultValue = JSONUtility.convertValue(bc, type, defaultValue);
                JSONUtility.verifyType(bc, type, convDefaultValue);
            } catch (Exception e) {
                JSONUtility.catchParsingException(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                        new IllegalArgumentException(
                                "Failed to create default value for output \"" + outputName + "\" : " + jsonOutput, e),
                        log);
                return null;
            }
        }
        Set<String> tags = getTags(moduleTypeUID, jsonOutput, exceptions, log);
        return new Output(outputName, type, label, description, tags, reference, convDefaultValue);
    }

    /**
     * This method parses the tags of the Output from json format.
     *
     * @param moduleTypeUID is the unique identifier of the ModuleType.
     * @param jsonOutput is a json representation of Output.
     * @param exceptions is a list used for collecting the exceptions occurred during tags creation.
     * @param log is used for logging the exceptions.
     * @return a set of parsed tags.
     */
    static Set<String> getTags(String moduleTypeUID, JSONObject jsonOutput, List<ParsingNestedException> exceptions,
            Logger log) {
        JSONArray jsonTags = JSONUtility.getJSONArray(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.TAGS, true, jsonOutput, log);
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
     * This method sSerializes the {@link Output} to formated JSON string and write it by the {@link OutputStreamWriter}
     * .
     *
     * @param output is an {@link Output} for serializing.
     * @param writer is the {@link OutputStreamWriter} for writing the {@link Output}.
     * @throws IOException when I/O operation has failed or has been interrupted
     */
    static void outputToJSON(Output output, OutputStreamWriter writer) throws IOException {
        writer.write("        \"" + JSONStructureConstants.TYPE + "\":\"" + output.getType() + "\",\n");
        String label = output.getLabel();
        if (label != null)
            writer.write("        \"" + JSONStructureConstants.LABEL + "\":\"" + label + "\",\n");
        String description = output.getDescription();
        if (description != null)
            writer.write("        \"" + JSONStructureConstants.DESCRIPTION + "\":\"" + description + "\",\n");
        writer.write("        \"" + JSONStructureConstants.REFERENCE + "\":\"" + output.getReference() + "\"");
    }

}
