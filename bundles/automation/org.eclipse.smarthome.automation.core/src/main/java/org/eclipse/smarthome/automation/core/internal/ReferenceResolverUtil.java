/**
 * Copyright (c) 1997, 2016 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.config.core.Configuration;
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
 * 'configurationProperty': '${singleReference}'
 * </li>
 * <li>
 * Complex reference configuration value where only reference parts are replaced in the whole configuration property
 * value.
 * <br/>
 * 'configurationProperty': '{key1: ${complexReference1}, key2: ${complexReference2}'
 * </li>
 * </ul>
 *
 * Given Module 'A' is child of CompositeModule then its inputs can have '${singleReferences}' to CompositeModule.
 * <ul>
 * <li>
 * Single reference to CompositeModule inputs where whole input value is replaced with the referenced value
 * <br/>
 * 'childInput' : '${compositeModuleInput}'
 * </li>
 * <li>
 * Single reference to CompositeModule configuration where whole input value is replaced with the referenced value
 * <br/>
 * 'childInput' : '${compositeModuleConfiguration}'
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
     * 1) If a module configuration property has a value '${name}' the method looks for such key in context
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
        Configuration config = module.getConfiguration();
        for (String configKey : config.keySet()) {
            Object o = config.get(configKey);
            if (o instanceof String) {
                String childConfigPropertyValue = (String) o;
                if (isReference(childConfigPropertyValue)) {
                    Object result = resolveReference(childConfigPropertyValue, context);
                    if (result != null) {
                        config.put(configKey, result);
                    }
                } else if (containsPattern(childConfigPropertyValue)) {
                    Object result = resolvePattern(childConfigPropertyValue, context);
                    config.put(configKey, result);
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
     * Resolves single reference '${singleReference}' from given context.
     *
     * @param reference single reference to parse
     * @param context from where the value will be get
     * @return resolved value
     */
    public static Object resolveReference(String reference, Map<String, ?> context) {
        Object result = reference;
        if (isReference(reference)) {
            final String trimmedVal = reference.trim();
            result = context.get(trimmedVal.substring(2, trimmedVal.length() - 1));// ${substring}
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
                sb.append(referencedValue);
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
     * Determines whether given Text is '${reference}'.
     *
     * @param value to be evaluated
     * @return True if this value is a '${reference}', false otherwise.
     */
    private static boolean isReference(String value) {
        String trimmedVal = value == null ? null : value.trim();
        return trimmedVal != null && trimmedVal.lastIndexOf("${") == 0 // starts with '${' and it contains it only once
                && trimmedVal.indexOf('}') == trimmedVal.length() - 1 // contains '}' only once - last char
                && trimmedVal.length() > 3; // reference is not empty '${}'
    }

    /**
     * Determines whether given Text is '.....${reference}...'.
     *
     * @param value to be evaluated
     * @return True if this value is a '.....${reference}...', false otherwise.
     */
    private static boolean containsPattern(String value) {
        return value != null && value.trim().contains("${") && value.trim().indexOf("${") < value.trim().indexOf("}");
    }

    /**
     * This method tries to extract value from Bean or Map.
     * <li>To get Map value, the square brackets have to be used as reference: [x] is equivalent to the call
     * ((Map)object).get(x)
     * <li>To get Bean value, the dot and property name have to be used as reference: .x is equivalent to the call
     * object.getX()
     *
     * For example: ref = [x].y[z] will execute the call: ((Map)((Map)object).get(x).getY()).get(z)
     *
     * @param object Bean ot map object
     * @param ref reference path to the value
     * @return the value when it exist on specified reference path or null otherwise.
     */
    public static Object getValue(Object object, String ref) {
        Object result = null;
        int idx = -1;
        if (object == null) {
            return null;
        }
        if ((ref == null) || (ref.length() == 0)) {
            return object;
        }

        char ch = ref.charAt(0);
        if ('.' == ch) {
            ref = ref.substring(1, ref.length());
        }

        if ('[' == ch) {
            if (!(object instanceof Map)) {
                return null;
            }
            idx = ref.indexOf(']');
            if (idx == -1) {
                return null;
            }
            String key = ref.substring(1, idx++);
            Map map = (Map) object;
            result = map.get(key);
        } else {
            String key = null;
            idx = getNextRefToken(ref, 1);
            key = idx != ref.length() ? ref.substring(0, idx) : ref;
            String getter = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
            try {
                Method m = object.getClass().getMethod(getter, new Class[0]);
                if (m != null) {
                    result = m.invoke(object, null);
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }
        if ((result != null) && (idx < ref.length())) {
            return getValue(result, ref.substring(idx));
        }
        return result;
    }

    /**
     * Gets the end of current token of reference path.
     *
     * @param ref reference path used to access value in bean or map objects
     * @param startIndex starting point to check for next tokens.
     * @return end of current token.
     */
    public static int getNextRefToken(String ref, int startIndex) {
        int idx1 = ref.indexOf('[', startIndex);
        int idx2 = ref.indexOf('.', startIndex);
        int idx;
        if ((idx1 != -1) && ((idx2 == -1) || (idx1 < idx2))) {
            idx = idx1;
        } else {
            if ((idx2 != -1) && ((idx1 == -1) || (idx2 < idx1))) {
                idx = idx2;
            } else {
                idx = ref.length();
            }
        }
        return idx;
    }

}
