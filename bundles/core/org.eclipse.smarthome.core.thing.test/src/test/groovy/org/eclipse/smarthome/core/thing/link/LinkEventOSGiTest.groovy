/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.link.events.ItemChannelLinkAddedEvent
import org.eclipse.smarthome.core.thing.link.events.ItemChannelLinkRemovedEvent
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test

import com.google.common.collect.Sets

/**
 * Event Tests for {@link ItemChannelLinkRegistry} and {@link ItemThingLinkRegistry}.
 *
 * @author Dennis Nobel - Initial contribution
 */
class LinkEventOSGiTest extends OSGiTest {


    ItemChannelLinkRegistry itemChannelLinkRegistry
    Event lastReceivedEvent = null

    @Before
    void setup() {
        registerVolatileStorageService()
        itemChannelLinkRegistry = getService(ItemChannelLinkRegistry)
        def eventSubscriber = [
            getSubscribedEventTypes: {
                Sets.newHashSet(ItemChannelLinkAddedEvent.TYPE,
                        ItemChannelLinkRemovedEvent.TYPE)
            },
            getEventFilter: { null },
            receive: { event -> lastReceivedEvent = event }
        ] as EventSubscriber
        registerService(eventSubscriber)
    }

    @Test
    void 'assert item channel link events are sent'() {
        def link = new ItemChannelLink("item", new ChannelUID("a:b:c:d"))

        itemChannelLinkRegistry.add(link)
        waitFor { lastReceivedEvent != null }
        waitForAssert { assertThat lastReceivedEvent.type, is(ItemChannelLinkAddedEvent.TYPE) }
        assertThat lastReceivedEvent.topic, is("smarthome/links/item-a:b:c:d/added")

        itemChannelLinkRegistry.remove(link.ID)
        waitForAssert { assertThat lastReceivedEvent.type, is(ItemChannelLinkRemovedEvent.TYPE) }
        assertThat lastReceivedEvent.topic, is("smarthome/links/item-a:b:c:d/removed")
    }
}
