/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
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
 * this tests the RuleEngine
 *
 * @author Benedikt Niehues - initial contribution
 *
 */
class RuntimeRuleTest extends OSGiTest{

    final Logger logger = LoggerFactory.getLogger(RuntimeRuleTest.class)
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
                    new SwitchItem("myLampItem2"),
                    new SwitchItem("myMotionItem3"),
                    new SwitchItem("myPresenceItem3"),
                    new SwitchItem("myLampItem3"),
                    new SwitchItem("myMotionItem4"),
                    new SwitchItem("myPresenceItem4"),
                    new SwitchItem("myLampItem4"),
                ]
            },
            addProviderChangeListener: {},
            removeProviderChangeListener: {},
            allItemsChanged: {}] as ItemProvider
        registerService(itemProvider)
        registerService(volatileStorageService)

        enableItemAutoUpdate()
    }


    @Test
    @Ignore
    public void testPredefinedRule() {

        def EventPublisher eventPublisher = getService(EventPublisher)
        def ItemRegistry itemRegistry = getService(ItemRegistry)
        SwitchItem myMotionItem = itemRegistry.getItem("myMotionItem")
        eventPublisher.post(ItemEventFactory.createStateEvent("myPresenceItem", OnOffType.ON))
        Event event = null
        def eventHandler = [
            receive: { Event e ->
                logger.info("Event: " + e.topic)
                event=e
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(ItemCommandEvent.TYPE, ItemStateEvent.TYPE)
            },

            getEventFilter:{ null }

        ] as EventSubscriber

        registerService(eventHandler)
        eventPublisher.post(ItemEventFactory.createStateEvent("myMotionItem", OnOffType.ON))
        waitForAssert ({
            assertThat event, is(notNullValue())
            assertThat event.topic, is(equalTo("smarthome/items/myLampItem/command"))
        }
        , 5000, 100)
        assertThat event.topic, is(equalTo("smarthome/items/myLampItem/command"))
        assertThat (((ItemCommandEvent)event).itemCommand, is(OnOffType.ON))
    }


    @Test
    public void 'assert that item state is updated by simple rule'() {
        //Creation of RULE
        def triggerConfig = new Configuration([eventSource:"myMotionItem2", eventTopic:"smarthome/*", eventTypes:"ItemStateEvent"])
        def condition1Config = new Configuration([operator:"=", itemName:"myPresenceItem2", state:"ON"])
        def condition2Config = new Configuration([itemName:"myMotionItem2"])
        def actionConfig = new Configuration([itemName:"myLampItem2", command:"ON"])
        def triggers = [new Trigger("ItemStateChangeTrigger2", "GenericEventTrigger", triggerConfig)]
        def conditions = [new Condition("ItemStateCondition3", "ItemStateCondition", condition1Config, null), new Condition("ItemStateCondition4", "ItemStateEvent_ON_Condition", condition2Config, [event:"ItemStateChangeTrigger2.event"])]
        def actions = [new Action("ItemPostCommandAction2", "ItemPostCommandAction", actionConfig, null)]

        def rule = new Rule("myRule21"+new Random().nextInt())
        rule.triggers = triggers
        rule.conditions = conditions
        rule.actions = actions
        // I would expect the factory to create the UID of the rule and the name to be in the list of parameters.
        rule.name="RuleByJAVA_API"

        logger.info("Rule created: "+rule.getUID())

        def ruleRegistry = getService(RuleRegistry) as RuleRegistry
        ruleRegistry.add(rule)
        ruleRegistry.setEnabled(rule.UID, true)

        waitForAssert({
            assertThat ruleRegistry.getStatusInfo(rule.UID).status, is(RuleStatus.IDLE)
        })
        //TEST RULE

        def EventPublisher eventPublisher = getService(EventPublisher)
        def ItemRegistry itemRegistry = getService(ItemRegistry)
        SwitchItem myMotionItem = itemRegistry.getItem("myMotionItem2")
        SwitchItem myPresenceItem = itemRegistry.getItem("myPresenceItem2")
        myPresenceItem.send(OnOffType.ON)

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
        myMotionItem.send(OnOffType.ON)
        waitForAssert ({ assertThat itemEvent, is(notNullValue())} , 3000, 100)
        assertThat itemEvent.topic, is(equalTo("smarthome/items/myLampItem2/state"))
        assertThat (((ItemStateEvent)itemEvent).itemState, is(OnOffType.ON))
        def myLampItem2 = itemRegistry.getItem("myLampItem2")
        assertThat myLampItem2, is(notNullValue())
        logger.info("myLampItem2 State: " + myLampItem2.state)
        assertThat myLampItem2.state, is(OnOffType.ON)
    }


    @Test
    public void 'check if moduleTypes are registered'(){
        def mtr = getService(ModuleTypeRegistry) as ModuleTypeRegistry
        waitForAssert({
            assertThat mtr.get("GenericEventTrigger"), is(notNullValue())
            assertThat mtr.get("ItemStateChangeTrigger"), is(notNullValue())
            assertThat mtr.get("EventCondition"), is(notNullValue())
            assertThat mtr.get("ItemStateEventCondition"), is(notNullValue())
            assertThat mtr.get("ItemStateEvent_ON_Condition"), is(notNullValue())
            assertThat mtr.get("ItemStateEvent_OFF_Condition"), is(notNullValue())
            assertThat mtr.get(CompareConditionHandler.MODULE_TYPE), is(notNullValue())
        },3000,100)
    }

    @Test
    public void 'assert that compareCondition works'(){
        def conditionConfiguration = new Configuration([right:"ON", operator:"="])
        def inputs = [input:"someTrigger.someoutput"]
        def Condition condition = new Condition("id", "GenericCompareCondition", conditionConfiguration, inputs)
        def handler = new CompareConditionHandler(condition)

        assertThat handler.isSatisfied([input:OnOffType.ON]), is(true)
        assertThat handler.isSatisfied([input:"ON"]), is(true)
        assertThat handler.isSatisfied([input:"OFF"]), is(false)
        assertThat handler.isSatisfied([input:OnOffType.OFF]), is(false)

        condition.configuration=[right:"21", operator:"="]

        assertThat handler.isSatisfied([input:21]), is(true)
        assertThat handler.isSatisfied([input:22]), is(false)

        condition.configuration=[right:"21", operator:"<"]
        assertThat handler.isSatisfied([input:20]), is(true)
        assertThat handler.isSatisfied([input:22]), is(false)

        assertThat handler.isSatisfied([input:20l]), is(true)
        assertThat handler.isSatisfied([input:22l]), is(false)

        assertThat handler.isSatisfied([input:20.9d]), is(true)
        assertThat handler.isSatisfied([input:21.1d]), is(false)

        condition.configuration=[right:"21", operator:">"]
        assertThat handler.isSatisfied([input:20]), is(false)
        assertThat handler.isSatisfied([input:22]), is(true)

        assertThat handler.isSatisfied([input:20l]), is(false)
        assertThat handler.isSatisfied([input:22l]), is(true)

        assertThat handler.isSatisfied([input:20.9d]), is(false)
        assertThat handler.isSatisfied([input:21.1d]), is(true)

        condition.configuration=[right:".*anything.*", operator:"matches"]
        assertThat handler.isSatisfied([input:'something matches?']), is(false)
        assertThat handler.isSatisfied([input:'anything matches?']), is(true)

        assertThat handler.isSatisfied([noting:"nothing"]), is(false)

        condition.configuration=[right:"ONOFF", operator:"matches"]
        assertThat handler.isSatisfied([input:OnOffType.ON]), is(false)
        def Event event = ItemEventFactory.createStateEvent("itemName", OnOffType.OFF, "source")
        condition.configuration=[right:".*ON.*", operator:"matches", inputproperty:"itemName"]
        assertThat handler.isSatisfied([input:event]), is(false)
        condition.configuration=[right:"itemName", operator:"matches", inputproperty:"itemName"]
        assertThat handler.isSatisfied([input:event]), is(true)
        condition.configuration=[right:"null", operator:"="]
        assertThat handler.isSatisfied([input:null]), is(true)
        condition.configuration=[right:"notnull", operator:"="]
        assertThat handler.isSatisfied([input:null]), is(false)
        condition.configuration=[right:"ON", operator:"<"]
        assertThat handler.isSatisfied([input:OnOffType.ON]), is(false)

        condition.configuration=[right:"ON", operator:"<", inputproperty:"nothing"]
        assertThat handler.isSatisfied([input:event]), is(false)
        condition.configuration=[right:"ON", operator:"=", inputproperty:"nothing"]
        assertThat handler.isSatisfied([input:"ON"]), is(true)

    }

    @Test
    public void 'assert that rule is triggered by composite trigger'() {

        //Test the creation of a rule out of
        def triggerConfig = new Configuration([itemName:"myMotionItem3"])
        def condition1Config = new Configuration([operator:"=", itemName:"myPresenceItem3", state:"ON"])
        def condition2Config = new Configuration([itemName:"myMotionItem3"])
        def actionConfig = new Configuration([itemName:"myLampItem3", command:"ON"])
        def triggers = [new Trigger("ItemStateChangeTrigger3", "ItemStateChangeTrigger", triggerConfig)]
        def conditions = [new Condition("ItemStateCondition5", "ItemStateCondition", condition1Config, null), new Condition("ItemStateCondition6", "ItemStateEvent_ON_Condition", condition2Config, [event:"ItemStateChangeTrigger3.event"])]
        def actions = [new Action("ItemPostCommandAction3", "ItemPostCommandAction", actionConfig, null)]

        def rule = new Rule("myRule21"+new Random().nextInt()+ "_COMPOSITE")
        rule.triggers = triggers
        rule.conditions = conditions
        rule.actions = actions
        rule.name="RuleByJAVA_API_WithCompositeTrigger"

        logger.info("Rule created: "+rule.getUID())

        def ruleRegistry = getService(RuleRegistry)
        ruleRegistry.add(rule)

        //TEST RULE
        waitForAssert({
            assertThat ruleRegistry.getStatusInfo(rule.uid).getStatus(), is(RuleStatus.IDLE)
        })

        def ruleEvent = null
        def ruleStatusRunning = false;
        def ruleLastStatus = null

        def ruleEventHandler = [
            receive: {  Event e ->
                logger.info("RuleEvent: " + e.topic)
                ruleEvent = e
                def ruleStatusEvent = ruleEvent as RuleStatusInfoEvent
                if (ruleStatusEvent.getStatusInfo().getStatus() == RuleStatus.RUNNING) {
                    ruleStatusRunning = true
                }
                ruleLastStatus = ruleStatusEvent.getStatusInfo().getStatus();
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(RuleStatusInfoEvent.TYPE)
            },

            getEventFilter:{ null }
        ] as EventSubscriber
        registerService(ruleEventHandler)


        def EventPublisher eventPublisher = getService(EventPublisher)
        //        eventPublisher.post(ItemEventFactory.createStateEvent("myMotionItem3", OnOffType.ON))

        def ItemRegistry itemRegistry = getService(ItemRegistry)

        SwitchItem myPresenceItem3 = itemRegistry.getItem("myPresenceItem3")
        Command commandObjPresence = TypeParser.parseCommand(myPresenceItem3.getAcceptedCommandTypes(), "ON")
        eventPublisher.post(ItemEventFactory.createCommandEvent("myPresenceItem3", commandObjPresence))


        SwitchItem myMotionItem = itemRegistry.getItem("myMotionItem3")
        Command commandObjMotion = TypeParser.parseCommand(myMotionItem.getAcceptedCommandTypes(), "ON")
        eventPublisher.post(ItemEventFactory.createCommandEvent("myMotionItem3", commandObjMotion))


        waitForAssert({
            assertThat ruleEvent, is(notNullValue())
            assertThat ruleEvent, is(instanceOf(RuleStatusInfoEvent))
            assertTrue ruleStatusRunning
        })

        waitForAssert({
            assertEquals ruleLastStatus.getValue(), RuleStatus.IDLE.getValue()
        })

    }

    @Test @Ignore
    public void 'assert that RuleEnableHandlerWorks'() {
        def ruleRegistry = getService(RuleRegistry)
        def firstRuleUID = "FirstTestRule"
        def secondRuleUID = "SecondTestRule"
        def thirdRuleUID = "ThirdTestRule"
        def firstConfig = ["FirstTestRule", "SecondTestRule"]
        def secondConfig = ["FirstTestRule"]

        def firstRuleAction = "firstRuleAction"
        def secondRuleAction = "secondRuleAction"

        try {
            def triggerConfig = new Configuration([itemName:"myMotionItem3"])
            def actionConfig = new Configuration([enable:false, ruleUIDs:firstConfig])
            def triggers = [new Trigger("ItemStateChangeTrigger3", "ItemStateChangeTrigger", triggerConfig)]
            def actions = [new Action("RuleAction", "RuleEnablementAction", actionConfig, null)]
            def rule = new Rule(firstRuleAction)
            rule.triggers = triggers
            rule.actions = actions

            ruleRegistry.add(new Rule(firstRuleUID))
            ruleRegistry.add(new Rule(secondRuleUID))
            ruleRegistry.add(new Rule(thirdRuleUID))
            ruleRegistry.add(rule)

            def ItemRegistry itemRegistry = getService(ItemRegistry)
            def EventPublisher eventPublisher = getService(EventPublisher)
            SwitchItem myMotionItem = itemRegistry.getItem("myMotionItem3")
            Command commandObjMotion = TypeParser.parseCommand(myMotionItem.getAcceptedCommandTypes(), "ON")
            eventPublisher.post(ItemEventFactory.createCommandEvent("myMotionItem3", commandObjMotion))


            waitForAssert({
                assertThat ruleRegistry.getStatus(firstRuleUID), is(RuleStatus.DISABLED)
                assertThat ruleRegistry.getStatus(secondRuleUID), is(RuleStatus.DISABLED)
                assertThat ruleRegistry.getStatus(thirdRuleUID), is(RuleStatus.IDLE)
            },1000,100)

            triggerConfig = new Configuration([itemName:"myMotionItem3"])
            actionConfig = new Configuration([enable:true, ruleUIDs:secondConfig])
            triggers = [new Trigger("ItemStateChangeTrigger3", "ItemStateChangeTrigger", triggerConfig)]
            actions = [new Action("RuleAction", "RuleEnablementAction", actionConfig, null)]
            rule = new Rule(secondRuleAction)
            rule.triggers = triggers
            rule.actions = actions
            ruleRegistry.add(rule)

            myMotionItem = itemRegistry.getItem("myMotionItem3")
            commandObjMotion = TypeParser.parseCommand(myMotionItem.getAcceptedCommandTypes(), "OFF")
            eventPublisher.post(ItemEventFactory.createCommandEvent("myMotionItem3", commandObjMotion))

            waitForAssert({
                assertThat ruleRegistry.getStatus(firstRuleUID), is(RuleStatus.IDLE)
                assertThat ruleRegistry.getStatus(secondRuleUID), is(RuleStatus.DISABLED)
                assertThat ruleRegistry.getStatus(thirdRuleUID), is(RuleStatus.IDLE)

            },1000,100)

        } finally {
            ruleRegistry.remove(firstRuleUID)
            ruleRegistry.remove(secondRuleUID)
            ruleRegistry.remove(thirdRuleUID)
            ruleRegistry.remove(firstRuleAction)
            ruleRegistry.remove(secondRuleAction)
        }
    }

}