/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ItemRegistryChangeListener;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.link.ItemThingLink;
import org.eclipse.smarthome.core.thing.link.ItemThingLinkRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThingLinkManager} manages links between items and channels and
 * between items things.
 * <p>
 * An item is linked or unlinked to or from a channel on events from {@link ItemChannelLinkRegistry} or
 * {@link ItemRegistry}. An item is linked or unlinked to or from a thing on events from {@link ItemThingLinkRegistry}
 * <p>
 * The {@link ThingLinkManager} is used by the {@link ThingManager}.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Markus Rathgeb - Handle item registry's all items changed notification
 */
public class ThingLinkManager {

    private ItemChannelLinkRegistry itemChannelLinkRegistry;

    private final ItemRegistryChangeListener itemRegistryChangeListener = new ItemRegistryChangeListener() {

        @Override
        public void updated(Item oldElement, Item element) {
            removed(oldElement);
            added(element);
        }

        @Override
        public void removed(Item element) {
            Set<ChannelUID> linkedChannels = itemChannelLinkRegistry.getBoundChannels(element.getName());
            for (ChannelUID channelUID : linkedChannels) {
                Thing thing = thingRegistry.get(channelUID.getThingUID());
                if (thing != null) {
                    Channel channel = thing.getChannel(channelUID.getId());
                    if (channel != null) {
                        channel.removeLinkedItem(element);
                        informHandlerAboutUnlinkedChannel(thing, channel);
                    }
                }
            }
            if (element instanceof GroupItem) {
                Set<ThingUID> linkedThings = itemThingLinkRegistry.getLinkedThings(element.getName());
                for (ThingUID thingUID : linkedThings) {
                    Thing thing = thingRegistry.get(thingUID);
                    if (thing instanceof ThingImpl) {
                        ((ThingImpl) thing).setLinkedItem(null);
                    }
                }
            }
        }

        @Override
        public void added(Item element) {
            Set<ChannelUID> linkedChannels = itemChannelLinkRegistry.getBoundChannels(element.getName());
            for (ChannelUID channelUID : linkedChannels) {
                Thing thing = thingRegistry.get(channelUID.getThingUID());
                if (thing != null) {
                    Channel channel = thing.getChannel(channelUID.getId());
                    if (channel != null) {
                        channel.addLinkedItem(element);
                        informHandlerAboutLinkedChannel(thing, channel);
                    }
                }
            }
            if (element instanceof GroupItem) {
                Set<ThingUID> linkedThings = itemThingLinkRegistry.getLinkedThings(element.getName());
                for (ThingUID thingUID : linkedThings) {
                    Thing thing = thingRegistry.get(thingUID);
                    if (thing instanceof ThingImpl) {
                        ((ThingImpl) thing).setLinkedItem((GroupItem) element);
                    }
                }
            }
        }

        @Override
        public void allItemsChanged(final Collection<String> oldItemNames) {
            /*
             * Find all item channel links for old items (by name) and remove the link to the item from the channel.
             */
            final Collection<ItemChannelLink> itemChannelLinks = itemChannelLinkRegistry.getAll();
            for (final ItemChannelLink itemChannelLink : itemChannelLinks) {
                final String itemName = itemChannelLink.getItemName();
                if (oldItemNames.contains(itemName)) {
                    /*
                     * The current 'itemChannelLink' contains the old item with name 'itemName'. Find the channel object
                     * that contain the link to the item.
                     */
                    final Set<ChannelUID> boundChannels = itemChannelLinkRegistry.getBoundChannels(itemName);
                    for (final ChannelUID channelUID : boundChannels) {
                        final Thing thing = thingRegistry.get(channelUID.getThingUID());
                        if (thing != null) {
                            final Channel channel = thing.getChannel(channelUID.getId());
                            if (channel != null) {
                                /*
                                 * The current 'channel' contains the link to the old item with name 'itemName'. To
                                 * remove the link we need the item object. Collect all items that link needs to be
                                 * removed from 'channel'.
                                 */
                                final Set<Item> oldLinkedItems = new LinkedHashSet<>();
                                final Set<Item> linkedItems = channel.getLinkedItems();
                                for (final Item linkedItem : linkedItems) {
                                    if (linkedItem.getName().equals(itemName)) {
                                        oldLinkedItems.add(linkedItem);
                                    }
                                }
                                /*
                                 * Remove all the old linked items.
                                 */
                                for (final Item oldLinkedItem : oldLinkedItems) {
                                    channel.removeLinkedItem(oldLinkedItem);
                                    informHandlerAboutUnlinkedChannel(thing, channel);
                                }
                            }
                        }
                    }
                }
            }

            /*
             * After all old items are removed, we could call the added handler for all items that are still available.
             */
            final Collection<Item> allCurrentItems = itemRegistry.getAll();
            for (final Item item : allCurrentItems) {
                added(item);
            }
        }
    };

