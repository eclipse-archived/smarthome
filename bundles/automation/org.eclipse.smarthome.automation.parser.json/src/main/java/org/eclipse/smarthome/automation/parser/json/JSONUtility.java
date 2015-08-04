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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;

/**
 * @author Ana Dimova - Initial Contribution
 *
 */
public class JSONUtility {

    static final int TRIGGERS = 1;
    static final int CONDITIONS = 2;
    static final int ACTIONS = 3;
    static final int COMPOSITE = 4;

    static final int ON = 5;
    static final int IF = 6;
    static final int THEN = 7;
    static final int UID = 8;
    static final int TAGS = 9;
    static final int CONFIG = 10;
    static final int DESCRIPTION = 11;
    static final int VISIBILITY = 12;
    static final int NAME = 13;
    static final int ACTIVE = 14;
    static final int TEMPLATE_UID = 15;

    /**
     *
     * @param propertyName
     * @return
     */
    static int checkModuleTypeProperties(String propertyName) {
        if (propertyName.equals(JSONStructureConstants.TRIGGERS))
            return TRIGGERS;
        if (propertyName.equals(JSONStructureConstants.CONDITIONS))
            return CONDITIONS;
        if (propertyName.equals(JSONStructureConstants.ACTIONS))
            return ACTIONS;
        if (propertyName.equals(JSONStructureConstants.COMPOSITE))
            return COMPOSITE;
        return -1;
    }

    /**
     *
     * @param propertyName
     * @return
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
     * @param propertyName
     * @return
     */
    public static int checkRuleProperties(String propertyName) {
        if (propertyName.equals(JSONStructureConstants.TEMPLATE_UID))
            return TEMPLATE_UID;
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
        if (propertyName.equals(JSONStructureConstants.ACTIVE))
            return ACTIVE;
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
        Class<?> clazz = bc.getBundle().loadClass(type);
        if (clazz.isAssignableFrom(value.getClass())) {
            return;
        }
        throw new IllegalArgumentException(
                "Incompatible types : \"" + type + "\" and \"" + value.getClass().getName() + "\".");
    }

    /**
     * This method is used to verify if one type is compatible to another.
     *
     * @param type
     * @param value
     * @param status
     */
    static String verifyType(Type type, Object value, Status status) {
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
                }
            }
        }
        status.error("Incompatible types : \"" + type + "\" and \"" + value.getClass().getName() + "\".",
                new IllegalArgumentException());
        return null;
    }

    /**
     * This method is used to convert
     *
     * @param bc
     * @param type
     * @param value
     * @return
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

    static Boolean getBoolean(String key, boolean optional, boolean defValue, JSONObject json, Status status) {
        if (json.has(key)) {
            Object res = json.opt(key);
            if (!JSONObject.NULL.equals(res)) {
                if (res instanceof Boolean) {
                    return (Boolean) res;
                }
                status.error("\"" + key + "\" must be Boolean in : " + json, new IllegalArgumentException());
                return null;
            }
        }
        if (!optional) {
            status.error("\"" + key + "\" must be present in : " + json, new IllegalArgumentException());
        }
        return defValue;
    }

    static Boolean getBoolean(String key, int index, JSONArray jsonArray, Status status) {
        Object element = jsonArray.opt(index);
        if (element == null || !(element instanceof Boolean)) {
            status.error("Elements of \"" + key + "\" must be present and must be Boolean in : " + element,
                    new IllegalArgumentException());
            return null;
        }
        return (Boolean) element;
    }

    static Number getNumber(String key, boolean optional, JSONObject json, Status status) {
        if (json.has(key)) {
            Object res = json.opt(key);
            if (!JSONObject.NULL.equals(res)) {
                if (res instanceof Number) {
                    return (Number) res;
                }
                status.error("\"" + key + "\" must be Number in : " + json, new IllegalArgumentException());
                return null;
            }
        }
        if (!optional) {
            status.error("\"" + key + "\" must be present in : " + json, new IllegalArgumentException());
        }
        return null;
    }

    static Number getNumber(String key, int index, JSONArray jsonArray, Status status) {
        Object element = jsonArray.opt(index);
        if (element == null || !(element instanceof Number)) {
            status.error("Elements of \"" + key + "\" must be present and must be Number in : " + element,
                    new IllegalArgumentException());
            return null;
        }
        return (Number) element;
    }

    static String getString(String key, boolean optional, JSONObject json, Status status) {
        if (json.has(key)) {
            Object res = json.opt(key);
            if (!JSONObject.NULL.equals(res)) {
                if (res instanceof String) {
                    return (String) res;
                }
                status.error("\"" + key + "\" must be String in : " + json, new IllegalArgumentException());
                return null;
            }
        }
        if (!optional) {
            status.error("\"" + key + "\" must be present in : " + json, new IllegalArgumentException());
        }
        return null;
    }

    static String getString(String key, int index, JSONArray jsonArray, Status status) {
        Object element = jsonArray.opt(index);
        if (element == null || !(element instanceof String)) {
            status.error("Elements of \"" + key + "\" must be present and must be String in : " + element,
                    new IllegalArgumentException());
            return null;
        }
        return (String) element;
    }

    static JSONObject getJSONObject(String key, boolean optional, JSONObject json, Status status) {
        if (json.has(key)) {
            Object res = json.opt(key);
            if (!JSONObject.NULL.equals(res)) {
                if (res instanceof JSONObject) {
                    return (JSONObject) res;
                }
                status.error("\"" + key + "\" must be JSONObject in : " + json, new IllegalArgumentException());
                return null;
            }
        }
        if (!optional) {
            status.error("\"" + key + "\" must be present in : " + json, new IllegalArgumentException());
        }
        return null;
    }

    static JSONObject getJSONObject(String key, int index, JSONArray jsonArray, Status status) {
        Object element = jsonArray.opt(index);
        if (element == null || !(element instanceof JSONObject)) {
            status.error("Elements of \"" + key + "\" must be present and must be JSONObjects in : " + element,
                    new IllegalArgumentException());
            return null;
        }
        return (JSONObject) element;
    }

    static JSONArray getJSONArray(String key, boolean optional, JSONObject json, Status status) {
        if (json.has(key)) {
            Object res = json.opt(key);
            if (!JSONObject.NULL.equals(res)) {
                if (res instanceof JSONArray) {
                    return (JSONArray) res;
                }
                status.error("\"" + key + "\" must be JSONArray in : " + json, new IllegalArgumentException());
                return null;
            }
        }
        if (!optional) {
            status.error("\"" + key + "\" must be present in : " + json, new IllegalArgumentException());
        }
        return null;
    }

}
