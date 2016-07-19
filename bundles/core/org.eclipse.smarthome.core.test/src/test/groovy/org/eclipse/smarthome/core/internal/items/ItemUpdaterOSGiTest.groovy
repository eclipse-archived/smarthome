/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.items

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import java.util.concurrent.ConcurrentLinkedQueue

import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.events.ItemEventFactory
import org.eclipse.smarthome.core.items.events.ItemStateChangedEvent
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test

import com.google.common.collect.Sets

/**
 * The {@link ItemRegistryOSGiTest} runs inside an OSGi container and tests the {@link ItemRegistry}.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Andre Fuechsel - extended with tag tests
 * @author Kai Kreuzer - added tests for all items changed cases
 */
class ItemUpdaterOSGiTest extends OSGiTest {

    EventPublisher eventPublisher
    ItemRegistry itemRegistry
    ConcurrentLinkedQueue<Event> receivedEvents

    @Before
    void setUp() {
        registerVolatileStorageService()
        eventPublisher = getService(EventPublisher)
        itemRegistry = getService(ItemRegistry)
        itemRegistry.add(new SwitchItem("switch"))
        receivedEvents = new ConcurrentLinkedQueue<Event>()
        def eventSubscriber = [
            getSubscribedEventTypes: {
                Sets.newHashSet(ItemStateChangedEvent.TYPE)
            },
            getEventFilter: { null },
            receive: { event -> receivedEvents.add(event) }
        ] as EventSubscriber
        registerService(eventSubscriber)
    }

    @Test
    void 'assert ItemUpdater sets item state'() {
        eventPublisher.post(ItemEventFactory.createStateEvent("switch", OnOffType.ON))
        SwitchItem switchItem = itemRegistry.get("switch")
        waitForAssert {
            assertThat switchItem.state, is(OnOffType.ON)
        }
    }

    @Test
    void 'assert ItemUpdater sends state changed event'() {
        ItemStateChangedEvent changedEvent

        eventPublisher.post(ItemEventFactory.createStateEvent("switch", OnOffType.ON))

        SwitchItem switchItem = itemRegistry.get("switch")
        waitForAssert {
            assertThat switchItem.state, is(OnOffType.ON)
        }

        // change state
        eventPublisher.post(ItemEventFactory.createStateEvent("switch", OnOffType.OFF))

        // wait for an event that change the state from OFF to ON
        // there could be one remaining event from the 'ItemUpdater sets item state' test
        waitFor ({
            if (receivedEvents.isEmpty()) {
                return false;
            }

            changedEvent = (ItemStateChangedEvent)receivedEvents.poll();
            if (changedEvent == null) {
                return false;
            }

            if (changedEvent.oldItemState != OnOffType.ON) {
                return false;
            }

            if (changedEvent.itemState != OnOffType.OFF) {
                return false;
            }

            return true;
        }, 10000)

        // send update for same state
        eventPublisher.post(ItemEventFactory.createStateEvent("switch", OnOffType.OFF))

        // wait a few milliseconds
        Thread.sleep(100)

        // make sure no state changed event has been sent
        assertTrue(receivedEvents.isEmpty())
    }
}