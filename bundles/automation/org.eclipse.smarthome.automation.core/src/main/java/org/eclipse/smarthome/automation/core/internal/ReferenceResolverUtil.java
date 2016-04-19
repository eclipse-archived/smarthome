/**
 * Copyright (c) 1997, 2016 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves Module references.
 * They can be
 * <ul>
 * <li>
 * Module configuration property to Rule Configuration property
 * </li>
 * <li>
 * Module configuration property to Composite Module configuration property
 * </li>
 * <li>
 * Module inputs to Composite Module inputs
 * </li>
 * <li>
 * Module inputs to Composite Module Configuration
 * </li>
 * </ul>
 *
 * Module 'A' Configuration properties can have references to either CompositeModule Configuration properties or Rule
 * Configuration properties depending where Module 'A' is placed.
 * <br/>
 * Note. If Module 'A' is child of CompositeModule - it cannot have direct configuration references to the Rule that is
 * holding the CompositeModule.
 * <ul>
 * <li>
 * Single reference configuration value where whole configuration property value is replaced(if found) with the
 * referenced value
 * <br/>
 * 'configurationProperty': '$singleReference'
 * </li>
 * <li>
 * Complex reference configuration value where only reference parts are replaced in the whole configuration property
 * value.
 * <br/>
 * 'configurationProperty': '{key1: ${complexReference1}, key2: ${complexReference2}'
 * </li>
 * </ul>
 *
 * Given Module 'A' is child of CompositeModule then its inputs can have '$singleReferences' to CompositeModule.
 * <ul>
 * <li>
 * Single reference to CompositeModule inputs where whole input value is replaces with the referenced value
 * <br/>
 * 'childInput' : '$compositeModuleInput'
 * </li>
 * <li>
 * Single reference to CompositeModule configuration where whole input value is replaces with the referenced value
 * <br/>
 * 'childInput' : '$compositeModuleConfiguration'
 * </li>
 * </ul>
 *
 * @author Vasil Ilchev - Initial Contribution
 */
public class ReferenceResolverUtil {

    private static final Logger logger = LoggerFactory.getLogger(ReferenceResolverUtil.class);

    private ReferenceResolverUtil() {
    }

    /**
     * Updates (changes) configuration properties of module base on given context (it can be CompositeModule
     * Configuration or Rule Configuration).
     * For example:
     * 1) If a module configuration property has a value '$name' the method looks for such key in context
     * and if found - replace the module's configuration value as it is.
     *
     * 2) If a module configuration property has complex value 'Hello ${firstName} ${lastName}'
     * the method tries to parse it and replace (if values are found) referenced parts in module's configuration value.
     * Will try to find values for ${firstName} and ${lastName} in the given context and replace them.
     * References that are not found in the context - are not replaced.
     *
     * @param module module that is directly part of Rule or part of CompositeModule
     * @param context containing Rule configuration or Composite configuration values.
     */
    public static void updateModuleConfiguration(Module module, Map<String, ?> context) {
        for (Entry<String, Object> config : module.getConfiguration().entrySet()) {
            Object o = config.getValue();
            if (o instanceof String) {
                String childConfigPropertyValue = (String) o;
                if (isReference(childConfigPropertyValue)) {
                    Object result = resolveReference(childConfigPropertyValue, context);
                    if (result != null) {
                        config.setValue(result);
                    }
                } else if (isPattern(childConfigPropertyValue)) {
                    Object result = resolvePattern(childConfigPropertyValue, context);

                    config.setValue(result);
                }
            }
        }
    }

