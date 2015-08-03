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

package org.eclipse.smarthome.automation.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;

/**
 * Abstract class used for resolving references in {@link ConfigDescriptionParameter} , {@link Input} and {@link Output}
 * in {@link Module}
 *
 * @author Vasil Ilchev
 */
public abstract class AbstractModuleHandler implements ModuleHandler {
    /**
     * Symbol that can be put in {@link ConfigDescriptionParameter} property <code>context</code>.<br/>
     * ConfigDescriptionParameter context property syntax:
     *
     * <pre>
     * context: "{
     *  ...,
     *  \"{@link #PROPERTY_CONTEXT_NAMEREF}\": \"$someNameReference\",    //optional
     *  \"{@link #PROPERTY_CONTEXT_VALUEREF}\": \"$someNameReference\",    //optional
     *  ...
     * }"
     * </pre>
     *
     * Symbol can be put also in {@link Input} / {@link Output} <code>reference</code><br/>
     * Needed for resolving references between Custom MouduleType's Input / Output and their parent(System) ModuleType's
     * Input / Output
     *
     * <pre>
     * Input reference syntax:
     *
     * input: {
     *  ...,
     *  "reference": "$parentInputName",
     *  ...
     * }
     *
     * It means that Input with this reference must set its value to Input from its Parent ModuleType with the given name.
     * </pre>
     *
     *
     *
     * Output reference syntax:
     *
     * <pre>
     *
     * output: {
     *  ...,
     *  "reference": "$referredName",
     *  ...
     * }
     *
     * In Output reference can have the follow meanings:
     * - Output have reference to parent ModuleType Output which means current ModuleType's Output must take value from parent ModuleType's Output
     * - Output have reference to ConfigurationProperty (of same ModuleType which current ModuleType's Output takes place)
     * - Output have reference to Input (of same ModuleType which current Input takes place)
     * </pre>
     */
    public static final char REFERENCE_SYMBOL = '$';
    /**
     * Symbol that separates custom ModuleTypeUID from system ModuleTypeUID.
     * For example: SystemModuleType:CustomModuleType
     * Note:
     * Custom ModuleTypeUID is the whole UID - 'SystemModuleType:CustomModuleType'
     * System ModuleTypeUID is the 'SystemModuleType'
     *
     */
    public static final char MODULE_TYPE_SEPARATOR = ':';

    /**
     * Used in {@link ConfigDescriptionParameter} context property.
     * Value of this property is the name of ConfigDescriptionParameter in parent ModuleType with
     * {@link #REFERENCE_SYMBOL} prefix.
     * For example:
     *
     * <pre>
     *   context: {
     *      ..
     *      "nameRef": "$someParentConfigName",
     *      ..
     *   }
     * </pre>
     *
     * It means that Configuration Property with this context have to set its value to the referred Configuration
     * property
     * (configuration property with the given name in its parent ModuleType)
     */
    public static final String PROPERTY_CONTEXT_NAMEREF = "nameRef";

    /**
     * Used in {@link ConfigDescriptionParameter} context property.
     * Value of this property is the name of {@link Input} in same ModuleType with {@link #REFERENCE_SYMBOL} prefix.
     * For example:
     *
     * <pre>
     *   context: {
     *      ..
     *      "valueRef": "$someInputName",
     *      ..
     *   }
     * </pre>
     *
     * It means that Configuration Property with this context must take its value from {@link Input} with the given name
     * in same Module.
     */
    public static final String PROPERTY_CONTEXT_VALUEREF = "valueRef";

    private ServiceReference moduleTypeRegistryRef;
    private ModuleTypeRegistry moduleTypeRegistry;
    private Module module;
    private BundleContext bc;
    private Logger log;
    // descriptions
    private List<ModuleType> moduleTypes;
    private Map<String, ConfigDescriptionParameter> configParametersMap;
    private Map<String, Input> inputDescriptionsMap;
    private Map<String, Output> outputDescriptionsMap;
    // --descriptions
    // connections
    private Map<String, Set<String>> configParameterConnectionsMap;
    private Map<String, Set<String>> inputConnectionsMap;
    private Map<String, Set<String>> outputConnectionsMap;

    // --connections

    /**
     * Creates Abstract {@link ModuleHandler} object.
     *
     * @param module the Module for which ModuleHandler is created
     */
    public AbstractModuleHandler(Module module) {
        if (module == null) {
            throw new IllegalArgumentException("Module must not be null.");
        }
        this.module = module;
        this.log = LoggerFactory.getLogger(AbstractModuleHandler.class);
        init();
    }

    public void dispose() {
        moduleTypeRegistry = null;
    }
    
