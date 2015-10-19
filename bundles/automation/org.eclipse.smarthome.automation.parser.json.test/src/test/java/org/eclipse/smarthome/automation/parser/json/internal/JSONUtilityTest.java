/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.json.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.eclipse.smarthome.automation.parser.ParsingException;
import org.eclipse.smarthome.automation.parser.ParsingNestedException;
import org.eclipse.smarthome.automation.parser.json.internal.Introspector;
import org.eclipse.smarthome.automation.parser.json.internal.JSONUtility;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Junit Test for JSONUtility class
 *
 * @author Marin Mitev - initial version
 */
public class JSONUtilityTest {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Test
    public void verifyType()
            throws JSONException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {
        Assert.assertEquals("false", JSONUtility.verifyType(Type.BOOLEAN, "false"));
        Assert.assertEquals("true", JSONUtility.verifyType(Type.BOOLEAN, "true"));
        Assert.assertEquals("false", JSONUtility.verifyType(Type.BOOLEAN, Boolean.FALSE));
        Assert.assertEquals("true", JSONUtility.verifyType(Type.BOOLEAN, Boolean.TRUE));
        Assert.assertEquals("2.5", JSONUtility.verifyType(Type.DECIMAL, (float) 2.5));
        Assert.assertEquals("2.5", JSONUtility.verifyType(Type.DECIMAL, 2.5));
        Assert.assertEquals("2.5", JSONUtility.verifyType(Type.DECIMAL, "2.5"));
        Assert.assertEquals("5", JSONUtility.verifyType(Type.INTEGER, 5));
        Assert.assertEquals("5", JSONUtility.verifyType(Type.INTEGER, (byte) 5));
        Assert.assertEquals("5", JSONUtility.verifyType(Type.INTEGER, (long) 5));
        Assert.assertEquals("5", JSONUtility.verifyType(Type.INTEGER, "5"));
        Assert.assertEquals("abc", JSONUtility.verifyType(Type.TEXT, "abc"));
        Assert.assertEquals("true", JSONUtility.verifyType(Type.TEXT, "true"));
        Assert.assertEquals("5", JSONUtility.verifyType(Type.TEXT, "5"));

        // Assert.assertNull(JSONUtility.verifyType(Type.DECIMAL, false));
        // Assert.assertNull(JSONUtility.verifyType(Type.DECIMAL, "abc"));
        // Assert.assertNull(JSONUtility.verifyType(Type.INTEGER, (float) 2.5));
        // Assert.assertNull(JSONUtility.verifyType(Type.INTEGER, false));
        // Assert.assertNull(JSONUtility.verifyType(Type.INTEGER, "abc"));
        // Assert.assertNull(JSONUtility.verifyType(Type.INTEGER, "2.5"));
        //
        // Assert.assertNull(JSONUtility.verifyType(Type.TEXT, true));
        // Assert.assertNull(JSONUtility.verifyType(Type.TEXT, (float) 2.5));
        // Assert.assertNull(JSONUtility.verifyType(Type.TEXT, 5));
    }

    Object convertValue(String type, Object value)
            throws ClassNotFoundException, JSONException, IllegalAccessException, InvocationTargetException {
        if (value instanceof JSONObject || value instanceof JSONArray) {
            Class<?> clazz = this.getClass().getClassLoader().loadClass(type);
            Introspector i = new Introspector();
            return i.deserialize(value, clazz);
        }
        return value;
    }

    @Test
    public void convertValue()
            throws JSONException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {
        Assert.assertNotNull(convertValue(Boolean.class.getName(), new JSONTokener("false").nextValue()));
        Assert.assertNotNull(convertValue(Boolean.class.getName(), new JSONTokener("true").nextValue()));
        Assert.assertNotNull(convertValue(Float.class.getName(), new JSONTokener("2.5").nextValue()));
        Assert.assertNotNull(convertValue(Integer.class.getName(), new JSONTokener("5").nextValue()));
        Assert.assertNotNull(convertValue(String.class.getName(), new JSONTokener("'abc'").nextValue()));
        Assert.assertNotNull(convertValue(Object.class.getName(), new JSONTokener("{'aa':'bb','c':'d'}").nextValue()));
        Assert.assertNotNull(convertValue(Dictionary.class.getName(), new JSONTokener("{'a':'b','x':5}").nextValue()));
        Assert.assertNotNull(convertValue(Dictionary.class.getName(),
                new JSONTokener("{'a':'b','x':5,'json.elementType':'java.lang.String'}").nextValue()));
        Assert.assertNotNull(convertValue(Properties.class.getName(),
                new JSONTokener("{'a':'b','x':5,'json.elementType':'java.lang.String'}").nextValue()));
        Assert.assertNotNull(convertValue(HashMap.class.getName(),
                new JSONTokener("{'a':'b','x':5,'json.elementType':'java.lang.String'}").nextValue()));
        Assert.assertNotNull(convertValue(ArrayList.class.getName(), new JSONTokener("[false,true]").nextValue()));

        Assert.assertNotNull(convertValue(org.eclipse.smarthome.automation.Rule.class.getName(),
                new JSONTokener("{'name':'name1','description':'description1'}").nextValue()));
    }

