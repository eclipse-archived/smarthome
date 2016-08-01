/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items.events;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventFactory
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.events.ItemEventFactory.ItemEventPayloadBean
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test

import com.google.common.collect.Sets
import com.google.gson.Gson

/**
 * The {@link AbstractItemEventSubscriberOSGiTest} runs inside an OSGi container and tests the {@link AbstractItemEventSubscriber}.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
class AbstractItemEventSubscriberOSGiTest extends OSGiTest {

    def ITEM_NAME = "SomeItem"
    EventPublisher eventPublisher
    ItemProvider itemProvider
    ItemCommandEvent commandEvent
    ItemStateEvent updateEvent

    @Before
    public void setup() {
        commandEvent = null
        updateEvent = null
        eventPublisher = getService(EventPublisher)
        
        itemProvider = [
            getAll: {[new SwitchItem(ITEM_NAME)]},
            addProviderChangeListener: {},
            removeProviderChangeListener: {},
            allItemsChanged: {}
        ] as ItemProvider
        registerService(itemProvider)
        
        def itemEventSubscriber = [
            receiveCommand: { event -> commandEvent = event },
            receiveUpdate: { event -> updateEvent = event },
        ] as AbstractItemEventSubscriber
        registerService(itemEventSubscriber, EventSubscriber.class.getName())
    }

    @Test
    public void 'AbstractItemEventSubscriber receives ItemCommandEvents and ItemUpdateEvents correctly'() {
        eventPublisher.post(ItemEventFactory.createCommandEvent(ITEM_NAME, OnOffType.ON))
        waitForAssert { assertThat commandEvent, not(null)}
        waitForAssert { assertThat updateEvent, is(null)}
        
        commandEvent = null
        updateEvent = null
        eventPublisher.post(ItemEventFactory.createStateEvent(ITEM_NAME, OnOffType.ON))
        waitForAssert { assertThat commandEvent, is(null)}
        waitForAssert { assertThat updateEvent, not(null)}
    }
    
    @Test
    public void 'AbstractItemEventSubscriber do not receive events if event type is not supported'() {
        def someEventType = "SOME_EVENT_TYPE"
        def someEventFactory = [
            createEvent: { eventType, topic, payload, source ->
                [ getType: {eventType}, getTopic: {topic}, getPayload: {payload}, getSource: {source} ] as Event
            },
            getSupportedEventTypes: { Sets.newHashSet(someEventType) }
        ] as EventFactory
        registerService(someEventFactory)

        Event event = [ getType: { someEventType }, getPayload: { "{a: 'A', b: 'B'}" }, getTopic: { "smarthome/items" }, getSource: { null } ] as Event
        
        eventPublisher.post(event)
        waitForAssert { assertThat commandEvent, is(null)}
        waitForAssert { assertThat updateEvent, is(null)}
    }
    
}
