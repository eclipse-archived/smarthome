/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.inbox.events;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.discovery.dto.DiscoveryResultDTOMapper
import org.eclipse.smarthome.config.discovery.internal.DiscoveryResultImpl
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.thing.ThingUID
import org.junit.Test

import com.google.gson.Gson

/**
 * {@link InboxEventFactoryTests} tests the {@link InboxEventFactory}.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
class InboxEventFactoryTest {
    InboxEventFactory factory = new InboxEventFactory()

    def THING_UID = new ThingUID("binding:type:id")

    def DISCOVERY_RESULT = new DiscoveryResultImpl(THING_UID, null, null, null, null, 60)

    def INBOX_ADDED_EVENT_TYPE = InboxAddedEvent.TYPE

    def INBOX_ADDED_EVENT_TOPIC = InboxEventFactory.INBOX_ADDED_EVENT_TOPIC.replace("{thingUID}", THING_UID.getAsString())

    def INBOX_ADDED_EVENT_PAYLOAD = new Gson().toJson(DiscoveryResultDTOMapper.map(DISCOVERY_RESULT))


    @Test
    void 'InboxEventFactory creates Event as InboxAddedEvent correctly'() {
        Event event = factory.createEvent(INBOX_ADDED_EVENT_TYPE, INBOX_ADDED_EVENT_TOPIC, INBOX_ADDED_EVENT_PAYLOAD, null)

        assertThat event, is(instanceOf(InboxAddedEvent))
        InboxAddedEvent inboxAddedEvent = event as InboxAddedEvent
        assertThat inboxAddedEvent.getType(), is(INBOX_ADDED_EVENT_TYPE)
        assertThat inboxAddedEvent.getTopic(), is(INBOX_ADDED_EVENT_TOPIC)
        assertThat inboxAddedEvent.getPayload(), is(INBOX_ADDED_EVENT_PAYLOAD)
        assertThat inboxAddedEvent.getDiscoveryResult(), not(null)
        assertThat inboxAddedEvent.getDiscoveryResult().thingUID, is(THING_UID.getAsString())
    }

    @Test
    void 'InboxEventFactory creates InboxAddedEvent correctly'() {
        InboxAddedEvent event = InboxEventFactory.createAddedEvent(DISCOVERY_RESULT)

        assertThat event.getType(), is(INBOX_ADDED_EVENT_TYPE)
        assertThat event.getTopic(), is(INBOX_ADDED_EVENT_TOPIC)
        assertThat event.getPayload(), is(INBOX_ADDED_EVENT_PAYLOAD)
        assertThat event.getDiscoveryResult(), not(null)
        assertThat event.getDiscoveryResult().thingUID, is(THING_UID.getAsString())
    }
}
