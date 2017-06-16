/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
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
import org.eclipse.smarthome.core.library.items.ColorItem
import org.eclipse.smarthome.core.library.items.DimmerItem
import org.eclipse.smarthome.core.library.items.NumberItem
import org.eclipse.smarthome.core.library.items.RollershutterItem
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.ArithmeticGroupFunction
import org.eclipse.smarthome.core.library.types.HSBType
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.library.types.PercentType
import org.eclipse.smarthome.core.library.types.RawType
import org.eclipse.smarthome.core.types.RefreshType
import org.eclipse.smarthome.core.types.UnDefType
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test


/**
 * The GroupItemTest tests functionality of the GroupItem.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Christoph Knauf - event tests
 * @author Stefan Triller - tests for group status with and without functions
 */
class GroupItemTest extends OSGiTest {

    /** Time to sleep when a file is created/modified/deleted, so the event can be handled */
    private final static int WAIT_EVENT_TO_BE_HANDLED = 1000

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
    public void testGetAllMembersWithFilter() {
        GroupItem rootGroupItem = new GroupItem("root")

        TestItem member1 = new TestItem("member1");
        member1.setLabel("mem1");
        rootGroupItem.addMember(member1)

        TestItem member2 = new TestItem("member2");
        member2.setLabel("mem1");
        rootGroupItem.addMember(member2)

        rootGroupItem.addMember(new TestItem("member3"))
        GroupItem subGroup = new GroupItem("subGroup1")
        subGroup.addMember(new TestItem("subGroup member 1"))
        subGroup.addMember(new TestItem("subGroup member 2"))
        subGroup.addMember(new TestItem("subGroup member 3"))
        subGroup.addMember(member1)
        rootGroupItem.addMember(subGroup)

        Set<Item> members = rootGroupItem.getMembers({Item i -> i instanceof GroupItem})
        assertThat members.size(), is(1)

        members = rootGroupItem.getMembers({Item i -> i.getLabel().equals("mem1")})
        assertThat members.size(), is(2)
    }

