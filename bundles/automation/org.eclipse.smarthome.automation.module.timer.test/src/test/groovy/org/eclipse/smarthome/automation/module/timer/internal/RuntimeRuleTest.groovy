/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.timer.internal;


import static org.junit.Assert.*

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.automation.Action
import org.eclipse.smarthome.automation.Condition
import org.eclipse.smarthome.automation.Rule
import org.eclipse.smarthome.automation.RuleRegistry
import org.eclipse.smarthome.automation.RuleStatus
import org.eclipse.smarthome.automation.RuleStatusInfo
import org.eclipse.smarthome.automation.Trigger
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry
import org.eclipse.smarthome.automation.events.RuleStatusInfoEvent
import org.eclipse.smarthome.automation.module.core.handler.CompareConditionHandler;
import org.eclipse.smarthome.automation.module.timer.handler.TimerTriggerHandler
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
import org.eclipse.smarthome.core.types.State;
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
 * this tests the Timer Trigger
 * 
 * @author Christoph Knauf - initial contribution
 *
 */
class RuntimeRuleTest extends OSGiTest{

    final Logger logger = LoggerFactory.getLogger(RuntimeRuleTest.class)
    VolatileStorageService volatileStorageService = new VolatileStorageService()
    def RuleRegistry ruleRegistry

    @Before
    void before() {

        def itemProvider = [
            getAll: {
                [new SwitchItem("myLampItem")]
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
        enableItemAutoUpdate()
    }

    @Test
    public void 'check if timer trigger moduleType is registered'(){
        def mtr = getService(ModuleTypeRegistry) as ModuleTypeRegistry
        waitForAssert({
            assertThat mtr.get(TimerTriggerHandler.MODULE_TYPE_ID), is(notNullValue())
        },3000,100)
    }

    @Test
    public void 'assert that timerTrigger works'(){
        def testExpression = "* * * * * ?"
        def testItemName = "myLampItem"

        def triggerConfig = [cronExpression:testExpression]
        def triggers = [new Trigger("MyTimerTrigger", "TimerTrigger", triggerConfig)]

        def actionConfig = [itemName:testItemName, command:"ON"]
        def actions = [new Action("MyItemPostCommandAction", "ItemPostCommandAction", actionConfig, null)]

        def conditionConfig = [operator:"=", itemName:testItemName, state:"OFF"]
        def conditions = [new Condition("MyItemStateCondition", "ItemStateCondition", conditionConfig, null)]

        def rule = new Rule("MyRule"+new Random().nextInt(),triggers, conditions, actions, null, null)
        rule.name="MyTimerTriggerTestRule"
        logger.info("Rule created: "+rule.getUID())

        def ItemRegistry itemRegistry = getService(ItemRegistry)
        def SwitchItem lampItem = itemRegistry.getItem(testItemName)
        lampItem.send(OnOffType.OFF);
        waitForAssert({
            assertThat lampItem.state,is(OnOffType.OFF)
        })
        
        ruleRegistry.add(rule)
        ruleRegistry.setEnabled(rule.UID, true)
        waitForAssert({
            println ruleRegistry.getStatus(rule.UID).statusDetail
            assertThat ruleRegistry.getStatus(rule.UID).status, is(RuleStatus.IDLE)
        })

        def numberOfTests = 3
        for (int i=0; i < numberOfTests;i++){
            lampItem.send(OnOffType.OFF);
            waitForAssert({
                assertThat lampItem.state,is(OnOffType.OFF)
            })
            waitForAssert({
                assertThat lampItem.state,is(OnOffType.ON)
            })
        }
    }
}