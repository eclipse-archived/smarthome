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

import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;

import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.automation.type.Input;

/**
 * @author Ana Dimova - Initial Contribution
 *
 */
public class InputJSONParser {

    /**
     *
     * @param bc
     * @param jsonInputs
     * @param inputs
     * @param status
     */
    static boolean collectInputs(BundleContext bc, JSONObject jsonInputs, Set<Input> inputs, Status status) {
        boolean res = true;
        Iterator<?> jsonInputsI = jsonInputs.keys();
        while (jsonInputsI.hasNext()) {
            String inputName = (String) jsonInputsI.next();
            JSONObject inputInfo = JSONUtility.getJSONObject(inputName, false, jsonInputs, status);
            if (inputInfo == null) {
                res = false;
                continue;
            }
            Input input = InputJSONParser.createInput(bc, inputName, inputInfo, status);
            if (input != null)
                inputs.add(input);
            else
                res = false;
        }
        return res;
    }

    /**
     * This method is used for creation of {@link Input} objects based on its JSON description.
     *
     * @param inputName is a string representing the name of the {@link Input}.
     * @param jsonInputDescription is a JSON object describing the {@link Input}.
     * @return an object representing the {@link Input} or <code>null</code>.
     */
    static Input createInput(BundleContext bc, String inputName, JSONObject jsonInputDescription, Status status) {
        String type = JSONUtility.getString(JSONStructureConstants.TYPE, false, jsonInputDescription, status);
        if (type == null)
            return null;
        String label = JSONUtility.getString(JSONStructureConstants.LABEL, true, jsonInputDescription, status);
        String description = JSONUtility.getString(JSONStructureConstants.DESCRIPTION, true, jsonInputDescription,
                status);
        boolean required = JSONUtility.getBoolean(JSONStructureConstants.REQUIRED, true, false, jsonInputDescription,
                status);
        String reference = JSONUtility.getString(JSONStructureConstants.REFERENCE, true, jsonInputDescription, status);
        Object defaultValue = jsonInputDescription.has(JSONStructureConstants.DEFAULT_VALUE)
                ? jsonInputDescription.opt(JSONStructureConstants.DEFAULT_VALUE) : null;
        Object convDefaultValue = null;
        if (defaultValue != null) {
            try {
                convDefaultValue = JSONUtility.convertValue(bc, type, defaultValue);
                JSONUtility.verifyType(bc, type, convDefaultValue);
            } catch (Exception e) {
                status.error("Failed to create input \"" + inputName + "\" : " + jsonInputDescription, e);
                return null;
            }
        }
        JSONArray jsonTags = JSONUtility.getJSONArray(JSONStructureConstants.TAGS, true, jsonInputDescription, status);
        LinkedHashSet<String> tags = null;
        if (jsonTags != null) {
            tags = new LinkedHashSet<String>();
            for (int j = 0; j < jsonTags.length(); j++) {
                String tag = JSONUtility.getString(JSONStructureConstants.TAGS, j, jsonTags, status);
                if (tag != null)
                    tags.add(tag);
            }
        }
        return new Input(inputName, type, label, description, tags, required, reference, convDefaultValue);
    }

    /**
     * Converts the {@link Input} to formated JSON string and write it by the {@link OutputStreamWriter}.
     *
     * @param input the {@link Input}
     * @param writer the {@link OutputStreamWriter}
     * @throws IOException
     */
    static void inputToJSON(Input input, OutputStreamWriter writer) throws IOException {
        writer.write("        \"" + JSONStructureConstants.TYPE + "\":\"" + input.getType() + "\",\n");
        String label = input.getLabel();
        if (label != null)
            writer.write("        \"" + JSONStructureConstants.LABEL + "\":\"" + label + "\",\n");
        String description = input.getDescription();
        if (description != null)
            writer.write("        \"" + JSONStructureConstants.DESCRIPTION + "\":\"" + description + "\",\n");
        writer.write("        \"" + JSONStructureConstants.REFERENCE + "\":\"" + input.getReference() + "\"");
    }

}
