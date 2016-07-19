/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.items.events.GroupItemStateChangedEvent
import org.eclipse.smarthome.core.items.events.ItemEventFactory
import org.eclipse.smarthome.core.library.items.NumberItem
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.RawType
import org.eclipse.smarthome.core.types.RefreshType
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test


/**
 * The GroupItemTest tests functionality of the GroupItem.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Christoph Knauf - event tests
 */
class GroupItemTest extends OSGiTest {

    List<Event> events = []
    EventPublisher publisher

    @Before
    void setUp(){
        publisher = [
            post : { event ->
                events.add(event)
            }
        ] as EventPublisher
    }

    @Test(expected = UnsupportedOperationException.class)
    void 'assert accepted command types cannot be changed'() {
        new GroupItem("switch").acceptedCommandTypes.clear()
    }

    @Test()
    void 'assert acceptedCommandTypes on GroupItems returns subset of command types supported by all members'() {

        def switchItem = new SwitchItem("switch")
        def numberItem = new NumberItem("number")

        GroupItem groupItem = new GroupItem("group")
        groupItem.addMember(switchItem)
        groupItem.addMember(numberItem)

        assertThat groupItem.acceptedCommandTypes, hasItems(RefreshType)
    }

    @Test
    public void testGetAllMembers() {
        GroupItem rootGroupItem = new GroupItem("root")
        rootGroupItem.addMember(new TestItem("member1"))
        rootGroupItem.addMember(new TestItem("member2"))
        rootGroupItem.addMember(new TestItem("member2"))
        GroupItem subGroup = new GroupItem("subGroup1")
        subGroup.addMember(new TestItem("subGroup member 1"))
        subGroup.addMember(new TestItem("subGroup member 2"))
        subGroup.addMember(new TestItem("subGroup member 3"))
        subGroup.addMember(new TestItem("member1"))
        rootGroupItem.addMember(subGroup)
        int expectedAmountOfMembers = 5
        Assert.assertEquals(expectedAmountOfMembers, rootGroupItem.getAllMembers().size())
        for (Item member : rootGroupItem.getAllMembers()) {
            if (member instanceof GroupItem) {
                fail("There are no GroupItems allowed in this Collection")
            }
        }
    }

    @Test
    void 'assert that group item posts events for changes correctly' (){
        events.clear()
        GroupItem groupItem = new GroupItem("root")
        def member = new TestItem("member1")
        groupItem.addMember(member)
        groupItem.setEventPublisher(publisher)
        def oldGroupState = groupItem.getState()

        //State changes -> one change event is fired
        member.setState(new RawType())

        waitForAssert {
            assertThat events.size(), is(1)
        }

        def changes = events.findAll{it instanceof GroupItemStateChangedEvent}
        assertThat changes.size(), is(1)

        def change = changes.getAt(0) as GroupItemStateChangedEvent
        assertTrue change.getItemName().equals(groupItem.getName())
        assertTrue change.getMemberName().equals(member.getName())
        assertTrue change.getTopic().equals(
                ItemEventFactory.GROUPITEM_STATE_CHANGED_EVENT_TOPIC.replace("{memberName}", member.getName()).replace("{itemName}", groupItem.getName())
                )
        assertTrue change.getItemState().equals(groupItem.getState())
        assertTrue change.getOldItemState().equals(oldGroupState)

        events.clear()

        //State doesn't change -> no events are fired
        member.setState(member.getState())
        assertThat events.size(), is(0)
    }
}
