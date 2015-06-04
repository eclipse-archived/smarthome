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

package org.eclipse.smarthome.automation.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.core.type.ModuleTypeManager;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util used for resolving references in {@link ConfigDescriptionParameter} , {@link Input} and {@link Output} in Module
 *
 * @author Vasil Ilchev - Initial Contribution
 */
public class ModuleUtil {
    /**
     * Input/Output <code>reference</code> and Config property <code>context</code> can start with this symbol.
     * i.e. That indicates that Config property points to other Config property.
     */
    public static final String PARSABLE_DOLLAR = "$";
    /**
     * ModuleType UID separator.
     * i.e. SystemModuleType:CustomModuleType
     */
    public static final String MODULE_SEPARATOR = ":";

    private static Logger log;

    /**
     * Resolves references in configuration.
     * Configuration property can set its value to other Configuration properties with its context.
     * {@link ConfigDescriptionParameter#getContext()} i.e. Custom Config property can set its value to System Config
     * property
     *
     * @param moduleTypeUID the ModuleType UID
     * @param configuration the original configuration
     * @return Map with resolved configuration - {@link Module} will be ready to work with<br/>
     *         key: name of the Config property, value: value of the Config property
     */
    public static final Map<String, Object> resolveConfiguration(String moduleTypeUID, Map<String, ?> configuration) {
        Map<String, Object> resolvedConfiguration = null;
        if (configuration != null) {
            Map<String, ConfigDescriptionParameter> configDescriptionsMap = getAllModuleTypeConfigDescriptions(moduleTypeUID);
            if (configDescriptionsMap != null) {
                resolvedConfiguration = new HashMap(); // initialize
                for (ConfigDescriptionParameter configDescription : configDescriptionsMap.values()) {
                    String configName = configDescription.getName();
                    if (!resolvedConfiguration.containsKey(configName)) {
                        Object configValue = configuration.get(configName);
                        if (configValue == null) {// get default value
                            configValue = getConfigDefaultValue(configDescription, configDescriptionsMap, configuration);
                        }
                        resolvedConfiguration.put(configName, configValue);
                        // fill up all referred properties in the chain
                        fillReferredConfigs(configDescription, configValue, configDescriptionsMap,
                                resolvedConfiguration);
                    }
                }
            }
        }

        return resolvedConfiguration;
    }

    /**
     * Resolves references between inputs. Returned Map is later on used in {@link #resolveInputs(Map, Map)}.
     * Input can set its value to other Inputs with its reference. {@link Input#getReference()}.
     * i.e. Custom Input can set its value to System Input.
     *
     * @param moduleTypeUID the ModuleType UID
     * @return Map with resolved Input references.<br/>
     *         key: Input with reference to other Inputs, value: the referred Inputs.
     */
    public static final Map<Input, List<Input>> resolveInputReferences(String moduleTypeUID) {
        Map<Input, List<Input>> resolvedInputReferences = null;
        Map<String, Input> inputDescriptionsMap = getAllModuleTypeInputs(moduleTypeUID);
        if (inputDescriptionsMap != null) {
            resolvedInputReferences = new HashMap(); // initialize
            for (Input input : inputDescriptionsMap.values()) {
                if (isParsable(input.getReference())) {
                    fillReferredInputs(input, inputDescriptionsMap, resolvedInputReferences);
                }
            }
        }

        return resolvedInputReferences;
    }

    /**
     * Resolves Inputs.
     *
     * @param resolvedInputReferences the resolved Input references. Firstly must be called -
     *            {@link #resolveInputReferences(String)}
     * @param inputValues the Input values by the moment (i.e. custom Input values)
     * @return Map with the resolved Inputs values - {@link Module} will be ready to work with<br/>
     *         key: the name of the Input, value: the Input value
     */
    public static final Map<String, Object> resolveInputs(Map<Input, List<Input>> resolvedInputReferences,
            Map<String, ?> inputValues) {

        Map<String, Object> resolvedInputs = null;
        if (inputValues != null && !inputValues.isEmpty()) {// add custom values
            resolvedInputs = new HashMap(inputValues);
        }
        if (resolvedInputReferences != null && !resolvedInputReferences.isEmpty()) {
            resolvedInputs = new HashMap(); // initialize
            for (Map.Entry<Input, List<Input>> entry : resolvedInputReferences.entrySet()) {
                Input input = entry.getKey();
                List<Input> inputReferences = entry.getValue();
                Object inputValue = inputValues.get(input.getName());
                if (inputValue == null) {
                    inputValue = input.getDefaultValue();
                }
                for (Input referredInput : inputReferences) {
                    if (validateConnectionType(referredInput.getType(), inputValue)) {
                        resolvedInputs.put(referredInput.getName(), inputValue);
                    } else {
                        getLog().error(
                                "Input: " + input.getName() + " value type " + inputValue.getClass().getName()
                                        + " is not assignableFrom referred Input type " + referredInput.getType());
                    }
                }
            }
        }

        return resolvedInputs;
    }

