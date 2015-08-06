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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.FilterCriteria;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class serves for creating the Auxiliary automation objects (ConfigDescriptionParameter, Input, Output,
 * Connection) from JSON Objects.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class ConfigPropertyJSONParser {

    static final Object FAIL = new Object();
    static final Object SUCCESS = new Object();

    /**
     * This method is used for creation of {@link ConfigDescriptionParameter} from its JSONObject representation.
     *
     * @param configPropertyName is a name of a configuration parameter.
     * @param configDescription is a JSON description of the configuration parameter.
     * @param status is used to log the errors and to return the result of creation of
     *            {@link ConfigDescriptionParameter}.
     * @return the newly created {@link ConfigDescriptionParameter} if the creation is successful
     *         or <code>null</code> if it is unsuccessful.
     */
    static ConfigDescriptionParameter createConfigPropertyDescription(String configPropertyName,
            JSONObject configDescription, Status status) {
        String typeStr = JSONUtility.getString(JSONStructureConstants.TYPE, false, configDescription, status);
        if (typeStr == null) {
            return null;
        }
        typeStr = typeStr.toUpperCase();
        List<FilterCriteria> filter = new ArrayList<FilterCriteria>();
        JSONArray jsonFilter = JSONUtility.getJSONArray(JSONStructureConstants.FILTER_CRITERIA, true, configDescription,
                status);
        if (jsonFilter != null) {
            for (int i = 0; i < jsonFilter.length(); i++) {
                JSONObject criteria = JSONUtility.getJSONObject(JSONStructureConstants.FILTER_CRITERIA, i, jsonFilter,
                        status);
                if (criteria == null) {
                    continue;
                }
                String name = JSONUtility.getString(JSONStructureConstants.NAME, false, criteria, status);
                String val = JSONUtility.getString(JSONStructureConstants.VALUE, false, criteria, status);
                if (name == null || val == null) {
                    continue;
                }
                filter.add(new FilterCriteria(name, val));
            }
        }
        List<ParameterOption> options = new ArrayList<ParameterOption>();
        JSONArray jsonOptions = JSONUtility.getJSONArray(JSONStructureConstants.OPTIONS, true, configDescription,
                status);
        if (jsonOptions != null) {
            for (int i = 0; i < jsonOptions.length(); i++) {
                JSONObject option = JSONUtility.getJSONObject(JSONStructureConstants.OPTIONS, i, jsonOptions, status);
                if (option == null) {
                    continue;
                }
                String labelOption = JSONUtility.getString(JSONStructureConstants.LABEL, false, option, status);
                String val = JSONUtility.getString(JSONStructureConstants.VALUE, false, option, status);
                if (labelOption == null || val == null) {
                    continue;
                }
                options.add(new ParameterOption(val, labelOption));
            }
        }
        String label = JSONUtility.getString(JSONStructureConstants.LABEL, true, configDescription, status);
        String pattern = JSONUtility.getString(JSONStructureConstants.PATTERN, true, configDescription, status);
        String context = JSONUtility.getString(JSONStructureConstants.CONTEXT, true, configDescription, status);
        String description = JSONUtility.getString(JSONStructureConstants.DESCRIPTION, true, configDescription, status);
        BigDecimal min = (BigDecimal) JSONUtility.getNumber(JSONStructureConstants.MIN, true, configDescription,
                status);
        BigDecimal max = (BigDecimal) JSONUtility.getNumber(JSONStructureConstants.MAX, true, configDescription,
                status);
        BigDecimal step = (BigDecimal) JSONUtility.getNumber(JSONStructureConstants.STEP, true, configDescription,
                status);
        Boolean required = JSONUtility.getBoolean(JSONStructureConstants.REQUIRED, true, false, configDescription,
                status);
        Boolean multiple = JSONUtility.getBoolean(JSONStructureConstants.MULTIPLE, true, false, configDescription,
                status);
        Boolean readOnly = JSONUtility.getBoolean(JSONStructureConstants.READ_ONLY, true, false, configDescription,
                status);

        String groupName = null;
        Boolean advanced = false;
        Boolean limitToOptions = true;
        Integer multipleLimit = 0;

        try {
            String defValue = getDefaultValue(configPropertyName, typeStr, configDescription, status);
            return new ConfigDescriptionParameter(configPropertyName, Type.valueOf(typeStr), max, min, step, pattern,
                    required, readOnly, multiple, context, defValue, label, description, options, filter, groupName,
                    advanced, limitToOptions, multipleLimit);
        } catch (IllegalArgumentException iae) {
            status.error("Failed to create ConfigDescriptionParameter " + configPropertyName, iae);
            return null;
        }
    }

    /**
     * This method is used to provide to the map "config" the pairs of configuration parameter names and configuration
     * parameter values.
     *
     * @param jsonConfig is a JSON representation of the Configuration.
     * @return a map with pairs of configuration parameter names and configuration parameter values.
     */
    static Map<String, Object> getConfigurationValues(JSONObject jsonConfig) {
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
                e.printStackTrace();
            }
        }
        return configurations;
    }

    /**
     * Supplies to the map "config" the pairs of configuration parameter names and configuration parameter values.
     * Also completes the set with the descriptions of the configuration parameters.
     *
     * @param jsonConfigInfo is a JSON representation of the Configuration.
     *            It contains descriptions of the parameters and parameter values.
     * @return a map with pairs of configuration parameter names and configuration parameter values.
     */
    static Map<String, Object> getConfiguration(JSONObject jsonConfigInfo,
            Set<ConfigDescriptionParameter> configDescriptions, Status status) {
        Map<String, Object> configurations = new HashMap<String, Object>();
        boolean res = true;
        Iterator<?> i = jsonConfigInfo.keys();
        while (i.hasNext()) {
            Object configValue = null;
            String configPropertyName = (String) i.next();
            JSONObject configPropertyInfo = JSONUtility.getJSONObject(configPropertyName, false, jsonConfigInfo,
                    status);
            if (configPropertyInfo == null) {
                continue;
            }
            ConfigDescriptionParameter configProperty = ConfigPropertyJSONParser
                    .createConfigPropertyDescription(configPropertyName, configPropertyInfo, status);
            if (configProperty == null) {
                continue;
            }
            configDescriptions.add(configProperty);

            if (configPropertyInfo.has(JSONStructureConstants.VALUE)) {
                configValue = configPropertyInfo.opt(JSONStructureConstants.VALUE);
            }
            Object value = processValue(configValue, configProperty, status);
            if (value != null) {
                if (value == FAIL)
                    res = false;
                else if (value != SUCCESS)
                    configurations.put(configPropertyName, value);
            }
        }
        if (res)
            return configurations;
        return null;
    }

    /**
     * Processing the Value in order to check its type.
     *
     * @param configValue the Configuration property value.
     * @param configProperty the Configuration property ConfigDescriptionParameter
     * @param status Status from the operation performed
     * @return
     */
    static Object processValue(Object configValue, ConfigDescriptionParameter configProperty, Status status) {
        if (!JSONObject.NULL.equals(configValue) || JSONObject.NULL != configValue && configValue != null) {
            return checkType(configValue, configProperty, status);
        }
        configValue = configProperty.getDefault();
        if (!JSONObject.NULL.equals(configValue) || JSONObject.NULL != configValue && configValue != null) {
            return checkType(configValue, configProperty, status) == FAIL ? FAIL : SUCCESS;
        }
        if (configProperty.isRequired()) {
            status.error("Required configuration property missing: \"" + configProperty.getName() + "\"!",
                    new IllegalArgumentException());
        }
        return null;
    }

    /**
     * Checks type of given ConfigurationProperty value
     *
     * @param configValue the ConfigurationProperty value
     * @param configProperty the ConfigurationDescriptionProperty
     * @param status status object
     * @return configurationProperty value in its correct type
     */
    static Object checkType(Object configValue, ConfigDescriptionParameter configProperty, Status status) {
        Object value = null;
        Type type = configProperty.getType();
        if (configProperty.isMultiple()) {
            if (configValue instanceof JSONArray) {
                int size = ((JSONArray) configValue).length();
                for (int index = 0; index < size; index++) {
                    if (Type.TEXT.equals(type))
                        value = JSONUtility.getString(configProperty.getName(), index, (JSONArray) configValue, status);
                    else if (Type.BOOLEAN.equals(type))
                        value = JSONUtility.getBoolean(configProperty.getName(), index, (JSONArray) configValue,
                                status);
                    else
                        value = JSONUtility.getNumber(configProperty.getName(), index, (JSONArray) configValue, status);
                    if (JSONUtility.verifyType(type, value, status) == null) {
                        return FAIL;
                    }
                }
                return toList((JSONArray) configValue);
            }
            status.error(
                    "Unexpected value for configuration property \"" + configProperty.getName()
                            + "\". Expected is JSONArray with type for elements : " + type.toString() + "!",
                    new IllegalArgumentException());
        } else {
            value = JSONUtility.verifyType(type, configValue, status);
        }
        return value == null ? FAIL : value;
    }

    /**
     * This method converts {@link ConfigDescriptionParameter} to JSON format.
     *
     * @param configParameter the {@link ConfigDescriptionParameter} to convert.
     * @param writer is the {@link OutputStreamWriter} used to encode into bytes the {@link ConfigDescriptionParameter}.
     *
     * @return JSONObject representing the {@link ConfigDescriptionParameter}.
     * @throws IOException
     * @throws JSONException
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
     *
     * @param configParameter
     * @param value
     * @param writer
     * @throws IOException
     * @throws JSONException
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
     * @param jsonModuleType ModuleType in json format
     * @param status status object
     * @return collection with ConfigurationDescriptionParameters
     */
    @SuppressWarnings("unchecked")
    static LinkedHashSet<ConfigDescriptionParameter> initializeConfigDescriptions(JSONObject jsonModuleType,
            Status status) {
        LinkedHashSet<ConfigDescriptionParameter> configDescriptions = new LinkedHashSet<ConfigDescriptionParameter>();
        JSONObject jsonConfigDescriptions = JSONUtility.getJSONObject(JSONStructureConstants.CONFIG, true,
                jsonModuleType, status);
        if (jsonConfigDescriptions != null) {
            Iterator<String> configI = jsonConfigDescriptions.keys();
            while (configI.hasNext()) {
                String configPropertyName = configI.next();
                JSONObject configPropertyInfo = JSONUtility.getJSONObject(configPropertyName, false,
                        jsonConfigDescriptions, status);
                if (configPropertyInfo == null) {
                    return null;
                }
                ConfigDescriptionParameter configProperty = ConfigPropertyJSONParser
                        .createConfigPropertyDescription(configPropertyName, configPropertyInfo, status);
                if (configProperty == null) {
                    return null;
                }
                configDescriptions.add(configProperty);
            }
        }
        return configDescriptions;
    }

    private static String getDefaultValue(String configPropertyName, String typeStr, JSONObject configDescription,
            Status status) throws IllegalArgumentException {
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
            defValue = JSONUtility.verifyType(Type.valueOf(typeStr), jsonDefValue, status);
            if (defValue == null)
                invalid = true;
        }
        if (invalid)
            throw new IllegalArgumentException();
        return defValue;
    }

    private static JSONObject filterCriteriaToJSON(FilterCriteria filter) {
        Map<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("name", filter.getName());
        filterMap.put("value", filter.getValue());
        return new JSONObject(filterMap);
    }

    private static JSONObject parameterOptionToJSON(ParameterOption option) {
        Map<String, String> optionMap = new HashMap<String, String>();
        optionMap.put("label", option.getLabel());
        optionMap.put("value", option.getValue());
        return new JSONObject(optionMap);
    }

    private static List<Object> toList(JSONArray jsonArr) {
        List<Object> list = null;
        if (jsonArr != null) {
            list = new ArrayList<Object>();
            for (int i = 0; i < jsonArr.length(); i++) {
                try {
                    list.add(jsonArr.get(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

}
