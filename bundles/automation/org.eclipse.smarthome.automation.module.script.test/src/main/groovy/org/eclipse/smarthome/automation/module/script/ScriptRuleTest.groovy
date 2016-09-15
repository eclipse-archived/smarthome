/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script;


import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.automation.Action
import org.eclipse.smarthome.automation.Condition
import org.eclipse.smarthome.automation.Rule
import org.eclipse.smarthome.automation.RuleRegistry
import org.eclipse.smarthome.automation.RuleStatus
import org.eclipse.smarthome.automation.RuleStatusInfo
import org.eclipse.smarthome.automation.Trigger
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

import com.google.common.collect.Sets


/**
 * This tests the script modules
 *
 * @author Kai Kreuzer - initial contribution
 *
 */
class ScriptRuleTest extends OSGiTest {

    final Logger logger = LoggerFactory.getLogger(ScriptRuleTest.class)
    VolatileStorageService volatileStorageService = new VolatileStorageService()

    ItemCommandEvent receivedEvent

    @Before
    void before() {

        def itemProvider = [
            getAll: {
                [
                    new SwitchItem("MyTrigger"),
                    new SwitchItem("ScriptItem")
                ]
            },
            addProviderChangeListener: {},
            removeProviderChangeListener: {},
            allItemsChanged: {}] as ItemProvider
        registerService(itemProvider)
        registerService(volatileStorageService)

        enableItemAutoUpdate()

        def eventSubscriber = [
            receive: {  event ->
                receivedEvent = event
                logger.info("received event from item {}, command {}", receivedEvent.itemName, receivedEvent.itemCommand)
            },
            getSubscribedEventTypes: {
                Sets.newHashSet(ItemCommandEvent.TYPE)
            },
            getEventFilter: { null },
        ] as EventSubscriber
        registerService(eventSubscriber)
        def scriptScopeProvider = getService(ScriptScopeProvider)
        assertThat ScriptScopeProvider, is(notNullValue())
    }


    @Test
    public void testPredefinedRule() {

        def EventPublisher eventPublisher = getService(EventPublisher)
        def ItemRegistry itemRegistry = getService(ItemRegistry)
        def RuleRegistry ruleRegistry = getService(RuleRegistry)



        //WAIT until Rule modules types are parsed and the rule becomes IDLE
        waitForAssert({
            assertThat ruleRegistry.getAll().isEmpty(), is(false)
            def rule2 = ruleRegistry.get("javascript.rule1") as Rule
            assertThat rule2, is(notNullValue())
            def ruleStatus2 = ruleRegistry.getStatusInfo(rule2.uid) as RuleStatusInfo
            assertThat ruleStatus2.getStatus(), is(RuleStatus.IDLE)
        }, 10000, 200)
        def rule = ruleRegistry.get("javascript.rule1") as Rule
        assertThat rule, is(notNullValue())
        assertThat rule.name, is("DemoScriptRule")
        def trigger = rule.triggers.find{it.id.equals("trigger")} as Trigger
        assertThat trigger, is(notNullValue())
        assertThat trigger.typeUID, is("GenericEventTrigger")
        assertThat trigger.configuration.get("eventSource"), is ("MyTrigger")
        assertThat trigger.configuration.get("eventTopic"), is("smarthome/items/MyTrigger/state")
        assertThat trigger.configuration.get("eventTypes"), is("ItemStateEvent")
        def condition1 = rule.conditions.find{it.id.equals("condition")} as Condition
        assertThat condition1, is(notNullValue())
        assertThat condition1.typeUID, is("ScriptCondition")
        assertThat condition1.configuration.get("type"), is("application/javascript")
        assertThat condition1.configuration.get("script"), is("trigger.event.itemState==ON")
        def action = rule.actions.find{it.id.equals("action")} as Action
        assertThat action, is(notNullValue())
        assertThat action.typeUID, is("ScriptAction")
        assertThat action.configuration.get("type"), is("application/javascript")
        assertThat action.configuration.get("script"), is("print(items.MyTrigger), print(things.getAll()), print(trigger.event), events.sendCommand('ScriptItem', 'ON')")
        def ruleStatus = ruleRegistry.getStatusInfo(rule.uid) as RuleStatusInfo
        assertThat ruleStatus.getStatus(), is(RuleStatus.IDLE)

        SwitchItem myTriggerItem = itemRegistry.getItem("MyTrigger")
        logger.info("Triggering item: {}", myTriggerItem.name)
        eventPublisher.post(ItemEventFactory.createStateEvent("MyTrigger", OnOffType.ON))

        waitForAssert {
            assertThat receivedEvent, not(null)
        }
        assert receivedEvent.itemName, is(equalTo("ScriptItem"))
        assert receivedEvent.itemCommand, is(OnOffType.ON)
    }
}