    /**
     * Initialize all needed utilities.
     */
    private void init() {
        // initialize moduleTypeRegistry
        initModuleTypeRegistry();
        // initialize descriptions
        moduleTypes = getAllModuleTypes(module.getTypeUID());
        configParametersMap = getAllConfigParametersMap(moduleTypes);
        if (module instanceof Condition || module instanceof Action) {
            inputDescriptionsMap = getAllInputDescriptionsMap(moduleTypes);
        }
        if (module instanceof Trigger || module instanceof Action) {
            outputDescriptionsMap = getAllOutputDescriptions(moduleTypes);
        }
        // initialize connections
        configParameterConnectionsMap = getConfigParameterConnections(configParametersMap);
        inputConnectionsMap = getInputConnections(inputDescriptionsMap);
        outputConnectionsMap = getOutputConnections(outputDescriptionsMap);
    }
    
    /**
     * Getting ModuleTypeRegistry.
     */
    private void initModuleTypeRegistry() {
        moduleTypeRegistry = getModuleTypeRegistry();
        if (moduleTypeRegistry == null) {
            throw new IllegalArgumentException("ModuleTypeRegistry must not be null.");
        }
    }

    /**
     * Resolves references in Module Inputs (Custom Inputs sets their values to System Inputs).
     * Input can set its value to other Input in its parent ModuleType with its <code>reference</code>.
     *
     * <pre>
     * For example:
     *
     * inputName: {
     *  ....,
     *  reference: $parentInputName,
     *  ....
     * }
     *
     * It means that current Input with name 'inputName' have to set its value to its parent ModuleType's Input with name 'parentInputName'.
     *
     * @param inputValues the Input values by the moment (i.e. custom Input values)
     * @return Map with the resolved Inputs values - {@link Module} will be ready to work with<br/>
     * key: Name of the Input, value: Input value
     */
    protected final Map<String, Object> getResolvedInputs(Map<String, ?> inputValues) {

        Map resolvedInputs = new HashMap();
        if (inputDescriptionsMap != null) {
            for (Map.Entry<String, Input> entry : inputDescriptionsMap.entrySet()) {
                String inputName = entry.getKey();
                Input inputDescription = entry.getValue();
                if (!resolvedInputs.containsKey(inputName)) {
                    Object inputValue = getInputValue(inputDescription, inputValues);
                    resolvedInputs.put(inputName, inputValue);
                    // fill up all referred Inputs in the chain
                    fillReferred(inputName, inputValue, resolvedInputs, inputConnectionsMap);
                }
            }
        } else if (inputValues != null) {
            resolvedInputs = inputValues;
        }

        return !resolvedInputs.isEmpty() ? resolvedInputs : null;
    }

    /**
     * Resolves references in {@link Module} Configuration.
     * Configuration property can set its value to other Configuration properties with its <code>context</code>
     * {@link #PROPERTY_CONTEXT_NAMEREF}.</br>
     * (Custom ConfigurationProperty to its parent ConfigurationProperty)
     * Configuration property can get its value from {@link Input} with its <code>context</code>
     * {@link #PROPERTY_CONTEXT_VALUEREF}.<br/>
     * (ConfigurationProperty from current ModuleType Inputs)
     *
     * @see #PROPERTY_CONTEXT_NAMEREF
     * @see #PROPERTY_CONTEXT_VALUEREF
     * @see #getResolvedInputs(Map)
     * @param resolvedInputs resolved Input values - retrieved from {@link #getResolvedInputs(Map)}
     * @return Map with resolved configuration - {@link ModuleHandler} will be ready to work with<br/>
     *         key: name of the Configuration property, value: value of the Configuration property
     */
    protected final Map<String, Object> getResolvedConfiguration(Map<String, ?> resolvedInputs) {
        Map resolvedConfiguration = new HashMap();
        Map<String, Object> configuration = module.getConfiguration();
        if (configParametersMap != null) {
            for (Map.Entry<String, ConfigDescriptionParameter> entry : configParametersMap.entrySet()) {
                String configName = entry.getKey();
                ConfigDescriptionParameter configParameter = entry.getValue();
                if (!resolvedConfiguration.containsKey(configName)) {
                    Object configValue = getConfigValue(configParameter, configuration, resolvedInputs);
                    resolvedConfiguration.put(configName, configValue);
                    // fill up all referred properties in the chain
                    fillReferred(configName, configValue, resolvedConfiguration, configParameterConnectionsMap);
                }
            }
        } else if (configuration != null) {
            resolvedConfiguration = configuration;
        }
        return !resolvedConfiguration.isEmpty() ? resolvedConfiguration : null;
    }

