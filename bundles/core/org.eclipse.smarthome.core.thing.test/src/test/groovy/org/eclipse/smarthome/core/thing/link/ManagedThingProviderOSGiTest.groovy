/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
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
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests for {@link ManagedItemChannelLinkProvider}.
 * 
 * @author Dennis Nobel - Initial contribution
 */
class ManagedThingProviderOSGiTest extends OSGiTest {

    ChannelUID CHANNEL_UID = new ChannelUID("binding:typeId:thingId:channelId")
    ItemChannelLink ITEM_CHANNEL_LINK = new ItemChannelLink("item", CHANNEL_UID)

    ManagedItemChannelLinkProvider managedItemChannelLinkProvider
    ItemChannelLinkRegistry itemChannelLinkRegistry

    @Before
    void setup() {
        itemChannelLinkRegistry = getService ItemChannelLinkRegistry
        managedItemChannelLinkProvider = getService ManagedItemChannelLinkProvider
        assertThat managedItemChannelLinkProvider, is(notNullValue())
    }

    @After
    void teardown() {
        managedItemChannelLinkProvider.getItemChannelLinks().each {
            managedItemChannelLinkProvider.removeItemChannelLink(it.getID())
        }
    }

    @Test
    void 'assert ItemChannelLink is present in ItemChannelLinkRegistry when added to ManagedItemChannelLinkProvider'() {
        assertThat itemChannelLinkRegistry.getItemChannelLinks().size(), is(0)
        assertThat managedItemChannelLinkProvider.getItemChannelLinks().size(), is(0)

        managedItemChannelLinkProvider.addItemChannelLink ITEM_CHANNEL_LINK

        assertThat  itemChannelLinkRegistry.getItemChannelLinks().size(), is(1)
        assertThat  managedItemChannelLinkProvider.getItemChannelLinks().size(), is(1)

        managedItemChannelLinkProvider.removeItemChannelLink(ITEM_CHANNEL_LINK)

        assertThat itemChannelLinkRegistry.getItemChannelLinks().size(), is(0)
        assertThat managedItemChannelLinkProvider.getItemChannelLinks().size(), is(0)
    }
}
