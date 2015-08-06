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
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.automation.type.Output;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;

/**
 * Parser for Outputs.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class OutputJSONParser {

    /**
     * Collects Outputs from JSON.
     *
     * @param bc bundleContext
     * @param jsonOutputs Outputs in JSON format
     * @param outputs the collected Outputs
     * @param status Status Object.
     */
    static boolean collectOutputs(BundleContext bc, JSONObject jsonOutputs, Set<Output> outputs, Status status) {
        boolean res = true;
        Iterator<?> joutputs = jsonOutputs.keys();
        while (joutputs.hasNext()) {
            String outputName = (String) joutputs.next();
            JSONObject outputInfo = JSONUtility.getJSONObject(outputName, false, jsonOutputs, status);
            if (outputInfo == null) {
                res = false;
                continue;
            }
            Output output = OutputJSONParser.createOutput(bc, outputName, outputInfo, status);
            if (output != null)
                outputs.add(output);
            else {
                res = false;
            }
        }
        return res;
    }

    /**
     * This method is used for creation of {@link Output} objects based on its JSON description.
     *
     * @param outputName is a string representing the name of the {@link Output}.
     * @param jsonOutputDescription is a JSON object describing the {@link Output}.
     * @param status
     * @return an object representing the {@link Output} or <code>null</code>.
     */
    static Output createOutput(BundleContext bc, String outputName, JSONObject jsonOutputDescription, Status status) {
        String type = JSONUtility.getString(JSONStructureConstants.TYPE, false, jsonOutputDescription, status);
        if (type == null)
            return null;
        String label = JSONUtility.getString(JSONStructureConstants.LABEL, true, jsonOutputDescription, status);
        String description = JSONUtility.getString(JSONStructureConstants.DESCRIPTION, true, jsonOutputDescription,
                status);
        String reference = JSONUtility.getString(JSONStructureConstants.REFERENCE, false, jsonOutputDescription,
                status);
        if (reference == null)
            return null;
        Object defaultValue = jsonOutputDescription.has(JSONStructureConstants.DEFAULT_VALUE)
                ? jsonOutputDescription.opt(JSONStructureConstants.DEFAULT_VALUE) : null;
        Object convDefaultValue = null;
        if (defaultValue != null) {
            try {
                convDefaultValue = JSONUtility.convertValue(bc, type, defaultValue);
                JSONUtility.verifyType(bc, type, convDefaultValue);
            } catch (Exception e) {
                status.error("Failed to create output \"" + outputName + "\" : " + jsonOutputDescription, e);
                return null;
            }
        }
        JSONArray jsonTags = JSONUtility.getJSONArray(JSONStructureConstants.TAGS, true, jsonOutputDescription, status);
        LinkedHashSet<String> tags = new LinkedHashSet<String>();
        if (jsonTags != null) {
            for (int j = 0; j < jsonTags.length(); j++) {
                String tag = JSONUtility.getString(JSONStructureConstants.TAGS, j, jsonTags, status);
                if (tag != null)
                    tags.add(tag);
            }
        }
        return new Output(outputName, type, label, description, tags, reference, convDefaultValue);
    }

    /**
     * Converts the {@link Output} to formated JSON string and write it by the {@link OutputStreamWriter}.
     *
     * @param output
     * @param writer
     * @throws IOException
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
