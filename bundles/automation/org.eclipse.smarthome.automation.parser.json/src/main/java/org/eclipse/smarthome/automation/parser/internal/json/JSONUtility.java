/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.internal.json;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.smarthome.automation.parser.ParsingNestedException;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;

/**
 * Utility class for performing operations over JSON structures.
 *
 * @author Ana Dimova - Initial Contribution
 * @author Ana Dimova - refactor Parser interface.
 *
 */
public class JSONUtility {

    static final int TRIGGERS = 1;
    static final int CONDITIONS = 2;
    static final int ACTIONS = 3;

    static final int ON = 4;
    static final int IF = 5;
    static final int THEN = 6;
    static final int UID = 7;
    static final int TAGS = 8;
    static final int CONFIG = 9;
    static final int DESCRIPTION = 10;
    static final int VISIBILITY = 11;
    static final int NAME = 12;
    static final int ACTIVE = 13;
    static final int TEMPLATE_UID = 14;

    /**
     * Checks JSON content.
     *
     * @param propertyName for checking.
     * @return int value for using it in switch block or -1 if the property is invalid
     */
    static int checkModuleTypeProperties(String propertyName) {
        if (propertyName.equals(JSONStructureConstants.TRIGGERS))
            return TRIGGERS;
        if (propertyName.equals(JSONStructureConstants.CONDITIONS))
            return CONDITIONS;
        if (propertyName.equals(JSONStructureConstants.ACTIONS))
            return ACTIONS;
        return -1;
    }

    /**
     * Checks JSON content.
     *
     * @param propertyName for checking.
     * @return int value for using it in switch block or -1 if the property is invalid
     */
    static int checkTemplateProperties(String propertyName) {
        if (propertyName.equals(JSONStructureConstants.ON))
            return ON;
        if (propertyName.equals(JSONStructureConstants.IF))
            return IF;
        if (propertyName.equals(JSONStructureConstants.THEN))
            return THEN;
        if (propertyName.equals(JSONStructureConstants.UID))
            return UID;
        if (propertyName.equals(JSONStructureConstants.TAGS))
            return TAGS;
        if (propertyName.equals(JSONStructureConstants.CONFIG))
            return CONFIG;
        if (propertyName.equals(JSONStructureConstants.DESCRIPTION))
            return DESCRIPTION;
        if (propertyName.equals(JSONStructureConstants.VISIBILITY))
            return VISIBILITY;
        return -1;
    }

    /**
     * Checks JSON content.
     *
     * @param propertyName for checking.
     * @return int value for using it in switch block or -1 if the property is invalid
     */
    public static int checkRuleProperties(String propertyName) {
        if (propertyName.equals(JSONStructureConstants.TEMPLATE_UID))
            return TEMPLATE_UID;
        if (propertyName.equals(JSONStructureConstants.UID))
            return UID;
        if (propertyName.equals(JSONStructureConstants.NAME))
            return NAME;
        if (propertyName.equals(JSONStructureConstants.ON))
            return ON;
        if (propertyName.equals(JSONStructureConstants.IF))
            return IF;
        if (propertyName.equals(JSONStructureConstants.THEN))
            return THEN;
        if (propertyName.equals(JSONStructureConstants.UID))
            return UID;
        if (propertyName.equals(JSONStructureConstants.TAGS))
            return TAGS;
        if (propertyName.equals(JSONStructureConstants.CONFIG))
            return CONFIG;
        if (propertyName.equals(JSONStructureConstants.DESCRIPTION))
            return DESCRIPTION;
        return -1;
    }

    /**
     * This method is used to verify if one type is compatible to another.
     *
     * @param type1 type to compare.
     * @param type2 type to compare.
     * @throws IllegalArgumentException if the types are not compatible.
     * @throws ClassNotFoundException
     */
    static void verifyType(BundleContext bc, String type, Object value)
            throws IllegalArgumentException, ClassNotFoundException {
        if (value == null)
            return;
        Class<?> clazz;
        if (bc != null) {
            clazz = bc.getBundle().loadClass(type);
        } else {
            // used in JUnitTests without OSGi runtime
            clazz = JSONUtility.class.getClassLoader().loadClass(type);
        }
        if (clazz.isAssignableFrom(value.getClass())) {
            return;
        }
        throw new IllegalArgumentException(
                "Incompatible types : \"" + type + "\" and \"" + value.getClass().getName() + "\".");
    }