    /**
     * Resolves references in Module Outputs.
     * With its <code>reference</code> property {@link Output} points from which element must get its value from.
     * Output can hold reference to parent's Output, ConfigurationProperty in current Module or Input in current Module.
     *
     * @param resolvedConfiguration resolved Configuration - retrieved from {@link #getResolvedConfiguration(Map)}
     * @param resolvedInputs resolved Input values - retrieved from {@link #getResolvedInputs(Map)}
     * @param outputValues current outputValues (i.e. System Outputs)
     * @return Map with resolved Output values - {@link Module} will be ready to work with<br/>
     *         key: Name of Output, value: Output Value
     */
    protected final Map<String, Object> getResolvedOutputs(Map<String, ?> resolvedConfiguration,
            Map<String, ?> resolvedInputs, Map<String, ?> outputValues) {

        Map resolvedOutputs = new HashMap();
        if (outputDescriptionsMap != null) {
            for (Map.Entry<String, Output> entry : outputDescriptionsMap.entrySet()) {
                String outputName = entry.getKey();
                Output outputDescription = entry.getValue();
                if (!resolvedOutputs.containsKey(outputName)) {
                    Object outputValue = getOutputValue(outputDescription, resolvedConfiguration, resolvedInputs,
                            outputValues);
                    resolvedOutputs.put(outputName, outputValue);
                    // fill up all referred Outputs in the chain
                    fillReferred(outputName, outputValue, resolvedOutputs, outputConnectionsMap);
                }
            }
        } else if (outputValues != null) {
            resolvedOutputs = outputValues;
        }

        return !resolvedOutputs.isEmpty() ? resolvedOutputs : null;
    }
    
    /**
     * Inheritors must provide the {@link ModuleTypeRegistry} service.
     * It is used for retrieving meta-information about Module.
     * @return ModuleTypeRegistry service
     */
    protected abstract ModuleTypeRegistry getModuleTypeRegistry();
    
    /**
     * Utility method for getting ConfigurationProperty's value.
     * Priority search is as follows:
     * 1) value defined by user configuration
     * 2) value from <code>context</code> {@link #PROPERTY_CONTEXT_VALUEREF} 3) value from ConfigurationProperty default
     * value.
     *
     * @param configParameter The ConfigDescriptionParameter searching value for
     * @param configuration the user configuration
     * @param resolvedInputValues the resolved Inputs
     * @return value for passed ConfigurationProperty
     */
    private Object getConfigValue(ConfigDescriptionParameter configParameter, Map<String, ?> configuration,
            Map<String, ?> resolvedInputValues) {
        Object configValue;
        String configName = configParameter.getName();
        if (configuration != null && configuration.containsKey(configName)) {// get user configuration value
            configValue = configuration.get(configName);
        } else {// priority: 1context, 2defaultValue
            JSONObject context = getContextJSON(configParameter);
            if (context != null && context.has(PROPERTY_CONTEXT_VALUEREF)) {
                String referredInput = "";
                try {
                    referredInput = context.getString(PROPERTY_CONTEXT_VALUEREF);
                } catch (JSONException e) {
                    log.error("cannot get context property " + PROPERTY_CONTEXT_VALUEREF, e);
                }
                if (isParsable(referredInput)) {// get context value
                    String inputName = parse(referredInput);
                    if (resolvedInputValues != null && resolvedInputValues.containsKey(inputName)) {
                        configValue = resolvedInputValues.get(inputName);
                    } else {// get default value
                        configValue = getConfigDefaultValue(configParameter);
                    }
                } else {// get default value
                    configValue = getConfigDefaultValue(configParameter);
                }
            } else {// get default value
                configValue = getConfigDefaultValue(configParameter);
            }
        }
        return configValue;
    }

    /**
     * Utility method for getting Input's value.
     * Priority search is as follows:
     * 1) value from resolved inputs
     * 2) value from Input's default value
     *
     * @param inputDescription The Input searching value for
     * @param inputValues current Input values
     * @return value for passed Input
     */
    private Object getInputValue(Input inputDescription, Map<String, ?> inputValues) {
        Object inputValue;
        String inputName = inputDescription.getName();
        if (inputValues != null && inputValues.containsKey(inputName)) { // get resolved input value
            inputValue = inputValues.get(inputName);
        } else { // get default value
            inputValue = inputDescription.getDefaultValue();
        }
        return inputValue;
    }

