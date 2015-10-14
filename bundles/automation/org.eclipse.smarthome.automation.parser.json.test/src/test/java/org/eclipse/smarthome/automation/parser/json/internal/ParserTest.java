/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.json.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.parser.ParsingException;
import org.eclipse.smarthome.automation.parser.json.internal.ModuleTypeJSONParser;
import org.eclipse.smarthome.automation.parser.json.internal.RuleJSONParser;
import org.eclipse.smarthome.automation.parser.json.internal.TemplateJSONParser;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.Output;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Junit Test for automation parsers which import and export rules, module types and rule templates in JSON format
 *
 * @author Marin Mitev - initial version
 */
public class ParserTest {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Test
    public void importModuleTypes() throws JSONException, FileNotFoundException, ParsingException {
        File file = new File("src/test/resources/moduletypes.json");
        Assert.assertTrue("Not existing file: " + file.getAbsolutePath(), file.exists());
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        ModuleTypeJSONParser parser = new ModuleTypeJSONParser(null);
        Set<ModuleType> providedObjects = parser.parse(inputStreamReader);
        Assert.assertNotNull(providedObjects);
        Assert.assertFalse(providedObjects.isEmpty());
        Iterator<ModuleType> i = providedObjects.iterator();
        HashMap<String, ModuleType> map = new HashMap<>();
        while (i.hasNext()) {
            ModuleType providedObject = i.next();
            Assert.assertNotNull(providedObject);
            map.put(providedObject.getUID(), providedObject);
        }
        Assert.assertNotNull(map.get("SampleTrigger"));
        Assert.assertNotNull(map.get("SampleTrigger:CustomTrigger"));
        Assert.assertNotNull(map.get("SampleCondition"));
        Assert.assertNotNull(map.get("SampleAction"));
        Assert.assertNotNull(map.get("SampleAction:CustomAction"));
    }

    @Test
    public void importRuleTemplates() throws JSONException, FileNotFoundException, ParsingException {
        File file = new File("src/test/resources/templates.json");
        Assert.assertTrue("Not existing file: " + file.getAbsolutePath(), file.exists());
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        TemplateJSONParser parser = new TemplateJSONParser();
        Set<RuleTemplate> providedObjects = parser.parse(inputStreamReader);
        Assert.assertNotNull(providedObjects);
        Assert.assertFalse(providedObjects.isEmpty());
        Iterator<RuleTemplate> i = providedObjects.iterator();
        HashMap<String, RuleTemplate> map = new HashMap<>();
        while (i.hasNext()) {
            RuleTemplate providedObject = i.next();
            Assert.assertNotNull(providedObject);
            map.put(providedObject.getUID(), providedObject);
        }
        Assert.assertNotNull(map.get("SampleRuleTemplate"));
    }

    @Test
    public void importRulesByModuleTypes() throws JSONException, FileNotFoundException, ParsingException {
        File file = new File("src/test/resources/rules1bymodules.json");
        Assert.assertTrue("Not existing file: " + file.getAbsolutePath(), file.exists());
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        RuleJSONParser parser = new RuleJSONParser();
        Set<Rule> providedObjects = parser.parse(inputStreamReader);
        Assert.assertNotNull(providedObjects);
        Assert.assertFalse(providedObjects.isEmpty());
        Iterator<Rule> i = providedObjects.iterator();
        HashMap<String, Rule> map = new HashMap<>();
        while (i.hasNext()) {
            Rule providedObject = i.next();
            Assert.assertNotNull(providedObject);
            map.put(providedObject.getUID(), providedObject);
        }
        Assert.assertNotNull(map.get("sample.rule1"));
        Assert.assertNotNull(map.get("sample.rule2"));
    }

    @Test
    public void importRulesByTemplates() throws JSONException, FileNotFoundException, ParsingException {
        File file = new File("src/test/resources/rules2bytemplate.json");
        Assert.assertTrue("Not existing file: " + file.getAbsolutePath(), file.exists());
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        RuleJSONParser parser = new RuleJSONParser();
        Set<Rule> providedObjects = parser.parse(inputStreamReader);
        Assert.assertNotNull(providedObjects);
        Assert.assertFalse(providedObjects.isEmpty());
        Iterator<Rule> i = providedObjects.iterator();
        HashMap<String, Rule> map = new HashMap<>();
        while (i.hasNext()) {
            Rule providedObject = i.next();
            Assert.assertNotNull(providedObject);
            map.put(providedObject.getUID(), providedObject);
        }
        Assert.assertNotNull(map.get("sample.rulebytemplate"));
    }

