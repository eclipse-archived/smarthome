/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.events.GroupItemStateChangedEvent;
import org.eclipse.smarthome.core.items.events.ItemUpdatedEvent;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.ArithmeticGroupFunction;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class GroupItemTest extends JavaOSGiTest {

    /** Time to sleep when a file is created/modified/deleted, so the event can be handled */
    private final static int WAIT_EVENT_TO_BE_HANDLED = 1000;

    List<Event> events = new LinkedList<>();
    EventPublisher publisher;

    ItemRegistry itemRegistry;

    @Before
    public void setUp() {
        registerVolatileStorageService();
        publisher = event -> events.add(event);

        itemRegistry = getService(ItemRegistry.class);
        assertNotNull(itemRegistry);

        registerService(new EventSubscriber() {

            @Override
            public void receive(Event event) {
                events.add(event);
            }

            @Override
            public Set<String> getSubscribedEventTypes() {
                HashSet<String> hs = new HashSet<>();
                hs.add(ItemUpdatedEvent.TYPE);
                return hs;
            }

            @Override
            public EventFilter getEventFilter() {
                return null;
            }
        });
    }

    @Ignore
    @Test
    public void testItemUpdateWithItemRegistry() {
        GroupItem item = new GroupItem("mySimpleGroupItem");
        item.setLabel("firstLabel");

        itemRegistry.add(item);

        GroupItem updatedItem = (GroupItem) itemRegistry.get("mySimpleGroupItem");
        assertNotNull(updatedItem);

        events.clear();

        updatedItem.setLabel("secondLabel");
        itemRegistry.update(updatedItem);
        waitForAssert(() -> assertThat(events.size(), is(1)));

        List<Event> stateChanges = events.stream().filter(it -> it instanceof ItemUpdatedEvent)
                .collect(Collectors.toList());
        assertThat(stateChanges.size(), is(1));

        ItemUpdatedEvent change = (ItemUpdatedEvent) stateChanges.get(0);

        assertThat(change.getItem().label, is("secondLabel"));
    }

    @Test()
    public void assertAcceptedCommandTypesOnGroupItemsReturnsSubsetOfCommandTypesSupportedByAllMembers() {
        SwitchItem switchItem = new SwitchItem("switch");
        NumberItem numberItem = new NumberItem("number");

        GroupItem groupItem = new GroupItem("group");
        groupItem.addMember(switchItem);
        groupItem.addMember(numberItem);

        assertThat(groupItem.getAcceptedCommandTypes(), hasItems(RefreshType.class));
    }

    @Test
    public void testGetAllMembers() {
        GroupItem rootGroupItem = new GroupItem("root");
        rootGroupItem.addMember(new TestItem("member1"));
        rootGroupItem.addMember(new TestItem("member2"));
        rootGroupItem.addMember(new TestItem("member2"));
        GroupItem subGroup = new GroupItem("subGroup1");
        subGroup.addMember(new TestItem("subGroup member 1"));
        subGroup.addMember(new TestItem("subGroup member 2"));
        subGroup.addMember(new TestItem("subGroup member 3"));
        subGroup.addMember(new TestItem("member1"));
        rootGroupItem.addMember(subGroup);
        assertThat(rootGroupItem.getAllMembers().size(), is(5));
        for (Item member : rootGroupItem.getAllMembers()) {
            if (member instanceof GroupItem) {
                fail("There are no GroupItems allowed in this Collection");
            }
        }
    }

    @Test
    public void testGetAllMembersWithCircleDependency() {
        GroupItem rootGroupItem = new GroupItem("root");
        rootGroupItem.addMember(new TestItem("member1"));
        rootGroupItem.addMember(new TestItem("member2"));
        GroupItem subGroup = new GroupItem("subGroup1");
        subGroup.addMember(new TestItem("subGroup member 1"));
        subGroup.addMember(rootGroupItem);
        rootGroupItem.addMember(subGroup);
        assertThat(rootGroupItem.getAllMembers().size(), is(3));
        for (Item member : rootGroupItem.getAllMembers()) {
            if (member instanceof GroupItem) {
                fail("There are no GroupItems allowed in this Collection");
            }
        }
    }

    @Test
    public void testGetAllMembersWithFilter() {
        GroupItem rootGroupItem = new GroupItem("root");

        TestItem member1 = new TestItem("member1");
        member1.setLabel("mem1");
        rootGroupItem.addMember(member1);

        TestItem member2 = new TestItem("member2");
        member2.setLabel("mem1");
        rootGroupItem.addMember(member2);

        TestItem member3 = new TestItem("member3");
        member3.setLabel("mem3");
        rootGroupItem.addMember(member3);

        GroupItem subGroup = new GroupItem("subGroup1");
        subGroup.setLabel("subGrp1");
        TestItem subMember1 = new TestItem("subGroup member 1");
        subMember1.setLabel("subMem1");
        subGroup.addMember(subMember1);
        TestItem subMember2 = new TestItem("subGroup member 2");
        subMember2.setLabel("subMem2");
        subGroup.addMember(subMember2);
        TestItem subMember3 = new TestItem("subGroup member 3");
        subMember3.setLabel("subMem3");
        subGroup.addMember(subMember3);
        subGroup.addMember(member1);
        rootGroupItem.addMember(subGroup);

        Set<Item> members = rootGroupItem.getMembers(i -> i instanceof GroupItem);
        assertThat(members.size(), is(1));

        members = rootGroupItem.getMembers(i -> i.getLabel().equals("mem1"));
        assertThat(members.size(), is(2));
    }

    @Test
    public void assertThatGroupItemPostsEventsForChangesCorrectly() {
        // from ItemEventFactory.GROUPITEM_STATE_CHANGED_EVENT_TOPIC
        String GROUPITEM_STATE_CHANGED_EVENT_TOPIC = "smarthome/items/{itemName}/{memberName}/statechanged";

        events.clear();
        GroupItem groupItem = new GroupItem("root", new SwitchItem("mySwitch"), new GroupFunction.Equality());
        SwitchItem member = new SwitchItem("member1");
        groupItem.addMember(member);
        groupItem.setEventPublisher(publisher);
        State oldGroupState = groupItem.getState();

        // State changes -> one change event is fired
        member.setState(OnOffType.ON);

        waitForAssert(() -> assertThat(events.size(), is(1)));

        List<Event> changes = events.stream().filter(it -> it instanceof GroupItemStateChangedEvent)
                .collect(Collectors.toList());
        assertThat(changes.size(), is(1));

        GroupItemStateChangedEvent change = (GroupItemStateChangedEvent) changes.get(0);
        assertTrue(change.getItemName().equals(groupItem.getName()));
        assertTrue(change.getMemberName().equals(member.getName()));
        assertTrue(change.getTopic().equals(GROUPITEM_STATE_CHANGED_EVENT_TOPIC
                .replace("{memberName}", member.getName()).replace("{itemName}", groupItem.getName())));
        assertTrue(change.getItemState().equals(groupItem.getState()));
        assertTrue(change.getOldItemState().equals(oldGroupState));

        events.clear();

        // State doesn't change -> no events are fired
        member.setState(member.getState());
        assertThat(events.size(), is(0));
    }

    @Test
    public void assertThatGroupItemChangesRespectGroupFunctionOR() {
        events.clear();
        GroupItem groupItem = new GroupItem("root", new SwitchItem("mySwitch"),
                new ArithmeticGroupFunction.Or(OnOffType.ON, OnOffType.OFF));
        SwitchItem sw1 = new SwitchItem("switch1");
        SwitchItem sw2 = new SwitchItem("switch2");
        groupItem.addMember(sw1);
        groupItem.addMember(sw2);

        groupItem.setEventPublisher(publisher);

        // State changes -> one change event is fired
        sw1.setState(OnOffType.ON);

        waitForAssert(() -> assertThat(events.size(), is(1)));

        List<Event> changes = events.stream().filter(it -> it instanceof GroupItemStateChangedEvent)
                .collect(Collectors.toList());
        assertThat(changes.size(), is(1));

        GroupItemStateChangedEvent change = (GroupItemStateChangedEvent) changes.get(0);
        assertTrue(change.getItemName().equals(groupItem.getName()));

        assertTrue(change.getOldItemState().equals(UnDefType.NULL));
        assertTrue(change.getItemState().equals(OnOffType.ON));

        assertTrue(groupItem.getState().equals(OnOffType.ON));
    }

    @Test
    public void assertThatGroupItemChangesRespectGroupFunctionORwithUNDEF() throws InterruptedException {
        events.clear();
        GroupItem groupItem = new GroupItem("root", new SwitchItem("mySwitch"),
                new ArithmeticGroupFunction.Or(OnOffType.ON, OnOffType.OFF));
        SwitchItem sw1 = new SwitchItem("switch1");
        SwitchItem sw2 = new SwitchItem("switch2");
        groupItem.addMember(sw1);
        groupItem.addMember(sw2);

        groupItem.setEventPublisher(publisher);

        // State changes -> one change event is fired
        sw1.setState(OnOffType.ON);

        waitForAssert(() -> assertThat(events.size(), is(1)));

        List<Event> changes = events.stream().filter(it -> it instanceof GroupItemStateChangedEvent)
                .collect(Collectors.toList());
        assertThat(changes.size(), is(1));

        GroupItemStateChangedEvent change = (GroupItemStateChangedEvent) changes.get(0);
        assertTrue(change.getItemName().equals(groupItem.getName()));

        assertTrue(change.getOldItemState().equals(UnDefType.NULL));
        assertTrue(change.getItemState().equals(OnOffType.ON));

        events.clear();

        sw2.setState(OnOffType.ON);

        sw2.setState(UnDefType.UNDEF);

        // wait to see that the event doesn't fire
        Thread.sleep(WAIT_EVENT_TO_BE_HANDLED);

        assertThat(events.size(), is(0));

        assertTrue(groupItem.getState().equals(OnOffType.ON));
    }

    @Test
    public void assertThatGroupItemChangesRespectGroupFunctionAND() {
        events.clear();
        GroupItem groupItem = new GroupItem("root", new SwitchItem("mySwitch"),
                new ArithmeticGroupFunction.And(OnOffType.ON, OnOffType.OFF));
        SwitchItem sw1 = new SwitchItem("switch1");
        SwitchItem sw2 = new SwitchItem("switch2");
        groupItem.addMember(sw1);
        groupItem.addMember(sw2);

        groupItem.setEventPublisher(publisher);

        // State changes -> one change event is fired
        sw1.setState(OnOffType.ON);

        waitForAssert(() -> assertThat(events.size(), is(1)));

        List<Event> changes = events.stream().filter(it -> it instanceof GroupItemStateChangedEvent)
                .collect(Collectors.toList());
        assertThat(changes.size(), is(1));

        GroupItemStateChangedEvent change = (GroupItemStateChangedEvent) changes.get(0);
        assertTrue(change.getItemName().equals(groupItem.getName()));

        // we expect that the group should now have status "OFF"
        assertTrue(change.getOldItemState().equals(UnDefType.NULL));
        assertTrue(change.getItemState().equals(OnOffType.OFF));

        events.clear();

        // State changes -> one change event is fired
        sw2.setState(OnOffType.ON);

        waitForAssert(() -> assertThat(events.size(), is(1)));

        changes = events.stream().filter(it -> it instanceof GroupItemStateChangedEvent).collect(Collectors.toList());
        assertThat(changes.size(), is(1));

        change = (GroupItemStateChangedEvent) changes.get(0);
        assertTrue(change.getItemName().equals(groupItem.getName()));

        // we expect that the group should now have status "ON"
        assertTrue(change.getOldItemState().equals(OnOffType.OFF));
        assertTrue(change.getItemState().equals(OnOffType.ON));

        assertTrue(groupItem.getState().equals(OnOffType.ON));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void assertThatGroupItemChangesDoNotAffectTheGroupStatusIfnoFunctionOrBaseItemAreDefined()
            throws InterruptedException {
        events.clear();
        GroupItem groupItem = new GroupItem("root");
        TestItem member = new TestItem("member1");
        groupItem.addMember(member);
        groupItem.setEventPublisher(publisher);
        State oldGroupState = groupItem.getState();

        // State changes -> NO change event should be fired
        member.setState(new RawType());

        // wait to see that the event doesn't fire
        Thread.sleep(WAIT_EVENT_TO_BE_HANDLED);

        assertThat(events.size(), is(0));

        assertTrue(groupItem.getState().equals(oldGroupState));
    }

    @Test
    public void assertThatGroupItemWithoutFunctionCanHaveAconvertibleState() {
        GroupItem groupItem = new GroupItem("root");
        PercentType pt = new PercentType(50);
        groupItem.setState(pt);

        State groupStateAsOnOff = groupItem.getStateAs(OnOffType.class);

        // any value >0 means on, so 50% means the group state should be ON
        assertTrue(OnOffType.ON.equals(groupStateAsOnOff));
    }

    @Test
    public void assertThatGroupItemWithRollershutterBaseItemConversionWorks() {
        // initially this group has State UndefType.NULL
        GroupItem groupItem = new GroupItem("root", new RollershutterItem("myRollerShutter"));
        State groupStateAsOnOff = groupItem.getStateAs(OnOffType.class);

        // a state conversion from NULL to OnOffType should not be possible
        assertNull(groupStateAsOnOff);

        // init group
        groupItem.setState(new PercentType(70));
        groupStateAsOnOff = groupItem.getStateAs(OnOffType.class);

        // any value >0 means on, so 50% means the group state should be ON
        assertTrue(OnOffType.ON.equals(groupStateAsOnOff));
    }

    @Test
    public void assertThatGroupItemWithColoritemBaseItemConversionWorks() {
        // initially this group has State UndefType.NULL
        GroupItem groupItem = new GroupItem("root", new ColorItem("myColor"));
        State groupStateAsPercent = groupItem.getStateAs(PercentType.class);

        // a state conversion from NULL to PercentType should not be possible
        assertNull(groupStateAsPercent);

        // init group
        groupItem.setState(new HSBType("200,80,80"));
        groupStateAsPercent = groupItem.getStateAs(PercentType.class);

        assertTrue(groupStateAsPercent instanceof PercentType);
        assertThat(((PercentType) groupStateAsPercent).intValue(), is(80));
    }

    @Test
    public void assertThatGroupItemWithDimmeritemBaseItemConversionWorks() {
        // initially this group has State UndefType.NULL
        GroupItem groupItem = new GroupItem("root", new DimmerItem("myDimmer"));
        State groupStateAsPercent = groupItem.getStateAs(PercentType.class);

        // a state conversion from NULL to PercentType should not be possible
        assertNull(groupStateAsPercent);

        // init group
        groupItem.setState(new PercentType(80));
        groupStateAsPercent = groupItem.getStateAs(PercentType.class);

        assertTrue(groupStateAsPercent instanceof PercentType);
        assertThat(((PercentType) groupStateAsPercent).intValue(), is(80));
    }

    @Test
    public void assertThatGroupItemwithDimmeritemAcceptsGetsPercentTypeStateIfMembersHavePercentTypeStates() {
        events.clear();
        GroupItem groupItem = new GroupItem("root", new DimmerItem("myDimmer"), new ArithmeticGroupFunction.Avg());

        DimmerItem member1 = new DimmerItem("dimmer1");
        groupItem.addMember(member1);
        DimmerItem member2 = new DimmerItem("dimmer2");
        groupItem.addMember(member2);
        groupItem.setEventPublisher(publisher);

        member1.setState(new PercentType(50));

        waitForAssert(() -> assertThat(events.size(), is(1)));

        List<Event> changes = events.stream().filter(it -> it instanceof GroupItemStateChangedEvent)
                .collect(Collectors.toList());
        GroupItemStateChangedEvent change = (GroupItemStateChangedEvent) changes.get(0);
        assertTrue(change.getItemName().equals(groupItem.getName()));

        State newEventState = change.getItemState();
        assertTrue(newEventState instanceof PercentType);
        assertThat(((PercentType) newEventState).intValue(), is(50));

        State newGroupState = groupItem.getState();
        assertTrue(newGroupState instanceof PercentType);
        assertThat(((PercentType) newGroupState).intValue(), is(50));

        events.clear();

        member2.setState(new PercentType(10));

        waitForAssert(() -> assertThat(events.size(), is(1)));

        changes = events.stream().filter(it -> it instanceof GroupItemStateChangedEvent).collect(Collectors.toList());
        assertThat(changes.size(), is(1));

        change = (GroupItemStateChangedEvent) changes.get(0);
        assertTrue(change.getItemName().equals(groupItem.getName()));

        newEventState = change.getItemState();
        assertTrue(newEventState instanceof PercentType);
        assertThat(((PercentType) newEventState).intValue(), is(30));

        newGroupState = groupItem.getState();
        assertTrue(newGroupState instanceof PercentType);
        assertThat(((PercentType) newGroupState).intValue(), is(30));
    }

}