    /**
     * Utility method for getting Output's value .
     * Priority search is as follows:
     * 1) reference to other Output(skip it and get defaultValue - as the referred Output will override it with its
     * value(if have) afterwards)
     * 2) reference to ConfigurationProperty
     * 3) reference to Input
     *
     * @param outputDescription the Output searching value for
     * @param configurationValues
     * @param inputValues
     * @param outputValues
     * @return value for passed Output
     */
    private Object getOutputValue(Output outputDescription, Map<String, ?> configurationValues,
            Map<String, ?> inputValues, Map<String, ?> outputValues) {
        String outputName = outputDescription.getName();
        Object outputValue = null;
        if (outputValues != null && outputValues.containsKey(outputName)) { // get resolved output value
            outputValue = outputValues.get(outputName);
        } else { // get reference - Output, Input, ConfigurationProperty
            String outputReference = outputDescription.getReference();
            if (isParsable(outputReference)) {
                String parsedNameRef = parse(outputReference);
                if (!outputDescriptionsMap.containsKey(parsedNameRef)) { // skip if reference is Output
                    if (configurationValues != null && configurationValues.containsKey(parsedNameRef)) { // reference is
                                                                                                         // ConfigurationProperty
                        outputValue = configurationValues.get(parsedNameRef);
                    } else if (inputValues != null && inputValues.containsKey(parsedNameRef)) { // reference is Input
                        outputValue = inputValues.get(parsedNameRef);
                    } else { // get default
                        outputValue = outputDescription.getDefaultValue();
                    }
                } else { // get default
                    outputValue = outputDescription.getDefaultValue();
                }
            } else { // get default
                outputValue = outputDescription.getDefaultValue();
            }
        }

        return outputValue;
    }

    /**
     * Utility method for ConfigurationProperty connections.
     *
     * @see #PROPERTY_CONTEXT_NAMEREF
     * @param configParametersMap all ConfigurationDescriptionParameters from all ModuleTypes in the chain
     * @return Map key: name of ConfigurationProperty, value: set of ConfigurationProperties names
     */
    private Map<String, Set<String>> getConfigParameterConnections(
            Map<String, ConfigDescriptionParameter> configParametersMap) {

        Map<String, Set<String>> resultConfigConnections = new HashMap();
        if (configParametersMap != null) {
            Set<String> visitedConfigs = new HashSet();
            for (Map.Entry<String, ConfigDescriptionParameter> entry : configParametersMap.entrySet()) {
                String configName = entry.getKey();
                if (!visitedConfigs.contains(configName)) {// check if Config is already visited
                    ConfigDescriptionParameter configParameter = entry.getValue();
                    Set<String> referredConfigNames = getReferredConfigNames(configParameter, configParametersMap,
                            visitedConfigs, resultConfigConnections);
                    if (referredConfigNames != null) {
                        resultConfigConnections.put(configName, referredConfigNames);
                    }
                }
            }
        }
        return !resultConfigConnections.isEmpty() ? resultConfigConnections : null;
    }

    /**
     * Utility method for Input connections.
     * Input connections map holds links between Inputs(Input holds reference to parent ModuleType's Input)
     *
     * @param inputDescriptionsMap all Inputs from all ModuleTypes in the chain
     * @return Map key: name of Input, value: set of Input names
     */
    private Map<String, Set<String>> getInputConnections(Map<String, Input> inputDescriptionsMap) {
        Map<String, Set<String>> resultInputConnections = new HashMap();
        if (inputDescriptionsMap != null) {
            Set<String> visitedInputs = new HashSet();
            for (Map.Entry<String, Input> entry : inputDescriptionsMap.entrySet()) {
                String inputName = entry.getKey();
                if (!visitedInputs.contains(inputName)) {// check if Input is already visited
                    Input inputDescription = entry.getValue();
                    Set<String> referredInputNames = getReferredInputNames(inputDescription, inputDescriptionsMap,
                            visitedInputs, resultInputConnections);
                    if (referredInputNames != null) {
                        resultInputConnections.put(inputName, referredInputNames);
                    }
                }
            }
        }
        return !resultInputConnections.isEmpty() ? resultInputConnections : null;
    }

    /**
     * Utility method for Output connections.
     * Output connections map holds links between Outputs(Output holds reference to parent ModuleType's Output)
     *
     * @param outputDescriptionsMap all Outputs from all ModuleTypes in the chain
     * @return Map key: name of Output, value: set of Output names
     */
    private Map<String, Set<String>> getOutputConnections(Map<String, Output> outputDescriptionsMap) {
        Map<String, LinkedList<String>> resultOutputConnections = new HashMap();
        if (outputDescriptionsMap != null) {
            Set<String> visitedOutputs = new HashSet();
            for (Map.Entry<String, Output> entry : outputDescriptionsMap.entrySet()) {
                String outputName = entry.getKey();
                if (!visitedOutputs.contains(outputName)) {// check if Output is already visited
                    Output outputDescription = entry.getValue();
                    LinkedList<String> referredOutputNames = getReferredOutputNames(outputDescription,
                            outputDescriptionsMap, visitedOutputs, resultOutputConnections);
                    if (referredOutputNames != null) {
                        resultOutputConnections.put(outputName, referredOutputNames);
                    }
                }
            }
        }
        return !resultOutputConnections.isEmpty() ? processOutputConnections(resultOutputConnections) : null;
    }

