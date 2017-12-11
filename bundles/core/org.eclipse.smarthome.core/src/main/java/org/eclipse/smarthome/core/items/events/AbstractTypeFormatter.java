/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Abstract behavior to format and parse Type instances.
 *
 * @author Henning Treu - initial contribution
 *
 * @param <T> the concrete type an implementation handles.
 */
abstract class AbstractTypeFormatter<T extends Type> {

    private static final String TYPE_POSTFIX = "Type";

    private static final String CORE_LIBRARY_PACKAGE = "org.eclipse.smarthome.core.library.types.";

    abstract String format(T type);

    abstract T parse(String type, String value, Map<String, String> stateMap);

    T parse(String type, String value) {
        return parse(type, value, null);
    }

    String getType(T type) {
        return StringUtils.removeEnd(type.getClass().getSimpleName(), TYPE_POSTFIX);
    }

    protected T parseType(String typeName, String valueToParse, Class<T> desiredClass) {
        Object parsedObject = null;
        String simpleClassName = typeName + TYPE_POSTFIX;
        parsedObject = parseSimpleClassName(simpleClassName, valueToParse);

        if (parsedObject == null || !desiredClass.isAssignableFrom(parsedObject.getClass())) {
            String parsedObjectClassName = parsedObject != null ? parsedObject.getClass().getName() : "<undefined>";
            throw new IllegalArgumentException("Error parsing simpleClasssName '" + simpleClassName + "' with value '"
                    + valueToParse + "'. Desired type was '" + desiredClass.getName() + "' but got '"
                    + parsedObjectClassName + "'.");
        }

        return desiredClass.cast(parsedObject);
    }

    private Object parseSimpleClassName(String simpleClassName, String valueToParse) {
        if (simpleClassName.equals(UnDefType.class.getSimpleName())) {
            return UnDefType.valueOf(valueToParse);
        }
        if (simpleClassName.equals(RefreshType.class.getSimpleName())) {
            return RefreshType.valueOf(valueToParse);
        }

        try {
            Class<?> stateClass = Class.forName(CORE_LIBRARY_PACKAGE + simpleClassName);
            Method valueOfMethod = stateClass.getMethod("valueOf", String.class);
            return valueOfMethod.invoke(null, valueToParse);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Error getting class for simple name: '" + simpleClassName
                    + "' using package name '" + CORE_LIBRARY_PACKAGE + "'.", e);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException(
                    "Error getting method #valueOf(String) of class '" + CORE_LIBRARY_PACKAGE + simpleClassName + "'.",
                    e);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalStateException("Error invoking #valueOf(String) on class '" + CORE_LIBRARY_PACKAGE
                    + simpleClassName + "' with value '" + valueToParse + "'.", e);
        }
    }

}