    /**
     * Resolves Outputs of the given ModuleType.
     * Custom Outputs hold <code>reference</code> to System/Other Custom Output.
     *
     * @param moduleTypeUID the ModuleType UID
     * @param systemOutputs calculated System Outputs
     * @return Map with resolved Outputs (System Outputs & Custom Outputs)
     */
    public static final Map<String, Object> resolveOutputs(String moduleTypeUID, Map<String, ?> systemOutputs) {
        Map<String, Object> resolvedOutputs = null;
        if (systemOutputs != null && !systemOutputs.isEmpty()) {
            Map<String, Output> outputDescriptions = getAllModuleTypeOutputDescriptions(moduleTypeUID);
            if (outputDescriptions != null) {
                resolvedOutputs = new HashMap(systemOutputs); // initialize
                for (Output output : outputDescriptions.values()) {
                    if (systemOutputs.containsKey(output.getName()))
                        continue; // skip System Outputs
                    fillReferencedOutputs(output, outputDescriptions, resolvedOutputs);
                }
            }
        }

        return resolvedOutputs;
    }

    /** private */

    private static Map<String, ConfigDescriptionParameter> getAllModuleTypeConfigDescriptions(String moduleTypeUID) {
        Set<ConfigDescriptionParameter> configDescriptions = null;
        List<ModuleType> moduleTypes = getAllModuleTypes(moduleTypeUID);
        if (moduleTypes != null) {
            configDescriptions = new HashSet();
            for (ModuleType moduleType : moduleTypes) {
                configDescriptions.addAll(moduleType.getConfigurationDescription());
            }
        }

        return configDescriptions != null ? toConfigDescriptionMap(configDescriptions) : null;
    }

    private static Map<String, Input> getAllModuleTypeInputs(String moduleTypeUID) {
        Set<Input> inputs = null;
        List<ModuleType> moduleTypes = getAllModuleTypes(moduleTypeUID);
        if (moduleTypes != null) {
            inputs = new HashSet();
            for (ModuleType moduleType : moduleTypes) {
                if (moduleType instanceof ConditionType) {
                    inputs.addAll(((ConditionType) moduleType).getInputs());
                } else if (moduleType instanceof ActionType) {
                    inputs.addAll(((ActionType) moduleType).getInputs());
                } else {
                    // error case
                    inputs = null;
                    getLog().error(
                            "From ModuleType uid=" + moduleTypeUID + " -> no suitable ModuleType uid="
                                    + moduleType.getUID());
                    break;
                }
            }
        }
        return inputs != null ? toInputMap(inputs) : null;
    }

    private static Map<String, Output> getAllModuleTypeOutputDescriptions(String moduleTypeUID) {
        Set<Output> outputs = null;
        List<ModuleType> moduleTypes = getAllModuleTypes(moduleTypeUID);
        if (moduleTypes != null) {
            outputs = new HashSet();
            for (ModuleType moduleType : moduleTypes) {
                if (moduleType instanceof TriggerType) {
                    outputs.addAll(((TriggerType) moduleType).getOutputs());
                } else if (moduleType instanceof ActionType) {
                    outputs.addAll(((ActionType) moduleType).getOutputs());
                } else {
                    // error case
                    outputs = null;
                    getLog().error(
                            "From ModuleType uid=" + moduleTypeUID + " -> no suitable ModuleType uid="
                                    + moduleType.getUID());
                    break;
                }
            }
        }
        return outputs != null ? toOutputMap(outputs) : null;
    }