    /**
     * Output connections must be processed one last time.
     * With its reference Output points from where to get its value from.
     * In custom Module Outputs can have references to parent Module Outputs.
     * This means parent Module Output value will be set to the Custom Module Output that have reference to it.
     * For example: sourceOutput->output1->output2->....->outputN (possible chain of references Output-Output)
     * Last element('outputN') on the queue(maybe system Output) is the Output that have to set its value on the Outputs
     * in the
     * chain. Result of the process must be 'outputN' set as key of the result Map, all referred to it as value of the
     * result Map
     *
     * @param currentOutputConnections key: sourceOutput; value: LinkedList pointing(outputN) to the Output which value
     *            have to be taken
     * @return reversed Map: key: outputN value to take from, value: Set of the all Outputs(sourceOutput too) that value
     *         must be set.
     */
    private Map<String, Set<String>> processOutputConnections(Map<String, LinkedList<String>> currentOutputConnections) {

        Map<String, Set<String>> resultOutputConnections = new HashMap();
        for (Map.Entry<String, LinkedList<String>> entry : currentOutputConnections.entrySet()) {
            String beginPoint = entry.getKey();
            LinkedList<String> referredOutputNames = entry.getValue();
            String endPoint = referredOutputNames.removeLast();// the source(system) output
            referredOutputNames.addFirst(beginPoint);
            resultOutputConnections.put(endPoint, new HashSet(referredOutputNames));// order not matter
        }
        return resultOutputConnections;
    }

    private LinkedList<String> getReferredOutputNames(Output outputDescription,
            Map<String, Output> outputDescriptionsMap2, Set<String> visitedOutputs,
            Map<String, LinkedList<String>> currentOutputConnections) {

        Output currentOutputDescription = outputDescription;

        String sourceOutputName = currentOutputDescription.getName();
        String currentOutputName = sourceOutputName;
        String currentReference = currentOutputDescription.getReference();
        LinkedList<String> referredOutputNames = new LinkedList(); // order needed: last element- source element
        while (!visitedOutputs.contains(currentOutputName) && currentReference != null) {
            visitedOutputs.add(currentOutputName); // mark Output as visited
            if (isParsable(currentReference)) {
                String parsedNameRef = parse(currentReference);
                currentOutputDescription = outputDescriptionsMap.get(parsedNameRef);
                if (currentOutputDescription != null) { // check if Output with parsed name exists
                    checkLoop(referredOutputNames, sourceOutputName, parsedNameRef);
                    validateOutputType(outputDescription, currentOutputDescription);
                    if (currentOutputConnections.containsKey(parsedNameRef)) { // referred output has
                                                                               // outputConnections
                        LinkedList ll = currentOutputConnections.remove(parsedNameRef);
                        ll.addFirst(parsedNameRef);
                        referredOutputNames.addAll(ll); // add all its referred and save the order
                    } else {
                        referredOutputNames.add(parsedNameRef);
                    }
                    currentOutputName = currentOutputDescription.getName();
                    currentReference = currentOutputDescription.getReference();
                } else {
                    // check Output's reference is ConfigurationProperty or Input
                    if (configParametersMap.get(parsedNameRef) == null
                            && inputDescriptionsMap.get(parsedNameRef) == null) {
                        log.error("Can't find referred Element: " + parsedNameRef + ", referred by Output: "
                                + sourceOutputName);
                    }
                }
            }
        }

        return !referredOutputNames.isEmpty() ? referredOutputNames : null;
    }

    private Set<String> getReferredConfigNames(ConfigDescriptionParameter configParamater,
            Map<String, ConfigDescriptionParameter> configDescriptionsMap, Set<String> visitedConfigs,
            Map<String, Set<String>> currentConfigurationConnections) {

        String sourceConfigName = configParamater.getName();
        String currentConfigName = sourceConfigName;
        ConfigDescriptionParameter currentConfigParameter = configParamater;
        JSONObject currentContext = getContextJSON(currentConfigParameter);
        Set<String> referredConfigNames = new HashSet();
        while (!visitedConfigs.contains(currentConfigName) && currentContext != null) {
            visitedConfigs.add(currentConfigName); // mark config as visited
            String nameRef = currentContext.optString(PROPERTY_CONTEXT_NAMEREF, null);
            if (isParsable(nameRef)) {
                String referredConfigName = parse(nameRef);
                currentConfigParameter = configDescriptionsMap.get(referredConfigName);
                if (currentConfigParameter != null) { // check if ConfigDescriptionParamaeter with parsed name exists
                    checkLoop(referredConfigNames, sourceConfigName, referredConfigName);
                    validateConfigType(configParamater, currentConfigParameter);
                    if (currentConfigurationConnections.containsKey(referredConfigName)) { // referred config has
                        // configConnections
                        Set s = currentConfigurationConnections.remove(referredConfigName);
                        s.add(referredConfigName);
                        referredConfigNames.addAll(s); // add all its referred
                    } else {
                        referredConfigNames.add(referredConfigName);
                    }
                    currentConfigName = currentConfigParameter.getName();
                    currentContext = getContextJSON(currentConfigParameter);
                } else {
                    log.error("Can't find referred ConfigurationProperty: " + referredConfigName
                            + ", referred by ConfigurationProperty: " + sourceConfigName);
                }
            }
        }
        return !referredConfigNames.isEmpty() ? referredConfigNames : null;
    }

