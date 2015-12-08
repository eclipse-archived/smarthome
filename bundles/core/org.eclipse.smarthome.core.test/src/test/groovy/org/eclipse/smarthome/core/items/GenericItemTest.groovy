/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import java.util.List
import java.util.Set
import javax.management.InstanceOfQueryExp
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.items.events.GroupItemStateChangedEvent
import org.eclipse.smarthome.core.items.events.ItemEventFactory
import org.eclipse.smarthome.core.items.events.ItemStateChangedEvent
import org.eclipse.smarthome.core.items.events.ItemStateEvent
import org.eclipse.smarthome.core.library.items.NumberItem
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.RawType
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.core.types.RefreshType
import org.eclipse.smarthome.core.types.State
import org.eclipse.smarthome.core.types.UnDefType
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.items.TestItem
import org.eclipse.smarthome.core.events.EventPublisher
import org.junit.Before

/**
 * The GenericItemTest tests functionality of the GenericItem.
 *
 * @author Christoph Knauf - Initial contribution, event tests 
 */
class GenericItemTest {

    List<Event> events = []
    EventPublisher publisher

    @Before
    void setUp() {
        publisher = [
            post : { event ->
                events.add(event)
            }
        ] as EventPublisher
    }

    @Test
    void 'assert that item posts events for updates and changes correctly'() {
        def item = new TestItem("member1")
        item.setEventPublisher(publisher)
        def oldState = item.getState()

        //State changes -> one change event is fired
        item.setState(new RawType())

        def changes = events.findAll{it instanceof ItemStateChangedEvent}
        def updates = events.findAll{it instanceof ItemStateEvent}

        assertThat events.size(), is(1)
        assertThat changes.size(), is(1)
        assertThat updates.size(), is(0)

        def change = changes.getAt(0) as ItemStateChangedEvent
        assertTrue change.getItemName().equals(item.getName())
        assertTrue change.getTopic().equals(
                ItemEventFactory.ITEM_STATE_CHANGED_EVENT_TOPIC.replace("{itemName}", item.getName())
                )
        assertTrue change.getOldItemState().equals(oldState)
        assertTrue change.getItemState().equals(item.getState())
        assertTrue change.getType().equals(ItemStateChangedEvent.TYPE)

        events.clear()

        //State doesn't change -> no event is fired
        item.setState(item.getState())
        assertThat events.size(), is(0)
    }



}
