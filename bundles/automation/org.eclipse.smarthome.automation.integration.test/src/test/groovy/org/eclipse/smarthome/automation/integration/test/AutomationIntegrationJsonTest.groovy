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
import org.eclipse.smarthome.automation.Rule
import org.eclipse.smarthome.automation.RuleRegistry
import org.eclipse.smarthome.automation.RuleStatus
import org.eclipse.smarthome.automation.Trigger
import org.eclipse.smarthome.automation.events.RuleStatusInfoEvent
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry
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

        enableItemAutoUpdate()

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
        //        def condition2 = rule.conditions.find{it.id.equals("ItemStateConditionID2")} as Condition
        //        assertThat condition2, is(notNullValue())
        //        assertThat condition2.typeUID, is("ItemStateCondition")
        //        assertThat condition2.configuration.get("operator"), is("=")
        //        assertThat condition2.configuration.get("itemName"), is("myMotionItem")
        //        assertThat condition2.configuration.get("state"), is("ON")
        def action = rule.actions.find{it.id.equals("ItemPostCommandActionID")} as Action
        assertThat action, is(notNullValue())
        assertThat action.typeUID, is("ItemPostCommandAction")
        assertThat action.configuration.get("itemName"), is("myLampItem")
        assertThat action.configuration.get("command"), is("ON")

        def ruleStatus = ruleRegistry.getStatus(rule.uid) as RuleStatus
        assertThat ruleStatus, is(RuleStatus.IDLE)
        def mtr = getService(ModuleTypeRegistry) as ModuleTypeRegistry
        waitForAssert({
            assertThat mtr.get("EventCondition"), is(notNullValue())
        })

        //    }
        //
        //
        //    @Test
        //    public void 'assert that rule added by json is executed correctly' () {
        logger.info('assert that rule added by json is executed correctly');
        waitForAssert({
            assertThat ruleRegistry.getAll().isEmpty(), is(false)
        }, 3000, 200)

        //def rule = ruleRegistry.getAll().find{it.tags!=null && it.tags.contains("jsonTest")} as Rule
        assertThat rule, is(notNullValue())

        waitForAssert ({
            assertThat ruleRegistry.getStatus(rule.UID), is(RuleStatus.IDLE)
        })
        SwitchItem myPresenceItem = itemRegistry.getItem("myPresenceItem")
        eventPublisher.post(ItemEventFactory.createStateEvent("myPresenceItem", OnOffType.ON))
        //        myPresenceItem.setState(OnOffType.ON);

        SwitchItem myLampItem = itemRegistry.getItem("myLampItem")
        assertThat myLampItem.getState(), is(UnDefType.NULL)

        SwitchItem myMotionItem = itemRegistry.getItem("myMotionItem")

        //TODO workarround issue with async event delivery - rule engine receives event before the item manages to update its state
        //        myMotionItem.setState(OnOffType.ON);

        Event ruleEvent = null
        def ruleEventHandler = [
            receive: { Event e ->
                logger.info("RuleEvent: " + e.topic + " --> " + e.payload)
                if (e.topic == "smarthome/rules/org_eclipse_smarthome_automation_integration_test_0/state" && e.payload.contains("RUNNING")){

                    ruleEvent=e
                }
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(RuleStatusInfoEvent.TYPE)
            },

            getEventFilter:{ null }

        ] as EventSubscriber


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

        registerService(ruleEventHandler)
        registerService(eventHandler)
        //        eventPublisher.post(ItemEventFactory.createStateEvent("myPresenceItem", OnOffType.ON))
        eventPublisher.post(ItemEventFactory.createStateEvent("myMotionItem", OnOffType.ON))
        waitForAssert ({
            assertThat ruleEvent, is(notNullValue())
            //            assertThat (myLampItem.getState(), is(OnOffType.ON))
            assertThat event, is(notNullValue())
            assertThat event.topic, is(equalTo("smarthome/items/myLampItem/state"))
        }
        , 9000, 100)
        assertThat event.topic, is(equalTo("smarthome/items/myLampItem/state"))
        assertThat (((ItemStateEvent)event).itemState, is(OnOffType.ON))
    }


}