    private Set<String> getReferredInputNames(Input inputDescription, Map<String, Input> inputDescriptionsMap,
            Set<String> visitedInputs, Map<String, Set<String>> currentInputConnections) {

        Input currentInputDescription = inputDescription;

        String sourceInputName = currentInputDescription.getName();
        String currentInputName = sourceInputName;
        String currentReference = currentInputDescription.getReference();
        Set<String> referredInputNames = new HashSet();
        while (!visitedInputs.contains(currentInputName) && currentReference != null) {
            visitedInputs.add(currentInputName); // mark Input as visited
            if (isParsable(currentReference)) {
                String parsedNameRef = parse(currentReference);
                currentInputDescription = inputDescriptionsMap.get(parsedNameRef);
                if (currentInputDescription != null) { // check if Input with parsed name exists
                    checkLoop(referredInputNames, sourceInputName, parsedNameRef);
                    validateInputType(inputDescription, currentInputDescription);
                    if (currentInputConnections.containsKey(parsedNameRef)) { // referred input has
                                                                              // inputConnections
                        Set s = currentInputConnections.remove(parsedNameRef);
                        s.add(parsedNameRef);
                        referredInputNames.addAll(s); // add all its referred
                    } else {
                        referredInputNames.add(parsedNameRef);
                    }
                    currentInputName = currentInputDescription.getName();
                    currentReference = currentInputDescription.getReference();
                } else {
                    log.error("Can't find referred Input: " + sourceInputName + ", referred by Input: "
                            + sourceInputName);
                }
            }
        }
        return !referredInputNames.isEmpty() ? referredInputNames : null;
    }

    /**
     * Utility method for filling all Elements that hold reference to sourceElement.
     *
     * @param sourceName the source Element name
     * @param sourceValue the value of the source Element
     * @param currentResolved Map with current resolved references
     * @param connectionsMap Map with connections- sourceElement must fill it's value to its connections(elements that
     *            hold reference to it(directly or indirectly))
     */
    private void fillReferred(String sourceName, Object sourceValue, Map<String, Object> currentResolved,
            Map<String, Set<String>> connectionsMap) {
        if (connectionsMap != null) {
            Set<String> reffered = connectionsMap.get(sourceName);
            if (reffered != null) {
                for (String referredName : reffered) {
                    currentResolved.put(referredName, sourceValue);
                }
            }
        }
    }

    /**
     * Utility method checking for loop in connections chain.
     * For example:<br/>
     * <code>sourceElement->element1->element2->element3->sourceElement (or) element1 (or) element2</code> (not allowed)
     *
     * @param collection current connections
     * @param sourceName the source Element from where search for connections has began
     * @param referredName the reached Element
     */
    private void checkLoop(Collection<String> collection, String sourceName, String referredName) {
        String loopErrMsg = null;
        if (sourceName.equals(referredName)) {
            collection.add(sourceName);
            loopErrMsg = "Loop found: " + sourceName + "->" + collection.toString();
        } else if (collection.contains(referredName)) {
            loopErrMsg = "Loop found: " + referredName + "->" + collection.toString();
        }
        if (loopErrMsg != null) {
            throw new IllegalArgumentException(loopErrMsg);
        }
    }

    /**
     * Validates if ConfigDescriptionParameter Type is equal to referred ConfigDescriptionParameter Type
     *
     * @param configParameter the source ConfigDescriptionParameter
     * @param referredConfigParameter the referred ConfigDescriptionParameter name
     */
    private void validateConfigType(ConfigDescriptionParameter configParameter,
            ConfigDescriptionParameter referredConfigParameter) {

        Type configType = configParameter != null ? configParameter.getType() : null;
        Type refferedConfigType = referredConfigParameter != null ? referredConfigParameter.getType() : null;
        if ((configType == null || refferedConfigType == null) || !configType.equals(refferedConfigType)) {
            throw new IllegalArgumentException("ConfigProperties Type missmatch:" + configParameter.getName() + ":"
                    + configType + "->" + referredConfigParameter.getName() + ":" + refferedConfigType);
        }
    }

