/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.json;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserTest {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Test
    public void importModuleTypes() throws JSONException, FileNotFoundException {
        File file = new File("src/test/resources/moduletypes.json");
        Assert.assertTrue("Not existing file: " + file.getAbsolutePath(), file.exists());
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        ModuleTypeJSONParser parser = new ModuleTypeJSONParser(null);
        Set<Status> providedObjects = parser.importData(inputStreamReader);
        Assert.assertNotNull(providedObjects);
        Assert.assertFalse(providedObjects.isEmpty());
        Iterator<Status> i = providedObjects.iterator();
        HashMap<String, ModuleType> map = new HashMap<>();
        while (i.hasNext()) {
            Status status = i.next();
            ModuleType providedObject = (ModuleType) status.getResult();
            Assert.assertFalse("Found errors when parsing: " + status.getErrors(), status.hasErrors());
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
    public void importRuleTemplates() throws JSONException, FileNotFoundException {
        File file = new File("src/test/resources/templates.json");
        Assert.assertTrue("Not existing file: " + file.getAbsolutePath(), file.exists());
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        TemplateJSONParser parser = new TemplateJSONParser();
        Set<Status> providedObjects = parser.importData(inputStreamReader);
        Assert.assertNotNull(providedObjects);
        Assert.assertFalse(providedObjects.isEmpty());
        Iterator<Status> i = providedObjects.iterator();
        HashMap<String, RuleTemplate> map = new HashMap<>();
        while (i.hasNext()) {
            Status status = i.next();
            RuleTemplate providedObject = (RuleTemplate) status.getResult();
            Assert.assertFalse("Found errors when parsing: " + status.getErrors(), status.hasErrors());
            Assert.assertNotNull(providedObject);
            map.put(providedObject.getUID(), providedObject);
        }
        Assert.assertNotNull(map.get("SampleRuleTemplate"));
    }

    @Test
    public void importRulesByModuleTypes() throws JSONException, FileNotFoundException {
        File file = new File("src/test/resources/rules1bymodules.json");
        Assert.assertTrue("Not existing file: " + file.getAbsolutePath(), file.exists());
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        RuleJSONParser parser = new RuleJSONParser();
        Set<Status> providedObjects = parser.importData(inputStreamReader);
        Assert.assertNotNull(providedObjects);
        Assert.assertFalse(providedObjects.isEmpty());
        Iterator<Status> i = providedObjects.iterator();
        HashMap<String, Rule> map = new HashMap<>();
        while (i.hasNext()) {
            Status status = i.next();
            Rule providedObject = (Rule) status.getResult();
            Assert.assertFalse("Found errors when parsing: " + status.getErrors(), status.hasErrors());
            Assert.assertNotNull(providedObject);
            map.put(providedObject.getUID(), providedObject);
        }
        Assert.assertNotNull(map.get("sample.rule1"));
        Assert.assertNotNull(map.get("sample.rule2"));
    }

    @Test
    public void importRulesByTemplates() throws JSONException, FileNotFoundException {
        File file = new File("src/test/resources/rules2bytemplate.json");
        Assert.assertTrue("Not existing file: " + file.getAbsolutePath(), file.exists());
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        RuleJSONParser parser = new RuleJSONParser();
        Set<Status> providedObjects = parser.importData(inputStreamReader);
        Assert.assertNotNull(providedObjects);
        Assert.assertFalse(providedObjects.isEmpty());
        Iterator<Status> i = providedObjects.iterator();
        HashMap<String, Rule> map = new HashMap<>();
        while (i.hasNext()) {
            Status status = i.next();
            Rule providedObject = (Rule) status.getResult();
            Assert.assertFalse("Found errors when parsing: " + status.getErrors(), status.hasErrors());
            Assert.assertNotNull(providedObject);
            map.put(providedObject.getUID(), providedObject);
        }
        Assert.assertNotNull(map.get("sample.rulebytemplate"));
    }

    @Test
    public void exportModuleTypes() throws JSONException, IOException {
        File file = new File("src/test/resources/moduletypes.json");
        Assert.assertTrue("Not existing file: " + file.getAbsolutePath(), file.exists());
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        ModuleTypeJSONParser parser = new ModuleTypeJSONParser(null);
        Set<Status> providedObjects = parser.importData(inputStreamReader);
        Assert.assertNotNull(providedObjects);
        Assert.assertFalse(providedObjects.isEmpty());
        Iterator<Status> i = providedObjects.iterator();
        Set<ModuleType> moduleTypes = new LinkedHashSet<ModuleType>();
        while (i.hasNext()) {
            Status status = i.next();
            ModuleType providedObject = (ModuleType) status.getResult();
            Assert.assertFalse("Found errors when parsing: " + status.getErrors(), status.hasErrors());
            Assert.assertNotNull(providedObject);
            moduleTypes.add(providedObject);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos);
        parser.exportData(moduleTypes, writer);
        writer.flush();
        byte[] ba = baos.toByteArray();
        Assert.assertNotNull(ba);
        System.out.println("ModuleTypes exported to JSON: " + new String(ba));
        Assert.assertTrue(ba.length > 0);
    }

    @Test
    public void exportRuleTemplates() throws JSONException, IOException {
        File file = new File("src/test/resources/templates.json");
        Assert.assertTrue("Not existing file: " + file.getAbsolutePath(), file.exists());
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        TemplateJSONParser parser = new TemplateJSONParser();
        Set<Status> providedObjects = parser.importData(inputStreamReader);
        Assert.assertNotNull(providedObjects);
        Assert.assertFalse(providedObjects.isEmpty());
        Iterator<Status> i = providedObjects.iterator();
        Set<RuleTemplate> ruleTemplates = new LinkedHashSet<RuleTemplate>();
        while (i.hasNext()) {
            Status status = i.next();
            RuleTemplate providedObject = (RuleTemplate) status.getResult();
            Assert.assertFalse("Found errors when parsing: " + status.getErrors(), status.hasErrors());
            Assert.assertNotNull(providedObject);
            ruleTemplates.add(providedObject);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos);
        parser.exportData(ruleTemplates, writer);
        writer.flush();
        byte[] ba = baos.toByteArray();
        Assert.assertNotNull(ba);
        System.out.println("RuleTemplates exported to JSON: " + new String(ba));
        Assert.assertTrue(ba.length > 0);
    }

    @Test
    public void exportRulesByModuleTypes() throws JSONException, IOException {
        File file = new File("src/test/resources/rules1bymodules.json");
        Assert.assertTrue("Not existing file: " + file.getAbsolutePath(), file.exists());
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        RuleJSONParser parser = new RuleJSONParser();
        Set<Status> providedObjects = parser.importData(inputStreamReader);
        Assert.assertNotNull(providedObjects);
        Assert.assertFalse(providedObjects.isEmpty());
        Iterator<Status> i = providedObjects.iterator();
        Set<Rule> rules = new LinkedHashSet<Rule>();
        while (i.hasNext()) {
            Status status = i.next();
            Rule providedObject = (Rule) status.getResult();
            Assert.assertFalse("Found errors when parsing: " + status.getErrors(), status.hasErrors());
            Assert.assertNotNull(providedObject);
            rules.add(providedObject);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos);
        parser.exportData(rules, writer);
        writer.flush();
        byte[] ba = baos.toByteArray();
        Assert.assertNotNull(ba);
        System.out.println("Rules (by module types) exported to JSON: " + new String(ba));
        Assert.assertTrue(ba.length > 0);
    }

    @Test
    public void exportRulesByTemplates() throws JSONException, IOException {
        File file = new File("src/test/resources/rules2bytemplate.json");
        Assert.assertTrue("Not existing file: " + file.getAbsolutePath(), file.exists());
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        RuleJSONParser parser = new RuleJSONParser();
        Set<Status> providedObjects = parser.importData(inputStreamReader);
        Assert.assertNotNull(providedObjects);
        Assert.assertFalse(providedObjects.isEmpty());
        Iterator<Status> i = providedObjects.iterator();
        Set<Rule> rules = new LinkedHashSet<Rule>();
        while (i.hasNext()) {
            Status status = i.next();
            Rule providedObject = (Rule) status.getResult();
            Assert.assertFalse("Found errors when parsing: " + status.getErrors(), status.hasErrors());
            Assert.assertNotNull(providedObject);
            rules.add(providedObject);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos);
        parser.exportData(rules, writer);
        writer.flush();
        byte[] ba = baos.toByteArray();
        Assert.assertNotNull(ba);
        System.out.println("Rules (by templates) exported to JSON: " + new String(ba));
        Assert.assertTrue(ba.length > 0);
    }

}
