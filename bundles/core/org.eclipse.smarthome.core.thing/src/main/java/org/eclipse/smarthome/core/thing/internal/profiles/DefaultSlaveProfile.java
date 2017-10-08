/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal.profiles;

import java.util.concurrent.TimeoutException;

import org.eclipse.smarthome.core.common.SafeMethodCaller;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfile;
import org.eclipse.smarthome.core.thing.util.ThingHandlerHelper;
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
 * The ThingHandler may send commands to the framework, but no state updates are forwarded.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class DefaultSlaveProfile implements StateProfile {

    private final Logger logger = LoggerFactory.getLogger(DefaultSlaveProfile.class);
    public static final ProfileTypeUID UID = new ProfileTypeUID(ProfileTypeUID.SYSTEM_SCOPE, "slave", "Slave");

    @Override
    public void onUpdate(ItemChannelLink link, Thing thing, State state) {
        if (!(state instanceof Command)) {
            logger.debug("The given state {} could not be transformed to a command for channel {}.", state,
                    link.getLinkedUID());
            return;
        }
        Command command = (Command) state;
        if (thing != null) {
            final ThingHandler handler = thing.getHandler();
            if (handler != null) {
                if (ThingHandlerHelper.isHandlerInitialized(thing)) {
                    logger.debug("Delegating command '{}' for item '{}' to handler for channel '{}'", command,
                            link.getItemName(), link.getLinkedUID());
                    try {
                        SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {
                            @Override
                            public Void call() throws Exception {
                                handler.handleCommand(link.getLinkedUID(), command);
                                return null;
                            }
                        });
                    } catch (TimeoutException ex) {
                        logger.warn("Handler for thing '{}' takes more than {}ms for handling a command",
                                handler.getThing().getUID(), SafeMethodCaller.DEFAULT_TIMEOUT);
                    } catch (Exception ex) {
                        logger.error("Exception occurred while calling handler: {}", ex.getMessage(), ex);
                    }
                } else {
                    logger.debug("Not delegating command '{}' for item '{}' to handler for channel '{}', "
                            + "because handler is not initialized (thing must be in status UNKNOWN, ONLINE or OFFLINE but was {}).",
                            command, link.getItemName(), link.getLinkedUID(), thing.getStatus());
                }
            } else {
                logger.warn("Cannot delegate command '{}' for item '{}' to handler for channel '{}', "
                        + "because no handler is assigned. Maybe the binding is not installed or not "
                        + "propertly initialized.", command, link.getItemName(), link.getLinkedUID());
            }
        } else {
            logger.warn(
                    "Cannot delegate command '{}' for item '{}' to handler for channel '{}', "
                            + "because no thing with the UID '{}' could be found.",
                    command, link.getItemName(), link.getLinkedUID(), link.getLinkedUID().getThingUID());
        }
    }

    @Override
    public void postCommand(EventPublisher eventPublisher, ItemChannelLink link, Command command, Item item) {
        eventPublisher
                .post(ItemEventFactory.createCommandEvent(link.getItemName(), command, link.getLinkedUID().toString()));
    }

    @Override
    public void onCommand(ItemChannelLink link, Thing thing, Command command) {
        // no-op
    }

    @Override
    public void stateUpdated(EventPublisher eventPublisher, ItemChannelLink link, State state, Item item) {
        // no-op
    }

}