    /**
     * Validates if Input type is equal to referred Input type
     *
     * @param inputDescription source Input
     * @param referredInputDescription referred Input
     */
    private void validateInputType(Input inputDescription, Input referredInputDescription) {
        String inputType = inputDescription.getType();
        String refferedInputType = referredInputDescription.getType();
        if (!inputType.equals(refferedInputType)) {
            throw new IllegalArgumentException("Input types missmatch:" + inputDescription.getName() + ":" + inputType
                    + " -> " + referredInputDescription.getName() + ":" + refferedInputType);
        }
    }

    /**
     * Validates if Output type is equal to referred Output type
     *
     * @param outputDescription source Output
     * @param referredInputDescription referred Output
     */
    private void validateOutputType(Output outputDescription, Output referredOutputDescription) {
        String outputType = outputDescription.getType();
        String refferedOutputType = referredOutputDescription.getType();
        if (!outputType.equals(refferedOutputType)) {
            throw new IllegalArgumentException("Output types missmatch:" + outputDescription.getName() + ":"
                    + outputType + " -> " + referredOutputDescription.getName() + ":" + refferedOutputType);
        }
    }

    /**
     * Retrieves all ModuleTypes ConfigDescriptionParameters in the chain.
     *
     * @param moduleTypes all moduleTypes in the chain
     * @return map of all ConfigDescrpition parameters.
     */
    private Map<String, ConfigDescriptionParameter> getAllConfigParametersMap(List<ModuleType> moduleTypes) {
        Map<String, ConfigDescriptionParameter> resultMap = null;
        Set<ConfigDescriptionParameter> configDescriptions = null;
        if (moduleTypes != null) {
            configDescriptions = new HashSet();
            Set currentConfigurationDescriptions = null;
            for (ModuleType moduleType : moduleTypes) {
                currentConfigurationDescriptions = moduleType.getConfigurationDescription();
                if (currentConfigurationDescriptions != null) {
                    configDescriptions.addAll(currentConfigurationDescriptions);
                }
            }
        }
        if (configDescriptions != null && !configDescriptions.isEmpty()) {
            resultMap = toConfigDescriptionParameterMap(configDescriptions);
        }
        return resultMap;
    }

    /**
     * Retrieves all ModuleTypes Inputs in the chain.
     *
     * @param moduleTypes all moduleTypes in the chain
     * @return map of all Inputs.
     */
    private Map<String, Input> getAllInputDescriptionsMap(List<ModuleType> moduleTypes) {
        Map<String, Input> resultMap = null;
        Set<Input> inputDescriptions = null;
        if (moduleTypes != null) {
            inputDescriptions = new HashSet();
            Set currentInputs = null;
            for (ModuleType moduleType : moduleTypes) {
                if (moduleType instanceof ConditionType) {
                    currentInputs = ((ConditionType) moduleType).getInputs();
                } else if (moduleType instanceof ActionType) {
                    currentInputs = ((ActionType) moduleType).getInputs();
                } else {
                    // error case
                    inputDescriptions = null;
                    log.error("Retrieving Inputs: ModuleType uid=" + module.getTypeUID()
                            + " -> Inputs not supported in ModuleType uid=" + moduleType.getUID());
                    break;
                }
                if (currentInputs != null) {
                    inputDescriptions.addAll(currentInputs);
                }
            }
        }
        if (inputDescriptions != null && !inputDescriptions.isEmpty()) {
            resultMap = toInputDescriptionMap(inputDescriptions);
        }
        return resultMap;
    }

    /**
     * Retrieves all ModuleTypes Outputs in the chain.
     *
     * @param moduleTypes all moduleTypes in the chain
     * @return map of all Outputs.
     */
    private Map<String, Output> getAllOutputDescriptions(List<ModuleType> moduleTypes) {
        Map<String, Output> resultMap = null;
        Set<Output> outputDescriptions = null;
        if (moduleTypes != null) {
            outputDescriptions = new HashSet();
            Set currentOutputs = null;
            for (ModuleType moduleType : moduleTypes) {
                if (moduleType instanceof TriggerType) {
                    currentOutputs = ((TriggerType) moduleType).getOutputs();
                } else if (moduleType instanceof ActionType) {
                    currentOutputs = ((ActionType) moduleType).getOutputs();
                } else {
                    // error case
                    outputDescriptions = null;
                    log.error("Retrieving Outputs: ModuleType uid=" + module.getTypeUID()
                            + " -> Outputs not supported in ModuleType uid=" + moduleType.getUID());
                    break;
                }
                if (currentOutputs != null) {
                    outputDescriptions.addAll(currentOutputs);
                }
            }
        }
        if (outputDescriptions != null && !outputDescriptions.isEmpty()) {
            resultMap = toOutputDescriptionMap(outputDescriptions);
        }
        return resultMap;
    }

