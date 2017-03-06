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

import org.eclipse.smarthome.automation.RuleRegistry
import org.eclipse.smarthome.automation.module.timer.handler.TimeOfDayTriggerHandler
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.Before
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * this tests the timeOfDay trigger
 *
 * @author Kai Kreuzer - initial contribution
 *
 */
class TimeOfDayTriggerHandlerTest extends OSGiTest{

    final Logger logger = LoggerFactory.getLogger(RuntimeRuleTest.class)
    VolatileStorageService volatileStorageService = new VolatileStorageService()
    def RuleRegistry ruleRegistry

    @Before
    void before() {
        registerService(volatileStorageService)
        waitForAssert({
            ruleRegistry = getService(RuleRegistry) as RuleRegistry
            assertThat ruleRegistry, is(notNullValue())
        }, 3000, 100)
    }


    @Test
    public void 'check if moduleType is registered'() {
        def mtr = getService(ModuleTypeRegistry) as ModuleTypeRegistry
        waitForAssert({
            assertThat mtr.get(TimeOfDayTriggerHandler.MODULE_TYPE_ID), is(notNullValue())
        },3000,100)
    }
}