    @Test
    public void exportModuleTypes() throws Exception {
        File file = new File("src/test/resources/moduletypes.json");
        Assert.assertTrue("Not existing file: " + file.getAbsolutePath(), file.exists());
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        ModuleTypeJSONParser parser = new ModuleTypeJSONParser(null);
        Set<ModuleType> providedObjects = parser.parse(inputStreamReader);
        Assert.assertNotNull(providedObjects);
        Assert.assertFalse(providedObjects.isEmpty());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos);
        parser.serialize(providedObjects, writer);
        writer.flush();
        byte[] ba = baos.toByteArray();
        Assert.assertNotNull(ba);
        System.out.println("ModuleTypes exported to JSON: " + new String(ba));
        Assert.assertTrue(ba.length > 0);
    }

    @Test
    public void exportRuleTemplates() throws Exception {
        File file = new File("src/test/resources/templates.json");
        Assert.assertTrue("Not existing file: " + file.getAbsolutePath(), file.exists());
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        TemplateJSONParser parser = new TemplateJSONParser();
        Set<RuleTemplate> providedObjects = parser.parse(inputStreamReader);
        Assert.assertNotNull(providedObjects);
        Assert.assertFalse(providedObjects.isEmpty());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos);
        parser.serialize(providedObjects, writer);
        writer.flush();
        byte[] ba = baos.toByteArray();
        Assert.assertNotNull(ba);
        System.out.println("RuleTemplates exported to JSON: " + new String(ba));
        Assert.assertTrue(ba.length > 0);
    }

    @Test
    public void exportRulesByModuleTypes() throws Exception {
        File file = new File("src/test/resources/rules1bymodules.json");
        Assert.assertTrue("Not existing file: " + file.getAbsolutePath(), file.exists());
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        RuleJSONParser parser = new RuleJSONParser();
        Set<Rule> providedObjects = parser.parse(inputStreamReader);
        Assert.assertNotNull(providedObjects);
        Assert.assertFalse(providedObjects.isEmpty());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos);
        parser.serialize(providedObjects, writer);
        writer.flush();
        byte[] ba = baos.toByteArray();
        Assert.assertNotNull(ba);
        System.out.println("Rules (by module types) exported to JSON: " + new String(ba));
        Assert.assertTrue(ba.length > 0);
    }

    @Test
    public void exportRulesByTemplates() throws Exception {
        File file = new File("src/test/resources/rules2bytemplate.json");
        Assert.assertTrue("Not existing file: " + file.getAbsolutePath(), file.exists());
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        RuleJSONParser parser = new RuleJSONParser();
        Set<Rule> providedObjects = parser.parse(inputStreamReader);
        Assert.assertNotNull(providedObjects);
        Assert.assertFalse(providedObjects.isEmpty());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos);
        parser.serialize(providedObjects, writer);
        writer.flush();
        byte[] ba = baos.toByteArray();
        Assert.assertNotNull(ba);
        System.out.println("Rules (by templates) exported to JSON: " + new String(ba));
        Assert.assertTrue(ba.length > 0);
    }

    @Test
    public void importCompositeModuleTypes() throws JSONException, FileNotFoundException, ParsingException {
        File file = new File("src/test/resources/composite.json");
        Assert.assertTrue("Not existing file: " + file.getAbsolutePath(), file.exists());
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        ModuleTypeJSONParser parser = new ModuleTypeJSONParser(null);
        Set<ModuleType> providedObjects = parser.parse(inputStreamReader);
        Assert.assertNotNull(providedObjects);
        Assert.assertFalse(providedObjects.isEmpty());
        Iterator<ModuleType> i = providedObjects.iterator();
        HashMap<String, ModuleType> map = new HashMap<>();
        while (i.hasNext()) {
            ModuleType providedObject = i.next();
            Assert.assertNotNull(providedObject);
            map.put(providedObject.getUID(), providedObject);
        }
        Assert.assertNotNull(map.get("SampleTrigger"));
        ModuleType compositeTrigger = map.get("CompositeSampleTrigger");
        Assert.assertNotNull(compositeTrigger);
        Assert.assertTrue("Not composite trigger type", compositeTrigger instanceof CompositeTriggerType);
        CompositeTriggerType compositeTriggerType = (CompositeTriggerType) compositeTrigger;
        Set<Output> tOutputs = compositeTriggerType.getOutputs();
        Assert.assertNotNull(tOutputs);
        Output tOutput0 = tOutputs.iterator().next();
        Assert.assertNotNull(tOutput0);
        Assert.assertEquals("compositeTriggerOutput", tOutput0.getName());
        Assert.assertEquals("sampleTrigger1.triggerOutput", tOutput0.getReference());

        List<Trigger> tchildren = compositeTriggerType.getModules();
        Assert.assertNotNull(tchildren);
        Assert.assertNotNull(tchildren.get(0));
        Assert.assertEquals("sampleTrigger1", tchildren.get(0).getId());
        Assert.assertEquals("SampleTrigger", tchildren.get(0).getTypeUID());

        Assert.assertNotNull(map.get("SampleCondition"));

        Assert.assertNotNull(map.get("SampleAction"));
        ModuleType compositeAction = map.get("CompositeSampleAction");
        Assert.assertNotNull(compositeAction);
        Assert.assertTrue("Not composite action type", compositeAction instanceof CompositeActionType);
        CompositeActionType compositeActionType = (CompositeActionType) compositeAction;
        List<Action> achildren = compositeActionType.getModules();
        Assert.assertNotNull(achildren);
        Assert.assertNotNull(achildren.get(0));
        Assert.assertEquals("SampleAction1", achildren.get(0).getId());
        Map<String, Object> caconfig = achildren.get(0).getConfiguration();
        Assert.assertNotNull(caconfig);
        Assert.assertNotNull(caconfig.get("message"));
        Assert.assertEquals("$compositeMessage", caconfig.get("message"));
    }

}