    /**
     * Collects all ModuleTypes for given ModuleTypeUID
     *
     * @param moduleTypeUID the source module type
     * @return list of all module types in the hierarchy
     */
    private static List<ModuleType> getAllModuleTypes(String moduleTypeUID) {
        List<ModuleType> allModuleTypes = null;
        // ModuleTypeManager moduleTypeRegistry = Activator.getModuleTypeManager();
        ModuleTypeManager moduleTypeRegistry = ModuleTypeManager.getInstance();
        if (moduleTypeRegistry != null) {
            allModuleTypes = new ArrayList();
            String currentModuleTypeUID = moduleTypeUID;
            ModuleType currentModuleType;
            do {
                currentModuleType = moduleTypeRegistry.getType(currentModuleTypeUID);
                if (currentModuleType != null) {
                    allModuleTypes.add(currentModuleType);
                    currentModuleTypeUID = getParentModuleTypeUID(currentModuleTypeUID);
                } else {
                    // error case
                    allModuleTypes = null;
                    getLog().error(
                            "From ModuleType uid=" + moduleTypeUID + " -> ModuleType uid=" + currentModuleTypeUID
                                    + " is not available.");
                    break;
                }
            } while (currentModuleTypeUID != null);
        } else {
            getLog().error("ModuleTypeRegistry is not available.");
        }
        return allModuleTypes;
    }

    /**
     * Fills up all referred config properties in the chain by the source config property.
     *
     * @param configDescription the source ConfigDescriptionParameter
     * @param configValue the value of the source config property
     * @param configDescriptionsMap map key: name of ConfigDescriptionParameter, value: the ConfigDescriptionParameter
     * @param resolvedConfiguration current resultConfiguration
     */
    private static void fillReferredConfigs(ConfigDescriptionParameter configDescription, Object configValue,
            Map<String, ConfigDescriptionParameter> configDescriptionsMap, Map resolvedConfiguration) {

        String configContext = configDescription.getContext();
        while (isParsable(configContext)) {
            String referredConfigName = parse(configContext);
            validateConfigType(configDescription, referredConfigName, configDescriptionsMap);
            resolvedConfiguration.put(referredConfigName, configValue);
            configContext = configDescriptionsMap.get(referredConfigName).getContext();
        }
    }

    /**
     * Fill up all referred Inputs in the chain by the source Input.
     *
     * @param input the source Input
     * @param inputDescriptionsMap Map with meta info for current ModuleType Inputs
     * @param resolvedInputReferences current resolved Map with references.<br/>
     *            If referred Input has already resolved references to other Inputs (its already put in the Map) -
     *            it is removed from the map, added to its List of references and the result List is added to the source
     *            Input<br/>
     *            key: source Input, value: List of referred Inputs.
     */
    private static void fillReferredInputs(Input input, Map<String, Input> inputDescriptionsMap,
            Map<Input, List<Input>> resolvedInputReferences) {

        String referredInputName = parse(input.getReference());
        Input referredInput = inputDescriptionsMap.get(referredInputName);
        if (referredInput != null) {
            List<Input> referredInputs = null;
            if (resolvedInputReferences.containsKey(referredInput)) {// check if referredInput has already reference to
                                                                     // other Inputs
                referredInputs = resolvedInputReferences.remove(referredInput);
                referredInputs.add(referredInput);
            } else {
                referredInputs = new ArrayList<Input>();
                referredInputs.add(referredInput);
            }
            resolvedInputReferences.put(input, referredInputs);
        } else {
            getLog().error(
                    "Can't find referred Input: " + referredInputName + ", referred by Input: " + input.getName());
        }

    }

