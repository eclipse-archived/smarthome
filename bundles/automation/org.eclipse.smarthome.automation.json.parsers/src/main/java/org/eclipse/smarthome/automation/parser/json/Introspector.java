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

package org.eclipse.smarthome.automation.parser.json;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs mapping from JSON to Objects.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class Introspector {

    static final String NAN = "NaN"; //$NON-NLS-1$
    static final String N_INFINITY = "-Infinity"; //$NON-NLS-1$
    static final String P_INFINITY = "Infinity"; //$NON-NLS-1$
    private Map<Class<?>, Class<?>> primitiveMap;
    private Logger log;

    public Introspector() {
        if (primitiveMap == null) {
            Map<Class<?>, Class<?>> m = new HashMap<Class<?>, Class<?>>();
            m.put(int.class, Integer.class);
            m.put(short.class, Short.class);
            m.put(long.class, Long.class);
            m.put(byte.class, Byte.class);
            m.put(char.class, Character.class);
            m.put(float.class, Float.class);
            m.put(double.class, Double.class);
            m.put(boolean.class, Boolean.class);
            primitiveMap = m;
        }
        log = LoggerFactory.getLogger(Introspector.class);
    }

    /**
     * Converts a {@link JSONArray} object to the class types specified in the
     * second parameter. If error happens during conversion, even for one element
     * only, the while conversion fails.
     *
     * @param array the array values which to convert to valid Java objects
     * @param types the target types
     * @return objects array with converted values.
     * @throws JSONException if JSON data de-serialization error occurs
     * @throws InstantiationException if any of the target types is array, List,
     *             Map, Dictionary and the target type cannot be instantiated.
     * @throws IllegalAccessException if any of the target types is array, List,
     *             Map, Dictionary and the target type cannot be instantiated.
     * @throws IllegalArgumentException if the value passes as parameter cannot be
     *             converted to requested parameter type.
     * @throws java.lang.reflect.InvocationTargetException if the underlying method throws an
     *             exception.
     */
    Object[] deserialize(boolean passAdditionalFirstParam, JSONArray array, Class[] types) throws JSONException {
        int additionalParam = passAdditionalFirstParam ? 1 : 0;
        final Object[] ret = new Object[types.length];
        for (int i = additionalParam; i < types.length; i++) {
            ret[i] = deserialize(array.get(i - additionalParam), types[i]);
        }
        return ret;
    }

    /**
     * Converts a {@link JSONObject} object to the class type specified in the
     * second parameter. If error happens during conversion, the conversion fails.
     *
     * @param jsonData value which to convert to valid Java object
     * @param toType the target type
     * @return
     * @throws JSONException
     */
    Object deserialize(Object jsonData, Class toType) throws JSONException {
        if (JSONObject.NULL.equals(jsonData)) {
            return null;
        }

        final Class primClass = primitiveMap.get(toType);
        if (primClass != null) {
            toType = primClass;
        }
        if (jsonData.getClass() == toType) {
            return jsonData;
        }

        if (String.class == toType) {
            return jsonData.toString();
        }
        if (StringBuffer.class == toType) {
            return new StringBuffer(jsonData.toString());
        }
        if (Character.class == toType) {
            String sjsonData = jsonData.toString();
            if (sjsonData.length() == 1) {
                return new Character(sjsonData.charAt(0));
            }
            throw new JSONException(String.format("Expected %s, but was %s", toType, sjsonData));
        }

        if (jsonData instanceof Number) { // Integer, Long or Double
            final Number nJsondata = (Number) jsonData;
            if (Integer.class == toType) {
                return new Integer(nJsondata.intValue());
            }
            if (Float.class == toType) {
                return new Float(nJsondata.floatValue());
            }
            if (Long.class == toType) {
                return new Long(nJsondata.longValue());
            }
            if (Byte.class == toType) {
                return new Byte(nJsondata.byteValue()); // NOPMD - Byte.valueOf() n/a in OSGi/Minimum EE
            }
            if (Short.class == toType) {
                return new Short(nJsondata.shortValue()); // NOPMD - Short.valueOf() n/a in OSGi/Minimum EE
            }
            if (Float.class == toType) {
                return new Float(nJsondata.floatValue());
            }
            if (Double.class == toType) {
                return new Double(nJsondata.doubleValue());
            }
            if (Object.class == toType || Number.class == toType || Number.class.isAssignableFrom(toType)) {
                return jsonData;
            }
        } else if (jsonData instanceof JSONArray) {
            List res = null;
            final JSONArray array = (JSONArray) jsonData;
            if (Object.class == toType || List.class == toType || ArrayList.class == toType) { // NOPMD
                res = new ArrayList();
            } else if (List.class.isAssignableFrom(toType)) {
                try {
                    res = (List) toType.newInstance();
                } catch (Throwable t) {
                    if (log != null) {
                        log.warn(String.format("Unable to instantiate %s", toType), t);
                    }
                }
            }
            if (res != null) {
                Class defaultElementType = null;
                boolean isJsonObject = false;
                for (int i = 0; i < array.length(); i++) {
                    Object o = array.get(i);
                    if (isJsonObject = o instanceof JSONObject) {
                        String elementTypeName = optString(((JSONObject) o), "json.elementType");
                        if (elementTypeName != null && elementTypeName.length() > 0) {
                            try {
                                defaultElementType = Class.forName(elementTypeName);
                            } catch (Throwable t) {
                                if (log != null) {
                                    log.warn(
                                            String.format(
                                                    "Unable to get the Class object associated with the class or interface with the given name : %s",
                                                    elementTypeName), t);
                                }
                            }
                            continue;
                        }
                    }
                    o = defaultElementType != null ? deserialize(o, defaultElementType) : isJsonObject ? deserialize(o,
                            Map.class) : o instanceof JSONArray ? deserialize(o, List.class) : deserialize(o,
                            Object.class);
                    res.add(o);
                }
                return res;
            }
            if (toType.isArray()) {
                final Class component = toType.getComponentType();
                final int length = array.length();
                final Object ret = Array.newInstance(component, length);
                for (int i = 0; i < length; i++) {
                    Array.set(ret, i, deserialize(array.get(i), component));
                }
                return ret;
            }
        } else if (jsonData instanceof JSONObject) {
            final JSONObject jsonObject = (JSONObject) jsonData;
            Class defaultElementType = null;
            Map map = null;
            if (Object.class == toType || Map.class == toType || HashMap.class == toType) { // NOPMD
                map = new HashMap();
            } else if (Map.class.isAssignableFrom(toType)) {
                try {
                    map = (Map) toType.newInstance();
                } catch (Throwable t) {
                    if (log != null) {
                        log.warn(String.format("Unable to instantiate %s", toType), t);
                    }
                }
            }
            if (map != null) {
                String elTypeName = optString(jsonObject, "json.elementType");
                if (elTypeName != null && elTypeName.length() > 0) {
                    try {
                        defaultElementType = Class.forName(elTypeName);
                    } catch (Throwable t) {
                        if (log != null) {
                            log.warn(
                                    String.format(
                                            "Unable to get the Class object associated with the class or interface with the given name : %s",
                                            elTypeName), t);
                        }
                    }
                }
                for (Iterator i = jsonObject.keys(); i.hasNext();) {
                    final String key = (String) i.next();
                    Object o = jsonObject.get(key);
                    if ("json.elementType".equals(key)) {
                        continue;
                    }
                    o = defaultElementType != null ? deserialize(o, defaultElementType)
                            : o instanceof JSONObject ? deserialize(o, Map.class)
                                    : o instanceof JSONArray ? deserialize(o, List.class)
                                            : deserialize(o, Object.class);
                    map.put(key, o);
                }
                return map;
            }
            Dictionary dict = null;
            if (Dictionary.class == toType || Hashtable.class == toType) { // NOPMD
                dict = new Hashtable();
            } else if (Dictionary.class.isAssignableFrom(toType)) {
                try {
                    dict = (Dictionary) toType.newInstance();
                } catch (Throwable t) {
                    if (log != null) {
                        log.warn(String.format("Unable to instantiate %s", toType), t);
                    }
                }
            }
            if (dict != null) {
                String elTypeName = optString(jsonObject, "json.elementType");
                if (elTypeName != null && elTypeName.length() > 0) {
                    try {
                        defaultElementType = Class.forName(elTypeName);
                    } catch (Throwable t) {
                        if (log != null) {
                            log.warn(
                                    String.format(
                                            "Unable to get the Class object associated with the class or interface with the given name : %s",
                                            elTypeName), t);
                        }
                    }
                }
                for (Iterator i = jsonObject.keys(); i.hasNext();) {
                    final String key = (String) i.next();
                    Object o = jsonObject.get(key);
                    if ("json.elementType".equals(key)) {
                        continue;
                    }
                    o = defaultElementType != null ? deserialize(o, defaultElementType)
                            : o instanceof JSONObject ? deserialize(o, Map.class)
                                    : o instanceof JSONArray ? deserialize(o, List.class)
                                            : deserialize(o, Object.class);
                    dict.put(key, o);
                }
                return dict;
            }
            Object bean = mapToBean(jsonObject, toType);
            if (bean != null) {
                return bean;
            }
        } else { // jsonData is String
            if (Object.class == toType) {
                return jsonData;
            }
            if (Double.class == toType) { // check for Double NaN, NEGATIVE_INFINITY, POSITIVE_INFINITY
                if (NAN.equals(jsonData)) {
                    return new Double(Double.NaN);
                } else if (N_INFINITY.equals(jsonData)) {
                    return new Double(Double.NEGATIVE_INFINITY);
                } else if (P_INFINITY.equals(jsonData)) {
                    return new Double(Double.POSITIVE_INFINITY);
                }
            } else if (Float.class == toType) {
                if (NAN.equals(jsonData)) {
                    return new Float(Float.NaN);
                } else if (N_INFINITY.equals(jsonData)) {
                    return new Float(Float.NEGATIVE_INFINITY);
                } else if (P_INFINITY.equals(jsonData)) {
                    return new Float(Float.POSITIVE_INFINITY);
                }
            }
        }

        throw new JSONException(String.format("Expected %s, but was %s", toType, jsonData));
    }

    private Object mapToBean(JSONObject beanData, Class toType) {
        try {
            final Object bean = toType.newInstance();
            final Method[] methods = toType.getMethods();
            for (Iterator i = beanData.keys(); i.hasNext();) {
                final String key = (String) i.next();
                final Object val = beanData.get(key);
                final Method propSetter = findSetter("set" + capitalize(key), methods); //$NON-NLS-1$
                if (propSetter == null) {
                    if (log != null) {
                        log.warn(String.format("Unable to find Setter for %s in class %s", key, toType));
                    }
                    continue;
                }
                final Class[] paramTypes = propSetter.getParameterTypes();
                AccessController.doPrivileged(new PrivilegedAction() {
                    @Override
                    public Object run() {
                        Object obj;
                        try {
                            obj = new Object[] { deserialize(val, paramTypes[0]) };
                        } catch (JSONException e) {
                            if (log != null) {
                                log.warn(String.format("Unable to deserialize %s", val), e);
                            } else {
                                e.printStackTrace();
                            }
                            return null;
                        }
                        try {
                            return propSetter.invoke(bean, obj);
                        } catch (Throwable t) {
                            if (log != null) {
                                log.warn(String.format("Unable to invoke %s", propSetter), t);
                            } else {
                                t.printStackTrace();
                            }
                        }
                        return obj;
                    }
                });
            }
        } catch (Throwable t) {
            if (log != null) {
                log.warn(String.format("Unable to instantiate %s", toType), t);
            }
        }
        return null;
    }

    private Method findSetter(String name, Method[] methods) {
        for (int i = 0; i < methods.length; i++) {
            if (methods[i] != null && methods[i].getName().equals(name)) {
                Method m = methods[i];
                methods[i] = null;
                final Class[] paramTypes = m.getParameterTypes();
                if (paramTypes != null && paramTypes.length == 1) {
                    Class declaringClass = m.getDeclaringClass();
                    if (Modifier.isPublic(declaringClass.getModifiers())) {
                        return m;
                    }
                    m = findPublicMethod(m, declaringClass, paramTypes[0]);
                    if (m != null) {
                        return m;
                    }
                }
            }
        }
        return null;
    }

    private Method findPublicMethod(Method m, Class clazz, Class param) {
        Class returnType = m.getReturnType();
        Class[] interfaces = clazz.getInterfaces();
        String name = m.getName();
        for (int i = 0; i < interfaces.length; i++) {
            if (Modifier.isPublic(interfaces[i].getModifiers())) {
                final Method newM = findPublicMethod(name, returnType, param, interfaces[i]);
                if (newM != null) {
                    return newM;
                }
            }
        }
        Class superClass = clazz.getSuperclass();
        return superClass == Object.class ? null : findPublicMethod(name, returnType, param, superClass);
    }

    private Method findPublicMethod(String name, Class returnType, Class param, Class fromClass) {
        Method[] methods = fromClass.getMethods();
        for (int j = 0; j < methods.length; j++) {
            final Class[] paramTypes = methods[j].getParameterTypes();
            if (methods[j].getName().equals(name)
                    && methods[j].getReturnType().equals(returnType)
                    && (param == null && paramTypes.length == 0 || paramTypes.length == 1
                            && paramTypes[0].equals(param))) {
                return methods[j];
            }
        }
        return null;
    }

    /**
     * Capitalizes the first letter of the given string argument. For example,
     * "value" will become "Value", "valueString" will become "ValueString",
     * "Ready" will stay "Ready" and "READY" will stay "READY".
     *
     * @param lower the initial string. It must not be null and its length after
     *            trimming must not be 0
     * @return the transformed string argument or null if it does not have the
     *         required form.
     */
    private String capitalize(String lower) {
        if (lower == null || lower.trim().length() == 0) {
            return null;
        }
        char[] chars = lower.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return String.valueOf(chars);
    }

    private String optString(JSONObject json, String key) {
        return json.has(key) ? json.optString(key) : null;
    }

}