    private final RegistryChangeListener<ItemChannelLink> itemChannelLinkRegistryChangeListener = new RegistryChangeListener<ItemChannelLink>() {

        @Override
        public void added(ItemChannelLink itemChannelLink) {
            ChannelUID channelUID = itemChannelLink.getUID();
            Thing thing = thingRegistry.get(channelUID.getThingUID());
            if (thing != null) {
                Channel channel = thing.getChannel(channelUID.getId());
                if (channel != null) {
                    ThingLinkManager.this.addLinkedItemToChannel(thing, channel, itemChannelLink.getItemName());
                }
            }
        }

        @Override
        public void removed(ItemChannelLink itemChannelLink) {
            ChannelUID channelUID = itemChannelLink.getUID();
            Thing thing = thingRegistry.get(channelUID.getThingUID());
            if (thing != null) {
                Channel channel = thing.getChannel(channelUID.getId());
                if (channel != null) {
                    ThingLinkManager.this.removeLinkedItemFromChannel(thing, channel, itemChannelLink.getItemName());
                }
            }
        }

        @Override
        public void updated(ItemChannelLink oldElement, ItemChannelLink element) {
            if (!oldElement.equals(element)) {
                this.removed(oldElement);
                this.added(element);
            }
        }

    };
    private ItemRegistry itemRegistry;
    private ItemThingLinkRegistry itemThingLinkRegistry;
    private final RegistryChangeListener<ItemThingLink> itemThingLinkRegistryChangeListener = new RegistryChangeListener<ItemThingLink>() {

        @Override
        public void added(ItemThingLink itemThingLink) {
            Thing thing = thingRegistry.get(itemThingLink.getUID());
            if (thing != null) {
                addLinkedItemToThing((ThingImpl) thing, itemThingLink.getItemName());
            }
        }

        @Override
        public void removed(ItemThingLink itemThingLink) {
            Thing thing = thingRegistry.get(itemThingLink.getUID());
            if (thing != null) {
                removeLinkedItemFromThing((ThingImpl) thing);
            }
        }

        @Override
        public void updated(ItemThingLink olditemThingLink, ItemThingLink itemThingLink) {
            added(itemThingLink);
        }

    };

    private Logger logger = LoggerFactory.getLogger(ThingLinkManager.class);
    private ThingRegistry thingRegistry;

    /**
     * Creates a new {@link ThingLinkManager} instance.
     *
     * @param itemRegistry
     *            the {@link ItemRegistry} to listen on change events
     * @param thingRegistry
     *            the {@link ThingRegistry} to access {@link Thing}s
     * @param itemChannelLinkRegistry
     *            the {@link ItemChannelLinkRegistry} to listen on change events
     * @param itemThingLinkRegistry
     *            the {@link ItemThingLinkRegistry} to listen on change events
     */
    public ThingLinkManager(ItemRegistry itemRegistry, ThingRegistry thingRegistry,
            ItemChannelLinkRegistry itemChannelLinkRegistry, ItemThingLinkRegistry itemThingLinkRegistry) {
        this.itemRegistry = itemRegistry;
        this.thingRegistry = thingRegistry;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.itemThingLinkRegistry = itemThingLinkRegistry;
    }

    /**
     * Starts listening on events from {@link ItemRegistry}, {@link ItemChannelLinkRegistry}, and
     * {@link ItemThingLinkRegistry}.
     */
    public void startListening() {
        itemRegistry.addRegistryChangeListener(itemRegistryChangeListener);
        itemChannelLinkRegistry.addRegistryChangeListener(itemChannelLinkRegistryChangeListener);
        itemThingLinkRegistry.addRegistryChangeListener(itemThingLinkRegistryChangeListener);
    }

    /**
     * Stops listening on events from {@link ItemRegistry}, {@link ItemChannelLinkRegistry}, and
     * {@link ItemThingLinkRegistry}.
     */
    public void stopListening() {
        itemRegistry.removeRegistryChangeListener(itemRegistryChangeListener);
        itemChannelLinkRegistry.removeRegistryChangeListener(itemChannelLinkRegistryChangeListener);
        itemThingLinkRegistry.removeRegistryChangeListener(itemThingLinkRegistryChangeListener);
    }