    @Test
    public void getBoolean1() throws JSONException {
        {
            // test 'key' in json object
            JSONTokener tokener = new JSONTokener("{'key':false}");
            JSONObject json = (JSONObject) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            Boolean obj = JSONUtility.getBoolean(0, "getBoolean1", exceptions, "key", false, false, json, log);
            Assert.assertEquals("The method \"getBoolean\" returns wrong value.", false, obj);
            Assert.assertTrue("Has errors: " + new ParsingException(exceptions).getMessage(), exceptions.isEmpty());
        }
        {
            // test 'key' with invalid type in json object
            JSONTokener tokener = new JSONTokener("{'key':1}");
            JSONObject json = (JSONObject) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            Boolean obj = JSONUtility.getBoolean(0, "getBoolean1", exceptions, "key", false, false, json, log);
            Assert.assertEquals("The method \"getBoolean\" returns wrong value.", false, obj);
            Assert.assertFalse("The method does not register that the property \"key\" has wrong value.",
                    exceptions.isEmpty());
        }
        {
            // test 'unknown' in json object
            JSONTokener tokener = new JSONTokener("{'key':true}");
            JSONObject json = (JSONObject) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            Boolean obj = JSONUtility.getBoolean(0, "getBoolean1", exceptions, "unknown", false, false, json, log);
            Assert.assertEquals("The method \"getBoolean\" returns wrong value.", Boolean.FALSE, obj);
            Assert.assertFalse("The method does not register that the required property \"unknown\" is missing.",
                    exceptions.isEmpty());
        }
    }

