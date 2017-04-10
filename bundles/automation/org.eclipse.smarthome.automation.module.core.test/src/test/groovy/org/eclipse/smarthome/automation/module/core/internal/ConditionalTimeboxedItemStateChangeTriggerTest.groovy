/**
 * Copyright (c) 2017 by Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.core.internal;


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
import org.eclipse.smarthome.automation.module.core.handler.CompareConditionHandler
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.events.ItemCommandEvent
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
import org.junit.Ignore
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.common.collect.Sets
/**
 * this tests the RunRuleAction
 *
 * @author Benedikt Niehues - initial contribution
 *
 */
class ConditionalTimeboxedItemStateChangeTriggerTest extends OSGiTest{

    final Logger logger = LoggerFactory.getLogger(RuntimeRuleTest.class)

    @Before
    void before() {

        def itemProvider = [
            getAll: {
                [
                    new SwitchItem("switch1"),
                    new SwitchItem("switch2"),
                    new SwitchItem("switch3"),
                    new SwitchItem("ruleTrigger")
                ]
            },
            addProviderChangeListener: {},
            removeProviderChangeListener: {},
            allItemsChanged: {}] as ItemProvider
        registerService(itemProvider)
        registerVolatileStorageService()

        enableItemAutoUpdate()
    }



    @Test
    public void 'assert that the rule is triggered by a CTISCTrigger'() {
        //Creation of scene RULE
        def action1Config = new Configuration([itemName:"switch2", command:"ON"])
        def triggerConfig = new Configuration([itemName:"ruleTrigger",operator:"EQUALS",value:"ON"])
        def ruleActions = [
            new Action("itemPostCommandAction","core.ItemCommandAction",action1Config,null),
        ]
        def ruleTriggers = [
            new Trigger("conditionalTrigger", "core.ConditionalTimeboxedItemStateChangeTrigger", triggerConfig)
        ]
        def rule = new Rule("rule1")
        rule.actions=ruleActions
        rule.triggers=ruleTriggers
        rule.name="Rule1"
        logger.info("rule created: "+rule.getUID())

        def ruleRegistry = getService(RuleRegistry) as RuleRegistry
        ruleRegistry.add(rule)
        ruleRegistry.setEnabled(rule.UID, true)

        waitForAssert({
            assertThat ruleRegistry.getStatusInfo(rule.UID).status, is(RuleStatus.IDLE)
        })
        //TEST RULE

        def EventPublisher eventPublisher = getService(EventPublisher)
        def ItemRegistry itemRegistry = getService(ItemRegistry)
        SwitchItem ruleTriggerItem = itemRegistry.getItem("ruleTrigger")

        Event itemEvent = null

        def itemEventHandler = [
            receive: {  Event e ->
                logger.info("Event: " + e.topic)
                if (e.topic.contains("switch2")){
                    itemEvent=e
                }
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(ItemUpdatedEvent.TYPE, ItemStateEvent.TYPE)
            },

            getEventFilter:{ null }

        ] as EventSubscriber

        registerService(itemEventHandler)
        //trigger rule by switching triggerItem ON
        ruleTriggerItem.send(OnOffType.OFF)
        ruleTriggerItem.send(OnOffType.ON)

        waitForAssert ({ assertThat itemEvent, is(notNullValue())} , 3000, 100)
        assertThat itemEvent.topic, is(equalTo("smarthome/items/switch2/state"))
        assertThat (((ItemStateEvent)itemEvent).itemState, is(OnOffType.ON))

        def switch2 = itemRegistry.getItem("switch2")
        assertThat switch2, is(notNullValue())
        assertThat switch2.state, is(OnOffType.ON)
    }

