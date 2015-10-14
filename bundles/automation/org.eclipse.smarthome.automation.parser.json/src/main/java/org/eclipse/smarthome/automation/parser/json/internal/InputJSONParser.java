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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.smarthome.automation.parser.ParsingNestedException;
import org.eclipse.smarthome.automation.type.Input;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;

/**
 * This class is responsible for parsing the Inputs.
 *
 * @author Ana Dimova - Initial Contribution
 * @author Ana Dimova - refactor Parser interface.
 *
 */
public class InputJSONParser {

    /**
     * Collects Inputs from JSON.
     *
     * @param bc BundleContext
     * @param moduleTypeUID is the unique identifier of the ModuleType.
     * @param jsonInputs Inputs in json format
     * @param exceptions is a list used for collecting the exceptions occurred during {@link Input}s creation.
     * @param log is used for logging the exceptions.
     */
    static LinkedHashSet<Input> collectInputs(BundleContext bc, String moduleTypeUID, JSONObject jsonInputs,
            List<ParsingNestedException> exceptions, Logger log) {
        LinkedHashSet<Input> inputs = new LinkedHashSet<Input>();
        Iterator<?> jsonInputsI = jsonInputs.keys();
        while (jsonInputsI.hasNext()) {
            String inputName = (String) jsonInputsI.next();
            JSONObject inputInfo = JSONUtility.getJSONObject(ParsingNestedException.MODULE_TYPE, moduleTypeUID,
                    exceptions, inputName, false, jsonInputs, log);
            Input input = InputJSONParser.createInput(bc, moduleTypeUID, inputName, inputInfo, exceptions, log);
            if (input != null)
                inputs.add(input);
        }
        return inputs;
    }

    /**
     * This method is used for creation of {@link Input} objects based on its JSON description.
     *
     * @param bc BundleContext
     * @param moduleTypeUID is the unique identifier of the ModuleType.
     * @param inputName is a string representing the name of the {@link Input}.
     * @param jsonInputDescription is a JSON object describing the {@link Input}.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link Input}'s creation.
     * @param log is used for logging the exceptions.
     * @return an object representing the {@link Input} or <code>null</code>.
     */
    static Input createInput(BundleContext bc, String moduleTypeUID, String inputName, JSONObject jsonInputDescription,
            List<ParsingNestedException> exceptions, Logger log) {
        String type = JSONUtility.getString(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.TYPE, false, jsonInputDescription, log);
        if (type == null)
            return null;
        String label = JSONUtility.getString(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.LABEL, true, jsonInputDescription, log);
        String description = JSONUtility.getString(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.DESCRIPTION, true, jsonInputDescription, log);
        boolean required = JSONUtility.getBoolean(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.REQUIRED, true, false, jsonInputDescription, log);
        String reference = JSONUtility.getString(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.REFERENCE, true, jsonInputDescription, log);
        Object defaultValue = jsonInputDescription.has(JSONStructureConstants.DEFAULT_VALUE)
                ? jsonInputDescription.opt(JSONStructureConstants.DEFAULT_VALUE) : null;
        Object convDefaultValue = null;
        if (defaultValue != null) {
            try {
                convDefaultValue = JSONUtility.convertValue(bc, type, defaultValue);
                JSONUtility.verifyType(bc, type, convDefaultValue);
            } catch (Exception e) {
                JSONUtility
                        .catchParsingException(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                                new IllegalArgumentException(
                                        "Failed to create input \"" + inputName + "\" : " + jsonInputDescription, e),
                                log);
            }
        }
        JSONArray jsonTags = JSONUtility.getJSONArray(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                JSONStructureConstants.TAGS, true, jsonInputDescription, log);
        LinkedHashSet<String> tags = null;
        if (jsonTags != null) {
            tags = new LinkedHashSet<String>();
            for (int j = 0; j < jsonTags.length(); j++) {
                String tag = JSONUtility.getString(ParsingNestedException.MODULE_TYPE, moduleTypeUID, exceptions,
                        JSONStructureConstants.TAGS, j, jsonTags, log);
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
     * @throws IOException is used for logging the exceptions.
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
