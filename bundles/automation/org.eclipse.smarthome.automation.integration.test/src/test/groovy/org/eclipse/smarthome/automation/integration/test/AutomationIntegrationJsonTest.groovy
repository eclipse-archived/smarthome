/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
import org.eclipse.smarthome.automation.Connection
import org.eclipse.smarthome.automation.Rule
import org.eclipse.smarthome.automation.RuleRegistry
import org.eclipse.smarthome.automation.RuleStatus
import org.eclipse.smarthome.automation.Trigger
import org.eclipse.smarthome.automation.events.RuleStatusInfoEvent
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
import org.eclipse.smarthome.core.types.Command
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
    def Event ruleEvent
    static def VolatileStorageService volatileStorageService = new VolatileStorageService()//keep storage with rules imported from json files

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
        eventPublisher = getService(EventPublisher)
        itemRegistry = getService(ItemRegistry)
        ruleRegistry = getService(RuleRegistry)
        waitForAssert ({
            assertThat storageService, is(notNullValue())

            assertThat eventPublisher, is(notNullValue()) //sometimes assert fails because EventPublisher service is null
            assertThat itemRegistry, is(notNullValue())
            assertThat ruleRegistry, is(notNullValue())
        }, 9000)
        logger.info('@Before.finish');
    }

    @After
    void after() {
        logger.info('@After');
    }


    protected void registerVolatileStorageService() {
        registerService(volatileStorageService);
    }


    @Test
    public void 'assert that a rule from json file is added automatically' () {
        logger.info("assert that a rule from json file is added automatically");

        //WAIT until Rule modules types are parsed and the rule becomes IDLE
        waitForAssert({
            assertThat ruleRegistry.getAll().isEmpty(), is(false)
            def rule2 = ruleRegistry.getAll().find{it.tags!=null && it.tags.contains("jsonTest")} as Rule
            assertThat rule2, is(notNullValue())
            def ruleStatus2 = ruleRegistry.getStatus(rule2.uid) as RuleStatus
            assertThat ruleStatus2, is(RuleStatus.IDLE)
        }, 10000, 200)
        def rule = ruleRegistry.getAll().find{it.tags!=null && it.tags.contains("jsonTest")} as Rule
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
        def ruleStatus = ruleRegistry.getStatus(rule.uid) as RuleStatus
        assertThat ruleStatus, is(RuleStatus.IDLE)
    }


    @Test
    public void 'assert that a rule from json file is executed correctly' () {
        logger.info('assert that rule added by json is executed correctly');
        waitForAssert({
            assertThat ruleRegistry.getAll().isEmpty(), is(false)
        }, 3000, 200)
        SwitchItem myPresenceItem = itemRegistry.getItem("myPresenceItem")
        Command
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
        //        eventPublisher.post(ItemEventFactory.createStateEvent("myPresenceItem", OnOffType.ON))
        eventPublisher.post(ItemEventFactory.createCommandEvent("myMotionItem", OnOffType.ON))
        waitForAssert ({
            //            assertThat ruleEvent, is(notNullValue())
            assertThat (myLampItem.getState(), is(OnOffType.ON))
            assertThat event, is(notNullValue())
            assertThat event.topic, is(equalTo("smarthome/items/myLampItem/state"))
        }, 9000, 100)

        assertThat event.topic, is(equalTo("smarthome/items/myLampItem/state"))
        assertThat (((ItemStateEvent)event).itemState, is(OnOffType.ON))


    }

    ///// Test Rule with custom module types ///////////
    @Test
    public void 'assert that a rule from json file with custom module types is executed 1' () {
        logger.info("assert that a rule from json file with custom module types is executed 1");
        //WAIT until Rule modules types are parsed and the rule becomes IDLE
        waitForAssert({
            assertThat ruleRegistry.getAll().isEmpty(), is(false)
            def rule2 = ruleRegistry.getAll().find{it.tags!=null && it.tags.contains("custom1")} as Rule
            assertThat rule2, is(notNullValue())
            def ruleStatus2 = ruleRegistry.getStatus(rule2.uid) as RuleStatus
            assertThat ruleStatus2, is(RuleStatus.IDLE)

        }, 10000, 200)

        def rule2 = ruleRegistry.getAll().find{it.tags!=null && it.tags.contains("custom1")} as Rule
        assertThat rule2, is(notNullValue())

        SwitchItem myLampItemNew = itemRegistry.getItem("myLampItem")

        logger.info("post event for myMotionItem11");
        //        eventPublisher.post(ItemEventFactory.createStateEvent("myMotionItem11", OnOffType.ON))
        eventPublisher.post(ItemEventFactory.createCommandEvent("myMotionItem11", OnOffType.ON))

        //check rule with custom trigger is executed
        waitForAssert ({
            assertThat (myLampItemNew.getState(), is(OnOffType.OFF))
        }, 9000, 100)

    }

    @Test
    public void 'assert that a rule from json file with custom module types is executed 2' () {
        logger.info("assert that a rule from json file with custom module types is executed 2");

        //WAIT until Rule modules types are parsed and the rule becomes IDLE
        waitForAssert({
            assertThat ruleRegistry.getAll().isEmpty(), is(false)
            def rule2 = ruleRegistry.getAll().find{it.tags!=null && it.tags.contains("custom2")} as Rule
            assertThat rule2, is(notNullValue())
            def ruleStatus2 = ruleRegistry.getStatus(rule2.uid) as RuleStatus
            assertThat ruleStatus2, is(RuleStatus.IDLE)

        }, 10000, 200)

        def rule2 = ruleRegistry.getAll().find{it.tags!=null && it.tags.contains("custom2")} as Rule
        assertThat rule2, is(notNullValue())

        assertThat rule2.name, is("ItemCustomRule2")
        assertTrue rule2.tags.any{it == "custom2"}
        assertTrue rule2.tags.any{it == "item"}
        assertTrue rule2.tags.any{it == "rule"}

        def triggerCustom = rule2.triggers.find{it.id.equals("ItemStateChangeTriggerID2")} as Trigger
        assertThat triggerCustom, is(notNullValue())
        assertThat triggerCustom.typeUID, is("GenericEventTrigger:Custom1")
        assertThat triggerCustom.configuration.get("eventSource"), is ("myMotionItem11")
        assertThat triggerCustom.configuration.get("eventTopic"), is("smarthome/items/*")
        assertThat triggerCustom.configuration.get("eventTypes"), is("ItemStateEvent")

        def condition1Custom = rule2.conditions.find{it.id.equals("ItemStateConditionID2")} as Condition
        assertThat condition1Custom, is(notNullValue())
        assertThat condition1Custom.typeUID, is("EventCondition")
        assertThat condition1Custom.configuration.get("topic"), is("smarthome/items/myMotionItem11/state")
        assertThat condition1Custom.configuration.get("payload"), is(".*ON.*")

        def actionCustom = rule2.actions.find{it.id.equals("ItemPostCommandActionID3")} as Action
        assertThat actionCustom, is(notNullValue())
        assertThat actionCustom.typeUID, is("ItemPostCommandAction:Custom1")
        assertThat actionCustom.configuration.get("customItemCommand"), is("ON")

        SwitchItem myLampItem11 = itemRegistry.getItem("myLampItem11")
        assertThat myLampItem11.getState(), is(UnDefType.NULL)


        logger.info("post event for myMotionItem11");
        //        eventPublisher.post(ItemEventFactory.createStateEvent("myMotionItem11", OnOffType.ON))
        eventPublisher.post(ItemEventFactory.createCommandEvent("myMotionItem11", OnOffType.ON))

        waitForAssert ({
            assertThat (myLampItem11.getState(), is(OnOffType.ON))
        }, 9000, 100)

    }

    @Test
    public void 'assert that a rule from json file with custom module types is executed 3' () {
        logger.info("assert that a rule from json file with custom module types is executed 3");

        def rule3 = ruleRegistry.getAll().find{it.tags!=null && it.tags.contains("custom3")} as Rule
        assertThat rule3, is(notNullValue())

        def actionCustom2 = rule3.actions.find{it.id.equals("ItemPostCommandActionID4")} as Action
        assertThat actionCustom2, is(notNullValue())
        assertThat actionCustom2.typeUID, is("ItemPostCommandAction:Custom2")
        assertThat actionCustom2.configuration.get("customItemCommand"), is("OFF")
        def actionCustom2Connection = actionCustom2.connections.find{it.inputName.equals("customActionInput")} as Connection
        assertThat actionCustom2Connection, is(notNullValue())
        assertThat actionCustom2Connection.ouputModuleId, is("ItemStateChangeTriggerID2")
        assertThat actionCustom2Connection.outputName, is("customTriggerOutputSource")

        SwitchItem myMotionItem11 = itemRegistry.getItem("myMotionItem11")
        assertThat myMotionItem11.getState(), is(UnDefType.NULL)

        logger.info("post event for myMotionItem11");
        //        eventPublisher.post(ItemEventFactory.createStateEvent("myMotionItem11", OnOffType.ON))
        eventPublisher.post(ItemEventFactory.createCommandEvent("myMotionItem11", OnOffType.ON))

        waitForAssert ({
            assertThat (myMotionItem11.getState(), is(OnOffType.OFF))
        }, 9000, 100)
    }


}