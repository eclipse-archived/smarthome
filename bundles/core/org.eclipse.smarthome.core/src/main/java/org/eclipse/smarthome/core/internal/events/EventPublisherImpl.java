/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.events;

import static org.eclipse.smarthome.core.events.EventConstants.TOPIC_PREFIX;
import static org.eclipse.smarthome.core.events.EventConstants.TOPIC_SEPERATOR;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemUtil;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.EventType;
import org.eclipse.smarthome.core.types.State;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * The {@link EventPublisherImpl} class is the main implementation of the {@link EventPublisher}
 * service interface.
 * <p>
 * This implementation uses the <i>OSGi Event Admin</i> service as event bus implementation
 * to broadcast <i>Eclipse SmartHome</i> events.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Michael Grammling - Javadoc and exception handling extended, Checkstyle compliancy,
 *     thread-safety
 */
public class EventPublisherImpl implements EventPublisher {

    private EventAdmin eventAdmin;


    public synchronized void setEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

    public synchronized void unsetEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = null;
    }

    /**
     * {@inheritDoc}
     */
    public void sendCommand(String itemName, Command command)
            throws IllegalArgumentException, IllegalStateException {

        ItemUtil.assertValidItemName(itemName);
        if (command == null) {
            throw new IllegalArgumentException("The command must not be null!");
        }

        synchronized (this) {
            if (this.eventAdmin != null) {
                this.eventAdmin.sendEvent(createCommandEvent(itemName, command));
            } else {
                throw new IllegalStateException("The event bus module is not available!");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void postCommand(String itemName, Command command) 
            throws IllegalArgumentException, IllegalStateException {

        ItemUtil.assertValidItemName(itemName);
        if (command == null) {
            throw new IllegalArgumentException("The command must not be null!");
        }

        synchronized (this) {
            if (this.eventAdmin != null) {
                this.eventAdmin.postEvent(createCommandEvent(itemName, command));
            } else {
                throw new IllegalStateException("The event bus module is not available!");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void postUpdate(String itemName, State newState)
            throws IllegalArgumentException, IllegalStateException {

        ItemUtil.assertValidItemName(itemName);
        if (newState == null) {
            throw new IllegalArgumentException("The state must not be null!");
        }

        synchronized (this) {
            if (this.eventAdmin != null) {
                this.eventAdmin.postEvent(createUpdateEvent(itemName, newState));
            } else {
                throw new IllegalStateException("The event bus module is not available!");
            }
        }
    }

    private String createTopic(EventType type, String itemName) {
        return TOPIC_PREFIX + TOPIC_SEPERATOR + type + TOPIC_SEPERATOR + itemName;
    }

    private Event createCommandEvent(String itemName, Command command) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
        properties.put("item", itemName);
        properties.put("command", command);
        return new Event(createTopic(EventType.COMMAND, itemName) , properties);
    }

    private Event createUpdateEvent(String itemName, State newState) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
        properties.put("item", itemName);
        properties.put("state", newState);
        return new Event(createTopic(EventType.UPDATE, itemName), properties);
    }

}