    /**
     * Links {@link Item}s to the {@link Thing} and its {@link Channel}s.
     *
     * @param thing
     *            the added {@link Thing} to create links for.
     */
    public void thingAdded(Thing thing) {
        String itemName = this.getFirstLinkedItem(thing.getUID());
        if (itemName != null) {
            addLinkedItemToThing((ThingImpl) thing, itemName);
        }
        List<Channel> channels = thing.getChannels();
        for (Channel channel : channels) {
            Set<String> linkedItems = itemChannelLinkRegistry.getLinkedItems(channel.getUID());
            for (String linkedItem : linkedItems) {
                addLinkedItemToChannel(thing, channel, linkedItem);
            }
        }
    }

    /**
     * Unlinks {@link Item}s from the {@link Thing} and its {@link Channel}s.
     *
     * @param thing
     *            the removed {@link Thing} to remove links for.
     */
    public void thingRemoved(Thing thing) {
        removeLinkedItemFromThing((ThingImpl) thing);
        List<Channel> channels = thing.getChannels();
        for (Channel channel : channels) {
            Set<String> linkedItems = itemChannelLinkRegistry.getLinkedItems(channel.getUID());
            for (String linkedItem : linkedItems) {
                removeLinkedItemFromChannel(thing, channel, linkedItem);
            }
        }
    }

    /**
     * Updates links from {@link Item}s from the {@link Thing} and its {@link Channel}s.
     *
     * @param thing the updated {@link Thing} to update links for.
     */
    public void thingUpdated(Thing thing) {
        // TODO: better implement a diff!
        thingRemoved(thing);
        thingAdded(thing);
    }

    private void addLinkedItemToChannel(Thing thing, Channel channel, String itemName) {
        try {
            Item item = itemRegistry.getItem(itemName);
            logger.debug("Adding linked item '{}' to channel '{}'.", item.getName(), channel.getUID());
            channel.addLinkedItem(item);
            informHandlerAboutLinkedChannel(thing, channel);
        } catch (ItemNotFoundException ignored) {
        }
    }

    private void addLinkedItemToThing(ThingImpl thing, String itemName) {
        try {
            Item item = itemRegistry.getItem(itemName);
            if (item instanceof GroupItem) {
                logger.debug("Assigning linked group item '{}' to thing '{}'.", item.getName(), thing.getUID());
                thing.setLinkedItem((GroupItem) item);
            }
        } catch (ItemNotFoundException ignored) {
        }
    }

    private void removeLinkedItemFromChannel(Thing thing, Channel channel, String itemName) {
        try {
            Item item = itemRegistry.getItem(itemName);
            logger.debug("Removing linked item '{}' from channel '{}'.", item.getName(), channel.getUID());
            channel.removeLinkedItem(item);
            informHandlerAboutUnlinkedChannel(thing, channel);
        } catch (ItemNotFoundException ignored) {
        }
    }

    private void informHandlerAboutLinkedChannel(Thing thing, Channel channel) {
        ThingHandler handler = thing.getHandler();
        if (handler != null) {
            try {
                handler.channelLinked(channel.getUID());
            } catch (Exception ex) {
                logger.error("Exception occured while informing handler:" + ex.getMessage(), ex);
            }
        } else {
            logger.trace("Can not inform handler about linked channel, because no handler is assigned to the thing {}.",
                    thing.getUID());
        }
    }

    private void informHandlerAboutUnlinkedChannel(Thing thing, Channel channel) {
        ThingHandler handler = thing.getHandler();
        if (handler != null) {
            try {
                handler.channelUnlinked(channel.getUID());
            } catch (Exception ex) {
                logger.error("Exception occured while informing handler:" + ex.getMessage(), ex);
            }
        } else {
            logger.trace(
                    "Can not inform handler about unlinked channel, because no handler is assigned to the thing {}.",
                    thing.getUID());
        }
    }

    private void removeLinkedItemFromThing(ThingImpl thing) {
        if (thing.getLinkedItem() != null) {
            logger.debug("Removing linked group item from thing '{}'.", thing.getUID());
            thing.setLinkedItem(null);
        }
    }

    private String getFirstLinkedItem(UID uid) {
        for (ItemThingLink link : itemThingLinkRegistry.getAll()) {
            if (link.getUID().equals(uid)) {
                return link.getItemName();
            }
        }
        return null;
    }
}