    /**
     * Resolves Composite child module's inputs references to CompositeModule context (inputs and configuration)
     *
     * @param module Composite Module's child module.
     * @param compositeContext Composite Module's context
     * @return context for given module ready for execution.
     */
    public static Map<String, Object> getCompositeChildContext(Module module, Map<String, ?> compositeContext) {
        Map<String, Object> resultContext = new HashMap<String, Object>();
        Map<String, String> inputs = null;

        if (module instanceof Condition) {
            inputs = ((Condition) module).getInputs();
        } else if (module instanceof Action) {
            inputs = ((Action) module).getInputs();
        }

        if (inputs != null) {
            for (Entry<String, String> input : inputs.entrySet()) {
                final String inputName = input.getKey();
                final String inputValue = input.getValue();
                if (isReference(inputValue)) {
                    final Object result = resolveReference(inputValue, compositeContext);

                    resultContext.put(inputName, result);
                }
            }
        }
        return resultContext;
    }

    /**
     * Resolves single reference '$singleReference' from given context.
     *
     * @param reference single reference to parse
     * @param context from where the value will be get
     * @return resolved value
     */
    public static Object resolveReference(String reference, Map<String, ?> context) {
        Object result = reference;
        if (isReference(reference)) {
            result = context.get(reference.trim().substring(1));
        }
        return result;
    }

    /**
     * Tries to resolve complex references e.g. 'Hello ${firstName} ${lastName}'..'{key1: ${reference1}, key2:
     * ${reference2}}'..etc.
     *
     * References are keys in the context map (without the '${' prefix and '}' suffix).
     *
     * If value is found in the given context it overrides the reference part in the configuration value.
     * For example:
     *
     * <pre>
     * configuration {
     * ..
     *   configProperty: 'Hello ${firstName} ${lastName}'
     * ..
     * }
     * </pre>
     *
     * And context that has value for '${lastName}':
     *
     * <pre>
     * ..
     *   firstName: MyFirstName
     * ..
     *   lastName: MyLastName
     * ..
     * </pre>
     *
     * Result will be:
     *
     * <pre>
     * configuration {
     * ..
     * configProperty: 'Hello MyFirstName MyLastName'
     * ..
     * }
     * </pre>
     *
     * References for which values are not found in the context - remain as they are in the configuration property.
     * (It will not stop resolving the remaining references(if there are) in the configuration property value)
     *
     * @param reference
     * @param context
     * @return
     */
    private static String resolvePattern(String reference, Map<String, ?> context) {
        final StringBuilder sb = new StringBuilder();
        int previous = 0;
        for (int start, end; (start = reference.indexOf("${", previous)) != -1; previous = end + 1) {
            sb.append(reference.substring(previous, start));
            end = reference.indexOf('}', start + 2);
            if (end == -1) {
                previous = start;
                logger.warn("Couldn't parse referenced key: " + reference.substring(start)
                        + ": expected reference syntax-> ${referencedKey}");
                break;
            }
            final String referencedKey = reference.substring(start + 2, end);
            final Object referencedValue = context.get(referencedKey);

            if (referencedValue != null) {
                if (isSupportedPatternReferenceType(referencedValue)) {
                    sb.append(referencedValue);
                } else {
                    // not supported type restore reference
                    sb.append(reference.substring(start, end + 1));
                    logger.warn("Not supported type: " + referencedValue.getClass());
                }
            } else {
                // remain as it is: value is null
                sb.append(reference.substring(start, end + 1));
                logger.warn("Cannot find reference for ${ {} } , it will remain the same.", referencedKey);
            }
        }
        sb.append(reference.substring(previous));
        return sb.toString();
    }

    /**
     * Found reference value should have meaningful String representation as it is part of configuration property which
     * type is String
     *
     * @param obj that is referenced in complex reference
     * @return true if referenced value is valid complex reference type, false otherwise
     */
    private static boolean isSupportedPatternReferenceType(Object obj) {
        return obj instanceof String || obj instanceof Number || obj instanceof Boolean;
    }

    private static boolean isReference(Object value) {
        boolean result = false;
        if (value instanceof String) {
            String strVal = ((String) value).trim();
            result = strVal.startsWith("$") && strVal.lastIndexOf("$") == 0 && strVal.length() > 1
                    && strVal.charAt(1) != '{';
        }
        return result;
    }

    private static boolean isPattern(String value) {
        return value != null && value.trim().contains("${") && value.trim().contains("}");
    }

}
