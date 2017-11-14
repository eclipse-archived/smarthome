/**
 * Copyright (c) 2017 Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.timer.internal;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import java.text.SimpleDateFormat

import org.eclipse.smarthome.automation.Action
import org.eclipse.smarthome.automation.Condition
import org.eclipse.smarthome.automation.Rule
import org.eclipse.smarthome.automation.RuleRegistry
import org.eclipse.smarthome.automation.RuleStatus
import org.eclipse.smarthome.automation.RuleStatusInfo
import org.eclipse.smarthome.automation.Trigger
import org.eclipse.smarthome.automation.module.core.handler.ItemCommandActionHandler
import org.eclipse.smarthome.automation.module.core.handler.ItemStateTriggerHandler
import org.eclipse.smarthome.automation.module.timer.handler.DayOfWeekConditionHandler
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.events.ItemCommandEvent
import org.eclipse.smarthome.core.items.events.ItemEventFactory
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.Before
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * this tests the dayOfWeek Condition
 *
 * @author Kai Kreuzer - initial contribution
 *
 */
class DayOfWeekConditionHandlerTest extends OSGiTest{

    final Logger logger = LoggerFactory.getLogger(DayOfWeekConditionHandlerTest.class)
    VolatileStorageService volatileStorageService = new VolatileStorageService()
    def RuleRegistry ruleRegistry
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.ENGLISH);
    String dayOfWeek = sdf.format(cal.getTime()).toUpperCase();

    @Before
    void before() {
        logger.info("Today is {}", dayOfWeek)

        def itemProvider = [
            getAll: {
                [
                    new SwitchItem("TriggeredItem"),
                    new SwitchItem("SwitchedItem")
                ]
            },
            addProviderChangeListener: {},
            removeProviderChangeListener: {},
            allItemsChanged: {}] as ItemProvider
        registerService(itemProvider)
        registerService(volatileStorageService)
        waitForAssert({
            ruleRegistry = getService(RuleRegistry) as RuleRegistry
            assertThat ruleRegistry, is(notNullValue())
        }, 3000, 100)
    }

    @Test
    public void 'assert that condition works'() {
        def conditionConfiguration = new Configuration([days:[
                "MON",
                "TUE",
                "WED",
                "THU",
                "FRI",
                "SAT",
                "SUN"
            ]])
        def Condition condition = new Condition("id", DayOfWeekConditionHandler.MODULE_TYPE_ID, conditionConfiguration, null)
        def handler = new DayOfWeekConditionHandler(condition)

        assertThat handler.isSatisfied(null), is(true)

        condition.configuration=[days:[]]
        handler = new DayOfWeekConditionHandler(condition)
        assertThat handler.isSatisfied(null), is(false)

        condition.configuration=[days:[dayOfWeek]]
        handler = new DayOfWeekConditionHandler(condition)
        assertThat handler.isSatisfied(null), is(true)
    }

    @Test
    public void 'check if moduleType is registered'() {
        def mtr = getService(ModuleTypeRegistry) as ModuleTypeRegistry
        waitForAssert({
            assertThat mtr.get(DayOfWeekConditionHandler.MODULE_TYPE_ID), is(notNullValue())
        },3000,100)
    }

    @Test
    public void 'assert that condition works in rule'() {
        def testItemName1 = "TriggeredItem"
        def testItemName2 = "SwitchedItem"

        def ItemRegistry itemRegistry = getService(ItemRegistry)
        def SwitchItem triggeredItem = itemRegistry.getItem(testItemName1)
        def SwitchItem switchedItem = itemRegistry.getItem(testItemName2)

        /*
         * Create Rule
         */
        logger.info("Create rule");
        def triggerConfig = new Configuration([itemName:testItemName1])
        def triggers = [
            new Trigger("MyTrigger", ItemStateTriggerHandler.UPDATE_MODULE_TYPE_ID, triggerConfig)
        ]

        def conditionConfig = new Configuration([days:[dayOfWeek]])
        def conditions = [
            new Condition("MyDOWCondition", DayOfWeekConditionHandler.MODULE_TYPE_ID, conditionConfig, null)
        ]

        def actionConfig = new Configuration([itemName:testItemName2, command:"ON"])
        def actions = [
            new Action("MyItemPostCommandAction", ItemCommandActionHandler.ITEM_COMMAND_ACTION, actionConfig, null)
        ]

        // prepare the execution
        def EventPublisher eventPublisher = getService(EventPublisher)

        ItemCommandEvent itemEvent = null
        def itemEventHandler = [
            receive: {  Event e ->
                logger.info("Event: " + e.topic)
                if (e.topic.contains(testItemName2)){
                    itemEvent=e
                }
            },

            getSubscribedEventTypes: {
                Collections.singleton(ItemCommandEvent.TYPE)
            },

            getEventFilter:{ null }
        ] as EventSubscriber
        registerService(itemEventHandler)

        def rule = new Rule("MyRule"+new Random().nextInt())
        rule.triggers = triggers
        rule.conditions = conditions
        rule.actions = actions
        rule.name="MyDOWConditionTestRule"
        logger.info("Rule created: "+rule.getUID())

        logger.info("Add rule");
        ruleRegistry.add(rule)
        logger.info("Rule added");

        logger.info("Enable rule and wait for idle status")
        ruleRegistry.setEnabled(rule.UID, true)
        waitForAssert({
            final RuleStatusInfo ruleStatus = ruleRegistry.getStatusInfo(rule.UID)
            assertThat ruleStatus.status, is(RuleStatus.IDLE)
        })
        logger.info("Rule is enabled and idle")

        logger.info("Send and wait for item state is ON")
        eventPublisher.post(ItemEventFactory.createStateEvent(testItemName1, OnOffType.ON))
        waitForAssert({
            assertThat itemEvent, is(notNullValue())
            assertThat itemEvent.itemCommand, is(OnOffType.ON)
        })
        logger.info("item state is ON")

        // now make the condition fail
        rule.conditions[0].configuration = new Configuration([days:[]])
        ruleRegistry.update(rule)

        // prepare the execution
        itemEvent = null
        eventPublisher.post(ItemEventFactory.createStateEvent(testItemName1, OnOffType.ON))
        sleep(1000)
        assertThat itemEvent, is(nullValue())
    }
}