    /**
     * Fill up all Custom Outputs that have references to other Outputs.
     * If referred Output does not have a value - referred Output reference is taken and searching for value is
     * continued until its reached
     * Custom Output with set value or System Output that has already set value).
     * After Output with value is reached - this value is set to all Outputs that are passed in the chain to the source
     * Output(from where searching has begun)
     *
     * @param output the Custom output
     * @param outputDescriptions Map with all output descriptions
     * @param resolvedOutputs current resolved Outputs (System Outputs too)
     */
    private static void fillReferencedOutputs(Output output, Map<String, Output> outputDescriptions, Map resolvedOutputs) {

        List<Output> referencedOutputs = new ArrayList(); // holder of referenced Outputs
        Output currentOutput = output;
        Object outputValue = null;
        boolean valueFound = false;
        while (!valueFound) {
            referencedOutputs.add(currentOutput); // add currentOutput to the list
            String reference = currentOutput.getReference();
            if (isParsable(reference)) {
                String parsedReference = parse(reference);
                Output referredOutput = outputDescriptions.get(parsedReference);
                if (referredOutput != null) {
                    if (resolvedOutputs.containsKey(parsedReference)) { // referred Output has already value
                        outputValue = resolvedOutputs.get(parsedReference);
                        valueFound = true; // break from the loop - value found in Custom Output
                    } else { // reference to Custom Output that have not value set too
                        currentOutput = referredOutput;// value not found - continue with the loop
                    }
                } else {
                    getLog().warn(
                            "Can't find Output: " + parsedReference + ". Referenced by Output:"
                                    + currentOutput.getName());
                    return;
                }
            } else {
                getLog().warn(
                        "Can't parse reference of Output: " + currentOutput.getName() + ", reference: " + reference);
                return;
            }
        }
        // fill referred Outputs with System value
        for (Output referencedOutput : referencedOutputs) {
            if (validateConnectionType(referencedOutput.getType(), outputValue)) {
                resolvedOutputs.put(referencedOutput.getName(), outputValue);
            } else {
                getLog().error(
                        "Output: " + referencedOutput.getName() + " value type " + outputValue.getClass().getName()
                                + " is not assignableFrom referred Output type " + referencedOutput.getType());
            }

        }
    }

    /**
     * Gets default value recursively.
     *
     * @param configDescription the source ConfigDescriptionParameter
     * @param defaultValue the default value of the config property
     * @param configContextMap map key: name of ConfigDescriptionParameter, value: the ConfigDescriptionParameter
     * @param configuration map with config properties
     * @return the default config value for passed configDescription
     */
    private static Object getConfigDefaultValue(ConfigDescriptionParameter configDescription,
            Map<String, ConfigDescriptionParameter> configContextMap, Map<String, ?> configuration) {

        String configDefaultValue = configDescription.getDefault();
        Object resultValue = null;
        while (resultValue == null && isParsable(configDefaultValue)) {
            String referredConfigName = parse(configDefaultValue);
            validateConfigType(configDescription, referredConfigName, configContextMap);
            resultValue = configuration.get(referredConfigName);
            configDefaultValue = configContextMap.get(referredConfigName).getDefault();
        }
        if (resultValue == null) {
            try {
                resultValue = convertToConfigType(configDefaultValue, configContextMap.get(configDescription.getName())
                        .getType());
            } catch (Exception e) {
                getLog().error(e.getMessage(), e);
            }
        }
        return resultValue;
    }

    /**
     * Tries to convert raw value to its type.
     *
     * @param value raw value
     * @param type the type to be converted to
     * @return the converted value
     * @throws Exception if conversion failed
     */
    private static Object convertToConfigType(String value, Type type) throws Exception {
        Object result;
        switch (type) {
            case TEXT:
                result = value;
                break;
            case BOOLEAN:
                String trimmed = value.trim();
                if (trimmed.equalsIgnoreCase("true")) {
                    result = Boolean.TRUE;
                } else if (trimmed.equalsIgnoreCase("false")) {
                    result = Boolean.FALSE;
                } else {
                    result = null;
                }
                break;
            case INTEGER:
                result = Integer.valueOf(value);
                break;
            case DECIMAL:
                result = Double.valueOf(value);
                break;
            default:
                result = null;
                break;
        }
        return result;
    }

    /**
     * Validates if ConfigDescriptionParameter Type is equal to referred ConfigDescriptionParameter Type
     *
     * @param configDescription the source ConfigDescriptionParameter
     * @param referredConfigName the referred ConfigDescriptionParameter name
     * @param configDescriptionsMap Map with ConfigDescriptionParameter {@link #toConfigDescriptionMap(Set)}
     */
    private static void validateConfigType(ConfigDescriptionParameter configDescription, String referredConfigName,
            Map<String, ConfigDescriptionParameter> configDescriptionsMap) {

        Type configType = configDescription != null ? configDescription.getType() : null;
        ConfigDescriptionParameter referredConfig = configDescriptionsMap.get(referredConfigName);
        Type refferedConfigType = referredConfig != null ? referredConfig.getType() : null;
        if ((configType == null || refferedConfigType == null) || !configType.equals(refferedConfigType)) {
            throw new IllegalArgumentException("ConfigProperties Type missmatch:" + configDescription.getName() + ":"
                    + configType + "->" + referredConfigName + ":" + refferedConfigType);
        }
    }

