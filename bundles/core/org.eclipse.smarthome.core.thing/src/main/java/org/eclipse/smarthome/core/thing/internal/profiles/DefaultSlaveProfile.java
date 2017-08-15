/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal.profiles;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the default implementation for a slave profile.
 *
 * In contrast to the {@link DefaultMasterProfile} it does not forward any commands to the ThingHandler. Instead, it
 * turn {@link State} updates into {@link Command}s (if possible) and then forwards those to the {@link ThingHandler}.
 * <p>
 * This allows devices to be operated as "slaves" of another one directly, without the need to write any rules.
 * <p>
 * No events are send for state updates or commands initiated by the ThingHandler. They are assumed to be passive only.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class DefaultSlaveProfile extends DefaultMasterProfile {

    private final Logger logger = LoggerFactory.getLogger(DefaultSlaveProfile.class);

    @Override
    public void onUpdate(ItemChannelLink link, Thing thing, State state) {
        if (!(state instanceof Command)) {
            return;
        }
        Command command = (Command) state;
        super.onCommand(link, thing, command);
    }

    @Override
    public void onCommand(@NonNull ItemChannelLink link, Thing thing, Command command) {
        // no-op
    }

    @Override
    public void stateUpdated(@NonNull EventPublisher eventPublisher, @NonNull ItemChannelLink link, State state,
            @NonNull Item item) {
        // no-op
    }

    @Override
    public void postCommand(@NonNull EventPublisher eventPublisher, @NonNull ItemChannelLink link, Command command,
            @NonNull Item item) {
        // no-op
    }

}
