/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.items;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.AbstractItemEventSubscriber;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ItemUpdater listens on the event bus and passes any received status update
 * to the item registry.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Stefan Bußweiler - Migration to new ESH event concept
 */
public class ItemUpdater extends AbstractItemEventSubscriber {

    private final Logger logger = LoggerFactory.getLogger(ItemUpdater.class);

    private ItemRegistry itemRegistry;

    private EventPublisher eventPublisher;

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    @Override
    protected void receiveUpdate(ItemStateEvent updateEvent) {
        if (itemRegistry != null) {
            String itemName = updateEvent.getItemName();
            State newState = updateEvent.getItemState();
            try {
                GenericItem item = (GenericItem) itemRegistry.getItem(itemName);
                boolean isAccepted = false;
                if (item.getAcceptedDataTypes().contains(newState.getClass())) {
                    isAccepted = true;
                } else {
                    // Look for class hierarchy
                    for (Class<? extends State> state : item.getAcceptedDataTypes()) {
                        try {
                            if (!state.isEnum()
                                    && state.newInstance().getClass().isAssignableFrom(newState.getClass())) {
                                isAccepted = true;
                                break;
                            }
                        } catch (InstantiationException e) {
                            logger.warn("InstantiationException on {}", e.getMessage()); // Should never happen
                        } catch (IllegalAccessException e) {
                            logger.warn("IllegalAccessException on {}", e.getMessage()); // Should never happen
                        }
                    }
                }
                if (isAccepted) {
                    State oldState = item.getState();
                    item.setState(newState);
                    if (!oldState.equals(newState)) {
                        EventPublisher eventPublisher = this.eventPublisher;
                        if (eventPublisher != null) {
                            eventPublisher.post(ItemEventFactory.createStateChangedEvent(itemName, newState, oldState));
                        }
                    }
                } else {
                    logger.debug("Received update of a not accepted type (" + newState.getClass().getSimpleName()
                            + ") for item " + itemName);
                }
            } catch (ItemNotFoundException e) {
                logger.debug("Received update for non-existing item: {}", e.getMessage());
            }
        }
    }

    @Override
    protected void receiveCommand(ItemCommandEvent commandEvent) {
        // if the item is a group, we have to pass the command to it as it needs to pass the command to its members
        if (itemRegistry != null) {
            try {
                Item item = itemRegistry.getItem(commandEvent.getItemName());
                if (item instanceof GroupItem) {
                    GroupItem groupItem = (GroupItem) item;
                    groupItem.send(commandEvent.getItemCommand());
                }
            } catch (ItemNotFoundException e) {
                logger.debug("Received command for non-existing item: {}", e.getMessage());
            }
        }
    }

}
