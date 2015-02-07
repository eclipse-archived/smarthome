/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.events;

import static org.eclipse.smarthome.core.events.EventConstants.TOPIC_PREFIX;
import static org.eclipse.smarthome.core.events.EventConstants.TOPIC_SEPERATOR;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
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
 * The {@link EventPublisherImpl} class is the main implementation of the {@link EventPublisher} service interface.
 * <p>
 * This implementation uses the <i>OSGi Event Admin</i> service as event bus implementation to broadcast <i>Eclipse
 * SmartHome</i> events.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Michael Grammling - Javadoc and exception handling extended, Checkstyle compliance,
 *         thread-safety
 * @author Michael Grammling - doPrivileged calls added, so that permissions to the internal
 *         event bus are no longer needed (permissions should be added at some other place
 *         in the future)
 */
public class EventPublisherImpl implements EventPublisher {

    private EventAdmin eventAdmin;

    public void setEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

    public void unsetEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendCommand(final String itemName, final Command command, final String source)
            throws IllegalArgumentException, IllegalStateException {

        ItemUtil.assertValidItemName(itemName);
        if (command == null) {
            throw new IllegalArgumentException("The command must not be null!");
        }

        final EventAdmin eventAdmin = this.eventAdmin;
        if (eventAdmin != null) {
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        eventAdmin.sendEvent(createCommandEvent(itemName, command, source));
                        return null;
                    }
                });
            } catch (PrivilegedActionException pae) {
                Exception ex = pae.getException();
                throw new IllegalStateException("Cannot send the command!", ex);
            }
        } else {
            throw new IllegalStateException("The event bus module is not available!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postCommand(final String itemName, final Command command, final String source)
            throws IllegalArgumentException, IllegalStateException {

        ItemUtil.assertValidItemName(itemName);
        if (command == null) {
            throw new IllegalArgumentException("The command must not be null!");
        }

        final EventAdmin eventAdmin = this.eventAdmin;
        if (eventAdmin != null) {
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        eventAdmin.postEvent(createCommandEvent(itemName, command, source));
                        return null;
                    }
                });
            } catch (PrivilegedActionException pae) {
                Exception ex = pae.getException();
                throw new IllegalStateException("Cannot post the command!", ex);
            }
        } else {
            throw new IllegalStateException("The event bus module is not available!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postUpdate(final String itemName, final State newState, final String source)
            throws IllegalArgumentException, IllegalStateException {

        ItemUtil.assertValidItemName(itemName);
        if (newState == null) {
            throw new IllegalArgumentException("The state must not be null!");
        }

        final EventAdmin eventAdmin = this.eventAdmin;
        if (eventAdmin != null) {
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        eventAdmin.postEvent(createUpdateEvent(itemName, newState, source));
                        return null;
                    }
                });
            } catch (PrivilegedActionException pae) {
                Exception ex = pae.getException();
                throw new IllegalStateException("Cannot post the update!", ex);
            }
        } else {
            throw new IllegalStateException("The event bus module is not available!");
        }
    }

    private String createTopic(EventType type, String itemName) {
        return TOPIC_PREFIX + TOPIC_SEPERATOR + type + TOPIC_SEPERATOR + itemName;
    }

    private Event createCommandEvent(String itemName, Command command, String source) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
        properties.put("item", itemName);
        properties.put("command", command);
        if (source != null)
            properties.put("source", source);
        return new Event(createTopic(EventType.COMMAND, itemName), properties);
    }

    private Event createUpdateEvent(String itemName, State newState, String source) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
        properties.put("item", itemName);
        properties.put("state", newState);
        if (source != null)
            properties.put("source", source);
        return new Event(createTopic(EventType.UPDATE, itemName), properties);
    }

    @Override
    public void sendCommand(String itemName, Command command) throws IllegalArgumentException, IllegalStateException {
        sendCommand(itemName, command, null);
    }

    @Override
    public void postCommand(String itemName, Command command) throws IllegalArgumentException, IllegalStateException {
        postCommand(itemName, command, null);
    }

    @Override
    public void postUpdate(String itemName, State newState) throws IllegalArgumentException, IllegalStateException {
        postUpdate(itemName, newState, null);
    }

}
