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
import org.eclipse.smarthome.core.items.ItemUtil;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.util.ThingHandlerHelper;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ProfileCallback} implementation.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class ProfileCallbackImpl implements ProfileCallback {

    private final Logger logger = LoggerFactory.getLogger(ProfileCallbackImpl.class);

    private final EventPublisher eventPublisher;
    private final Thing thing;
    private final ItemChannelLink link;
    private final Item item;

    public ProfileCallbackImpl(EventPublisher eventPublisher, ItemChannelLink link, Thing thing, Item item) {
        this.eventPublisher = eventPublisher;
        this.link = link;
        this.thing = thing;
        this.item = item;
    }

    @Override
    public void handleCommand(Command command) {
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
    public void handleUpdate(State state) {
        if (thing != null) {
            final ThingHandler handler = thing.getHandler();
            if (handler != null) {
                if (ThingHandlerHelper.isHandlerInitialized(thing)) {
                    logger.debug("Delegating update '{}' for item '{}' to handler for channel '{}'", state,
                            link.getItemName(), link.getLinkedUID());
                    try {
                        SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {
                            @Override
                            public Void call() throws Exception {
                                handler.handleUpdate(link.getLinkedUID(), state);
                                return null;
                            }
                        });
                    } catch (TimeoutException ex) {
                        logger.warn("Handler for thing '{}' takes more than {}ms for handling an update",
                                handler.getThing().getUID(), SafeMethodCaller.DEFAULT_TIMEOUT);
                    } catch (Exception ex) {
                        logger.error("Exception occurred while calling handler: {}", ex.getMessage(), ex);
                    }
                } else {
                    logger.debug("Not delegating update '{}' for item '{}' to handler for channel '{}', "
                            + "because handler is not initialized (thing must be in status UNKNOWN, ONLINE or OFFLINE but was {}).",
                            state, link.getItemName(), link.getLinkedUID(), thing.getStatus());
                }
            } else {
                logger.warn("Cannot delegate update '{}' for item '{}' to handler for channel '{}', "
                        + "because no handler is assigned. Maybe the binding is not installed or not "
                        + "propertly initialized.", state, link.getItemName(), link.getLinkedUID());
            }
        } else {
            logger.warn(
                    "Cannot delegate update '{}' for item '{}' to handler for channel '{}', "
                            + "because no thing with the UID '{}' could be found.",
                    state, link.getItemName(), link.getLinkedUID(), link.getLinkedUID().getThingUID());
        }
    }

    @Override
    public void sendCommandEvent(Command command) {
        eventPublisher
                .post(ItemEventFactory.createCommandEvent(link.getItemName(), command, link.getLinkedUID().toString()));
    }

    @Override
    public void sendStateEvent(State state) {
        State acceptedState = ItemUtil.convertToAcceptedState(state, item);
        eventPublisher.post(
                ItemEventFactory.createStateEvent(link.getItemName(), acceptedState, link.getLinkedUID().toString()));
    }

}
