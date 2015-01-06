/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.events;

import static org.eclipse.smarthome.core.events.EventConstants.TOPIC_PREFIX;
import static org.eclipse.smarthome.core.events.EventConstants.TOPIC_SEPERATOR;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.EventType;
import org.eclipse.smarthome.core.types.State;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.event.Event;


/**
 * The {@link AbstractEventSubscriberTest} is a <i>JUnit</i> test case which checks if even
 * wrong events sent through the <i>Eclipse SmartHome</i> event bus are processed correctly
 * by the {@link AbstractEventSubscriber).
 * 
 * @author Michael Grammling - Initial Contribution
 */
public class AbstractEventSubscriberTest extends AbstractEventSubscriber {

    private static final String ITEM_NAME_TEST = "item-name-test";
    private static final String TEST_SOURCE = "test-source";
    private static final String AUTOUPDATE_SOURCE = "org.eclipse.smarthome.core.autoupdate";

    private String itemName;
    private Command command;
    private State newState;


    private String createTopic(String type, String itemName) {
        return TOPIC_PREFIX + TOPIC_SEPERATOR + type + TOPIC_SEPERATOR + itemName;
    }

    private Command createCommand(final String formattedCommand) {
        Command command = new Command() {
            @Override
            public String format(String pattern) {
                return formattedCommand;
            }
        };

        return command;
    }

    private State createState(final String formattedState) {
        State state = new State() {
            @Override
            public String format(String pattern) {
                return formattedState;
            }
        };

        return state;
    }

