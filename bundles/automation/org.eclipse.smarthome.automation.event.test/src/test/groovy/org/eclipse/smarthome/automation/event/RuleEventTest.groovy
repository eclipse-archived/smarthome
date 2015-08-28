/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.event;


import static org.junit.Assert.*

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.automation.Action
import org.eclipse.smarthome.automation.Condition
import org.eclipse.smarthome.automation.Rule
import org.eclipse.smarthome.automation.RuleRegistry
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.Trigger
import org.eclipse.smarthome.automation.events.RuleAddedEvent
import org.eclipse.smarthome.automation.events.RuleRemovedEvent
import org.eclipse.smarthome.automation.events.RuleStatusInfoEvent
import org.eclipse.smarthome.automation.events.RuleUpdatedEvent
import org.eclipse.smarthome.core.autoupdate.AutoUpdateBindingConfigProvider
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.events.ItemEventFactory
import org.eclipse.smarthome.core.items.events.ItemStateEvent
import org.eclipse.smarthome.core.items.events.ItemUpdatedEvent
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.core.types.TypeParser
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.Before
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.common.collect.Sets

/**
 * This tests events of rules
 * 
 * @author Benedikt Niehues - initial contribution
 *
 */
class RuleEventTest extends OSGiTest{

    final Logger logger = LoggerFactory.getLogger(RuleEventTest.class)
    VolatileStorageService volatileStorageService = new VolatileStorageService()

    @Before
    void before() {
        def itemProvider = [
            getAll: {
                [
                    new SwitchItem("myMotionItem"),
                    new SwitchItem("myPresenceItem"),
                    new SwitchItem("myLampItem"),
                    new SwitchItem("myMotionItem2"),
                    new SwitchItem("myPresenceItem2"),
                    new SwitchItem("myLampItem2")
                ]
            },
            addProviderChangeListener: {},
            removeProviderChangeListener: {},
            allItemsChanged: {}] as ItemProvider
        registerService(itemProvider)
        registerService(volatileStorageService)

        def autoupdateConfig = [
            autoUpdate: { String itemName -> return true }

        ] as AutoUpdateBindingConfigProvider
        registerService(autoupdateConfig)
    }

    @Test
    public void testRuleEvents() {
        
        //Registering eventSubscriber
        def ruleEvents = [] as List<Event>
        
        def ruleEventHandler = [
            receive: {  Event e ->
                logger.info("RuleEvent: " + e.topic)
                    ruleEvents.add(e)
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(RuleAddedEvent.TYPE, RuleRemovedEvent.TYPE, RuleStatusInfoEvent.TYPE, RuleUpdatedEvent.TYPE)
            },

            getEventFilter:{ null }
            ] as EventSubscriber
        registerService(ruleEventHandler)
        
        //Creation of RULE
        def triggerConfig = [itemName:"myMotionItem2"]
        def condition1Config = [operator:"=", itemName:"myPresenceItem2", state:"ON"]
        def condition2Config = [operator:"=", itemName:"myMotionItem2", state:"ON"]
        def actionConfig = [itemName:"myLampItem2", command:"ON"]
        def triggers = [
            new Trigger("ItemStateChangeTrigger2", "ItemStateChangeTrigger", triggerConfig)
        ]
        def conditions = [
            new Condition("ItemStateCondition3", "ItemStateCondition", condition1Config, null),
            new Condition("ItemStateCondition4", "ItemStateCondition", condition2Config, null)
        ]
        def actions = [
            new Action("ItemPostCommandAction2", "ItemPostCommandAction", actionConfig, null)
        ]

        def rule = new Rule("myRule21",triggers, conditions, actions, null, null)
        rule.name="RuleEventTestingRule"

        logger.info("Rule created: "+rule.getUID())

        def ruleRegistry = getService(RuleRegistry)
        ruleRegistry.add(rule)
        ruleRegistry.setEnabled(rule.UID, true)

        //TEST RULE

        def EventPublisher eventPublisher = getService(EventPublisher)
        def ItemRegistry itemRegistry = getService(ItemRegistry)
        SwitchItem myMotionItem = itemRegistry.getItem("myMotionItem2")
        Command commandObj = TypeParser.parseCommand(myMotionItem.getAcceptedCommandTypes(), "ON")
        eventPublisher.post(ItemEventFactory.createCommandEvent("myPresenceItem2", commandObj))

        Event itemEvent = null

        def itemEventHandler = [
            receive: {  Event e ->
                logger.info("Event: " + e.topic)
                if (e.topic.contains("myLampItem2")){
                    itemEvent=e
                }
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(ItemUpdatedEvent.TYPE, ItemStateEvent.TYPE)
            },

            getEventFilter:{ null }

        ] as EventSubscriber

        registerService(itemEventHandler)
        commandObj = TypeParser.parseCommand(itemRegistry.getItem("myMotionItem2").getAcceptedCommandTypes(),"ON")
        eventPublisher.post(ItemEventFactory.createCommandEvent("myMotionItem2", commandObj))
        waitForAssert ({ assertThat itemEvent, is(notNullValue())} , 3000, 100)
        assertThat itemEvent.topic, is(equalTo("smarthome/items/myLampItem2/state"))
        assertThat (((ItemStateEvent)itemEvent).itemState, is(OnOffType.ON))
        def myLampItem2 = itemRegistry.getItem("myLampItem2")
        assertThat myLampItem2, is(notNullValue())
        logger.info("myLampItem2 State: " + myLampItem2.state)
        assertThat myLampItem2.state, is(OnOffType.ON)
        assertThat ruleEvents.size(), is(not(0))
        assertThat ruleEvents.find{it.topic =="smarthome/rules/myRule21/added"}, is(notNullValue())
        assertThat ruleEvents.find{it.topic =="smarthome/rules/myRule21/state"}, is(notNullValue())
        def stateEvents = ruleEvents.findAll{it.topic=="smarthome/rules/myRule21/state"} as List<RuleStatusInfoEvent>
        assertThat stateEvents, is(notNullValue())
        def runningEvent = stateEvents.find{it.statusInfo.status==RuleStatus.RUNNING}
        assertThat runningEvent, is(notNullValue())
        ruleRegistry.remove("myRule21")
        assertThat ruleEvents.find{it.topic=="smarthome/rules/myRule21/removed"}, is(notNullValue())
        
//        assertThat stateEvents.findAll{it.statusInfo.status=="IDLE"}, is(notNullValue())
    }
}