    /**
     * Validates source Input\Output value is assignableFrom Input\Output type.
     *
     * @param referredType the referred Input\Output type (fully qualified name i.e. java.lang.String)
     * @param value the source Input\Output value
     * @return true if is assignableFrom referred Input\Output type, false otherwise.
     */
    private static boolean validateConnectionType(String referredType, Object value) {
        boolean valid = false;
        try {
            Class<?> clazzOutputExpected = FrameworkUtil.getBundle(ModuleUtil.class).loadClass((referredType));
            if (clazzOutputExpected.isAssignableFrom(value.getClass())) {
                valid = true;
            }
        } catch (ClassNotFoundException e) {/**/
        }
        return valid;
    }

    /**
     * Converts Set of ConfigDescriptionParameter to Map <br/>
     * <code>key</code>: name of ConfigDescriptionParamater, <code>value</code>: ConfigDescriptionParamater
     *
     * @param configurationDescriptions Set of ConfigDescriptionParameter
     * @return Map with ConfigDescriptionParameters
     */
    private static Map<String, ConfigDescriptionParameter> toConfigDescriptionMap(
            Set<ConfigDescriptionParameter> configurationDescriptions) {

        Map<String, ConfigDescriptionParameter> configMap = new HashMap();
        for (ConfigDescriptionParameter config : configurationDescriptions) {
            configMap.put(config.getName(), config);
        }
        return configMap;
    }

    /**
     * Converts Set of Input to Map <br/>
     * <code>key</code>: name of Input, <code>value</code>: Input
     *
     * @param inputs Set of Input
     * @return Map with Inputs
     */
    private static Map<String, Input> toInputMap(Set<Input> inputs) {
        Map<String, Input> inputsMap = new HashMap();
        for (Input input : inputs) {
            inputsMap.put(input.getName(), input);
        }
        return inputsMap;
    }

    /**
     * Converts Set of Output to Map <br/>
     * <code>key</code>: name of Input, <code>value</code>: Input
     *
     * @param inputs Set of Input
     * @return Map with Inputs
     */
    private static Map<String, Output> toOutputMap(Set<Output> outputs) {
        Map<String, Output> outputsMap = new HashMap();
        for (Output output : outputs) {
            outputsMap.put(output.getName(), output);
        }
        return outputsMap;
    }

    /**
     * Checks if object is parsable or not(starts with {@link #PARSABLE_DOLLAR})
     *
     * @param obj the object to be checked
     * @return true if object is parsable, false otherwise
     */
    private static boolean isParsable(Object obj) {
        return obj instanceof String && ((String) obj).startsWith(PARSABLE_DOLLAR) && ((String) obj).length() > 1;
    }

    /**
     * Simple parsing of context/reference starting with {@link #PARSABLE_DOLLAR}.
     *
     * @param parsable the parsable context/reference
     * @return parsed context/reference.
     */
    private static String parse(String parsable) {
        return parsable.substring(1);
    }

    /**
     * Gets parent moduleTypeUID if passed moduleTypeUID has parent
     *
     * @param childModuleTypeUID the UID of the moduleType
     * @return parent module type UID if passed moduleType has parent, null otherwise
     */
    private static String getParentModuleTypeUID(String childModuleTypeUID) {
        String parentModuleTypeUID = null;
        if (childModuleTypeUID.indexOf(MODULE_SEPARATOR) != -1) {
            parentModuleTypeUID = childModuleTypeUID.substring(0, childModuleTypeUID.lastIndexOf(MODULE_SEPARATOR));
        }
        return parentModuleTypeUID;
    }

    private static Logger getLog() {
        if (log == null) {
            log = LoggerFactory.getLogger(ModuleUtil.class);
        }
        return log;
    }
}
