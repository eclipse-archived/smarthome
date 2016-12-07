/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.util

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder
import org.junit.Before
import org.junit.Test

/**
 * Test for the ThingHandlerHelper
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
class ThingHandlerHelperTest {

    def Thing thing
    def ThingHandler thingHandler

    @Before
    void setup() {
        thing = ThingBuilder.create(new ThingTypeUID("test:test"), new ThingUID("test:test:test")).build()
        thingHandler = [
            getThing: {thing}
        ] as ThingHandler
    }

    @Test
    void 'assert isHandlerInitialized works correctly for a ThingStatus'() {
        assertThat ThingHandlerHelper.isHandlerInitialized(ThingStatus.UNINITIALIZED), is(false)
        assertThat ThingHandlerHelper.isHandlerInitialized(ThingStatus.INITIALIZING), is(false)
        assertThat ThingHandlerHelper.isHandlerInitialized(ThingStatus.REMOVING), is(false)
        assertThat ThingHandlerHelper.isHandlerInitialized(ThingStatus.REMOVED), is(false)
        assertThat ThingHandlerHelper.isHandlerInitialized(ThingStatus.UNKNOWN), is(true)
        assertThat ThingHandlerHelper.isHandlerInitialized(ThingStatus.ONLINE), is(true)
        assertThat ThingHandlerHelper.isHandlerInitialized(ThingStatus.OFFLINE), is(true)
    }

    @Test
    void 'assert isHandlerInitialized works correctly for a Thing'() {
        thing.status = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED).build()
        assertThat ThingHandlerHelper.isHandlerInitialized(thing), is(false)

        thing.status = ThingStatusInfoBuilder.create(ThingStatus.INITIALIZING).build()
        assertThat ThingHandlerHelper.isHandlerInitialized(thing), is(false)

        thing.status = ThingStatusInfoBuilder.create(ThingStatus.REMOVING).build()
        assertThat ThingHandlerHelper.isHandlerInitialized(thing), is(false)

        thing.status = ThingStatusInfoBuilder.create(ThingStatus.REMOVED).build()
        assertThat ThingHandlerHelper.isHandlerInitialized(thing), is(false)

        thing.status = ThingStatusInfoBuilder.create(ThingStatus.UNKNOWN).build()
        assertThat ThingHandlerHelper.isHandlerInitialized(thing), is(true)

        thing.status = ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build()
        assertThat ThingHandlerHelper.isHandlerInitialized(thing), is(true)

        thing.status = ThingStatusInfoBuilder.create(ThingStatus.OFFLINE).build()
        assertThat ThingHandlerHelper.isHandlerInitialized(thing), is(true)
    }

    @Test
    void 'assert isHandlerInitialized works correctly for a ThingHandler'() {
        thing.status = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED).build()
        assertThat ThingHandlerHelper.isHandlerInitialized(thingHandler), is(false)

        thing.status = ThingStatusInfoBuilder.create(ThingStatus.INITIALIZING).build()
        assertThat ThingHandlerHelper.isHandlerInitialized(thingHandler), is(false)

        thing.status = ThingStatusInfoBuilder.create(ThingStatus.REMOVING).build()
        assertThat ThingHandlerHelper.isHandlerInitialized(thingHandler), is(false)

        thing.status = ThingStatusInfoBuilder.create(ThingStatus.REMOVED).build()
        assertThat ThingHandlerHelper.isHandlerInitialized(thingHandler), is(false)

        thing.status = ThingStatusInfoBuilder.create(ThingStatus.UNKNOWN).build()
        assertThat ThingHandlerHelper.isHandlerInitialized(thingHandler), is(true)

        thing.status = ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build()
        assertThat ThingHandlerHelper.isHandlerInitialized(thingHandler), is(true)

        thing.status = ThingStatusInfoBuilder.create(ThingStatus.OFFLINE).build()
        assertThat ThingHandlerHelper.isHandlerInitialized(thingHandler), is(true)
    }
}
