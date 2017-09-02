/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.eclipse.smarthome.core.common.SafeMethodCaller;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemUtil;
import org.eclipse.smarthome.core.items.events.AbstractItemEventSubscriber;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.events.ThingEventFactory;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.util.ThingHandlerHelper;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages the state related communication between bindings and the framework.
 *
 * It mainly mediates commands, state updates and triggers from ThingHandlers to the framework and vice versa.
 *
 * @author Simon Kaufmann - initial contribution and API, factored out of ThingManger
 *
 */
@Component(service = { EventSubscriber.class, CommunicationManager.class }, immediate = true)
public class CommunicationManager extends AbstractItemEventSubscriber {

    private final Logger logger = LoggerFactory.getLogger(CommunicationManager.class);

    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private ThingRegistry thingRegistry;
    private EventPublisher eventPublisher;

    private Thing getThing(ThingUID thingUID) {
        return thingRegistry.get(thingUID);
    }

    @Override
    protected void receiveCommand(ItemCommandEvent commandEvent) {
        String itemName = commandEvent.getItemName();
        final Command command = commandEvent.getItemCommand();
        Set<ChannelUID> boundChannels = this.itemChannelLinkRegistry.getBoundChannels(itemName);
        for (final ChannelUID channelUID : boundChannels) {
            // make sure a command event is not sent back to its source
            if (!channelUID.toString().equals(commandEvent.getSource())) {
                Thing thing = getThing(channelUID.getThingUID());
                if (thing != null) {
                    final ThingHandler handler = thing.getHandler();
                    if (handler != null) {
                        if (ThingHandlerHelper.isHandlerInitialized(thing)) {
                            logger.debug("Delegating command '{}' for item '{}' to handler for channel '{}'", command,
                                    itemName, channelUID);
                            try {
                                SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {
                                    @Override
                                    public Void call() throws Exception {
                                        handler.handleCommand(channelUID, command);
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
                                    + "because handler is not initialized (thing must be in status UNKNOWN, ONLINE or OFFLINE).",
                                    command, itemName, channelUID);
                        }
                    } else {
                        logger.warn("Cannot delegate command '{}' for item '{}' to handler for channel '{}', "
                                + "because no handler is assigned. Maybe the binding is not installed or not "
                                + "propertly initialized.", command, itemName, channelUID);
                    }
                } else {
                    logger.warn(
                            "Cannot delegate command '{}' for item '{}' to handler for channel '{}', "
                                    + "because no thing with the UID '{}' could be found.",
                            command, itemName, channelUID, channelUID.getThingUID());
                }
            }
        }
    }

    @Override
    protected void receiveUpdate(ItemStateEvent updateEvent) {
        String itemName = updateEvent.getItemName();
        final State newState = updateEvent.getItemState();
        Set<ChannelUID> boundChannels = this.itemChannelLinkRegistry.getBoundChannels(itemName);
        for (final ChannelUID channelUID : boundChannels) {
            // make sure an update event is not sent back to its source
            if (!channelUID.toString().equals(updateEvent.getSource())) {
                Thing thing = getThing(channelUID.getThingUID());
                if (thing != null) {
                    final ThingHandler handler = thing.getHandler();
                    if (handler != null) {
                        if (ThingHandlerHelper.isHandlerInitialized(thing)) {
                            logger.debug("Delegating update '{}' for item '{}' to handler for channel '{}'", newState,
                                    itemName, channelUID);
                            try {
                                SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {
                                    @Override
                                    public Void call() throws Exception {
                                        if (newState != null) {
                                            handler.handleUpdate(channelUID, newState);
                                        } else {
                                            throw new IllegalStateException(
                                                    "Trying to set state to null on channel " + channelUID);
                                        }
                                        return null;
                                    }
                                });
                            } catch (TimeoutException ex) {
                                logger.warn("Handler for thing {} takes more than {}ms for handling an update",
                                        handler.getThing().getUID(), SafeMethodCaller.DEFAULT_TIMEOUT);
                            } catch (Exception ex) {
                                logger.error("Exception occurred while calling handler: {}", ex.getMessage(), ex);
                            }
                        } else {
                            logger.debug("Not delegating update '{}' for item '{}' to handler for channel '{}', "
                                    + "because handler is not initialized (thing must be in status UNKNOWN, ONLINE or OFFLINE).",
                                    newState, itemName, channelUID);
                        }
                    } else {
                        logger.warn("Cannot delegate update '{}' for item '{}' to handler for channel '{}', "
                                + "because no handler is assigned. Maybe the binding is not installed or not "
                                + "propertly initialized.", newState, itemName, channelUID);
                    }
                } else {
                    logger.warn(
                            "Cannot delegate update '{}' for item '{}' to handler for channel '{}', "
                                    + "because no thing with the UID '{}' could be found.",
                            newState, itemName, channelUID, channelUID.getThingUID());
                }
            }
        }
    }

    public void stateUpdated(ChannelUID channelUID, State state) {
        Set<Item> items = itemChannelLinkRegistry.getLinkedItems(channelUID);
        for (Item item : items) {
            State acceptedState = ItemUtil.convertToAcceptedState(state, item);
            eventPublisher
                    .post(ItemEventFactory.createStateEvent(item.getName(), acceptedState, channelUID.toString()));
        }
    }

    public void postCommand(ChannelUID channelUID, Command command) {
        Set<String> items = itemChannelLinkRegistry.getLinkedItemNames(channelUID);
        for (String item : items) {
            eventPublisher.post(ItemEventFactory.createCommandEvent(item, command, channelUID.toString()));
        }
    }

    public void channelTriggered(Thing thing, ChannelUID channelUID, String event) {
        eventPublisher.post(ThingEventFactory.createTriggerEvent(event, channelUID));
    }

    @Reference
    protected void setItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    protected void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = null;
    }

    @Reference
    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    @Reference
    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

}
