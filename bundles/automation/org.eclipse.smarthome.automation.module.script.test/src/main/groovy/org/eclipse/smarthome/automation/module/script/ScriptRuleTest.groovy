/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script;


import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

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
            receive: { event -> receivedEvent = event },
            getSubscribedEventTypes: {
                Sets.newHashSet(ItemCommandEvent.TYPE)
            },
            getEventFilter: { null },
        ] as EventSubscriber
        registerService(eventSubscriber)
    }


    @Test
    public void testPredefinedRule() {

        def EventPublisher eventPublisher = getService(EventPublisher)
        def ItemRegistry itemRegistry = getService(ItemRegistry)
        SwitchItem myTriggerItem = itemRegistry.getItem("MyTrigger")
        eventPublisher.post(ItemEventFactory.createStateEvent("MyTrigger", OnOffType.ON))

        waitForAssert {
            assertThat receivedEvent, not(null)
        }
        assert receivedEvent.itemName, is(equalTo("ScriptItem"))
        assert receivedEvent.itemCommand, is(OnOffType.ON)
    }
}