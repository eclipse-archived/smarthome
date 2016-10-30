/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.integration.test;

import static org.junit.Assert.*

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.automation.Action
import org.eclipse.smarthome.automation.Condition
import org.eclipse.smarthome.automation.ManagedRuleProvider
import org.eclipse.smarthome.automation.Rule
import org.eclipse.smarthome.automation.RuleRegistry
import org.eclipse.smarthome.automation.RuleStatus
import org.eclipse.smarthome.automation.RuleStatusInfo
import org.eclipse.smarthome.automation.Trigger
import org.eclipse.smarthome.automation.events.RuleStatusInfoEvent
import org.eclipse.smarthome.automation.type.ActionType
import org.eclipse.smarthome.automation.type.Input
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry
import org.eclipse.smarthome.automation.type.Output
import org.eclipse.smarthome.automation.type.TriggerType
import org.eclipse.smarthome.core.autoupdate.AutoUpdateBindingConfigProvider
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.events.ItemEventFactory
import org.eclipse.smarthome.core.items.events.ItemStateEvent
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.storage.StorageService
import org.eclipse.smarthome.core.types.UnDefType
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.common.collect.Sets

/**
 * This tests the RuleEngine and the import from JSON resources contained in the ESH-INF folder.
 * This test must be run first otherwise imported rules will be cleared.
 *
 * @author Benedikt Niehues - initial contribution
 * @author Marin Mitev - make the test to pass on each run
 *
 */
class AutomationIntegrationJsonTest extends OSGiTest{

    final Logger logger = LoggerFactory.getLogger(AutomationIntegrationJsonTest.class)
    def EventPublisher eventPublisher
    def ItemRegistry itemRegistry
    def RuleRegistry ruleRegistry
    def ManagedRuleProvider managedRuleProvider
    def ModuleTypeRegistry moduleTypeRegistry
    def Event ruleEvent

    public static def VolatileStorageService VOLATILE_STORAGE_SERVICE = new VolatileStorageService()//keep storage with rules imported from json files

    @Before
    void before() {
        logger.info('@Before.begin');

        getService(ItemRegistry)
        def itemProvider = [
            getAll: {
                [
                    new SwitchItem("myMotionItem"),
                    new SwitchItem("myPresenceItem"),
                    new SwitchItem("myLampItem"),
                    new SwitchItem("myMotionItem2"),
                    new SwitchItem("myPresenceItem2"),
                    new SwitchItem("myLampItem2"),
                    new SwitchItem("myMotionItem11"),
                    new SwitchItem("myLampItem11"),
                    new SwitchItem("myMotionItem3"),
                    new SwitchItem("templ_MotionItem"),
                    new SwitchItem("templ_LampItem")
                ]
            },
            addProviderChangeListener: {},
            removeProviderChangeListener: {},
            allItemsChanged: {}] as ItemProvider
        registerService(itemProvider)
        registerVolatileStorageService()

        //        enableItemAutoUpdate()
        def autoupdateConfig = [
            autoUpdate: { String itemName ->
                println "AutoUpdate Item -> " + itemName
                return true }

        ] as AutoUpdateBindingConfigProvider
        registerService(autoupdateConfig)

        def ruleEventHandler = [
            receive: { Event e ->
                logger.info("RuleEvent: " + e.topic + " --> " + e.payload)
                println "RuleEvent: " + e.topic + " --> " + e.payload
                if (e.payload.contains("RUNNING")){

                    ruleEvent=e
                }
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(RuleStatusInfoEvent.TYPE)
            },

            getEventFilter:{ null }

        ] as EventSubscriber
        registerService(ruleEventHandler)

        def StorageService storageService = getService(StorageService)
        managedRuleProvider = getService(ManagedRuleProvider)
        eventPublisher = getService(EventPublisher)
        itemRegistry = getService(ItemRegistry)
        ruleRegistry = getService(RuleRegistry)
        moduleTypeRegistry = getService(ModuleTypeRegistry)
        waitForAssert ({
            assertThat storageService, is(notNullValue())

            assertThat eventPublisher, is(notNullValue()) //sometimes assert fails because EventPublisher service is null
            assertThat itemRegistry, is(notNullValue())
            assertThat ruleRegistry, is(notNullValue())
            assertThat managedRuleProvider, is(notNullValue())
            assertThat moduleTypeRegistry, is(notNullValue())
        }, 9000)
        logger.info('@Before.finish');
    }

    @After
    void after() {
        logger.info('@After');
    }


