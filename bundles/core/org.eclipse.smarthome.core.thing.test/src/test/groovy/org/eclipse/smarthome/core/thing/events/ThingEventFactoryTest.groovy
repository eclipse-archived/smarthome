/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.events;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingStatusDetail
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder
import org.eclipse.smarthome.core.thing.dto.ThingDTOMapper
import org.eclipse.smarthome.core.thing.events.ThingEventFactory.TriggerEventPayloadBean
import org.eclipse.smarthome.core.thing.internal.ThingImpl
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Test
import com.google.gson.Gson

/**
 * {@link ThingEventFactoryTests} tests the {@link ThingEventFactory}.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
class ThingEventFactoryTest extends OSGiTest {
    def THING_STATUS_INFO = ThingStatusInfoBuilder
    .create(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR)
    .withDescription("Some description").build();

    ThingEventFactory factory = new ThingEventFactory()

    def THING_UID = new ThingUID("binding:type:id")
    def THING = new ThingImpl(THING_UID)

    def THING_STATUS_EVENT_TYPE = ThingStatusInfoEvent.TYPE
    def THING_ADDED_EVENT_TYPE = ThingAddedEvent.TYPE
    def CHANNEL_TRIGGERED_EVENT_TYPE = ChannelTriggeredEvent.TYPE

    def THING_STATUS_EVENT_TOPIC = ThingEventFactory.THING_STATUS_INFO_EVENT_TOPIC.replace("{thingUID}", THING_UID.getAsString())
    def THING_ADDED_EVENT_TOPIC = ThingEventFactory.THING_ADDED_EVENT_TOPIC.replace("{thingUID}", THING_UID.getAsString())

    def THING_STATUS_EVENT_PAYLOAD = new Gson().toJson(THING_STATUS_INFO)
    def THING_ADDED_EVENT_PAYLOAD = new Gson().toJson(ThingDTOMapper.map(THING))

    def CHANNEL_UID = new ChannelUID(THING_UID, "channel")
    def CHANNEL_TRIGGERED_EVENT_TOPIC = ThingEventFactory.CHANNEL_TRIGGERED_EVENT_TOPIC.replace("{channelUID}", CHANNEL_UID.getAsString())
    def TRIGGER_EVENT = "PRESSED"
    def CHANNEL_TRIGGERED_EVENT_PAYLOAD = new Gson().toJson(new TriggerEventPayloadBean(TRIGGER_EVENT, CHANNEL_UID.getAsString()))

    @Test
    void 'ThingEventFactory creates Event as ThingStatusInfoEvent correctly'() {
        Event event = factory.createEvent(THING_STATUS_EVENT_TYPE, THING_STATUS_EVENT_TOPIC, THING_STATUS_EVENT_PAYLOAD, null)

        assertThat event, is(instanceOf(ThingStatusInfoEvent))
        ThingStatusInfoEvent statusEvent = event as ThingStatusInfoEvent
        assertThat statusEvent.getType(), is(THING_STATUS_EVENT_TYPE)
        assertThat statusEvent.getTopic(), is(THING_STATUS_EVENT_TOPIC)
        assertThat statusEvent.getPayload(), is(THING_STATUS_EVENT_PAYLOAD)
        assertThat statusEvent.getStatusInfo(), is(THING_STATUS_INFO)
        assertThat statusEvent.getThingUID(), is(THING_UID)
    }

    @Test
    void 'ThingEventFactory creates ThingStatusInfoEvent correctly'() {
        ThingStatusInfoEvent event = ThingEventFactory.createStatusInfoEvent(THING_UID, THING_STATUS_INFO)

        assertThat event.getType(), is(THING_STATUS_EVENT_TYPE)
        assertThat event.getTopic(), is(THING_STATUS_EVENT_TOPIC)
        assertThat event.getPayload(), is(THING_STATUS_EVENT_PAYLOAD)
        assertThat event.getStatusInfo(), is(THING_STATUS_INFO)
        assertThat event.getThingUID(), is(THING_UID)
    }

    @Test
    void 'ThingEventFactory creates Event as ThingAddedEvent correctly'() {
        Event event = factory.createEvent(THING_ADDED_EVENT_TYPE, THING_ADDED_EVENT_TOPIC, THING_ADDED_EVENT_PAYLOAD, null)

        assertThat event, is(instanceOf(ThingAddedEvent))
        ThingAddedEvent addedEvent = event as ThingAddedEvent
        assertThat addedEvent.getType(), is(THING_ADDED_EVENT_TYPE)
        assertThat addedEvent.getTopic(), is(THING_ADDED_EVENT_TOPIC)
        assertThat addedEvent.getPayload(), is(THING_ADDED_EVENT_PAYLOAD)
        assertThat addedEvent.getThing(), not(null)
        assertThat addedEvent.getThing().UID, is(THING_UID.getAsString())
    }

    @Test
    void 'ThingEventFactory creates ThingAddedEvent correctly'() {
        ThingAddedEvent event = ThingEventFactory.createAddedEvent(THING)

        assertThat event.getType(), is(THING_ADDED_EVENT_TYPE)
        assertThat event.getTopic(), is(THING_ADDED_EVENT_TOPIC)
        assertThat event.getPayload(), is(THING_ADDED_EVENT_PAYLOAD)
        assertThat event.getThing(), not(null)
        assertThat event.getThing().UID, is(THING_UID.getAsString())
    }

    @Test
    void 'ThingEventFactory creates ChannelTriggeredEvent correctly'() {
        ChannelTriggeredEvent event = ThingEventFactory.createTriggerEvent(TRIGGER_EVENT, CHANNEL_UID)

        assertThat event.getType(), is(CHANNEL_TRIGGERED_EVENT_TYPE)
        assertThat event.getTopic(), is(CHANNEL_TRIGGERED_EVENT_TOPIC)
        assertThat event.getPayload(), is(CHANNEL_TRIGGERED_EVENT_PAYLOAD)
        assertThat event.getEvent(), not(null)
        assertThat event.getEvent(), is(TRIGGER_EVENT)
    }

    @Test
    void 'ThingEventFactory creates Event as ChannelTriggeredEvent correctly'() {
        Event event = factory.createEvent(CHANNEL_TRIGGERED_EVENT_TYPE, CHANNEL_TRIGGERED_EVENT_TOPIC, CHANNEL_TRIGGERED_EVENT_PAYLOAD, null)

        assertThat event, is(instanceOf(ChannelTriggeredEvent))
        ChannelTriggeredEvent triggeredEvent = event as ChannelTriggeredEvent
        assertThat triggeredEvent.getType(), is(CHANNEL_TRIGGERED_EVENT_TYPE)
        assertThat triggeredEvent.getTopic(), is(CHANNEL_TRIGGERED_EVENT_TOPIC)
        assertThat triggeredEvent.getPayload(), is(CHANNEL_TRIGGERED_EVENT_PAYLOAD)
        assertThat triggeredEvent.getEvent(), not(null)
        assertThat triggeredEvent.getEvent(), is(TRIGGER_EVENT)
    }
}