    @Test
    public void 'assert that the rule is triggered by a CTISCTrigger withTimebox'() {
        //Creation of scene RULE
        def action1Config = new Configuration([itemName:"switch2", command:"ON"])
        def triggerConfig = new Configuration([itemName:"ruleTrigger",operator:"EQUALS",value:"ON", timeboxValue:"2", timeboxUnit:"SECONDS"])
        def ruleActions = [
            new Action("itemPostCommandAction","core.ItemCommandAction",action1Config,null),
        ]
        def ruleTriggers = [
            new Trigger("conditionalTrigger", "core.ConditionalTimeboxedItemStateChangeTrigger", triggerConfig)
        ]
        def rule = new Rule("rule1")
        rule.actions=ruleActions
        rule.triggers=ruleTriggers
        rule.name="Rule1"
        logger.info("rule created: "+rule.getUID())

        def ruleRegistry = getService(RuleRegistry) as RuleRegistry
        ruleRegistry.add(rule)
        ruleRegistry.setEnabled(rule.UID, true)

        waitForAssert({
            assertThat ruleRegistry.getStatusInfo(rule.UID).status, is(RuleStatus.IDLE)
        })
        //TEST RULE

        def EventPublisher eventPublisher = getService(EventPublisher)
        def ItemRegistry itemRegistry = getService(ItemRegistry)
        SwitchItem ruleTriggerItem = itemRegistry.getItem("ruleTrigger")

        Event itemEvent = null

        def itemEventHandler = [
            receive: {  Event e ->
                logger.info("Event: " + e.topic)
                if (e.topic.contains("switch2")){
                    itemEvent=e
                }
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(ItemUpdatedEvent.TYPE, ItemStateEvent.TYPE)
            },

            getEventFilter:{ null }

        ] as EventSubscriber

        registerService(itemEventHandler)
        //trigger rule by switching triggerItem ON
        ruleTriggerItem.send(OnOffType.OFF)
        ruleTriggerItem.send(OnOffType.ON)
        sleep(1000)
        assertThat itemEvent, is(null)
        waitForAssert ({ assertThat itemEvent, is(notNullValue())} , 3000, 100)
        assertThat itemEvent.topic, is(equalTo("smarthome/items/switch2/state"))
        assertThat (((ItemStateEvent)itemEvent).itemState, is(OnOffType.ON))

        def switch2 = itemRegistry.getItem("switch2")
        assertThat switch2, is(notNullValue())
        assertThat switch2.state, is(OnOffType.ON)
    }

    @Test
    public void 'assert that the rule is NOT triggered by a CTISCTrigger withTimebox when value changes during timebox'() {
        //Creation of scene RULE
        def action1Config = new Configuration([itemName:"switch2", command:"ON"])
        def triggerConfig = new Configuration([itemName:"ruleTrigger",operator:"EQUALS",value:"ON", timeboxValue:"3", timeboxUnit:"SECONDS"])
        def ruleActions = [
            new Action("itemPostCommandAction","core.ItemCommandAction",action1Config,null),
        ]
        def ruleTriggers = [
            new Trigger("conditionalTrigger", "core.ConditionalTimeboxedItemStateChangeTrigger", triggerConfig)
        ]
        def rule = new Rule("rule1")
        rule.actions=ruleActions
        rule.triggers=ruleTriggers
        rule.name="Rule1"
        logger.info("rule created: "+rule.getUID())

        def ruleRegistry = getService(RuleRegistry) as RuleRegistry
        ruleRegistry.add(rule)
        ruleRegistry.setEnabled(rule.UID, true)

        waitForAssert({
            assertThat ruleRegistry.getStatusInfo(rule.UID).status, is(RuleStatus.IDLE)
        })
        //TEST RULE

        def EventPublisher eventPublisher = getService(EventPublisher)
        def ItemRegistry itemRegistry = getService(ItemRegistry)
        def ruleTriggerItem = itemRegistry.getItem("ruleTrigger") as SwitchItem
        def switch2 = itemRegistry.getItem("switch2") as SwitchItem
        assertThat switch2, is(notNullValue())
        switch2.send(OnOffType.OFF)

        Event itemEvent = null

        def itemEventHandler = [
            receive: {  Event e ->
                logger.info("Event: " + e.topic)
                if (e.topic.contains("switch2")){
                    itemEvent=e
                }
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(ItemUpdatedEvent.TYPE, ItemStateEvent.TYPE)
            },

            getEventFilter:{ null }

        ] as EventSubscriber

        registerService(itemEventHandler)
        //trigger rule by switching triggerItem ON
        ruleTriggerItem.send(OnOffType.OFF)
        ruleTriggerItem.send(OnOffType.ON)
        sleep(1000)
        ruleTriggerItem.send(OnOffType.OFF)
        sleep(3000)
        assertThat itemEvent, is(null)

        assertThat switch2.state, is(OnOffType.OFF)

    }