    protected void registerVolatileStorageService() {
        registerService(VOLATILE_STORAGE_SERVICE);
    }

    @Test
    public void 'assert that module type inputs and outputs from json file are parsed correctly' () {
        logger.info("assert that module type inputs and outputs from json file are parsed correctly");

        //WAIT until module type resources are parsed
        waitForAssert({
            assertThat moduleTypeRegistry.getTriggers().isEmpty(), is(false)
            assertThat moduleTypeRegistry.getActions().isEmpty(), is(false)

            def moduleType1 = moduleTypeRegistry.get("CustomTrigger1") as TriggerType
            def moduleType2 = moduleTypeRegistry.get("CustomTrigger2") as TriggerType
            def moduleType3 = moduleTypeRegistry.get("CustomAction1") as ActionType
            def moduleType4 = moduleTypeRegistry.get("CustomAction2") as ActionType

            assertThat moduleType1.getOutputs(), is(notNullValue())
            def output1 = moduleType1.getOutputs().find{it.name == "customTriggerOutput1"} as Output
            assertThat output1, is(notNullValue())
            assertThat output1.defaultValue, is("true")

            assertThat moduleType2.getOutputs(), is(notNullValue())
            def output2 = moduleType2.getOutputs().find{it.name == "customTriggerOutput2"} as Output
            assertThat output2, is(notNullValue())
            assertThat output2.defaultValue, is("event")

            assertThat moduleType4.getInputs(), is(notNullValue())
            def input = moduleType4.getInputs().find{it.name == "customActionInput"} as Input
            assertThat input, is(notNullValue())
            assertThat input.defaultValue, is("5")

            assertThat moduleType3.getOutputs(), is(notNullValue())
            def output3 = moduleType3.getOutputs().find{it.name == "customActionOutput3"} as Output
            assertThat output3, is(notNullValue())
            assertThat output3.defaultValue, is("{\"command\":\"OFF\"}")

        }, 10000, 200)

    }

    @Test
    public void 'assert that a rule from json file is added automatically' () {
        logger.info("assert that a rule from json file is added automatically");

        //WAIT until Rule modules types are parsed and the rule becomes IDLE
        waitForAssert({
            assertThat ruleRegistry.getAll().isEmpty(), is(false)
            def rule2 = ruleRegistry.getAll().find{it.tags!=null && it.tags.contains("jsonTest") && !it.tags.contains("references")} as Rule
            assertThat rule2, is(notNullValue())
            def ruleStatus2 = ruleRegistry.getStatusInfo(rule2.uid) as RuleStatusInfo
            assertThat ruleStatus2.getStatus(), is(RuleStatus.IDLE)
        }, 10000, 200)
        def rule = ruleRegistry.getAll().find{it.tags!=null && it.tags.contains("jsonTest") && !it.tags.contains("references")} as Rule
        assertThat rule, is(notNullValue())
        assertThat rule.name, is("ItemSampleRule")
        assertTrue rule.tags.any{it == "sample"}
        assertTrue rule.tags.any{it == "item"}
        assertTrue rule.tags.any{it == "rule"}
        def trigger = rule.triggers.find{it.id.equals("ItemStateChangeTriggerID")} as Trigger
        assertThat trigger, is(notNullValue())
        assertThat trigger.typeUID, is("GenericEventTrigger")
        assertThat trigger.configuration.get("eventSource"), is ("myMotionItem")
        assertThat trigger.configuration.get("eventTopic"), is("smarthome/items/*")
        assertThat trigger.configuration.get("eventTypes"), is("ItemStateEvent")
        def condition1 = rule.conditions.find{it.id.equals("ItemStateConditionID")} as Condition
        assertThat condition1, is(notNullValue())
        assertThat condition1.typeUID, is("EventCondition")
        assertThat condition1.configuration.get("topic"), is("smarthome/items/myMotionItem/state")
        assertThat condition1.configuration.get("payload"), is(".*ON.*")
        def action = rule.actions.find{it.id.equals("ItemPostCommandActionID")} as Action
        assertThat action, is(notNullValue())
        assertThat action.typeUID, is("ItemPostCommandAction")
        assertThat action.configuration.get("itemName"), is("myLampItem")
        assertThat action.configuration.get("command"), is("ON")
        def ruleStatus = ruleRegistry.getStatusInfo(rule.uid) as RuleStatusInfo
        assertThat ruleStatus.getStatus(), is(RuleStatus.IDLE)
    }

