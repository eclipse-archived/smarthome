/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.events;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * The {@link EventPublisher} is a service which is used to send commands or status
 * updates through the <i>Eclipse SmartHome</i> event bus.
 * <p>
 * The event bus belongs to the <i>Eclipse SmartHome</i> core and is used as major intercommunication mechanism between
 * the different modules. The modules itself have usually no concrete dependencies between each other so that their
 * dependencies keep non-invasive (loose-coupling).
 * <p>
 * An event is always sent under a specific topic starting with the prefix {@link EventConstants#TOPIC_PREFIX}
 * (namespace) followed by a sub-topic. A listener can subscribe to a topic and receive the information subscribed to.
 * The simplest way of receiving events is to extend the {@link AbstractEventSubscriber} which already subscribes to the
 * according <i>Eclipse SmartHome</i> topics and which already implements the {@link EventSubscriber} listener
 * interface.
 *
 * @see EventSubscriber
 * @see AbstractEventSubscriber
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Michael Grammling - Javadoc and exception handling extended, Checkstyle compliancy
 */
public interface EventPublisher {

    /**
     * Sends a command under a specific item name through the event bus in a synchronous way.
     * This method does <i>not</i> return to the caller until all subscribers have processed
     * the command.
     *
     * @param itemName name of the item to send the command for
     *            (must neither be null, nor empty and must follow the general item name specification)
     * @param command the command to send (must not be null)
     *
     * @throws IllegalArgumentException if the item name is null or empty or does not follow
     *             the general item specification, or the command is null
     * @throws IllegalStateException if the underlying event bus module is not available
     *
     * @see #postCommand(String, Command)
     */
    void sendCommand(String itemName, Command command) throws IllegalArgumentException, IllegalStateException;

    /**
     * Sends a command under a specific item name through the event bus in a synchronous way.
     * This method does <i>not</i> return to the caller until all subscribers have processed
     * the command.
     *
     * @param itemName name of the item to send the command for
     *            (must neither be null, nor empty and must follow the general item name specification)
     * @param command the command to send (must not be null)
     * @param source a string identifying the sender. This should usually be the bundle symbolic name.
     *
     * @throws IllegalArgumentException if the item name is null or empty or does not follow
     *             the general item specification, or the command is null
     * @throws IllegalStateException if the underlying event bus module is not available
     *
     * @see #postCommand(String, Command)
     */
    void sendCommand(String itemName, Command command, String source) throws IllegalArgumentException,
            IllegalStateException;

    /**
     * Posts a command under a specific item name through the event bus in an asynchronous way.
     * This method returns immediately to the caller.
     *
     * @param itemName name of the item to send the command for
     *            (must neither be null, nor empty and must follow the general item name specification)
     * @param command the command to send (must not be null)
     *
     * @throws IllegalArgumentException if the item name is null or empty or does not follow
     *             the general item specification, or the command is null
     * @throws IllegalStateException if the underlying event bus module is not available
     *
     * @see #sendCommand(String, Command)
     */
    void postCommand(String itemName, Command command) throws IllegalArgumentException, IllegalStateException;

    /**
     * Posts a command under a specific item name through the event bus in an asynchronous way.
     * This method returns immediately to the caller.
     *
     * @param itemName name of the item to send the command for
     *            (must neither be null, nor empty and must follow the general item name specification)
     * @param command the command to send (must not be null)
     * @param source a string identifying the sender. This should usually be the bundle symbolic name.
     *
     * @throws IllegalArgumentException if the item name is null or empty or does not follow
     *             the general item specification, or the command is null
     * @throws IllegalStateException if the underlying event bus module is not available
     *
     * @see #sendCommand(String, Command)
     */
    void postCommand(String itemName, Command command, String source) throws IllegalArgumentException,
            IllegalStateException;

    /**
     * Posts a status update under a specific item name through the event bus in an asynchronous way.
     * This method returns immediately to the caller.
     *
     * @param itemName name of the item to send the command for
     *            (must neither be null, nor empty and must follow the general item name specification)
     * @param newState the new state to send (must not be null)
     *
     * @throws IllegalArgumentException if the item name is null or empty or does not follow
     *             the general item specification, or the command is null
     * @throws IllegalStateException if the underlying event bus module is not available
     */
    void postUpdate(String itemName, State newState) throws IllegalArgumentException, IllegalStateException;

    /**
     * Posts a status update under a specific item name through the event bus in an asynchronous way.
     * This method returns immediately to the caller.
     *
     * @param itemName name of the item to send the command for
     *            (must neither be null, nor empty and must follow the general item name specification)
     * @param newState the new state to send (must not be null)
     * @param source a string identifying the sender. This should usually be the bundle symbolic name.
     *
     * @throws IllegalArgumentException if the item name is null or empty or does not follow
     *             the general item specification, or the command is null
     * @throws IllegalStateException if the underlying event bus module is not available
     */
    void postUpdate(String itemName, State newState, String source) throws IllegalArgumentException,
            IllegalStateException;

}