    private Event createCommandEvent(String itemName, Command command) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
        properties.put("item", itemName);
        properties.put("command", command);
        return new Event(createTopic(EventType.COMMAND.toString(), itemName) , properties);
    }

    private Event createUpdateEvent(String itemName, State newState) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
        properties.put("item", itemName);
        properties.put("state", newState);
        return new Event(createTopic(EventType.UPDATE.toString(), itemName), properties);
    }

    private Event createCommandEvent(String itemName, Command command, String source) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
        properties.put("item", itemName);
        properties.put("command", command);
        properties.put("source", source);
        return new Event(createTopic(EventType.COMMAND.toString(), itemName), properties);
    }

    private Event createUpdateEvent(String itemName, State newState, String source) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
        properties.put("item", itemName);
        properties.put("state", newState);
        properties.put("source", source);
        return new Event(createTopic(EventType.UPDATE.toString(), itemName), properties);
    }

    @Test
    public void testWrongHandleEventItemName() {
        Dictionary<String, Object> properties = new Hashtable<String, Object>(1);

        // test wrong object in item name
        properties.put("item", new Object());
        Event event = new Event("anything", properties);
        super.handleEvent(event);

        Assert.assertNull(this.itemName);
        Assert.assertNull(this.command);
        Assert.assertNull(this.newState);

        // test empty string in item name
        properties.put("item", "");
        super.handleEvent(event);

        Assert.assertNull(this.itemName);
        Assert.assertNull(this.command);
        Assert.assertNull(this.newState);
    }

    @Test
    public void testWrongHandleEventTopic() {
        Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
        properties.put("item", ITEM_NAME_TEST);

        // test ignored topic name
        Event ignoredTopicNameEvent = new Event("anything", properties);
        super.handleEvent(ignoredTopicNameEvent);

        Assert.assertNull(this.itemName);
        Assert.assertNull(this.command);
        Assert.assertNull(this.newState);

        // test unknown topic name
        Event unknownTopicNameEvent = new Event(createTopic("ANY", "any"), properties);
        super.handleEvent(unknownTopicNameEvent);

        Assert.assertNull(this.itemName);
        Assert.assertNull(this.command);
        Assert.assertNull(this.newState);

        // test wrong topic name length
        Event wrongTopicNameLength = new Event(
                TOPIC_PREFIX + TOPIC_SEPERATOR + EventType.COMMAND, properties);

        super.handleEvent(wrongTopicNameLength);

        Assert.assertNull(this.itemName);
        Assert.assertNull(this.command);
        Assert.assertNull(this.newState);
    }

    @Test
    public void testAcceptedReceiveCommandEvent() {
        Command command = createCommand(ITEM_NAME_TEST);
        Event commandEvent = createCommandEvent(ITEM_NAME_TEST, command);

        super.handleEvent(commandEvent);

        Assert.assertEquals(ITEM_NAME_TEST, this.itemName);
        Assert.assertEquals(command, this.command);
        Assert.assertNull(this.newState);
    }

    @Test
    public void testAcceptedReceiveUpdateEvent() {
        State state = createState("true");
        Event updateEvent = createUpdateEvent(ITEM_NAME_TEST, state);

        super.handleEvent(updateEvent);

        Assert.assertEquals(ITEM_NAME_TEST, this.itemName);
        Assert.assertNull(this.command);
        Assert.assertEquals(state, this.newState);
    }

    @Test
    public void testAcceptedReceiveCommandEventWithSource() {
        Command command = createCommand(ITEM_NAME_TEST);
        Event commandEvent = createCommandEvent(ITEM_NAME_TEST, command, TEST_SOURCE);

        super.handleEvent(commandEvent);

        Assert.assertEquals(ITEM_NAME_TEST, this.itemName);
        Assert.assertEquals(command, this.command);
        Assert.assertNull(this.newState);
    }

    @Test
    public void testAcceptedReceiveCommandEventWithAutoUpdateSource() {
        Command command = createCommand(ITEM_NAME_TEST);
        Event commandEvent = createCommandEvent(ITEM_NAME_TEST, command, AUTOUPDATE_SOURCE);

        super.handleEvent(commandEvent);

        Assert.assertNull(this.itemName);
        Assert.assertNull(this.command);
        Assert.assertNull(this.newState);
    }

    @Test
    public void testAcceptedReceiveCommandEventWithSourceFiltered() {
        Command command = createCommand(ITEM_NAME_TEST);
        Event commandEvent = createCommandEvent(ITEM_NAME_TEST, command, TEST_SOURCE);
        getSourceFilterList().add(TEST_SOURCE);

        super.handleEvent(commandEvent);

        Assert.assertNull(this.itemName);
        Assert.assertNull(this.command);
        Assert.assertNull(this.newState);
    }

    @Test
    public void testAcceptedReceiveUpdateEventWithSource() {
        State state = createState("true");
        Event updateEvent = createUpdateEvent(ITEM_NAME_TEST, state, TEST_SOURCE);

        super.handleEvent(updateEvent);

        Assert.assertEquals(ITEM_NAME_TEST, this.itemName);
        Assert.assertNull(this.command);
        Assert.assertEquals(state, this.newState);
    }

    @Test
    public void testAcceptedReceiveUpdateEventWithAutoUpdateSource() {
        State state = createState("true");
        Event updateEvent = createUpdateEvent(ITEM_NAME_TEST, state, AUTOUPDATE_SOURCE);

        super.handleEvent(updateEvent);

        Assert.assertNull(this.itemName);
        Assert.assertNull(this.command);
        Assert.assertNull(this.newState);
    }

    @Test
    public void testAcceptedReceiveUpdateEventWithSourceFiltered() {
        State state = createState("true");
        Event updateEvent = createUpdateEvent(ITEM_NAME_TEST, state, TEST_SOURCE);
        getSourceFilterList().add(TEST_SOURCE);

        super.handleEvent(updateEvent);

        Assert.assertNull(this.itemName);
        Assert.assertNull(this.command);
        Assert.assertNull(this.newState);
    }

    @Override
    public void receiveCommand(String itemName, Command command) {
        this.itemName = itemName;
        this.command = command;
    }

    @Override
    public void receiveUpdate(String itemName, State newState) {
        this.itemName = itemName;
        this.newState = newState;
    }

}