    @Test
    public void getBoolean2() throws JSONException {
        {
            // test index 0 in json array [true, true, true]
            JSONTokener tokener = new JSONTokener("[true, true, true]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            Boolean obj = JSONUtility.getBoolean(0, "getBoolean2", exceptions, "key", 0, jsonArray, log);
            Assert.assertEquals("The method \"getBoolean\" returns wrong value.", true, obj);
            Assert.assertTrue("Has errors: " + new ParsingException(exceptions).getMessage(), exceptions.isEmpty());
        }

        {
            // test index 5 in json array [true, true, true]
            JSONTokener tokener = new JSONTokener("[1,2,3]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            Boolean obj = JSONUtility.getBoolean(0, "getBoolean2", exceptions, "key", 5, jsonArray, log);
            Assert.assertFalse("The method does not register that the json array does not contain 5 elements.",
                    exceptions.isEmpty());
            Assert.assertNull("The method \"getBoolean\" returns wrong value.", obj);
        }

        {
            // test index 0 in json array [1,2,3]
            JSONTokener tokener = new JSONTokener("[1,2,3]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            Boolean obj = JSONUtility.getBoolean(0, "getBoolean2", exceptions, "key", 0, jsonArray, log);
            Assert.assertFalse(
                    "The method does not register that the json array contains elements that have wrong value.",
                    exceptions.isEmpty());
            Assert.assertNull("The method \"getBoolean\" returns wrong value.", obj);
        }
    }

    @Test
    public void getNumber1() throws JSONException {
        {
            // test 'key' in json object
            JSONTokener tokener = new JSONTokener("{'key':1}");
            JSONObject json = (JSONObject) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            Number obj = JSONUtility.getNumber(0, "getNumber1", exceptions, "key", true, json, log);
            Assert.assertTrue("Has errors: " + new ParsingException(exceptions).getMessage(), exceptions.isEmpty());
            Assert.assertEquals("The method \"getNumber\" returns wrong value.", 1, obj);
        }
        {
            // test 'key' with invalid type in json object
            JSONTokener tokener = new JSONTokener("{'key':false}");
            JSONObject json = (JSONObject) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            Number obj = JSONUtility.getNumber(0, "getNumber1", exceptions, "key", false, json, log);
            Assert.assertNull("The method \"getNumber\" returns wrong value.", obj);
            Assert.assertFalse("The method does not register that the value of property \"key\" is wrong.",
                    exceptions.isEmpty());
        }
        {
            // test 'unknown' in json object
            JSONTokener tokener = new JSONTokener("{'key':2}");
            JSONObject json = (JSONObject) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            Number obj = JSONUtility.getNumber(0, "getNumber1", exceptions, "unknown", false, json, log);
            Assert.assertNull("The method \"getNumber\" returns wrong value.", obj);
            Assert.assertFalse("The method does not register that the required property \"unknown\" is missing.",
                    exceptions.isEmpty());
        }
    }

    @Test
    public void getNumber2() throws JSONException {
        {
            // test index 0 in json array [1,2,3]
            JSONTokener tokener = new JSONTokener("[1,2,3]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            Number obj = JSONUtility.getNumber(0, "getNumber2", exceptions, "key", 0, jsonArray, log);
            Assert.assertTrue("Has errors: " + new ParsingException(exceptions).getMessage(), exceptions.isEmpty());
            Assert.assertNotNull("The method \"getNumber\" returns wrong value.", obj);
        }

        {
            // test index 5 in json array [1,2,3]
            JSONTokener tokener = new JSONTokener("[1,2,3]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            Number obj = JSONUtility.getNumber(0, "getNumber2", exceptions, "key", 5, jsonArray, log);
            Assert.assertFalse("The method does not register that the json array does not contain 5 elements.",
                    exceptions.isEmpty());
            Assert.assertNull("The method \"getNumber\" returns wrong value.", obj);
        }

        {
            // test index 0 in json array [false,'string',true]
            JSONTokener tokener = new JSONTokener("[false,'string',true]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            Number obj = JSONUtility.getNumber(0, "getNumber2", exceptions, "key", 0, jsonArray, log);
            Assert.assertFalse("The method does not register that the json array contains elements with wrong values.",
                    exceptions.isEmpty());
            Assert.assertNull("The method \"getNumber\" returns wrong value.", obj);
        }
    }

    @Test
    public void getString1() throws JSONException {
        {
            // test 'key' in json object
            JSONTokener tokener = new JSONTokener("{'key':'a'}");
            JSONObject json = (JSONObject) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            String obj = JSONUtility.getString(0, "getString1", exceptions, "key", true, json, log);
            Assert.assertTrue("Has errors: " + new ParsingException(exceptions).getMessage(), exceptions.isEmpty());
            Assert.assertNotNull("The method \"getString\" returns wrong value.", obj);
        }
        {
            // test 'key' with invalid type in json object
            JSONTokener tokener = new JSONTokener("{'key':false}");
            JSONObject json = (JSONObject) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            String obj = JSONUtility.getString(0, "getString1", exceptions, "key", false, json, log);
            Assert.assertNull("The method \"getString\" returns wrong value.", obj);
            Assert.assertFalse("The method does not register that the property \"key\" has wrong value.",
                    exceptions.isEmpty());
        }
        {
            // test 'unknown' in json object
            JSONTokener tokener = new JSONTokener("{'key':'a'}");
            JSONObject json = (JSONObject) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            String obj = JSONUtility.getString(0, "getString1", exceptions, "unknown", false, json, log);
            Assert.assertNull("The method \"getString\" returns wrong value.", obj);
            Assert.assertFalse("The method does not register that the required property \"unknown\" is missing.",
                    exceptions.isEmpty());
        }
    }

    @Test
    public void getString2() throws JSONException {
        {
            // test index 0 in json array ['a','b','c']
            JSONTokener tokener = new JSONTokener("['a','b','c']");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            String obj = JSONUtility.getString(0, "getString2", exceptions, "key", 0, jsonArray, log);
            Assert.assertTrue("Has errors: " + new ParsingException(exceptions).getMessage(), exceptions.isEmpty());
            Assert.assertNotNull("The method \"getString\" returns wrong value.", obj);
        }

        {
            // test index 5 in json array ['a','b','c']
            JSONTokener tokener = new JSONTokener("['a','b','c']");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            String obj = JSONUtility.getString(0, "getString2", exceptions, "key", 5, jsonArray, log);
            Assert.assertFalse("The method does not register that the json array does not contain 5 elements.",
                    exceptions.isEmpty());
            Assert.assertNull("The method \"getString\" returns wrong value.", obj);
        }

        {
            // test index 0 in json array [1,2,3]
            JSONTokener tokener = new JSONTokener("[1,2,3]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            String obj = JSONUtility.getString(0, "getString2", exceptions, "key", 0, jsonArray, log);
            Assert.assertFalse("The method does not register that the json array contains elements with wrong values.",
                    exceptions.isEmpty());
            Assert.assertNull("The method \"getString\" returns wrong value.", obj);
        }
    }

    @Test
    public void getJSONObject1() throws JSONException {
        {
            // test 'key' in json object
            JSONTokener tokener = new JSONTokener("{'key':{'a':'b'}}");
            JSONObject json = (JSONObject) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            JSONObject obj = JSONUtility.getJSONObject(0, "getJSONObject1", exceptions, "key", true, json, log);
            Assert.assertTrue("Has errors: " + new ParsingException(exceptions).getMessage(), exceptions.isEmpty());
            Assert.assertNotNull("The method \"getJSONObject\" returns wrong value.", obj);
        }
        {
            // test 'unknown' in json object
            JSONTokener tokener = new JSONTokener("{'key':{'a':'b'}}");
            JSONObject json = (JSONObject) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            JSONObject obj = JSONUtility.getJSONObject(0, "getJSONObject1", exceptions, "unknown", false, json, log);
            Assert.assertNull("The method \"getJSONObject\" returns wrong value.", obj);
            Assert.assertFalse("The method does not register that the required property \"unknown\" is missing.",
                    exceptions.isEmpty());
        }
    }

    @Test
    public void getJSONObject2() throws JSONException {
        {
            // test index 0 in json array [1,2,3]
            JSONTokener tokener = new JSONTokener("[{},{},{}]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            JSONObject obj = JSONUtility.getJSONObject(0, "getJSONObject2", exceptions, "key", 0, jsonArray, log);
            Assert.assertTrue("Has errors: " + new ParsingException(exceptions).getMessage(), exceptions.isEmpty());
            Assert.assertNotNull("The method \"getJSONObject\" returns wrong value.", obj);
        }

        {
            // test index 5 in json array [1,2,3]
            JSONTokener tokener = new JSONTokener("[1,2,3]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            JSONObject obj = JSONUtility.getJSONObject(0, "getJSONObject2", exceptions, "key", 5, jsonArray, log);
            Assert.assertFalse("The method does not register that the json array does not contain 5 elements.",
                    exceptions.isEmpty());
            Assert.assertNull("The method \"getJSONObject\" returns wrong value.", obj);
        }
    }

    @Test
    public void getJSONArray() throws JSONException {
        {
            // test json array [1,2,3]
            JSONTokener tokener = new JSONTokener("{'key':[1,2,3]}");
            JSONObject json = (JSONObject) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            JSONArray arr = JSONUtility.getJSONArray(0, "getJSONArray", exceptions, "key", false, json, log);
            Assert.assertTrue("Has errors: " + new ParsingException(exceptions).getMessage(), exceptions.isEmpty());
            Assert.assertNotNull("The method \"getJSONArray\" returns wrong value.", arr);
        }

        {
            // test invalid json array
            JSONTokener tokener = new JSONTokener("{'key':''}");
            JSONObject json = (JSONObject) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            JSONArray arr = JSONUtility.getJSONArray(0, "getJSONArray", exceptions, "key", false, json, log);
            Assert.assertFalse("The method does not register that the property \"key\" has wrong value.",
                    exceptions.isEmpty());
            Assert.assertNull("The method \"getJSONArray\" returns wrong value.", arr);
        }

        {
            // test invalid json key
            JSONTokener tokener = new JSONTokener("{'key':[1,2,3]}");
            JSONObject json = (JSONObject) tokener.nextValue();
            List<ParsingNestedException> exceptions = new ArrayList<>();
            JSONArray arr = JSONUtility.getJSONArray(0, "getJSONArray", exceptions, "unknown", false, json, log);
            Assert.assertFalse("The method does not register that the required property \"unknown\" is missing.",
                    exceptions.isEmpty());
            Assert.assertNull("The method \"getJSONArray\" returns wrong value.", arr);
        }
    }

}
