/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.json;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONUtilityTest {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Test
    public void verifyType()
            throws JSONException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {
        Assert.assertNotNull(
                JSONUtility.verifyType(Type.BOOLEAN, "false", new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNotNull(
                JSONUtility.verifyType(Type.BOOLEAN, "true", new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNotNull(
                JSONUtility.verifyType(Type.BOOLEAN, Boolean.FALSE, new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNotNull(
                JSONUtility.verifyType(Type.BOOLEAN, Boolean.TRUE, new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNotNull(
                JSONUtility.verifyType(Type.DECIMAL, (float) 2.5, new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNotNull(JSONUtility.verifyType(Type.DECIMAL, 2.5, new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNotNull(
                JSONUtility.verifyType(Type.DECIMAL, "2.5", new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNotNull(JSONUtility.verifyType(Type.INTEGER, 5, new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNotNull(
                JSONUtility.verifyType(Type.INTEGER, (byte) 5, new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNotNull(
                JSONUtility.verifyType(Type.INTEGER, (long) 5, new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNotNull(JSONUtility.verifyType(Type.INTEGER, "5", new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNotNull(JSONUtility.verifyType(Type.TEXT, "abc", new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNotNull(JSONUtility.verifyType(Type.TEXT, "true", new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNotNull(JSONUtility.verifyType(Type.TEXT, "5", new Status(this.log, Status.MODULE_TYPE, null)));

        Assert.assertNull(JSONUtility.verifyType(Type.DECIMAL, false, new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNull(JSONUtility.verifyType(Type.DECIMAL, "abc", new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNull(
                JSONUtility.verifyType(Type.INTEGER, (float) 2.5, new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNull(JSONUtility.verifyType(Type.INTEGER, false, new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNull(JSONUtility.verifyType(Type.INTEGER, "abc", new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNull(JSONUtility.verifyType(Type.INTEGER, "2.5", new Status(this.log, Status.MODULE_TYPE, null)));

        Assert.assertNull(JSONUtility.verifyType(Type.TEXT, true, new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNull(
                JSONUtility.verifyType(Type.TEXT, (float) 2.5, new Status(this.log, Status.MODULE_TYPE, null)));
        Assert.assertNull(JSONUtility.verifyType(Type.TEXT, 5, new Status(this.log, Status.MODULE_TYPE, null)));
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
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("{'key':false}");
            JSONObject json = (JSONObject) tokener.nextValue();
            Boolean obj = JSONUtility.getBoolean("key", false, false, json, status);
            Assert.assertEquals("Status has errors: " + status.getErrors(), false, status.hasErrors());
            Assert.assertNotNull(obj);
        }
        {
            // test 'key' with invalid type in json object
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("{'key':1}");
            JSONObject json = (JSONObject) tokener.nextValue();
            Boolean obj = JSONUtility.getBoolean("key", false, false, json, status);
            Assert.assertNull(obj);
            Assert.assertEquals("Status should have errors: " + status.getErrors(), true, status.hasErrors());
        }
        {
            // test 'unknown' in json object
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("{'key':true}");
            JSONObject json = (JSONObject) tokener.nextValue();
            Boolean obj = JSONUtility.getBoolean("unknown", false, false, json, status);
            Assert.assertEquals(Boolean.FALSE, obj);
            Assert.assertEquals("Status should have errors: " + status.getErrors(), true, status.hasErrors());
        }
    }

    @Test
    public void getBoolean2() throws JSONException {
        {
            // test index 0 in json array [true, true, true]
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("[true, true, true]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            Boolean obj = JSONUtility.getBoolean("key", 0, jsonArray, status);
            Assert.assertEquals("Status has errors: " + status.getErrors(), false, status.hasErrors());
            Assert.assertNotNull(obj);
        }

        {
            // test index 5 in json array [true, true, true]
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("[1,2,3]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            Boolean obj = JSONUtility.getBoolean("key", 5, jsonArray, status);
            Assert.assertEquals("Status should have errors: " + status.getErrors(), true, status.hasErrors());
            Assert.assertNull(obj);
        }

        {
            // test index 0 in json array [1,2,3]
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("[1,2,3]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            Boolean obj = JSONUtility.getBoolean("key", 0, jsonArray, status);
            Assert.assertEquals("Status should have errors: " + status.getErrors(), true, status.hasErrors());
            Assert.assertNull(obj);
        }
    }

    @Test
    public void getNumber1() throws JSONException {
        {
            // test 'key' in json object
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("{'key':1}");
            JSONObject json = (JSONObject) tokener.nextValue();
            Number obj = JSONUtility.getNumber("key", true, json, status);
            Assert.assertEquals("Status has errors: " + status.getErrors(), false, status.hasErrors());
            Assert.assertNotNull(obj);
        }
        {
            // test 'key' with invalid type in json object
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("{'key':false}");
            JSONObject json = (JSONObject) tokener.nextValue();
            Number obj = JSONUtility.getNumber("unknown", false, json, status);
            Assert.assertNull(obj);
            Assert.assertEquals("Status should have errors: " + status.getErrors(), true, status.hasErrors());
        }
        {
            // test 'unknown' in json object
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("{'key':2}");
            JSONObject json = (JSONObject) tokener.nextValue();
            Number obj = JSONUtility.getNumber("unknown", false, json, status);
            Assert.assertNull(obj);
            Assert.assertEquals("Status should have errors: " + status.getErrors(), true, status.hasErrors());
        }
    }

    @Test
    public void getNumber2() throws JSONException {
        {
            // test index 0 in json array [1,2,3]
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("[1,2,3]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            Number obj = JSONUtility.getNumber("key", 0, jsonArray, status);
            Assert.assertEquals("Status has errors: " + status.getErrors(), false, status.hasErrors());
            Assert.assertNotNull(obj);
        }

        {
            // test index 5 in json array [1,2,3]
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("[1,2,3]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            Number obj = JSONUtility.getNumber("key", 5, jsonArray, status);
            Assert.assertEquals("Status should have errors: " + status.getErrors(), true, status.hasErrors());
            Assert.assertNull(obj);
        }

        {
            // test index 0 in json array [false,'string',true]
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("[false,'string',true]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            Number obj = JSONUtility.getNumber("key", 0, jsonArray, status);
            Assert.assertEquals("Status should have errors: " + status.getErrors(), true, status.hasErrors());
            Assert.assertNull(obj);
        }
    }

    @Test
    public void getString1() throws JSONException {
        {
            // test 'key' in json object
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("{'key':'a'}");
            JSONObject json = (JSONObject) tokener.nextValue();
            String obj = JSONUtility.getString("key", true, json, status);
            Assert.assertEquals("Status has errors: " + status.getErrors(), false, status.hasErrors());
            Assert.assertNotNull(obj);
        }
        {
            // test 'key' with invalid type in json object
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("{'key':false}");
            JSONObject json = (JSONObject) tokener.nextValue();
            String obj = JSONUtility.getString("unknown", false, json, status);
            Assert.assertNull(obj);
            Assert.assertEquals("Status should have errors: " + status.getErrors(), true, status.hasErrors());
        }
        {
            // test 'unknown' in json object
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("{'key':'a'}");
            JSONObject json = (JSONObject) tokener.nextValue();
            String obj = JSONUtility.getString("unknown", false, json, status);
            Assert.assertNull(obj);
            Assert.assertEquals("Status should have errors: " + status.getErrors(), true, status.hasErrors());
        }
    }

    @Test
    public void getString2() throws JSONException {
        {
            // test index 0 in json array ['a','b','c']
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("['a','b','c']");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            String obj = JSONUtility.getString("key", 0, jsonArray, status);
            Assert.assertEquals("Status has errors: " + status.getErrors(), false, status.hasErrors());
            Assert.assertNotNull(obj);
        }

        {
            // test index 5 in json array ['a','b','c']
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("['a','b','c']");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            String obj = JSONUtility.getString("key", 5, jsonArray, status);
            Assert.assertEquals("Status should have errors: " + status.getErrors(), true, status.hasErrors());
            Assert.assertNull(obj);
        }

        {
            // test index 0 in json array [1,2,3]
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("[1,2,3]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            String obj = JSONUtility.getString("key", 0, jsonArray, status);
            Assert.assertEquals("Status should have errors: " + status.getErrors(), true, status.hasErrors());
            Assert.assertNull(obj);
        }
    }

    @Test
    public void getJSONObject1() throws JSONException {
        {
            // test 'key' in json object
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("{'key':{'a':'b'}}");
            JSONObject json = (JSONObject) tokener.nextValue();
            JSONObject obj = JSONUtility.getJSONObject("key", true, json, status);
            Assert.assertEquals("Status has errors: " + status.getErrors(), false, status.hasErrors());
            Assert.assertNotNull(obj);
        }
        {
            // test 'unknown' in json object
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("{'key':{'a':'b'}}");
            JSONObject json = (JSONObject) tokener.nextValue();
            JSONObject obj = JSONUtility.getJSONObject("unknown", false, json, status);
            Assert.assertNull(obj);
            Assert.assertEquals("Status should have errors: " + status.getErrors(), true, status.hasErrors());
        }
    }

    @Test
    public void getJSONObject2() throws JSONException {
        {
            // test index 0 in json array [1,2,3]
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("[{},{},{}]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            JSONObject obj = JSONUtility.getJSONObject("key", 0, jsonArray, status);
            Assert.assertEquals("Status has errors: " + status.getErrors(), false, status.hasErrors());
            Assert.assertNotNull(obj);
        }

        {
            // test index 5 in json array [1,2,3]
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("[1,2,3]");
            JSONArray jsonArray = (JSONArray) tokener.nextValue();
            JSONObject obj = JSONUtility.getJSONObject("key", 5, jsonArray, status);
            Assert.assertEquals("Status should have errors: " + status.getErrors(), true, status.hasErrors());
            Assert.assertNull(obj);
        }
    }

    @Test
    public void getJSONArray() throws JSONException {
        {
            // test json array [1,2,3]
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("{'key':[1,2,3]}");
            JSONObject json = (JSONObject) tokener.nextValue();
            JSONArray arr = JSONUtility.getJSONArray("key", false, json, status);
            Assert.assertEquals("Status has errors: " + status.getErrors(), false, status.hasErrors());
            Assert.assertNotNull(arr);
        }

        {
            // test invalid json array
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("{'key':''}");
            JSONObject json = (JSONObject) tokener.nextValue();
            JSONArray arr = JSONUtility.getJSONArray("key", false, json, status);
            Assert.assertEquals("Status should have errors: " + status.getErrors(), true, status.hasErrors());
            Assert.assertNull(arr);
        }

        {
            // test invalid json key
            Status status = new Status(this.log, Status.MODULE_TYPE, null);
            JSONTokener tokener = new JSONTokener("{'key':[1,2,3]}");
            JSONObject json = (JSONObject) tokener.nextValue();
            JSONArray arr = JSONUtility.getJSONArray("unknown", false, json, status);
            Assert.assertEquals("Status should have errors: " + status.getErrors(), true, status.hasErrors());
            Assert.assertNull(arr);
        }
    }

}
