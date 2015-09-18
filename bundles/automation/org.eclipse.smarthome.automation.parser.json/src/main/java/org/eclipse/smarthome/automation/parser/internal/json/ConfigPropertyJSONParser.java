/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.internal.json;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.parser.ParsingNestedException;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.FilterCriteria;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 * This class serves for creating the Auxiliary automation objects (ConfigDescriptionParameter, Input, Output,
 * Connection) from JSON Objects.
 *
 * @author Ana Dimova - Initial Contribution
 * @author Ana Dimova - refactor Parser interface.
 *
 */
public class ConfigPropertyJSONParser {

    /**
     * This method is used for creation of {@link ConfigDescriptionParameter} from its JSONObject representation.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param UID is the unique identifier of the automation object - module type, rule or rule template.
     * @param exceptions is a list used for collecting the exceptions occurred during ConfigDescriptionParameter
     *            creation.
     * @param configPropertyName is a name of a configuration parameter.
     * @param configDescription is a JSON description of the configuration parameter.
     * @param log is used for logging the exceptions.
     * @return the newly created {@link ConfigDescriptionParameter}.
     */
    static ConfigDescriptionParameter createConfigPropertyDescription(int type, String UID,
            List<ParsingNestedException> exceptions, String configPropertyName, JSONObject configDescription,
            Logger log) {
        String typeStr = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.TYPE, false,
                configDescription, log);
        if (typeStr != null) {
            typeStr = typeStr.toUpperCase();
        }
        List<FilterCriteria> filter = new ArrayList<FilterCriteria>();
        JSONArray jsonFilter = JSONUtility.getJSONArray(type, UID, exceptions, JSONStructureConstants.FILTER_CRITERIA,
                true, configDescription, log);
        if (jsonFilter != null) {
            for (int i = 0; i < jsonFilter.length(); i++) {
                JSONObject criteria = JSONUtility.getJSONObject(type, UID, exceptions,
                        JSONStructureConstants.FILTER_CRITERIA, i, jsonFilter, log);
                if (criteria == null) {
                    continue;
                }
                String name = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.NAME, false, criteria,
                        log);
                String val = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.VALUE, false, criteria,
                        log);
                filter.add(new FilterCriteria(name, val));
            }
        }
        List<ParameterOption> options = new ArrayList<ParameterOption>();
        JSONArray jsonOptions = JSONUtility.getJSONArray(type, UID, exceptions, JSONStructureConstants.OPTIONS, true,
                configDescription, log);
        if (jsonOptions != null) {
            for (int i = 0; i < jsonOptions.length(); i++) {
                JSONObject option = JSONUtility.getJSONObject(type, UID, exceptions, JSONStructureConstants.OPTIONS, i,
                        jsonOptions, log);
                if (option == null) {
                    continue;
                }
                String labelOption = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.LABEL, false,
                        option, log);
                String val = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.VALUE, false, option,
                        log);
                options.add(new ParameterOption(val, labelOption));
            }
        }
        String label = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.LABEL, true,
                configDescription, log);
        String pattern = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.PATTERN, true,
                configDescription, log);
        String context = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.CONTEXT, true,
                configDescription, log);
        String description = JSONUtility.getString(type, UID, exceptions, JSONStructureConstants.DESCRIPTION, true,
                configDescription, log);
        BigDecimal min = (BigDecimal) JSONUtility.getNumber(type, UID, exceptions, JSONStructureConstants.MIN, true,
                configDescription, log);
        BigDecimal max = (BigDecimal) JSONUtility.getNumber(type, UID, exceptions, JSONStructureConstants.MAX, true,
                configDescription, log);
        BigDecimal step = (BigDecimal) JSONUtility.getNumber(type, UID, exceptions, JSONStructureConstants.STEP, true,
                configDescription, log);
        Boolean required = JSONUtility.getBoolean(type, UID, exceptions, JSONStructureConstants.REQUIRED, true, false,
                configDescription, log);
        Boolean multiple = JSONUtility.getBoolean(type, UID, exceptions, JSONStructureConstants.MULTIPLE, true, false,
                configDescription, log);
        Boolean readOnly = JSONUtility.getBoolean(type, UID, exceptions, JSONStructureConstants.READ_ONLY, true, false,
                configDescription, log);

        String groupName = null;
        Boolean advanced = false;
        Boolean limitToOptions = true;
        Integer multipleLimit = 0;

        String defValue = getDefaultValue(configPropertyName, typeStr, configDescription);
        return new ConfigDescriptionParameter(configPropertyName, Type.valueOf(typeStr), max, min, step, pattern,
                required, readOnly, multiple, context, defValue, label, description, options, filter, groupName,
                advanced, limitToOptions, multipleLimit);
    }

    /**
     * This method is used to provide to the map "config" the pairs of configuration parameter names and configuration
     * parameter values.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param UID is the unique identifier of the automation object - module type, rule or rule template.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link ConfigDescriptionParameter}
     *            creation.
     * @param jsonConfig is a JSON representation of the Configuration.
     * @param log is used for logging the exceptions.
     * @return a map with pairs of configuration parameter names and configuration parameter values.
     */
    static Map<String, Object> getConfigurationValues(int type, String UID, List<ParsingNestedException> exceptions,
            JSONObject jsonConfig, Logger log) {
        Map<String, Object> configurations = new HashMap<String, Object>();
        if (jsonConfig == null) {
            return configurations;
        }
        Iterator<?> i = jsonConfig.keys();
        while (i.hasNext()) {
            String configPropertyName = (String) i.next();
            try {
                configurations.put(configPropertyName, jsonConfig.get(configPropertyName));
            } catch (JSONException e) {
                JSONUtility.catchParsingException(type, UID, exceptions, new Throwable(
                        "Failed to get the value for ConfigDescriptionParameter - " + configPropertyName, e), log);
                return null;
            }
        }
        return configurations;
    }

    /**
     * Supplies to the map "config" the pairs of configuration parameter names and configuration parameter values.
     * Also completes the set with the descriptions of the configuration parameters.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param UID is the unique identifier of the automation object - module type, rule or rule template.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link ConfigDescriptionParameter}
     *            creation.
     * @param jsonConfigInfo is a JSON representation of the Configuration.
     *            It contains descriptions of the parameters and parameter values.
     * @param configDescriptions
     * @param log is used for logging the exceptions.
     * @return a map with pairs of configuration parameter names and configuration parameter values.
     */
    static Map<String, Object> getConfiguration(int type, String UID, List<ParsingNestedException> exceptions,
            JSONObject jsonConfigInfo, Set<ConfigDescriptionParameter> configDescriptions, Logger log) {
        Map<String, Object> configurations = new HashMap<String, Object>();
        Iterator<?> i = jsonConfigInfo.keys();
        while (i.hasNext()) {
            Object configValue = null;
            String configPropertyName = (String) i.next();
            JSONObject configPropertyInfo = JSONUtility.getJSONObject(type, UID, exceptions, configPropertyName, false,
                    jsonConfigInfo, log);
            if (configPropertyInfo != null) {
                if (configPropertyInfo.has(JSONStructureConstants.VALUE)) {
                    configValue = configPropertyInfo.opt(JSONStructureConstants.VALUE);
                }
            }
            ConfigDescriptionParameter configProperty = ConfigPropertyJSONParser.createConfigPropertyDescription(type,
                    UID, exceptions, configPropertyName, configPropertyInfo, log);
            if (configProperty != null) {
                configDescriptions.add(configProperty);
            }
            Object value = processValue(type, UID, exceptions, configValue, configProperty, log);
            if (value != null) {
                configurations.put(configPropertyName, value);
            }
        }
        return configurations;
    }

    /**
     * Processing the Value in order to check its type.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param UID is the unique identifier of the automation object - module type, rule or rule template.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link ConfigDescriptionParameter}
     *            creation.
     * @param configValue the Configuration property value.
     * @param configProperty the Configuration property ConfigDescriptionParameter
     * @param log is used for logging the exceptions.
     * @return
     */
    static Object processValue(int type, String UID, List<ParsingNestedException> exceptions, Object configValue,
            ConfigDescriptionParameter configProperty, Logger log) {
        if (!JSONObject.NULL.equals(configValue) || JSONObject.NULL != configValue && configValue != null) {
            return checkType(type, UID, exceptions, configValue, configProperty, log);
        }
        configValue = configProperty.getDefault();
        if (!JSONObject.NULL.equals(configValue) || JSONObject.NULL != configValue && configValue != null) {
            return checkType(type, UID, exceptions, configValue, configProperty, log);
        }
        if (configProperty.isRequired()) {
            JSONUtility
                    .catchParsingException(type, UID, exceptions,
                            new IllegalArgumentException(
                                    "Required configuration property missing: \"" + configProperty.getName() + "\"!"),
                            log);
        }
        return null;
    }

    /**
     * Checks type of given ConfigurationProperty value
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param UID is the unique identifier of the automation object - module type, rule or rule template.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link ConfigDescriptionParameter}
     *            creation.
     * @param configValue the ConfigurationProperty value
     * @param configProperty the ConfigurationDescriptionProperty
     * @param log is used for logging the exceptions.
     * @return configurationProperty value in its correct type
     */
    static Object checkType(int type, String UID, List<ParsingNestedException> exceptions, Object configValue,
            ConfigDescriptionParameter configProperty, Logger log) {
        Object value = null;
        Type configPropertyType = configProperty.getType();
        if (configProperty.isMultiple()) {
            if (configValue instanceof JSONArray) {
                int size = ((JSONArray) configValue).length();
                List<String> list = new ArrayList<>();
                for (int index = 0; index < size; index++) {
                    if (Type.TEXT.equals(configPropertyType))
                        value = JSONUtility.getString(type, UID, exceptions, configProperty.getName(), index,
                                (JSONArray) configValue, log);
                    else if (Type.BOOLEAN.equals(configPropertyType))
                        value = JSONUtility.getBoolean(type, UID, exceptions, configProperty.getName(), index,
                                (JSONArray) configValue, log);
                    else
                        value = JSONUtility.getNumber(type, UID, exceptions, configProperty.getName(), index,
                                (JSONArray) configValue, log);
                    try {
                        list.add(JSONUtility.verifyType(configPropertyType, value));
                    } catch (IllegalArgumentException e) {

                    }
                }
                return list;
            } else
                JSONUtility.catchParsingException(type, UID, exceptions,
                        new IllegalArgumentException("Unexpected value for configuration property \""
                                + configProperty.getName() + "\". Expected is JSONArray with type for elements : "
                                + configPropertyType.toString() + "!"),
                        log);
        } else
            try {
                value = JSONUtility.verifyType(configPropertyType, configValue);
                return value;
            } catch (IllegalArgumentException e) {

            }
        return null;
    }

    /**
     * This method converts {@link ConfigDescriptionParameter} to JSON format.
     *
     * @param configParameter the {@link ConfigDescriptionParameter} to convert.
     * @param writer is the {@link OutputStreamWriter} used to encode into bytes the {@link ConfigDescriptionParameter}.
     *
     * @return JSONObject representing the {@link ConfigDescriptionParameter}.
     * @throws IOException is thrown when the I/O operations are failed or interrupted.
     * @throws JSONException is thrown by the JSON.org classes when things are amiss.
     */
    static void configPropertyToJSON(ConfigDescriptionParameter configParameter, OutputStreamWriter writer)
            throws IOException, JSONException {
        writer.write("        \"" + JSONStructureConstants.TYPE + "\":\""
                + configParameter.getType().toString().toLowerCase() + "\",\n");
        String name = configParameter.getName();
        if (name != null)
            writer.write("        \"" + JSONStructureConstants.NAME + "\":\"" + name + "\",\n");
        String label = configParameter.getLabel();
        if (label != null)
            writer.write("        \"" + JSONStructureConstants.LABEL + "\":\"" + label + "\",\n");
        String description = configParameter.getDescription();
        if (description != null)
            writer.write("        \"" + JSONStructureConstants.DESCRIPTION + "\":\"" + description + "\",\n");
        writer.write("        \"" + JSONStructureConstants.REQUIRED + "\":" + configParameter.isRequired() + ",\n");
        writer.write("        \"" + JSONStructureConstants.READ_ONLY + "\":" + configParameter.isReadOnly() + ",\n");
        writer.write("        \"" + JSONStructureConstants.MULTIPLE + "\":" + configParameter.isMultiple());
        String defaultVal = configParameter.getDefault();
        if (defaultVal != null)
            writer.write(",\n        \"" + JSONStructureConstants.DEFAULT_VALUE + "\":\"" + defaultVal + "\"");
        String context = configParameter.getContext();
        if (context != null) {
            writer.write(",\n        \"" + JSONStructureConstants.CONTEXT + "\":\"" + context);
        }
        List<FilterCriteria> filters = configParameter.getFilterCriteria();
        if (filters != null && !filters.isEmpty()) {
            writer.write(",\n        \"" + JSONStructureConstants.FILTER_CRITERIA + "\":[");
            Iterator<FilterCriteria> i = filters.iterator();
            while (i.hasNext()) {
                FilterCriteria filter = i.next();
                filterCriteriaToJSON(filter).write(writer);
                if (i.hasNext())
                    writer.write(",");
            }
            writer.write("]");
        }
        List<ParameterOption> options = configParameter.getOptions();
        if (options != null && !options.isEmpty()) {
            writer.write(",\n        \"" + JSONStructureConstants.OPTIONS + "\":[");
            Iterator<ParameterOption> i = options.iterator();
            while (i.hasNext()) {
                ParameterOption option = i.next();
                parameterOptionToJSON(option).write(writer);
                if (i.hasNext())
                    writer.write(",");
            }
            writer.write("]");
        }
        String pattern = configParameter.getPattern();
        if (pattern != null)
            writer.write(",\n        \"" + JSONStructureConstants.PATTERN + "\":\"" + pattern + "\"");
        BigDecimal max = configParameter.getMaximum();
        if (max != null)
            writer.write(",\n        \"" + JSONStructureConstants.MAX + "\":" + max);
        BigDecimal min = configParameter.getMinimum();
        if (min != null)
            writer.write(",\n        \"" + JSONStructureConstants.MIN + "\":" + min);
        BigDecimal step = configParameter.getStepSize();
        if (step != null)
            writer.write(",\n        \"" + JSONStructureConstants.STEP + "\":" + step);
    }

    /**
     * Serializes the the {@link ConfigDescriptionParameter} into json.
     *
     * @param configParameter is the {@link ConfigDescriptionParameter} for serializing.
     * @param value is the value for the {@link ConfigDescriptionParameter}.
     * @param writer is the {@link OutputStreamWriter} used to encode into bytes the {@link ConfigDescriptionParameter}.
     * @throws IOException is thrown when the I/O operations are failed or interrupted.
     * @throws JSONException is thrown by the JSON.org classes when things are amiss.
     */
    static void ruleConfigPropertyToJSON(ConfigDescriptionParameter configParameter, Object value,
            OutputStreamWriter writer) throws IOException, JSONException {
        writer.write("        \"" + JSONStructureConstants.TYPE + "\":\""
                + configParameter.getType().toString().toLowerCase() + "\",\n");
        String name = configParameter.getName();
        if (name != null)
            writer.write("        \"" + JSONStructureConstants.NAME + "\":\"" + name + "\",\n");
        String label = configParameter.getLabel();
        if (label != null)
            writer.write("        \"" + JSONStructureConstants.LABEL + "\":\"" + label + "\",\n");
        String description = configParameter.getDescription();
        if (description != null)
            writer.write("        \"" + JSONStructureConstants.DESCRIPTION + "\":\"" + description + "\",\n");
        writer.write("        \"" + JSONStructureConstants.REQUIRED + "\":" + configParameter.isRequired() + ",\n");
        writer.write("        \"" + JSONStructureConstants.READ_ONLY + "\":" + configParameter.isReadOnly() + ",\n");
        writer.write("        \"" + JSONStructureConstants.MULTIPLE + "\":" + configParameter.isMultiple());
        String defaultVal = configParameter.getDefault();
        if (defaultVal != null)
            writer.write(",\n        \"" + JSONStructureConstants.DEFAULT_VALUE + "\":\"" + defaultVal + "\"");
        String context = configParameter.getContext();
        if (context != null) {
            writer.write(",\n        \"" + JSONStructureConstants.CONTEXT + "\":\"" + context);
        }
        List<FilterCriteria> filters = configParameter.getFilterCriteria();
        if (filters != null && !filters.isEmpty()) {
            writer.write(",\n        \"" + JSONStructureConstants.FILTER_CRITERIA + "\":[");
            Iterator<FilterCriteria> i = filters.iterator();
            while (i.hasNext()) {
                FilterCriteria filter = i.next();
                filterCriteriaToJSON(filter).write(writer);
                if (i.hasNext())
                    writer.write(",");
            }
            writer.write("]");
        }
        List<ParameterOption> options = configParameter.getOptions();
        if (options != null && !options.isEmpty()) {
            writer.write(",\n        \"" + JSONStructureConstants.OPTIONS + "\":[");
            Iterator<ParameterOption> i = options.iterator();
            while (i.hasNext()) {
                ParameterOption option = i.next();
                parameterOptionToJSON(option).write(writer);
                if (i.hasNext())
                    writer.write(",");
            }
            writer.write("]");
        }
        String pattern = configParameter.getPattern();
        if (pattern != null)
            writer.write(",\n        \"" + JSONStructureConstants.PATTERN + "\":\"" + pattern + "\"");
        BigDecimal max = configParameter.getMaximum();
        if (max != null)
            writer.write(",\n        \"" + JSONStructureConstants.MAX + "\":" + max);
        BigDecimal min = configParameter.getMinimum();
        if (min != null)
            writer.write(",\n        \"" + JSONStructureConstants.MIN + "\":" + min);
        BigDecimal step = configParameter.getStepSize();
        if (step != null)
            writer.write(",\n        \"" + JSONStructureConstants.STEP + "\":" + step);
        if (value != null)
            writer.write(",\n        \"" + JSONStructureConstants.VALUE + "\":\"" + value + "\"");
    }

    /**
     * Initializes ConfigDescriptions in given ModuleType.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param UID is the unique identifier of the automation object - module type, rule or rule template.
     * @param exceptions is a list used for collecting the exceptions occurred during {@link ConfigDescriptionParameter}
     *            creation.
     * @param jsonModuleType is a ModuleType in json format.
     * @param log is used for logging the exceptions.
     * @return collection with ConfigurationDescriptionParameters
     */
    @SuppressWarnings("unchecked")
    static Set<ConfigDescriptionParameter> initializeConfigDescriptions(int type, String UID,
            List<ParsingNestedException> exceptions, JSONObject jsonModuleType, Logger log) {
        Set<ConfigDescriptionParameter> configDescriptions = new HashSet<ConfigDescriptionParameter>();
        JSONObject jsonConfigDescriptions = JSONUtility.getJSONObject(type, UID, exceptions,
                JSONStructureConstants.CONFIG, true, jsonModuleType, log);
        if (jsonConfigDescriptions != null) {
            Iterator<String> configI = jsonConfigDescriptions.keys();
            while (configI.hasNext()) {
                String configPropertyName = configI.next();
                JSONObject configPropertyInfo = JSONUtility.getJSONObject(type, UID, exceptions, configPropertyName,
                        false, jsonConfigDescriptions, log);
                ConfigDescriptionParameter configProperty = null;
                if (configPropertyInfo != null) {
                    configProperty = ConfigPropertyJSONParser.createConfigPropertyDescription(type, UID, exceptions,
                            configPropertyName, configPropertyInfo, log);
                }
                configDescriptions.add(configProperty);
            }
        }
        return configDescriptions;
    }

    /**
     * Parses the DefaultValue of the ConfigDescriptionParameter.
     *
     * @param configPropertyName is a name of the ConfigDescriptionParameter.
     * @param typeStr is the tyep of the ConfigDescriptionParameter.
     * @param configDescription is a json representation of the ConfigDescriptionParameter.
     * @return parsed DefaultValue of the ConfigDescriptionParameter.
     * @throws IllegalArgumentException if the DefaultValue is invalid.
     */
    private static String getDefaultValue(String configPropertyName, String typeStr, JSONObject configDescription) {
        String defValue = null;
        Object value = null;
        boolean invalid = false;
        if (configDescription.has(JSONStructureConstants.VALUE)) {
            value = configDescription.opt(JSONStructureConstants.VALUE);
        }
        Object jsonDefValue = null;
        if (JSONObject.NULL == value || value == null) {
            if (configDescription.has(JSONStructureConstants.DEFAULT_VALUE)) {
                jsonDefValue = configDescription.opt(JSONStructureConstants.DEFAULT_VALUE);
            }
        } else {
            jsonDefValue = value;
        }
        if (JSONObject.NULL != jsonDefValue && jsonDefValue != null) {
            defValue = JSONUtility.verifyType(Type.valueOf(typeStr), jsonDefValue);
            if (defValue == null)
                invalid = true;
        }
        if (invalid)
            throw new IllegalArgumentException();
        return defValue;
    }

    /**
     * Serializes the FilterCriteria object to json.
     *
     * @param filter is a FilterCriteria object for serializing.
     * @return serialized FilterCriteria object
     */
    private static JSONObject filterCriteriaToJSON(FilterCriteria filter) {
        Map<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("name", filter.getName());
        filterMap.put("value", filter.getValue());
        return new JSONObject(filterMap);
    }

    /**
     * Serializes the ParameterOption object to json.
     *
     * @param option is a ParameterOption object for serializing.
     * @return serialized ParameterOption object
     */
    private static JSONObject parameterOptionToJSON(ParameterOption option) {
        Map<String, String> optionMap = new HashMap<String, String>();
        optionMap.put("label", option.getLabel());
        optionMap.put("value", option.getValue());
        return new JSONObject(optionMap);
    }

}