    @Test
    public void 'assert that the rule is triggered by a CTISCTrigger withTimebox when value changes during timebox with inverted logic'() {
        //Creation of scene RULE
        def action1Config = new Configuration([itemName:"switch2", command:"ON"])
        def triggerConfig = new Configuration([itemName:"ruleTrigger",operator:"EQUALS",value:"ON", timeboxValue:1, timeboxUnit:"SECONDS", invert:true])
        def ruleActions = [
            new Action("itemPostCommandAction","core.ItemCommandAction",action1Config,null),
        ]
        def ruleTriggers = [
            new Trigger("conditionalTrigger", "core.ConditionalTimeboxedItemStateChangeTrigger", triggerConfig)
        ]
        def rule = new Rule("rule1")
        rule.actions=ruleActions
        rule.triggers=ruleTriggers
        rule.name="Rule1"
        logger.info("rule created: "+rule.getUID())

        def ruleRegistry = getService(RuleRegistry) as RuleRegistry
        ruleRegistry.add(rule)
        ruleRegistry.setEnabled(rule.UID, true)

        waitForAssert({
            assertThat ruleRegistry.getStatusInfo(rule.UID).status, is(RuleStatus.IDLE)
        })
        //TEST RULE

        def EventPublisher eventPublisher = getService(EventPublisher)
        def ItemRegistry itemRegistry = getService(ItemRegistry)
        def ruleTriggerItem = itemRegistry.getItem("ruleTrigger") as SwitchItem
        def switch2 = itemRegistry.getItem("switch2") as SwitchItem
        assertThat switch2, is(notNullValue())
        switch2.send(OnOffType.OFF)

        Event itemEvent = null

        def itemEventHandler = [
            receive: {  Event e ->
                logger.info("Event: " + e.topic)
                if (e.topic.contains("switch2")){
                    itemEvent=e
                }
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(ItemUpdatedEvent.TYPE, ItemStateEvent.TYPE)
            },

            getEventFilter:{ null }

        ] as EventSubscriber

        registerService(itemEventHandler)
        //trigger rule by switching triggerItem ON
        ruleTriggerItem.send(OnOffType.ON)
        ruleTriggerItem.send(OnOffType.OFF)
        sleep(1000)
        waitForAssert ({ assertThat itemEvent, is(notNullValue())} , 3000, 100)
        assertThat itemEvent.topic, is(equalTo("smarthome/items/switch2/state"))
        assertThat (((ItemStateEvent)itemEvent).itemState, is(OnOffType.ON))

        assertThat switch2.state, is(OnOffType.ON)

    }

    @Test
    public void 'assert that the rule is triggered by a AutoInvertCTISCTrigger withTimebox'() {
        //Creation of scene RULE
        def action1Config = new Configuration([itemName:"switch2", command:"ON"])
        def triggerConfig = new Configuration([itemName:"ruleTrigger",operator:"EQUALS",value:"ON", timeboxValue:2, timeboxUnit:"SECONDS", timeboxValue_inverted:2, timeboxUnit_inverted:"SECONDS"])
        def ruleActions = [
            new Action("itemPostCommandAction","core.ItemCommandAction",action1Config,null),
        ]
        def ruleTriggers = [
            new Trigger("conditionalTrigger", "core.AutoInvertConditionalTimeboxedItemStateChangeTrigger", triggerConfig)
        ]
        def rule = new Rule("rule1")
        rule.actions=ruleActions
        rule.triggers=ruleTriggers
        rule.name="Rule1"
        logger.info("rule created: "+rule.getUID())

        def ruleRegistry = getService(RuleRegistry) as RuleRegistry
        ruleRegistry.add(rule)
        ruleRegistry.setEnabled(rule.UID, true)

        waitForAssert({
            def ruleStatusInfo = ruleRegistry.getStatusInfo(rule.UID)
            logger.info("RuleStatus: " + ruleStatusInfo)
            assertThat ruleStatusInfo.status, is(RuleStatus.IDLE)
        })
        //TEST RULE

        def EventPublisher eventPublisher = getService(EventPublisher)
        def ItemRegistry itemRegistry = getService(ItemRegistry)
        def ruleTriggerItem = itemRegistry.getItem("ruleTrigger") as SwitchItem
        def switch2 = itemRegistry.getItem("switch2") as SwitchItem
        assertThat switch2, is(notNullValue())
        switch2.send(OnOffType.OFF)

        Event itemEvent = null

        def itemEventHandler = [
            receive: {  Event e ->
                logger.info("Event: " + e.topic)
                if (e.topic.contains("switch2")){
                    itemEvent=e
                }
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(ItemUpdatedEvent.TYPE, ItemStateEvent.TYPE)
            },

            getEventFilter:{ null }

        ] as EventSubscriber

        registerService(itemEventHandler)
        //trigger rule by switching triggerItem ON
        ruleTriggerItem.send(OnOffType.OFF)
        ruleTriggerItem.send(OnOffType.ON)
        sleep(2000)
        waitForAssert ({ assertThat itemEvent, is(notNullValue())} , 3000, 100)
        assertThat itemEvent.topic, is(equalTo("smarthome/items/switch2/state"))
        assertThat (((ItemStateEvent)itemEvent).itemState, is(OnOffType.ON))

        itemEvent = null
        switch2.send(OnOffType.OFF)

        waitForAssert({assertThat itemEvent, is(notNullValue())}, 3000,100)
        assertThat itemEvent.topic, is(equalTo("smarthome/items/switch2/state"))
        assertThat (((ItemStateEvent)itemEvent).itemState, is(OnOffType.OFF))
        assertThat switch2.state, is(OnOffType.OFF)
        itemEvent = null
        
        ruleTriggerItem.send(OnOffType.OFF)
        sleep(2000)
        waitForAssert({assertThat itemEvent, is(notNullValue())}, 3000,100)
        assertThat itemEvent.topic, is(equalTo("smarthome/items/switch2/state"))
        assertThat (((ItemStateEvent)itemEvent).itemState, is(OnOffType.ON))
        assertThat switch2.state, is(OnOffType.ON)
        
    }

}