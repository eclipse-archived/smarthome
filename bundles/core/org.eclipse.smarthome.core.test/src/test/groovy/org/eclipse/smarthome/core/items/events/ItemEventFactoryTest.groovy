/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items.events;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.items.dto.ItemDTO
import org.eclipse.smarthome.core.items.dto.ItemDTOMapper
import org.eclipse.smarthome.core.items.events.ItemEventFactory.ItemEventPayloadBean
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.OnOffType
import org.junit.Test

import com.google.gson.Gson

/**
 * {@link ItemEventFactoryTests} tests the {@link ItemEventFactory}.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
class ItemEventFactoryTest {
    ItemEventFactory factory = new ItemEventFactory()

    def ITEM_NAME = "ItemA"
    def ITEM = new SwitchItem(ITEM_NAME)
    def SOURCE = "binding:type:id:channel"

    def ITEM_COMMAND_EVENT_TYPE = ItemCommandEvent.TYPE
    def ITEM_STATE_EVENT_TYPE = ItemStateEvent.TYPE
    def ITEM_ADDED_EVENT_TYPE = ItemAddedEvent.TYPE

    def ITEM_COMMAND_EVENT_TOPIC = ItemEventFactory.ITEM_COMAND_EVENT_TOPIC.replace("{itemName}", ITEM_NAME)
    def ITEM_STATE_EVENT_TOPIC = ItemEventFactory.ITEM_STATE_EVENT_TOPIC.replace("{itemName}", ITEM_NAME)
    def ITEM_ADDED_EVENT_TOPIC = ItemEventFactory.ITEM_ADDED_EVENT_TOPIC.replace("{itemName}", ITEM_NAME)

    def ITEM_COMMAND = OnOffType.ON
    def ITEM_COMMAND_EVENT_PAYLOAD = new Gson().toJson(new ItemEventPayloadBean(ITEM_COMMAND.getClass().getName(), ITEM_COMMAND.toString()))
    def ITEM_STATE = OnOffType.OFF
    def ITEM_STATE_EVENT_PAYLOAD = new Gson().toJson(new ItemEventPayloadBean(ITEM_STATE.getClass().getName(), ITEM_STATE.toString()))
    def ITEM_ADDED_EVENT_PAYLOAD = new Gson().toJson(ItemDTOMapper.map(ITEM, false))

    @Test
    void 'ItemEventFactory creates Event as ItemCommandEvent (type OnOffType) correctly'() {
        Event event = factory.createEvent(ITEM_COMMAND_EVENT_TYPE, ITEM_COMMAND_EVENT_TOPIC, ITEM_COMMAND_EVENT_PAYLOAD, SOURCE)

        assertThat event, is(instanceOf(ItemCommandEvent))
        ItemCommandEvent itemCommandEvent = event as ItemCommandEvent
        assertThat itemCommandEvent.getType(), is(ITEM_COMMAND_EVENT_TYPE)
        assertThat itemCommandEvent.getTopic(), is(ITEM_COMMAND_EVENT_TOPIC)
        assertThat itemCommandEvent.getPayload(), is(ITEM_COMMAND_EVENT_PAYLOAD)
        assertThat itemCommandEvent.getItemName(), is(ITEM_NAME)
        assertThat itemCommandEvent.getSource(), is(SOURCE)
        assertThat itemCommandEvent.getItemCommand(), is(instanceOf(OnOffType))
        assertThat itemCommandEvent.getItemCommand(), is(ITEM_COMMAND)
    }

    @Test
    void 'ItemEventFactory creates ItemCommandEvent (type OnOffType) correctly'() {
        ItemCommandEvent event = ItemEventFactory.createCommandEvent(ITEM_NAME, ITEM_COMMAND, SOURCE)

        assertThat event.getType(), is(ITEM_COMMAND_EVENT_TYPE)
        assertThat event.getTopic(), is(ITEM_COMMAND_EVENT_TOPIC)
        assertThat event.getPayload(), is(ITEM_COMMAND_EVENT_PAYLOAD)
        assertThat event.getItemName(), is(ITEM_NAME)
        assertThat event.getSource(), is(SOURCE)
        assertThat event.getItemCommand(), is(instanceOf(OnOffType))
        assertThat event.getItemCommand(), is(ITEM_COMMAND)
    }

    @Test
    void 'ItemEventFactory creates Event as ItemStateEvent (type OnOffType) correctly'() {
        Event event = factory.createEvent(ITEM_STATE_EVENT_TYPE, ITEM_STATE_EVENT_TOPIC, ITEM_STATE_EVENT_PAYLOAD, SOURCE)

        assertThat event, is(instanceOf(ItemStateEvent))
        ItemStateEvent itemStateEvent = event as ItemStateEvent
        assertThat itemStateEvent.getType(), is(ITEM_STATE_EVENT_TYPE)
        assertThat itemStateEvent.getTopic(), is(ITEM_STATE_EVENT_TOPIC)
        assertThat itemStateEvent.getPayload(), is(ITEM_STATE_EVENT_PAYLOAD)
        assertThat itemStateEvent.getItemName(), is(ITEM_NAME)
        assertThat itemStateEvent.getSource(), is(SOURCE)
        assertThat itemStateEvent.getItemState(), is(instanceOf(OnOffType))
        assertThat itemStateEvent.getItemState(), is(ITEM_STATE)
    }

    @Test
    void 'ItemEventFactory creates ItemStateEvent (type OnOffType) correctly'() {
        ItemStateEvent event = ItemEventFactory.createStateEvent(ITEM_NAME, ITEM_STATE, SOURCE)

        assertThat event.getType(), is(ITEM_STATE_EVENT_TYPE)
        assertThat event.getTopic(), is(ITEM_STATE_EVENT_TOPIC)
        assertThat event.getPayload(), is(ITEM_STATE_EVENT_PAYLOAD)
        assertThat event.getItemName(), is(ITEM_NAME)
        assertThat event.getSource(), is(SOURCE)
        assertThat event.getItemState(), is(instanceOf(OnOffType))
        assertThat event.getItemState(), is(ITEM_STATE)
    }

    @Test
    void 'ItemEventFactory creates Event as ItemAddedEvent correctly'() {
        Event event = factory.createEvent(ITEM_ADDED_EVENT_TYPE, ITEM_ADDED_EVENT_TOPIC, ITEM_ADDED_EVENT_PAYLOAD, null)

        assertThat event, is(instanceOf(ItemAddedEvent))
        ItemAddedEvent itemAddedEvent = event as ItemAddedEvent
        assertThat itemAddedEvent.getType(), is(ITEM_ADDED_EVENT_TYPE)
        assertThat itemAddedEvent.getTopic(), is(ITEM_ADDED_EVENT_TOPIC)
        assertThat itemAddedEvent.getPayload(), is(ITEM_ADDED_EVENT_PAYLOAD)
        assertThat itemAddedEvent.getItem(), not(null)
        assertThat itemAddedEvent.getItem().name, is(ITEM_NAME)
        assertThat itemAddedEvent.getItem().type, is("SwitchItem")
    }

    @Test
    void 'ItemEventFactory creates ItemAddedEvent correctly'() {
        ItemAddedEvent event = ItemEventFactory.createAddedEvent(ITEM)

        assertThat event.getType(), is(ItemAddedEvent.TYPE)
        assertThat event.getTopic(), is(ITEM_ADDED_EVENT_TOPIC)
        assertThat event.getItem(), not(null)
        assertThat event.getItem().name, is(ITEM_NAME)
        assertThat event.getItem().type, is("SwitchItem")
    }
}
