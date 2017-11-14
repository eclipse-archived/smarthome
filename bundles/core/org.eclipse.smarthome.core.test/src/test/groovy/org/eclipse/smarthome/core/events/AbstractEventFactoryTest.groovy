/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.events;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.items.events.ItemCommandEvent
import org.eclipse.smarthome.core.items.events.ItemEventFactory
import org.junit.Test

/**
 * {@link AbstractEventFactoryTests} tests the {@link AbstractEventFactory}.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
class AbstractEventFactoryTest {
    ItemEventFactory factory = new ItemEventFactory()

    def ITEM_NAME = "ItemA"
    def SOURCE = "binding:type:id:channel"
    def EVENT_TYPE = "SOME_EVENT_TYPE"
    def EVENT_TOPIC = "smarthome/some/topic"
    def EVENT_PAYLOAD = "{\"some\":\"payload\"}"

    @Test
    public void 'AbstractEventFactory throws exception for not supported event types' () {
        try {
            factory.createEvent("SOME_NOT_SUPPORTED_TYPE", EVENT_TOPIC, EVENT_PAYLOAD, SOURCE)
            fail("IllegalArgumentException expected!")
        } catch(IllegalArgumentException e) {
            assertThat e.getMessage(), is("The event type 'SOME_NOT_SUPPORTED_TYPE' is not supported by this factory.")
        }
    }

    @Test
    public void 'AbstractEventFactory validates arguments'() {
        try {
            factory.createEvent("", EVENT_TOPIC, EVENT_PAYLOAD, null)
            fail("IllegalArgumentException expected!")
        } catch(IllegalArgumentException e) {
            assertThat e.getMessage(), is("The argument 'eventType' must not be null or empty.")
        }
        try {
            factory.createEvent(EVENT_TYPE, "", EVENT_PAYLOAD, null)
            fail("IllegalArgumentException expected!")
        } catch(IllegalArgumentException e) {
            assertThat e.getMessage(), is("The argument 'topic' must not be null or empty.")
        }
        try {
            factory.createEvent(EVENT_TYPE, EVENT_TOPIC, "", null)
            fail("IllegalArgumentException expected!")
        } catch(IllegalArgumentException e) {
            assertThat e.getMessage(), is("The argument 'payload' must not be null or empty.")
        }
    }
}
