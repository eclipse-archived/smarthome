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
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests for {@link ManagedItemChannelLinkProvider}.
 *
 * @author Dennis Nobel - Initial contribution
 */
class ItemChannelLinkOSGiTest extends OSGiTest {

    ChannelUID CHANNEL_UID = new ChannelUID("binding:typeId:thingId:channelId")
    ItemChannelLink ITEM_CHANNEL_LINK = new ItemChannelLink("item", CHANNEL_UID)

    ManagedItemChannelLinkProvider managedItemChannelLinkProvider
    ItemChannelLinkRegistry itemChannelLinkRegistry
    ManagedThingProvider managedThingProvider
    ThingLinkManager thingLinkManager

    @Before
    void setup() {
        registerVolatileStorageService()
        thingLinkManager = getService ThingLinkManager
        thingLinkManager.deactivate()
        managedThingProvider = getService ManagedThingProvider
        managedThingProvider.add(ThingBuilder.create(CHANNEL_UID.getThingUID()).withChannels([
            ChannelBuilder.create(CHANNEL_UID, "Color").build()
        ]).build())
        itemChannelLinkRegistry = getService ItemChannelLinkRegistry
        managedItemChannelLinkProvider = getService ManagedItemChannelLinkProvider
        assertThat managedItemChannelLinkProvider, is(notNullValue())
    }

    @After
    void teardown() {
        managedItemChannelLinkProvider.getAll().each { managedItemChannelLinkProvider.remove(it.getID()) }
        managedThingProvider.getAll().each { managedThingProvider.remove(it.getUID()) }
        thingLinkManager.activate(null)
    }

    @Test
    void 'assert ItemChannelLink is present in ItemChannelLinkRegistry when added to ManagedItemChannelLinkProvider'() {
        assertThat itemChannelLinkRegistry.getAll().size(), is(0)
        assertThat managedItemChannelLinkProvider.getAll().size(), is(0)

        managedItemChannelLinkProvider.add ITEM_CHANNEL_LINK

        assertThat  itemChannelLinkRegistry.getAll().size(), is(1)
        assertThat  managedItemChannelLinkProvider.getAll().size(), is(1)

        managedItemChannelLinkProvider.remove(ITEM_CHANNEL_LINK.getID())

        assertThat itemChannelLinkRegistry.getAll().size(), is(0)
        assertThat managedItemChannelLinkProvider.getAll().size(), is(0)
    }

    @Test
    void 'assert isLinked returns true'() {
        managedItemChannelLinkProvider.add ITEM_CHANNEL_LINK
        assertThat  itemChannelLinkRegistry.isLinked("item", CHANNEL_UID), is(true)
    }

    @Test
    void 'assert isLinked returns false'() {
        assertThat  itemChannelLinkRegistry.isLinked("item", CHANNEL_UID), is(false)
    }

    @Test
    void 'assert getBoundChannels returns channel'() {
        managedItemChannelLinkProvider.add ITEM_CHANNEL_LINK
        def boundChannels = itemChannelLinkRegistry.getBoundChannels("item")
        assertThat boundChannels.size(), is(1)
        assertThat boundChannels.first(), is(equalTo(ITEM_CHANNEL_LINK.getUID()))
    }

    @Test
    void 'assert getBoundChannels returns empty set'() {
        def boundThings = itemChannelLinkRegistry.getBoundChannels("notExistingItem")
        assertThat boundThings.isEmpty(), is(true)
    }

    @Test
    void 'assert getBoundThings returns thing'() {
        managedItemChannelLinkProvider.add ITEM_CHANNEL_LINK
        def boundThings = itemChannelLinkRegistry.getBoundThings("item")
        assertThat boundThings.size(), is(1)
        assertThat boundThings.first().UID, is(equalTo(CHANNEL_UID.getThingUID()))
    }

    @Test
    void 'assert getBoundThings returns empty set'() {
        def boundThings = itemChannelLinkRegistry.getBoundThings("notExistingItem")
        assertThat boundThings.isEmpty(), is(true)
    }
}