    /**
     * Retrieves all ModuleTypes for given ModuleTypeUID
     *
     * @param moduleTypeUID the source module type
     * @return list of all module types in the hierarchy
     */
    private List<ModuleType> getAllModuleTypes(String moduleTypeUID) {
        List<ModuleType> allModuleTypes = new ArrayList();
        String currentModuleTypeUID = moduleTypeUID;
        ModuleType currentModuleType;
        do {
            currentModuleType = moduleTypeRegistry.get(currentModuleTypeUID);
            if (currentModuleType != null) {
                allModuleTypes.add(currentModuleType);
                currentModuleTypeUID = getParentModuleTypeUID(currentModuleTypeUID);
            } else {// error case
                allModuleTypes = null;
                log.error("From ModuleType uid=" + moduleTypeUID + " -> ModuleType uid=" + currentModuleTypeUID
                        + " is not available.");
                break;
            }
        } while (currentModuleTypeUID != null);// while there is parent ModuleType

        return allModuleTypes;
    }

    /**
     * Gets parent moduleTypeUID if passed moduleTypeUID has parent
     *
     * @param childModuleTypeUID the UID of the moduleType
     * @return parent module type UID if passed moduleType has parent, null otherwise
     */
    private String getParentModuleTypeUID(String childModuleTypeUID) {
        String parentModuleTypeUID = null;
        if (childModuleTypeUID.indexOf(MODULE_TYPE_SEPARATOR) != -1) {
            parentModuleTypeUID = childModuleTypeUID.substring(0, childModuleTypeUID.lastIndexOf(MODULE_TYPE_SEPARATOR));
        }
        return parentModuleTypeUID;
    }

    /**
     * Converts Set of ConfigDescriptionParameter to Map <br/>
     * <code>key</code>: name of ConfigDescriptionParamater, <code>value</code>: ConfigDescriptionParamater
     *
     * @param configurationDescriptions Set of ConfigDescriptionParameter
     * @return Map with ConfigDescriptionParameters
     */
    private Map<String, ConfigDescriptionParameter> toConfigDescriptionParameterMap(
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
    private Map<String, Input> toInputDescriptionMap(Set<Input> inputs) {
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
    private Map<String, Output> toOutputDescriptionMap(Set<Output> outputs) {
        Map<String, Output> outputsMap = new HashMap();
        for (Output output : outputs) {
            outputsMap.put(output.getName(), output);
        }
        return outputsMap;
    }

    /**
     * Checks if object is parsable or not(starts with {@link #REFERENCE_SYMBOL})
     *
     * @param obj the object to be checked
     * @return true if object is parsable, false otherwise
     */
    private boolean isParsable(Object obj) {
        boolean parsable = false;
        if (obj instanceof String) {
            String objString = (String) obj;
            parsable = ((objString.charAt(0) == REFERENCE_SYMBOL) && (objString.length() > 1));
        }
        return parsable;
    }

    /**
     * Simple parsing of context/reference starting with {@link #REFERENCE_SYMBOL}.
     *
     * @param parsable the parsable context/reference
     * @return parsed context/reference.
     */
    private String parse(String parsable) {
        return parsable.substring(1);
    }

    /**
     * Context property in ConfigDescriptionParameter can contain json
     *
     * @param configParameter the configParameter
     * @return context as JSON or <code>null</code>
     */
    private JSONObject getContextJSON(ConfigDescriptionParameter configParameter) {
        JSONObject context = null;
        String contextStr = configParameter.getContext();
        if (contextStr != null && contextStr.length() > 0) {
            try {
                context = new JSONObject(contextStr);
            } catch (JSONException e) {
                log.error("Context property of ConfigurationProperty is not a valid JSON.", e);
            }
        }
        return context;
    }

    /**
     * Retrieves default value from ConfigDescriptionParameter with its right Type.
     *
     * @param configParameter the ConfigDescriptionParameter
     * @return the default Configuration value for passed ConfigDescriptionParameter converted to correct Type
     */
    private Object getConfigDefaultValue(ConfigDescriptionParameter configParameter) {
        String configDefaultValue = configParameter.getDefault();
        Object resultValue = null;
        try {
            resultValue = convertToConfigType(configParameter, configDefaultValue);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return resultValue;
    }

    /**
     * Utility method converts raw value to its correct ConfigurationProperty type.
     *
     * @param configParameter the ConfigDescriptionParameter default value have to be taken
     * @param value raw value
     * @return the converted value to correct ConfigurationProperty type
     * @throws Exception if conversion failed
     */
    private Object convertToConfigType(ConfigDescriptionParameter configParameter, String value) throws Exception {
        Object result;
        Type type = configParameter.getType();
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
                } else { // error case: typo or not boolean
                    throw new IllegalArgumentException("Failed to convert boolean configProperty: "
                            + configParameter.getName() + ", value: " + value);
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

}
