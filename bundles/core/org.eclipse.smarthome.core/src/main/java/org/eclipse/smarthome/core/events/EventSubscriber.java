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
 * The {@link EventSubscriber} defines the callback interface for receiving events from
 * the <i>Eclipse SmartHome</i> event bus.
 * <p>
 * Any events containing a command or a status update can be received by this listener interface. Use the
 * {@link AbstractEventSubscriber} class as concrete implementation of this listener interface.
 * <p>
 * For further information about sending events through the event bus check the {@link EventPublisher} service
 * specification.
 *
 * @see EventPublisher
 * @see AbstractEventSubscriber
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Michael Grammling - Javadoc extended, Checkstyle compliancy
 */
public interface EventSubscriber {

    /**
     * Callback method if a command was sent on the event bus.
     * <p>
     * Any exceptions, which may occur in this callback method, are caught and logged.
     * <p>
     * Hint: Do not block the reception of this event for long-term tasks. For long-term tasks create an own thread.
     *
     * @param itemName the item for which a command was sent
     *            (not null, not empty, follows the item name specification)
     *
     * @param command the command that was sent (not null)
     */
    void receiveCommand(String itemName, Command command);

    /**
     * Callback method if a state update was sent on the event bus.
     * <p>
     * Any exceptions, which may occur in this callback method, are caught and logged.
     * <p>
     * Hint: Do not block the reception of this event for long-term tasks. For long-term tasks create an own thread.
     *
     * @param itemName the item for which a command was sent
     *            (not null, not empty, follows the item name specification)
     *
     * @param state the state that was sent (not null)
     */
    void receiveUpdate(String itemName, State newStatus);

}
