/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.junit.Test

class ThingBuilderTest {

    static final ThingTypeUID THING_TYPE_UID = new ThingTypeUID("test", "test")
    static final ThingUID THING_UID = new ThingUID(THING_TYPE_UID, "test")

    @Test(expected=IllegalArgumentException)
    void 'assert that no duplicate channels can be added individually'() {
        ThingBuilder thingBuilder = ThingBuilder.create(THING_TYPE_UID, THING_UID)
        thingBuilder.withChannel(new Channel(new ChannelUID(THING_UID, "channel1"), ""))
        thingBuilder.withChannel(new Channel(new ChannelUID(THING_UID, "channel1"), ""))
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that no duplicate channels can be added as bulk'() {
        ThingBuilder thingBuilder = ThingBuilder.create(THING_TYPE_UID, THING_UID)
        thingBuilder.withChannels(
                [
                    new Channel(new ChannelUID(THING_UID, "channel1"), ""),
                    new Channel(new ChannelUID(THING_UID, "channel1"), "")
                ])
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that no duplicate channels can be added as varargs'() {
        ThingBuilder thingBuilder = ThingBuilder.create(THING_TYPE_UID, THING_UID)
        thingBuilder.withChannels(
                new Channel(new ChannelUID(THING_UID, "channel1"), ""),
                new Channel(new ChannelUID(THING_UID, "channel1"), "")
                )
    }

    @Test
    void 'assert that channel can be removed'() {
        ThingBuilder thingBuilder = ThingBuilder.create(THING_TYPE_UID, THING_UID)
        thingBuilder.withChannels(
                new Channel(new ChannelUID(THING_UID, "channel1"), ""),
                new Channel(new ChannelUID(THING_UID, "channel2"), "")
                )
        thingBuilder.withoutChannel(new ChannelUID(THING_UID, "channel1"))
        assertThat thingBuilder.build().getChannels().size(), is(equalTo(1))
        assertThat thingBuilder.build().getChannels().get(0).getUID().id, is(equalTo("channel2"))
    }

    @Test
    void 'assert that removing a missing channel fails silently'() {
        ThingBuilder thingBuilder = ThingBuilder.create(THING_TYPE_UID, THING_UID)
        thingBuilder.withChannels(
                new Channel(new ChannelUID(THING_UID, "channel1"), ""),
                new Channel(new ChannelUID(THING_UID, "channel2"), "")
                )
        thingBuilder.withoutChannel(new ChannelUID(THING_UID, "channel3"))
        assertThat thingBuilder.build().getChannels().size(), is(equalTo(2))
    }
}
