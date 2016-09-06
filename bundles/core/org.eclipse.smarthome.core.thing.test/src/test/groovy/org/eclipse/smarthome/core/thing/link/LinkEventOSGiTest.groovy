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
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.library.items.StringItem
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

    ItemRegistry itemRegistry
    ItemChannelLinkRegistry itemChannelLinkRegistry
    Event lastReceivedEvent = null

    @Before
    void setup() {
        registerVolatileStorageService()

        itemRegistry = getService(ItemRegistry)
        assertNotNull itemRegistry

        itemChannelLinkRegistry = getService(ItemChannelLinkRegistry)
        assertNotNull itemChannelLinkRegistry

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
    void 'assert item channel link events are sent item first'() {
        final String itemName = "item1"
        final String channelName = "a:b:c:d"

        // Create item
        Item item = new StringItem(itemName)
        itemRegistry.add(item)

        // Create link
        def link = new ItemChannelLink(itemName, new ChannelUID(channelName))
        itemChannelLinkRegistry.add(link)

        waitFor { lastReceivedEvent != null }
        waitForAssert { assertThat lastReceivedEvent.type, is(ItemChannelLinkAddedEvent.TYPE) }
        assertThat lastReceivedEvent.topic, is("smarthome/links/" + itemName + "-" + channelName + "/added")

        itemChannelLinkRegistry.remove(link.ID)
        waitForAssert { assertThat lastReceivedEvent.type, is(ItemChannelLinkRemovedEvent.TYPE) }
        assertThat lastReceivedEvent.topic, is("smarthome/links/" + itemName + "-" + channelName + "/removed")
    }


    @Test
    void 'assert item channel link events are sent link first'() {
        final String itemName = "item2"
        final String channelName = "b:c:d:e"

        // Create link
        def link = new ItemChannelLink(itemName, new ChannelUID(channelName))
        itemChannelLinkRegistry.add(link)

        // Create item
        Item item = new StringItem(itemName)
        itemRegistry.add(item)

        waitFor { lastReceivedEvent != null }
        waitForAssert { assertThat lastReceivedEvent.type, is(ItemChannelLinkAddedEvent.TYPE) }
        assertThat lastReceivedEvent.topic, is("smarthome/links/" + itemName + "-" + channelName + "/added")

        itemChannelLinkRegistry.remove(link.ID)
        waitForAssert { assertThat lastReceivedEvent.type, is(ItemChannelLinkRemovedEvent.TYPE) }
        assertThat lastReceivedEvent.topic, is("smarthome/links/" + itemName + "-" + channelName + "/removed")
    }
}