    /**
     * This method is used to verify if one type is compatible to another.
     *
     * @param type is the type to compare.
     * @param value is the value whose type is to compare.
     * @throws IllegalArgumentException if the types are not compatible.
     */
    static String verifyType(Type type, Object value) {
        if (Type.TEXT.equals(type) && String.class.isAssignableFrom(value.getClass())) {
            return (String) value;
        }
        if (Type.BOOLEAN.equals(type)) {
            if (value instanceof Boolean) {
                if ((Boolean) value) {
                    return "true";
                }
                return "false";
            }
            if (value instanceof String && (value.equals("true") || value.equals("false"))) {
                return (String) value;
            }
        }
        if (Type.INTEGER.equals(type)) {
            if (value instanceof Short) {
                return ((Short) value).toString();
            }
            if (value instanceof Byte) {
                return ((Byte) value).toString();
            }
            if (value instanceof Integer) {
                return ((Integer) value).toString();
            }
            if (value instanceof Long) {
                return ((Long) value).toString();
            }
            if (value instanceof String) {
                try {
                    new Long((String) value);
                    return (String) value;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "String \"" + value + "\" can't be converted to " + Type.INTEGER.name(), e);
                }
            }
        }
        if (Type.DECIMAL.equals(type)) {
            if (value instanceof Float) {
                return ((Float) value).toString();
            }
            if (value instanceof Double) {
                return ((Double) value).toString();
            }
            if (value instanceof String) {
                try {
                    new Double((String) value);
                    return (String) value;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "String \"" + value + "\" can't be converted to " + Type.DECIMAL.name(), e);
                }
            }
        }
        throw new IllegalArgumentException(
                "Incompatible types : \"" + type + "\" and \"" + value.getClass().getName() + "\".");
    }

    /**
     * This method is used for deserializing the value from json format by using Introspector.
     *
     * @param bc BundleContext
     * @param type is the name of the class to be loaded.
     * @param value is the value for deserializing.
     * @return the serialized value
     * @throws ClassNotFoundException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws JSONException
     */
    static Object convertValue(BundleContext bc, String type, Object value)
            throws ClassNotFoundException, JSONException, IllegalAccessException, InvocationTargetException {
        if (value instanceof JSONObject) {
            Class<?> clazz = bc.getBundle().loadClass(type);
            Introspector i = new Introspector();
            return i.deserialize(value, clazz);
        }
        return value;
    }

    /**
     * Gets a Boolean value associated with the key if it is present.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param uid is the unique identifier of the automation object - module type, rule or rule template.
     * @param exceptions is a list used for collecting the exceptions occurred during parsing json.
     * @param key is a json property whose value has to be gotten.
     * @param optional declares whether the property is optional or required
     * @param defValue is default value for property that has to be gotten.
     * @param json a JSONObject for parsing
     * @param log is used for logging the exceptions
     * @return a Boolean value
     */
    static Boolean getBoolean(int type, String uid, List<ParsingNestedException> exceptions, String key,
            boolean optional, boolean defValue, JSONObject json, Logger log) {
        if (json.has(key)) {
            Object res = json.opt(key);
            if (!JSONObject.NULL.equals(res) && res instanceof Boolean) {
                return (Boolean) res;
            } else
                catchParsingException(type, uid, exceptions,
                        new IllegalArgumentException("\"" + key + "\" must be Boolean in : " + json), log);
        }
        if (!optional) {
            catchParsingException(type, uid, exceptions,
                    new IllegalArgumentException("\"" + key + "\" must be present in : " + json), log);
        }
        return defValue;
    }

    /**
     * Gets a Boolean value associated with the key if it is present.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param uid is the unique identifier of the automation object - module type, rule or rule template.
     * @param exceptions is a list used for collecting the exceptions occurred during parsing json.
     * @param key is a json property whose value has to be gotten.
     * @param index in the jsonArray
     * @param jsonArray a JSONArray for parsing
     * @param log is used for logging the exceptions
     * @return a Boolean value
     */
    static Boolean getBoolean(int type, String uid, List<ParsingNestedException> exceptions, String key, int index,
            JSONArray jsonArray, Logger log) {
        Object element = jsonArray.opt(index);
        if (element == null || !(element instanceof Boolean)) {
            catchParsingException(type, uid, exceptions, new IllegalArgumentException(
                    "Elements of \"" + key + "\" must be present and must be Boolean in : " + element), log);
            return null;
        }
        return (Boolean) element;
    }

    /**
     * Gets a Number value associated with the key if it is present.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param uid is the unique identifier of the automation object - module type, rule or rule template.
     * @param exceptions is a list used for collecting the exceptions occurred during parsing json.
     * @param key is a json property whose value has to be gotten.
     * @param optional declares whether the property is optional or required
     * @param json a JSONObject for parsing
     * @param log is used for logging the exceptions
     * @return a Number value
     */
    static Number getNumber(int type, String uid, List<ParsingNestedException> exceptions, String key, boolean optional,
            JSONObject json, Logger log) {
        if (json.has(key)) {
            Object res = json.opt(key);
            if (!JSONObject.NULL.equals(res) && res instanceof Number) {
                return (Number) res;
            } else
                catchParsingException(type, uid, exceptions,
                        new IllegalArgumentException("\"" + key + "\" must be Number in : " + json), log);
        }
        if (!optional) {
            catchParsingException(type, uid, exceptions,
                    new IllegalArgumentException("\"" + key + "\" must be present in : " + json), log);
        }
        return null;
    }

    /**
     * Gets a Number value associated with the key if it is present.
     *
     * @param key is a json property whose value has to be gotten.
     * @param index in the jsonArray
     * @param jsonArray a JSONArray for parsing
     * @return a Number value
     * @throws IllegalArgumentException
     */
    static Number getNumber(int type, String uid, List<ParsingNestedException> exceptions, String key, int index,
            JSONArray jsonArray, Logger log) {
        Object element = jsonArray.opt(index);
        if (element == null || !(element instanceof Number)) {
            catchParsingException(type, uid, exceptions, new IllegalArgumentException(
                    "Elements of \"" + key + "\" must be present and must be Number in : " + element), log);
            return null;
        }
        return (Number) element;
    }

    /**
     * Gets a String value associated with the key if it is present.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param uid is the unique identifier of the automation object - module type, rule or rule template.
     * @param exceptions is a list used for collecting the exceptions occurred during parsing json.
     * @param key is a json property whose value has to be gotten.
     * @param optional declares whether the property is optional or required
     * @param json a JSONObject for parsing
     * @param log is used for logging the exceptions
     * @return a String value
     */
    static String getString(int type, String uid, List<ParsingNestedException> exceptions, String key, boolean optional,
            JSONObject json, Logger log) {
        if (json.has(key)) {
            Object res = json.opt(key);
            if (!JSONObject.NULL.equals(res) && res instanceof String) {
                return (String) res;
            } else
                catchParsingException(type, uid, exceptions,
                        new IllegalArgumentException("\"" + key + "\" must be String in : " + json), log);
        }
        if (!optional) {
            catchParsingException(type, uid, exceptions,
                    new IllegalArgumentException("\"" + key + "\" must be present in : " + json), log);
        }
        return null;
    }

    /**
     * Gets a String value associated with the key if it is present.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param uid is the unique identifier of the automation object - module type, rule or rule template.
     * @param exceptions is a list used for collecting the exceptions occurred during parsing json.
     * @param key is a json property whose value has to be gotten.
     * @param index in the jsonArray
     * @param jsonArray a JSONArray for parsing
     * @param log is used for logging the exceptions
     * @return a String value
     */
    static String getString(int type, String uid, List<ParsingNestedException> exceptions, String key, int index,
            JSONArray jsonArray, Logger log) {
        Object element = jsonArray.opt(index);
        if (element == null || !(element instanceof String)) {
            catchParsingException(type, uid, exceptions, new IllegalArgumentException(
                    "Elements of \"" + key + "\" must be present and must be String in : " + element), log);
            return null;
        }
        return (String) element;
    }

    /**
     * Gets a JSONObject value associated with the key if it is present.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param uid is the unique identifier of the automation object - module type, rule or rule template.
     * @param exceptions
     * @param key is a json property whose value has to be gotten.
     * @param optional declares whether the property is optional or required
     * @param json a JSONObject for parsing
     * @param log is used for logging the exceptions
     * @return a JSONObject value
     */
    static JSONObject getJSONObject(int type, String uid, List<ParsingNestedException> exceptions, String key,
            boolean optional, JSONObject json, Logger log) {
        if (json.has(key)) {
            Object res = json.opt(key);
            if (!JSONObject.NULL.equals(res) && res instanceof JSONObject) {
                return (JSONObject) res;
            } else
                catchParsingException(type, uid, exceptions,
                        new IllegalArgumentException("\"" + key + "\" must be JSONObject in : " + json), log);
        }
        if (!optional) {
            catchParsingException(type, uid, exceptions,
                    new IllegalArgumentException("\"" + key + "\" must be present in : " + json), log);
        }
        return null;
    }

    /**
     * Gets a JSONObject value associated with the key if it is present.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param uid is the unique identifier of the automation object - module type, rule or rule template.
     * @param exceptions is a list used for collecting the exceptions occurred during parsing json.
     * @param key is a json property whose value has to be gotten.
     * @param index in the jsonArray
     * @param jsonArray a JSONArray for parsing
     * @param log is used for logging the exceptions
     * @return a JSONObject value
     */
    static JSONObject getJSONObject(int type, String uid, List<ParsingNestedException> exceptions, String key,
            int index, JSONArray jsonArray, Logger log) {
        Object element = jsonArray.opt(index);
        if (element == null || !(element instanceof JSONObject)) {
            catchParsingException(type, uid, exceptions,
                    new IllegalArgumentException(
                            "Elements of \"" + key + "\" must be present and must be JSONObjects in : " + element),
                    log);
        }
        return (JSONObject) element;
    }

    /**
     * Gets a JSONArray value associated with the key if it is present.
     *
     * @param type specifies the type of the automation object - module type, rule or rule template.
     * @param uid is the unique identifier of the automation object - module type, rule or rule template.
     * @param exceptions is a list used for collecting the exceptions occurred during parsing json.
     * @param key is a json property whose value has to be gotten.
     * @param optional declares whether the property is optional or required
     * @param json a JSONObject for parsing
     * @param log is used for logging the exceptions
     * @return a JSONArray value
     */
    static JSONArray getJSONArray(int type, String uid, List<ParsingNestedException> exceptions, String key,
            boolean optional, JSONObject json, Logger log) {
        if (json.has(key)) {
            Object res = json.opt(key);
            if (!JSONObject.NULL.equals(res) && res instanceof JSONArray) {
                return (JSONArray) res;
            } else
                catchParsingException(type, uid, exceptions,
                        new IllegalArgumentException("\"" + key + "\" must be JSONArray in : " + json), log);
        }
        if (!optional) {
            catchParsingException(type, uid, exceptions,
                    new IllegalArgumentException("\"" + key + "\" must be present in : " + json), log);
        }
        return null;
    }

    /**
     * Utility method for catching the Exceptions, transforming them to ParsingNestedExceptions and accumulating them in
     * list.
     *
     * @param type is the type of the automation object for parsing - module type, template or rule.
     * @param id is the UID of the automation object for parsing - module type, template or rule.
     * @param exceptions is a list used for collecting the exceptions occurred during parsing json.
     * @param t is the exception occurred during parsing json.
     * @param log is used for logging the exception
     */
    static void catchParsingException(int type, String id, List<ParsingNestedException> exceptions, Throwable t,
            Logger log) {
        ParsingNestedException pne = new ParsingNestedException(type, id, t);
        log.error("[JSON Prser]", pne);
        exceptions.add(pne);
    }

}
