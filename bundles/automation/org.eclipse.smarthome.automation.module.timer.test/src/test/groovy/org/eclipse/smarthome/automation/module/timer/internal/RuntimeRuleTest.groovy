/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.timer.internal;

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
import org.eclipse.smarthome.automation.module.timer.handler.TimerTriggerHandler
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.Before
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * this tests the Timer Trigger
 *
 * @author Christoph Knauf - initial contribution
 * @author Markus Rathgeb - fix module timer test
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
    public void 'check disable and enable of timer triggered rule'() {
        /*
         * Create Rule
         */
        logger.info("Create rule");
        def testExpression = "* * * * * ?"

        def triggerConfig = new Configuration([cronExpression:testExpression])
        def triggers = [new Trigger("MyTimerTrigger", "TimerTrigger", triggerConfig)]

        def rule = new Rule("MyRule"+new Random().nextInt())
        rule.triggers = triggers

        rule.name = "MyTimerTriggerTestEnableDisableRule"
        logger.info("Rule created: " + rule.getUID())

        logger.info("Add rule");
        ruleRegistry.add(rule)
        logger.info("Rule added");

        def numberOfTests = 1000
        for (int i=0; i < numberOfTests; ++i) {
            logger.info("Disable rule");
            ruleRegistry.setEnabled(rule.UID, false)
            waitForAssert({
                final RuleStatusInfo ruleStatus = ruleRegistry.getStatusInfo(rule.UID)
                println "Rule status (should be DISABLED): " + ruleStatus
                assertThat ruleStatus.status, is(RuleStatus.DISABLED)
            })
            logger.info("Rule is disabled");

            logger.info("Enable rule");
            ruleRegistry.setEnabled(rule.UID, true)
            waitForAssert({
                final RuleStatusInfo ruleStatus = ruleRegistry.getStatusInfo(rule.UID)
                println "Rule status (should be IDLE or RUNNING): " + ruleStatus
                boolean allFine
                if (ruleStatus.status.equals(RuleStatus.IDLE) || ruleStatus.status.equals(RuleStatus.RUNNING)) {
                    allFine = true
                } else {
                    allFine = false
                }
                assertThat allFine, is(true)
            })
            logger.info("Rule is enabled");
        }
    }

    @Test
    public void 'assert that timerTrigger works'(){
        def testItemName = "myLampItem"

        def ItemRegistry itemRegistry = getService(ItemRegistry)
        def SwitchItem lampItem = itemRegistry.getItem(testItemName)

        /*
         * Check if auto update is working and
         * ensure that the lamp item state if OFF after this check
         */
        logger.info("Check auto update");
        for (state in [OnOffType.OFF, OnOffType.ON, OnOffType.OFF]) {
            lampItem.send(state);
            waitForAssert({
                assertThat lampItem.state,is(state);
            })
        }
        logger.info("Auto update works");

        /*
         * Create Rule
         */
        logger.info("Create rule");
        def testExpression = "* * * * * ?"

        def triggerConfig = new Configuration([cronExpression:testExpression])
        def triggers = [new Trigger("MyTimerTrigger", "TimerTrigger", triggerConfig)]

        def actionConfig = new Configuration([itemName:testItemName, command:"ON"])
        def actions = [new Action("MyItemPostCommandAction", "ItemPostCommandAction", actionConfig, null)]

        def conditionConfig = new Configuration([operator:"=", itemName:testItemName, state:"OFF"])
        def conditions = [new Condition("MyItemStateCondition", "ItemStateCondition", conditionConfig, null)]

        def rule = new Rule("MyRule"+new Random().nextInt())
        rule.triggers = triggers
        rule.conditions =  conditions
        rule.actions = actions
        rule.name="MyTimerTriggerTestRule"
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

        def numberOfTests = 3
        for (int i=0; i < numberOfTests;i++) {
            logger.info("Disable rule");
            ruleRegistry.setEnabled(rule.UID, false)
            waitForAssert({
                final RuleStatusInfo ruleStatus = ruleRegistry.getStatusInfo(rule.UID)
                assertThat ruleStatus.status, is(RuleStatus.DISABLED)
            })
            logger.info("Rule is disabled");

            logger.info("Try to set lamp item state to OFF")
            lampItem.send(OnOffType.OFF);
            waitForAssert({
                assertThat lampItem.state,is(OnOffType.OFF)
            })
            logger.info("Lamp item state is OFF")

            logger.info("Enable rule and wait for lamp item state is ON")
            ruleRegistry.setEnabled(rule.UID, true)
            waitForAssert({
                assertThat lampItem.state,is(OnOffType.ON)
            })
            logger.info("lamp item state is ON")
        }
    }
}
