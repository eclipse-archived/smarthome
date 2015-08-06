/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.json;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a standard way for analyzing the class and super-classes of some object, looking for either
 * explicit or implicit information and using that information to build an object that comprehensively describes the
 * target one. The resulting object is in json format and it is used for serialization and deserialization of the
 * desired object.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class Introspector {

    static final WeakHashMap<Class<?>, List<MEntry>> METHOD_CACHE = new WeakHashMap<Class<?>, List<MEntry>>();
    static final String NAN = "NaN"; //$NON-NLS-1$
    static final String N_INFINITY = "-Infinity"; //$NON-NLS-1$
    static final String P_INFINITY = "Infinity"; //$NON-NLS-1$
    static int maxLog = 1024;

    static final long MAX_SAFE_INTEGER = 9007199254740991L; // JavaScript Number.MAX_SAFE_INTEGER
    static final long MIN_SAFE_INTEGER = -9007199254740991L; // JavaScript Number.MIN_SAFE_INTEGE

    private Map<Class<?>, Class<?>> primitiveMap;
    private Logger log;

    /**
     * Constructs Introspector.
     */
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
     * Tries to deeply introspect an object and convert it to JSON by starting the recursion.
     *
     * @param object the object it self - wrapper type, array, map, list or bean.
     * @throws JSONException if serialization to JSON failed
     */
    public Object serialize(Object o) {
        Set<Object> circularDependencies = new HashSet<Object>();
        Set<Object> circularObjects = new HashSet<Object>();
        try {
            return serializeObjectToJSON(o, circularDependencies, circularObjects);
        } catch (JSONException e) {
            log.error("cannot serializeObjectToJSON " + o, e);
            return null;
        }
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
    Object[] deserialize(boolean passAdditionalFirstParam, JSONArray array, Class<?>[] types) throws JSONException {
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
    @SuppressWarnings("unchecked")
    public Object deserialize(Object jsonData, Class<?> toType) throws JSONException {
        if (JSONObject.NULL.equals(jsonData)) {
            return null;
        }

        final Class<?> primClass = primitiveMap.get(toType);
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
            throw new JSONException("Expected " + toType + ", but was " + sjsonData);
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
            List<Object> res = null;
            final JSONArray array = (JSONArray) jsonData;
            if (Object.class == toType || List.class == toType || ArrayList.class == toType) { // NOPMD
                res = new ArrayList<Object>();
            } else if (List.class.isAssignableFrom(toType)) {
                try {
                    res = (List<Object>) toType.newInstance();
                } catch (Throwable t) {
                    if (log != null) {
                        log.warn("Unable to instantiate " + toType, t);
                    }
                }
            }
            if (res != null) {
                Class<?> defaultElementType = null;
                boolean isJsonObject = false;
                for (int i = 0; i < array.length(); i++) {
                    Object o = array.get(i);
                    isJsonObject = o instanceof JSONObject;
                    if (isJsonObject) {
                        String elementTypeName = optString(((JSONObject) o), "json.elementType");
                        if (elementTypeName != null && elementTypeName.length() > 0) {
                            try {
                                defaultElementType = Class.forName(elementTypeName);
                            } catch (Throwable t) {
                                if (log != null) {
                                    log.warn(
                                            "Unable to get the Class object associated with the class or interface with the given name : "
                                                    + elementTypeName,
                                            t);
                                }
                            }
                            continue;
                        }
                    }
                    o = defaultElementType != null ? deserialize(o, defaultElementType)
                            : isJsonObject ? deserialize(o, Map.class)
                                    : o instanceof JSONArray ? deserialize(o, List.class)
                                            : deserialize(o, Object.class);
                    res.add(o);
                }
                return res;
            }
            if (toType.isArray()) {
                final Class<?> component = toType.getComponentType();
                final int length = array.length();
                final Object ret = Array.newInstance(component, length);
                for (int i = 0; i < length; i++) {
                    Array.set(ret, i, deserialize(array.get(i), component));
                }
                return ret;
            }
        } else if (jsonData instanceof JSONObject) {
            final JSONObject jsonObject = (JSONObject) jsonData;
            Class<?> defaultElementType = null;
            Map<String, Object> map = null;
            if (Object.class == toType || Map.class == toType || HashMap.class == toType) { // NOPMD
                map = new HashMap<String, Object>();
            } else if (Map.class.isAssignableFrom(toType)) {
                try {
                    map = (Map<String, Object>) toType.newInstance();
                } catch (Throwable t) {
                    if (log != null) {
                        log.warn("Unable to instantiate  " + toType, t);
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
                                    "Unable to get the Class object associated with the class or interface with the given name : "
                                            + elTypeName,
                                    t);
                        }
                    }
                }
                for (Iterator<?> i = jsonObject.keys(); i.hasNext();) {
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
            Dictionary<String, Object> dict = null;
            if (Dictionary.class == toType || Hashtable.class == toType) { // NOPMD
                dict = new Hashtable<String, Object>();
            } else if (Dictionary.class.isAssignableFrom(toType)) {
                try {
                    dict = (Dictionary<String, Object>) toType.newInstance();
                } catch (Throwable t) {
                    if (log != null) {
                        log.warn("Unable to instantiate  " + toType, t);
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
                                    "Unable to get the Class object associated with the class or interface with the given name :  "
                                            + elTypeName,
                                    t);
                        }
                    }
                }
                for (Iterator<?> i = jsonObject.keys(); i.hasNext();) {
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

        throw new JSONException("Expected " + toType + ", but was " + jsonData);
    }

    /**
     * Tries to introspect an object and convert it to JSON.
     *
     * @param o is the target object for serialization.
     * @param circularDependencies holds all references for all serialized components of the target object to avoid
     *            infinite loops.
     * @param circularObjects
     * @return
     * @throws JSONException
     */
    private Object serializeObjectToJSON(final Object o, Set<Object> circularDependencies, Set<Object> circularObjects)
            throws JSONException {

        if (circularObjects.contains(o)) {
            return null;
        }
        if (circularDependencies.contains(o)) {
            circularDependencies.remove(o);
            circularObjects.add(o);
            return null;
        }
        circularDependencies.add(o);
        try {
            if (o == null || JSONObject.NULL.equals(o)) {
                return JSONObject.NULL;
            }

            Class<?> toType = o.getClass();
            final Class<?> primClass = primitiveMap.get(toType);
            if (primClass != null) {
                toType = primClass;
            }

            if (o instanceof JSONObject || o instanceof JSONArray || o instanceof JSONString || o instanceof Character
                    || o instanceof Boolean || o instanceof String) {
                return o;
            }

            if (o instanceof Number) {
                if (o instanceof Double) {
                    Double d = (Double) o;
                    if (d.isNaN()) {
                        return NAN;
                    } else if (d.isInfinite()) {
                        return d.doubleValue() == Double.NEGATIVE_INFINITY ? N_INFINITY : P_INFINITY;
                    }
                } else if (o instanceof Float) {
                    Float f = (Float) o;
                    if (f.isNaN()) {
                        return NAN;
                    } else if (f.isInfinite()) {
                        return f.floatValue() == Float.NEGATIVE_INFINITY ? N_INFINITY : P_INFINITY;
                    }
                }
                long l = ((Number) o).longValue();
                if (l < MIN_SAFE_INTEGER || l > MAX_SAFE_INTEGER) {
                    StringBuffer sb = new StringBuffer("\"");
                    sb.append(o);
                    sb.append('"');
                    return sb.toString();
                }
                return o;
            }

            if (o instanceof Map) {
                final Map<?, ?> d = (Map<?, ?>) o;
                final JSONObject json = new JSONObject();
                for (Map.Entry<?, ?> e : d.entrySet()) {
                    Object jKey = e.getKey();
                    Object jVal = e.getValue();
                    try {
                        Object val = serializeObjectToJSON(jVal, circularDependencies, circularObjects);
                        if (val == null)
                            continue;
                        json.put(jKey.toString(), val);
                    } catch (Throwable t) {
                        if (log != null) {
                            log.warn("The object for " + jKey + " could not be serialized : " + jVal, t);
                        }
                    }
                }
                return json;
            }

            if (o instanceof Dictionary) {
                final Dictionary<?, ?> d = (Dictionary<?, ?>) o;
                final JSONObject json = new JSONObject();
                for (Enumeration<?> e = d.keys(); e.hasMoreElements();) {
                    Object jKey = e.nextElement();
                    Object jVal = d.get(jKey);
                    try {
                        Object val = serializeObjectToJSON(jVal, circularDependencies, circularObjects);
                        if (val == null)
                            continue;
                        json.put(jKey.toString(), val);
                    } catch (Throwable t) {
                        if (log != null) {
                            log.warn("The object for " + jKey + " could not be serialized : " + jVal, t);
                        }
                    }
                }
                return json;
            }

            if (o instanceof Collection) {
                final Collection<?> d = (Collection<?>) o;
                final JSONArray json = new JSONArray();
                for (Iterator<?> i = d.iterator(); i.hasNext();) {
                    Object val = serializeObjectToJSON(i.next(), circularDependencies, circularObjects);
                    if (val == null)
                        continue;
                    json.put(val);
                }
                return json;
            }

            if (toType.isArray()) {
                final int length = Array.getLength(o);
                final JSONArray json = new JSONArray();
                for (int i = 0; i < length; i++) {
                    Object val = serializeObjectToJSON(Array.get(o, i), circularDependencies, circularObjects);
                    if (val == null)
                        continue;
                    json.put(val);
                }
                return json;
            }

            if (o instanceof StringBuffer) {
                return o.toString();
            }

            return beanToJSON2(o, circularDependencies, circularObjects);
        } finally {
            circularDependencies.remove(o);
        }
    }

    private JSONObject beanToJSON2(final Object o, final Set<Object> circularDependencies,
            final Set<Object> circularObjects) throws JSONException {
        final Class<?> clazz = o.getClass();
        final List<MEntry> methods = getSerializableMethods(clazz);
        final JSONObject json = new JSONObject();
        for (int i = 0; null != methods && i < methods.size(); i++) {
            final MEntry m = methods.get(i);
            Object res = AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    try {
                        return serializeObjectToJSON(m.method.invoke(o, (Object[]) null), circularDependencies,
                                circularObjects);
                    } catch (Throwable t) {
                        if (log != null) {
                            log.warn("Unable to invoke method " + m.method.getName(), t);
                        }
                        return null;
                    }
                }
            });
            if (res == null) {
                return null;
            }
            json.put(m.key, res);
        }
        return json;
    }

    private List<MEntry> getSerializableMethods(Class<?> clazz) {
        List<MEntry> ret = METHOD_CACHE.get(clazz);
        if (null != ret) {
            return ret;
        }

        Method[] methods = clazz.getMethods();
        ret = new ArrayList<MEntry>(methods.length);

        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            // if (!Modifier.isPublic(m.getModifiers())) continue;
            Class<?> declaringClass = m.getDeclaringClass();
            boolean isPublic = Modifier.isPublic(declaringClass.getModifiers());
            if (declaringClass == Object.class) {
                continue;
            }
            final String name = m.getName();
            final Class<?>[] types = m.getParameterTypes();
            if (types == null || types.length == 0) {
                if (name.startsWith("get")) { //$NON-NLS-1$
                    m = isPublic ? m : findPublicMethod(m, declaringClass, null);
                    if (m != null) {
                        ret.add(new MEntry(m, propName(name, 3, clazz)));
                    }
                } else if (name.startsWith("is")) { //$NON-NLS-1$
                    m = isPublic ? m : findPublicMethod(m, declaringClass, null);
                    if (m != null) {
                        ret.add(new MEntry(m, propName(name, 2, clazz)));
                    }
                }
            }
        }

        METHOD_CACHE.put(clazz, ret);
        return ret;
    }

    private Object mapToBean(JSONObject beanData, Class<?> toType) {
        try {
            final Object bean = toType.newInstance();
            final Method[] methods = toType.getMethods();
            for (Iterator<?> i = beanData.keys(); i.hasNext();) {
                final String key = (String) i.next();
                final Object val = beanData.get(key);
                final Method propSetter = findSetter("set" + capitalize(key), methods); //$NON-NLS-1$
                if (propSetter == null) {
                    if (log != null) {
                        log.warn("Unable to find Setter for " + key + " in class " + toType);
                    }
                    continue;
                }
                final Class<?>[] paramTypes = propSetter.getParameterTypes();
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    @Override
                    public Object run() {
                        Object obj = null;
                        try {
                            obj = new Object[] { deserialize(val, paramTypes[0]) };
                            return propSetter.invoke(bean, obj);
                        } catch (Throwable t) {
                            if (log != null) {
                                log.warn("Unable to invoke method " + propSetter, t);
                            }
                        }
                        return obj;
                    }
                });
            }
        } catch (Throwable t) {
            if (log != null) {
                log.warn("Unable to instantiate " + toType, t);
            }
        }
        return null;
    }

    private Method findSetter(String name, Method[] methods) {
        for (int i = 0; i < methods.length; i++) {
            if (methods[i] != null && methods[i].getName().equals(name)) {
                Method m = methods[i];
                methods[i] = null;
                final Class<?>[] paramTypes = m.getParameterTypes();
                if (paramTypes != null && paramTypes.length == 1) {
                    Class<?> declaringClass = m.getDeclaringClass();
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

    private Method findPublicMethod(Method m, Class<?> clazz, Class<?> param) {
        Class<?> returnType = m.getReturnType();
        Class<?>[] interfaces = clazz.getInterfaces();
        String name = m.getName();
        for (int i = 0; i < interfaces.length; i++) {
            if (Modifier.isPublic(interfaces[i].getModifiers())) {
                final Method newM = findPublicMethod(name, returnType, param, interfaces[i]);
                if (newM != null) {
                    return newM;
                }
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        return superClass == Object.class ? null : findPublicMethod(name, returnType, param, superClass);
    }

    private Method findPublicMethod(String name, Class<?> returnType, Class<?> param, Class<?> fromClass) {
        Method[] methods = fromClass.getMethods();
        for (int j = 0; j < methods.length; j++) {
            final Class<?>[] paramTypes = methods[j].getParameterTypes();
            if (methods[j].getName().equals(name) && methods[j].getReturnType().equals(returnType)
                    && (param == null && paramTypes.length == 0
                            || paramTypes.length == 1 && paramTypes[0].equals(param))) {
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

    private String propName(String methodName, int off, Class<?> clazz) {
        char[] chars = methodName.toCharArray();
        if (Character.isUpperCase(chars[off]) && // fist char is upper case
                chars.length - off > 1 && !Character.isUpperCase(chars[off + 1])) { // second char is lower case
            try {
                clazz.getDeclaredField(methodName); // field exists - no change
            } catch (NoSuchFieldException e) {
                chars[off] = Character.toLowerCase(chars[off]); // no field - decapitalize the first char
            }
        }
        return new String(chars, off, chars.length - off);
    }
}

/**
 * Class containing methods
 *
 * @author Ana Dimova - Initial Contribution
 */
final class MEntry {

    final Method method;
    final String key;

    MEntry(Method method, String key) {
        this.method = method;
        this.key = key;
    }
}