    @Test
    public void 'assert that a rule from json file is added automatically with resolved module references' () {
        logger.info("assert that a rule from json file is added automatically with resolved module references");

        //WAIT until Rule modules types are parsed and the rule becomes IDLE
        waitForAssert({
            assertThat ruleRegistry.getAll().isEmpty(), is(false)
            def rule2 = ruleRegistry.getAll().find{it.tags!=null && it.tags.contains("jsonTest") && it.tags.contains("references")} as Rule
            assertThat rule2, is(notNullValue())
            def ruleStatus2 = ruleRegistry.getStatusInfo(rule2.uid) as RuleStatusInfo
            assertThat ruleStatus2.getStatus(), is(RuleStatus.IDLE)
        }, 10000, 200)
        def rule = ruleRegistry.getAll().find{it.tags!=null && it.tags.contains("jsonTest") && it.tags.contains("references")} as Rule
        assertThat rule, is(notNullValue())
        assertThat rule.name, is("ItemSampleRuleWithReferences")
        assertTrue rule.tags.any{it == "sample"}
        assertTrue rule.tags.any{it == "item"}
        assertTrue rule.tags.any{it == "rule"}
        assertTrue rule.tags.any{it == "references"}
        def trigger = rule.triggers.find{it.id.equals("ItemStateChangeTriggerID")} as Trigger
        assertThat trigger, is(notNullValue())
        assertThat trigger.typeUID, is("GenericEventTrigger")
        assertThat trigger.configuration.get("eventSource"), is ("myMotionItem")
        assertThat trigger.configuration.get("eventTopic"), is("smarthome/items/*")
        assertThat trigger.configuration.get("eventTypes"), is("ItemStateEvent")
        def condition1 = rule.conditions.find{it.id.equals("ItemStateConditionID")} as Condition
        assertThat condition1, is(notNullValue())
        assertThat condition1.typeUID, is("EventCondition")
        assertThat condition1.configuration.get("topic"), is("smarthome/items/myMotionItem/state")
        assertThat condition1.configuration.get("payload"), is(".*ON.*")
        def action = rule.actions.find{it.id.equals("ItemPostCommandActionID")} as Action
        assertThat action, is(notNullValue())
        assertThat action.typeUID, is("ItemPostCommandAction")
        assertThat action.configuration.get("itemName"), is("myLampItem")
        assertThat action.configuration.get("command"), is("ON")
        def ruleStatus = ruleRegistry.getStatusInfo(rule.uid) as RuleStatusInfo
        assertThat ruleStatus.getStatus(), is(RuleStatus.IDLE)
    }

    @Test
    public void 'assert that a rule from json file is executed correctly' () {
        logger.info('assert that rule added by json is executed correctly');
        waitForAssert({
            assertThat ruleRegistry.getAll().isEmpty(), is(false)
            Rule r = ruleRegistry.get("ItemSampleRule")
            assertThat r, is(notNullValue())
            assertThat ruleRegistry.getStatusInfo(r.UID).getStatus(), is(RuleStatus.IDLE)

        }, 9000, 200)
        SwitchItem myPresenceItem = itemRegistry.getItem("myPresenceItem")
        eventPublisher.post(ItemEventFactory.createCommandEvent("myPresenceItem", OnOffType.ON))
        SwitchItem myLampItem = itemRegistry.getItem("myLampItem")
        assertThat myLampItem.getState(), is(UnDefType.NULL)
        SwitchItem myMotionItem = itemRegistry.getItem("myMotionItem")
        Event event = null
        def eventHandler = [
            receive: { Event e ->
                logger.info("Event: " + e.topic)
                if (e.topic == "smarthome/items/myLampItem/state"){
                    event=e
                }
            },
            getSubscribedEventTypes: {
                Sets.newHashSet(ItemStateEvent.TYPE)
            },
            getEventFilter:{ null }
        ] as EventSubscriber
        registerService(eventHandler)
        eventPublisher.post(ItemEventFactory.createCommandEvent("myMotionItem", OnOffType.ON))
        waitForAssert ({
            assertThat (myLampItem.getState(), is(OnOffType.ON))
            assertThat event, is(notNullValue())
            assertThat event.topic, is(equalTo("smarthome/items/myLampItem/state"))
        }, 9000, 100)

        assertThat event.topic, is(equalTo("smarthome/items/myLampItem/state"))
        assertThat (((ItemStateEvent)event).itemState, is(OnOffType.ON))

    }

}