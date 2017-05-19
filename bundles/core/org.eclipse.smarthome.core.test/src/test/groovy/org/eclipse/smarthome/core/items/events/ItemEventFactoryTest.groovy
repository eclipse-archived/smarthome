/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items.events;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.apache.commons.lang.StringUtils
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.items.GroupItem
import org.eclipse.smarthome.core.items.dto.ItemDTOMapper
import org.eclipse.smarthome.core.items.events.ItemEventFactory.ItemEventPayloadBean
import org.eclipse.smarthome.core.items.events.ItemEventFactory.ItemStateChangedEventPayloadBean
import org.eclipse.smarthome.core.library.CoreItemFactory
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.library.types.RawType
import org.eclipse.smarthome.core.types.RefreshType
import org.eclipse.smarthome.core.types.UnDefType
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Test

import com.google.gson.Gson

/**
 * {@link ItemEventFactoryTests} tests the {@link ItemEventFactory}.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
class ItemEventFactoryTest extends OSGiTest {
    ItemEventFactory factory = new ItemEventFactory()

    def ITEM_NAME = "ItemA"
    def ITEM = new SwitchItem(ITEM_NAME)
    def GROUP_NAME = "GroupA"
    def GROUP = new GroupItem(GROUP_NAME)
    def SOURCE = "binding:type:id:channel"

    def ITEM_COMMAND_EVENT_TYPE = ItemCommandEvent.TYPE
    def ITEM_STATE_EVENT_TYPE = ItemStateEvent.TYPE
    def ITEM_ADDED_EVENT_TYPE = ItemAddedEvent.TYPE
    def GROUPITEM_CHANGED_EVENT_TYPE = GroupItemStateChangedEvent.TYPE

    def ITEM_COMMAND_EVENT_TOPIC = ItemEventFactory.ITEM_COMAND_EVENT_TOPIC.replace("{itemName}", ITEM_NAME)
    def ITEM_STATE_EVENT_TOPIC = ItemEventFactory.ITEM_STATE_EVENT_TOPIC.replace("{itemName}", ITEM_NAME)
    def ITEM_ADDED_EVENT_TOPIC = ItemEventFactory.ITEM_ADDED_EVENT_TOPIC.replace("{itemName}", ITEM_NAME)
    def GROUPITEM_STATE_CHANGED_EVENT_TOPIC = ItemEventFactory.GROUPITEM_STATE_CHANGED_EVENT_TOPIC.replace("{itemName}", GROUP_NAME).replace("{memberName}", ITEM_NAME)


    def ITEM_COMMAND = OnOffType.ON
    def ITEM_COMMAND_EVENT_PAYLOAD = new Gson().toJson(new ItemEventPayloadBean(createTypeString(ITEM_COMMAND), ITEM_COMMAND.toString()))

    def ITEM_REFRESH_COMMAND_EVENT_PAYLOAD = new Gson().toJson(new ItemEventPayloadBean(createTypeString(RefreshType.REFRESH), RefreshType.REFRESH.toString()))
    def ITEM_UNDEF_STATE_EVENT_PAYLOAD = new Gson().toJson(new ItemEventPayloadBean(createTypeString(UnDefType.UNDEF), UnDefType.UNDEF.toString()))
    def ITEM_STATE = OnOffType.OFF
    def NEW_ITEM_STATE = OnOffType.ON
    def ITEM_STATE_EVENT_PAYLOAD = new Gson().toJson(new ItemEventPayloadBean(createTypeString(ITEM_STATE), ITEM_STATE.toString()))
    def ITEM_ADDED_EVENT_PAYLOAD = new Gson().toJson(ItemDTOMapper.map(ITEM))
    def ITEM_STATE_CHANGED_EVENT_PAYLOAD = new Gson().toJson(new ItemStateChangedEventPayloadBean(createTypeString(NEW_ITEM_STATE), NEW_ITEM_STATE.toString(), createTypeString(ITEM_STATE), ITEM_STATE.toString()))

    private createTypeString(type) {
        StringUtils.removeEnd(type.class.getSimpleName(), "Type")
    }

    def RAW_ITEM_STATE = new RawType(([1, 2, 3, 4, 5]as byte[]), RawType.DEFAULT_MIME_TYPE)
    def NEW_RAW_ITEM_STATE = new RawType(([5, 4, 3, 2, 1]as byte[]), RawType.DEFAULT_MIME_TYPE)



    @Test
    void 'ItemEventFactory creates Event as ItemCommandEvent OnOffType correctly'() {
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
    void 'ItemEventFactory creates ItemCommandEvent OnOffType correctly'() {
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
    void 'ItemEventFactory creates ItemCommandEvent RefreshType correctly'() {
        Event event = factory.createEvent(ITEM_COMMAND_EVENT_TYPE, ITEM_COMMAND_EVENT_TOPIC, ITEM_REFRESH_COMMAND_EVENT_PAYLOAD, SOURCE)

        assertThat event, is(instanceOf(ItemCommandEvent))
        ItemCommandEvent itemCommandEvent = event as ItemCommandEvent
        assertThat itemCommandEvent.getType(), is(ITEM_COMMAND_EVENT_TYPE)
        assertThat itemCommandEvent.getTopic(), is(ITEM_COMMAND_EVENT_TOPIC)
        assertThat itemCommandEvent.getPayload(), is(ITEM_REFRESH_COMMAND_EVENT_PAYLOAD)
        assertThat itemCommandEvent.getItemName(), is(ITEM_NAME)
        assertThat itemCommandEvent.getSource(), is(SOURCE)
        assertThat itemCommandEvent.getItemCommand(), is(RefreshType.REFRESH)
    }

    @Test
    void 'ItemEventFactory creates ItemStateEvent UnDefType correctly'() {
        Event event = factory.createEvent(ITEM_STATE_EVENT_TYPE, ITEM_STATE_EVENT_TOPIC, ITEM_UNDEF_STATE_EVENT_PAYLOAD, SOURCE)

        assertThat event, is(instanceOf(ItemStateEvent))
        ItemStateEvent itemStateEvent = event as ItemStateEvent

        assertThat itemStateEvent.getType(), is(ITEM_STATE_EVENT_TYPE)
        assertThat itemStateEvent.getTopic(), is(ITEM_STATE_EVENT_TOPIC)
        assertThat itemStateEvent.getPayload(), is(ITEM_UNDEF_STATE_EVENT_PAYLOAD)
        assertThat itemStateEvent.getItemName(), is(ITEM_NAME)
        assertThat itemStateEvent.getSource(), is(SOURCE)
        assertThat itemStateEvent.getItemState(), is(UnDefType.UNDEF)
    }

    @Test
    void 'ItemEventFactory creates GroupItemStateChangedEvent correctly'() {
        Event event = factory.createEvent(GROUPITEM_CHANGED_EVENT_TYPE, GROUPITEM_STATE_CHANGED_EVENT_TOPIC, ITEM_STATE_CHANGED_EVENT_PAYLOAD, SOURCE)

        assertThat event, is(instanceOf(GroupItemStateChangedEvent))
        GroupItemStateChangedEvent groupItemStateChangedEvent = event as GroupItemStateChangedEvent

        assertThat groupItemStateChangedEvent.getType(), is(GROUPITEM_CHANGED_EVENT_TYPE)
        assertThat groupItemStateChangedEvent.getTopic(), is(GROUPITEM_STATE_CHANGED_EVENT_TOPIC)
        assertThat groupItemStateChangedEvent.getPayload(), is(ITEM_STATE_CHANGED_EVENT_PAYLOAD)
        assertThat groupItemStateChangedEvent.getItemName(), is(GROUP_NAME)
        assertThat groupItemStateChangedEvent.getMemberName(), is(ITEM_NAME)
        assertThat groupItemStateChangedEvent.getSource(), is(null)
        assertThat groupItemStateChangedEvent.getItemState(), is(NEW_ITEM_STATE)
        assertThat groupItemStateChangedEvent.getOldItemState(), is(ITEM_STATE)
    }

    @Test
    void 'ItemEventFactory creates Event as ItemStateEvent OnOffType correctly'() {
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
    void 'ItemEventFactory creates ItemStateEvent OnOffType correctly'() {
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
        assertThat itemAddedEvent.getItem().type, is(CoreItemFactory.SWITCH)
    }

    @Test
    void 'ItemEventFactory creates ItemAddedEvent correctly'() {
        ItemAddedEvent event = ItemEventFactory.createAddedEvent(ITEM)

        assertThat event.getType(), is(ItemAddedEvent.TYPE)
        assertThat event.getTopic(), is(ITEM_ADDED_EVENT_TOPIC)
        assertThat event.getItem(), not(null)
        assertThat event.getItem().name, is(ITEM_NAME)
        assertThat event.getItem().type, is(CoreItemFactory.SWITCH)
    }

    @Test
    void 'ItemEventFactory creates GroupItemStateChangedEvent with RawTypes correctly'() {
        def giEvent_source = ItemEventFactory.createGroupStateChangedEvent(GROUP_NAME, ITEM_NAME, NEW_RAW_ITEM_STATE, RAW_ITEM_STATE) as GroupItemStateChangedEvent

        def giEvent_parsed = factory.createEvent(giEvent_source.getType(), giEvent_source.getTopic(), giEvent_source.getPayload(), giEvent_source.getSource())


        assertThat giEvent_parsed, is(instanceOf(GroupItemStateChangedEvent))
        GroupItemStateChangedEvent groupItemStateChangedEvent = giEvent_parsed as GroupItemStateChangedEvent

        assertThat groupItemStateChangedEvent.getType(), is(GROUPITEM_CHANGED_EVENT_TYPE)
        assertThat groupItemStateChangedEvent.getTopic(), is(GROUPITEM_STATE_CHANGED_EVENT_TOPIC)
        assertThat groupItemStateChangedEvent.getPayload(), is(giEvent_source.getPayload())
        assertThat groupItemStateChangedEvent.getItemName(), is(GROUP_NAME)
        assertThat groupItemStateChangedEvent.getMemberName(), is(ITEM_NAME)
        assertThat groupItemStateChangedEvent.getSource(), is(null)
        assertThat groupItemStateChangedEvent.getItemState(), is(NEW_RAW_ITEM_STATE)
        assertThat groupItemStateChangedEvent.getOldItemState(), is(RAW_ITEM_STATE)
    }
}
