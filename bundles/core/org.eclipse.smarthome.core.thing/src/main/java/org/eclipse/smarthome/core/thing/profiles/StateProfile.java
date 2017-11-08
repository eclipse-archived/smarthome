/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.profiles;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * A {@link StateProfile} defined the communication for channels of STATE kind.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public interface StateProfile extends Profile {

    /**
     * Will be called if a command should be forwarded to the binding.
     *
     * @param link
     * @param thing
     * @param command
     */
    void onCommand(@NonNull ItemChannelLink link, Thing thing, Command command);

    /**
     * Will be called if an item has changed its state and this information should be forwarded to the binding.
     *
     * @param link
     * @param thing
     * @param state
     */
    void onUpdate(@NonNull ItemChannelLink link, Thing thing, State state);

    /**
     * If the binding indicated a state update on a channel, then this method will be called for each linked item.
     *
     * @param eventPublisher
     * @param link
     * @param state
     * @param item
     */
    void stateUpdated(@NonNull EventPublisher eventPublisher, @NonNull ItemChannelLink link, State state,
            @NonNull Item item);

    /**
     * If a binding issued a command to a channel, this method will be called for each linked item.
     *
     * @param eventPublisher
     * @param link
     * @param command
     * @param item
     */
    void postCommand(@NonNull EventPublisher eventPublisher, @NonNull ItemChannelLink link, Command command,
            @NonNull Item item);

}