    @Test
    void 'assert that group item posts events for changes correctly' (){
        events.clear()
        GroupItem groupItem = new GroupItem("root", new SwitchItem("mySwitch"), new GroupFunction.Equality())
        def member = new SwitchItem("member1")
        groupItem.addMember(member)
        groupItem.setEventPublisher(publisher)
        def oldGroupState = groupItem.getState()

        //State changes -> one change event is fired
        member.setState(OnOffType.ON)

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

    @Test
    void 'assert that group item changes respect group function OR' (){
        events.clear()
        GroupItem groupItem = new GroupItem("root", new SwitchItem("mySwitch"), new ArithmeticGroupFunction.Or(OnOffType.ON, OnOffType.OFF))
        def sw1 = new SwitchItem("switch1");
        def sw2 = new SwitchItem("switch2");
        groupItem.addMember(sw1)
        groupItem.addMember(sw2)

        groupItem.setEventPublisher(publisher)

        //State changes -> one change event is fired
        sw1.setState(OnOffType.ON)

        waitForAssert {
            assertThat events.size(), is(1)
        }

        def changes = events.findAll{it instanceof GroupItemStateChangedEvent}
        assertThat changes.size(), is(1)

        def change = changes.getAt(0) as GroupItemStateChangedEvent
        assertTrue change.getItemName().equals(groupItem.getName())

        assertTrue change.getOldItemState().equals(UnDefType.NULL)
        assertTrue change.getItemState().equals(OnOffType.ON)

        assertTrue groupItem.getState().equals(OnOffType.ON)
    }

    @Test
    void 'assert that group item changes respect group function OR with UNDEF' (){
        events.clear()
        GroupItem groupItem = new GroupItem("root", new SwitchItem("mySwitch"), new ArithmeticGroupFunction.Or(OnOffType.ON, OnOffType.OFF))
        def sw1 = new SwitchItem("switch1");
        def sw2 = new SwitchItem("switch2");
        groupItem.addMember(sw1)
        groupItem.addMember(sw2)

        groupItem.setEventPublisher(publisher)

        //State changes -> one change event is fired
        sw1.setState(OnOffType.ON)

        waitForAssert {
            assertThat events.size(), is(1)
        }

        def changes = events.findAll{it instanceof GroupItemStateChangedEvent}
        assertThat changes.size(), is(1)

        def change = changes.getAt(0) as GroupItemStateChangedEvent
        assertTrue change.getItemName().equals(groupItem.getName())

        assertTrue change.getOldItemState().equals(UnDefType.NULL)
        assertTrue change.getItemState().equals(OnOffType.ON)

        events.clear();

        sw2.setState(OnOffType.ON);

        sw2.setState(UnDefType.UNDEF);

        //wait to see that the event doesn't fire
        sleep(WAIT_EVENT_TO_BE_HANDLED)

        assertThat events.size(), is(0)

        assertTrue groupItem.getState().equals(OnOffType.ON)
    }

    @Test
    void 'assert that group item changes respect group function AND' (){
        events.clear()
        GroupItem groupItem = new GroupItem("root", new SwitchItem("mySwitch"), new ArithmeticGroupFunction.And(OnOffType.ON, OnOffType.OFF))
        def sw1 = new SwitchItem("switch1");
        def sw2 = new SwitchItem("switch2");
        groupItem.addMember(sw1)
        groupItem.addMember(sw2)

        groupItem.setEventPublisher(publisher)

        //State changes -> one change event is fired
        sw1.setState(OnOffType.ON)

        waitForAssert {
            assertThat events.size(), is(1)
        }

        def changes = events.findAll{it instanceof GroupItemStateChangedEvent}
        assertThat changes.size(), is(1)

        def change = changes.getAt(0) as GroupItemStateChangedEvent
        assertTrue change.getItemName().equals(groupItem.getName())

        //we expect that the group should now have status "OFF"
        assertTrue change.getOldItemState().equals(UnDefType.NULL)
        assertTrue change.getItemState().equals(OnOffType.OFF)

        events.clear();

        //State changes -> one change event is fired
        sw2.setState(OnOffType.ON)

        waitForAssert {
            assertThat events.size(), is(1)
        }

        changes = events.findAll{it instanceof GroupItemStateChangedEvent}
        assertThat changes.size(), is(1)

        change = changes.getAt(0) as GroupItemStateChangedEvent
        assertTrue change.getItemName().equals(groupItem.getName())

        //we expect that the group should now have status "ON"
        assertTrue change.getOldItemState().equals(OnOffType.OFF)
        assertTrue change.getItemState().equals(OnOffType.ON)

        assertTrue groupItem.getState().equals(OnOffType.ON)
    }

    @Test
    void 'assert that group item changes do not affect the group status if no function or baseItem are defined' (){
        events.clear()
        GroupItem groupItem = new GroupItem("root")
        def member = new TestItem("member1")
        groupItem.addMember(member)
        groupItem.setEventPublisher(publisher)
        def oldGroupState = groupItem.getState()

        //State changes -> NO change event should be fired
        member.setState(new RawType())

        //wait to see that the event doesn't fire
        sleep(WAIT_EVENT_TO_BE_HANDLED)

        assertThat events.size(), is(0)

        assertTrue groupItem.getState().equals(oldGroupState)
    }

    @Test
    void 'assert that group item without function can have a convertible state' (){
        GroupItem groupItem = new GroupItem("root")
        PercentType pt = new PercentType(50);
        groupItem.setState(pt);

        def groupStateAsOnOff = groupItem.getStateAs(OnOffType);

        //any value >0 means on, so 50% means the group state should be ON
        assertTrue OnOffType.ON.equals(groupStateAsOnOff)
    }

    @Test
    void 'assert that group item with rollershutter baseItem conversion works' (){
        //initially this group has State UndefType.NULL
        GroupItem groupItem = new GroupItem("root", new RollershutterItem("myRollerShutter"))
        def groupStateAsOnOff = groupItem.getStateAs(OnOffType);

        //a state conversion from NULL to OnOffType should not be possible
        assertThat groupStateAsOnOff, is(null)

        //init group
        groupItem.setState(new PercentType(70));
        groupStateAsOnOff = groupItem.getStateAs(OnOffType);

        //any value >0 means on, so 50% means the group state should be ON
        assertTrue OnOffType.ON.equals(groupStateAsOnOff)
    }

    @Test
    void 'assert that group item with coloritem baseItem conversion works' (){
        //initially this group has State UndefType.NULL
        GroupItem groupItem = new GroupItem("root", new ColorItem("myColor"))
        def groupStateAsPercent = groupItem.getStateAs(PercentType);

        //a state conversion from NULL to PercentType should not be possible
        assertThat groupStateAsPercent, is(null)

        //init group
        groupItem.setState(new HSBType("200,80,80"));
        groupStateAsPercent = groupItem.getStateAs(PercentType);

        assertTrue groupStateAsPercent instanceof PercentType
        assertThat groupStateAsPercent.intValue(), is(80)
    }

    @Test
    void 'assert that group item with dimmeritem baseItem conversion works' (){
        //initially this group has State UndefType.NULL
        GroupItem groupItem = new GroupItem("root", new DimmerItem("myDimmer"))
        def groupStateAsPercent = groupItem.getStateAs(PercentType);

        //a state conversion from NULL to PercentType should not be possible
        assertThat groupStateAsPercent, is(null)

        //init group
        groupItem.setState(new PercentType(80));
        groupStateAsPercent = groupItem.getStateAs(PercentType);

        assertTrue groupStateAsPercent instanceof PercentType
        assertThat groupStateAsPercent.intValue(), is(80)
    }
}
