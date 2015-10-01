/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.setup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.events.TopicEventFilter;
import org.eclipse.smarthome.core.items.ActiveItem;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.events.ThingRemovedEvent;
import org.eclipse.smarthome.core.thing.internal.ThingManager;
import org.eclipse.smarthome.core.thing.link.AbstractLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.link.ItemThingLink;
import org.eclipse.smarthome.core.thing.link.ItemThingLinkRegistry;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link ThingSetupManager} provides various method to manage things. In
 * contrast to the {@link ThingRegistry}, the {@link ThingManager} also creates
 * Items and Links automatically and removes it, when the according thing is
 * removed.
 *
 * @author Dennis Nobel - Initial contribution, changed way of receiving channel types
 * @author Alex Tugarev - addThing operation returns created Thing instance
 * @author Chris Jackson - Remove children when deleted bridge. Add label/description.
 */
public class ThingSetupManager implements EventSubscriber {

    public static final String TAG_CHANNEL_GROUP = "channel-group";
    public static final String TAG_HOME_GROUP = "home-group";
    public static final String TAG_THING = "thing";

    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private List<ItemFactory> itemFactories = new CopyOnWriteArrayList<>();
    private ItemRegistry itemRegistry;
    private ItemThingLinkRegistry itemThingLinkRegistry;
    private final Logger logger = LoggerFactory.getLogger(ThingSetupManager.class);
    private List<ThingHandlerFactory> thingHandlerFactories = new CopyOnWriteArrayList<>();
    private ThingRegistry thingRegistry;
    private ThingTypeRegistry thingTypeRegistry;

    private final Set<String> subscribedEventTypes = ImmutableSet.of(ThingRemovedEvent.TYPE);
    private final EventFilter eventFiter = new TopicEventFilter("smarthome/things/.*/removed");

    /**
     * Adds a group to the system with the a 'home-group' tag.
     *
     * @param itemName
     *            name of group to be added
     * @param label
     *            label of the group to be added
     * @return created {@link GroupItem} instance
     */
    public GroupItem addHomeGroup(String itemName, String label) {
        GroupItem groupItem = new GroupItem(itemName);
        groupItem.setLabel(label);
        groupItem.addTag(TAG_HOME_GROUP);
        addItemSafely(groupItem);
        return groupItem;
    }

    /**
     * Adds a thing without a label, without group names, but enables all
     * channels.
     *
     * See {@link ThingSetupManager#addThing(ThingUID, Configuration, ThingUID, String, List, boolean)}
     *
     * @return created {@link Thing} instance (can be null)
     */
    public Thing addThing(ThingUID thingUID, Configuration configuration, ThingUID bridgeUID) {
        return addThing(thingUID, configuration, bridgeUID, null);
    }

    /**
     * Adds a thing without group names, but enables all channels.
     *
     * See {@link ThingSetupManager#addThing(ThingUID, Configuration, ThingUID, String, List, boolean)}
     *
     * @return created {@link Thing} instance (can be null)
     */
    public Thing addThing(ThingUID thingUID, Configuration configuration, ThingUID bridgeUID, String label) {
        return addThing(thingUID, configuration, bridgeUID, label, new ArrayList<String>());
    }

    /**
     * Adds a thing and enables all channels.
     *
     * See {@link ThingSetupManager#addThing(ThingUID, Configuration, ThingUID, String, List, boolean)}
     *
     * @return created {@link Thing} instance (can be null)
     */
    public Thing addThing(ThingUID thingUID, Configuration configuration, ThingUID bridgeUID, String label,
            List<String> groupNames) {
        return addThing(thingUID, configuration, bridgeUID, label, groupNames, true);
    }

    /**
     * Adds a new thing to the system and creates the according items and links.
     *
     * @param thingUID
     *            UID of the thing (must not be null)
     * @param configuration
     *            configuration (must not be null)
     * @param bridgeUID
     *            bridge UID (can be null)
     * @param label
     *            label (can be null)
     * @param groupNames
     *            list of group names, in which the thing should be added as
     *            member (must not be null)
     * @param enableChannels
     *            defines if all not 'advanced' channels should be enabled
     *            directly
     * @return created {@link Thing} instance (can be null)
     */
    public Thing addThing(ThingUID thingUID, Configuration configuration, ThingUID bridgeUID, String label,
            List<String> groupNames, boolean enableChannels) {

        if (thingUID == null) {
            throw new IllegalArgumentException("Thing UID must not be null");
        }

        ThingTypeUID thingTypeUID = thingUID.getThingTypeUID();

        return addThing(thingTypeUID, thingUID, configuration, bridgeUID, label, groupNames, enableChannels);
    }

    /**
     * Adds a new thing to the system and creates the according items and links.
     *
     * @param thingUID
     *            UID of the thing (must not be null)
     * @param configuration
     *            configuration (must not be null)
     * @param bridgeUID
     *            bridge UID (can be null)
     * @param label
     *            label (can be null)
     * @param groupNames
     *            list of group names, in which the thing should be added as
     *            member (must not be null)
     * @param enableChannels
     *            defines if all not 'advanced' channels should be enabled
     *            directly
     * @param properties
     *            map of properties to be added to the thing (can be null)
     * @return created {@link Thing} instance (can be null)
     */
    public Thing addThing(ThingUID thingUID, Configuration configuration, ThingUID bridgeUID, String label,
            List<String> groupNames, boolean enableChannels, Map<String, String> properties) {

        if (thingUID == null) {
            throw new IllegalArgumentException("Thing UID must not be null");
        }

        ThingTypeUID thingTypeUID = thingUID.getThingTypeUID();

        return addThing(thingTypeUID, thingUID, configuration, bridgeUID, label, groupNames, enableChannels,
                properties);
    }

    /**
     * Adds a new thing to the system and creates the according items and links.
     *
     * @param thingTypeUID
     *            UID of the thing type (must not be null)
     * @param configuration
     *            configuration (must not be null)
     * @param bridgeUID
     *            bridge UID (can be null)
     * @param label
     *            label (can be null)
     * @param groupNames
     *            list of group names, in which the thing should be added as
     *            member (must not be null)
     * @param enableChannels
     *            defines if all not 'advanced' channels should be enabled
     *            directly
     * @return created {@link Thing} instance (can be null)
     */
    public Thing addThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID bridgeUID, String label,
            List<String> groupNames, boolean enableChannels) {
        return addThing(thingTypeUID, null, configuration, bridgeUID, label, groupNames, enableChannels);
    }

    /**
     * Adds a new thing to the system and creates the according items and links.
     *
     * @param thingTypeUID
     *            UID of the thing type (must not be null)
     * @param configuration
     *            configuration (must not be null)
     * @param bridgeUID
     *            bridge UID (can be null)
     * @param label
     *            label (can be null)
     * @param groupNames
     *            list of group names, in which the thing should be added as
     *            member (must not be null)
     * @param enableChannels
     *            defines if all not 'advanced' channels should be enabled
     *            directly
     * @param properties
     *            map of properties to be added to the thing (can be null)
     * @return created {@link Thing} instance (can be null)
     */
    public Thing addThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID bridgeUID, String label,
            List<String> groupNames, boolean enableChannels, Map<String, String> properties) {
        return addThing(thingTypeUID, null, configuration, bridgeUID, label, groupNames, enableChannels, properties);
    }

    /**
     * Adds the given item to the given group.
     *
     * @param itemName
     *            item name (must not be null)
     * @param groupItemName
     *            group item name (must not be null)
     */
    public void addToHomeGroup(String itemName, String groupItemName) {
        ActiveItem item = (ActiveItem) this.itemRegistry.get(itemName);
        if (item != null) {
            item.addGroupName(groupItemName);
            this.itemRegistry.update(item);
        } else {
            logger.warn("Could not add item '{}' to group '{}', because thing is not linked.", itemName, groupItemName);
        }
    }

    /**
     * Adds a thing identified by the given thing UID to the given group.
     *
     * @param thingUID
     *            thing UID (must not be null)
     * @param groupItemName
     *            group item name (must not be null)
     */
    public void addToHomeGroup(ThingUID thingUID, String groupItemName) {
        String linkedItem = getFirstLinkedItem(thingUID);
        if (linkedItem != null) {
            addToHomeGroup(linkedItem, groupItemName);
        } else {
            logger.warn("Could not add thing '{}' to group '{}', because thing is not linked.", thingUID,
                    groupItemName);
        }
    }

    /**
     * Disables the channel with the given UID (removes the linked item).
     *
     * @param channelUID
     *            channel UID (must not be null)
     */
    public void disableChannel(ChannelUID channelUID) {
        Collection<ItemChannelLink> itemChannelLinks = this.itemChannelLinkRegistry.getAll();
        for (ItemChannelLink itemChannelLink : itemChannelLinks) {
            if (itemChannelLink.getUID().equals(channelUID)) {
                String itemName = itemChannelLink.getItemName();
                itemRegistry.remove(itemName);
                itemChannelLinkRegistry.remove(itemChannelLink.getID());
            }
        }
    }

    /**
     * Enables the channel with the given UID (adds a linked item).
     *
     * @param channelUID
     *            channel UID (must not be null)
     */
    public void enableChannel(ChannelUID channelUID) {
        // Get the thing so we can check if channel labels are defined
        Thing thing = getThing(channelUID.getThingUID());
        if (thing == null) {
            logger.warn("Could not enable channel '{}', because no thing was found.", channelUID);
            return;
        }

        // Get the channel,
        Channel channel = thing.getChannel(channelUID.getId());
        if (channel == null) {
            logger.warn("Could not enable channel '{}', because no channel was found.", channelUID);
            return;
        }

        // Enable the channel
        ChannelType channelType = thingTypeRegistry.getChannelType(channelUID);
        if (channelType != null) {
            String itemType = channelType.getItemType();
            ItemFactory itemFactory = getItemFactoryForItemType(itemType);
            if (itemFactory != null) {
                String itemName = toItemName(channelUID);
                GenericItem item = itemFactory.createItem(itemType, itemName);
                if (item != null) {
                    String thingGroupItemName = getThingGroupItemName(channelUID);
                    if (thingGroupItemName != null) {
                        if (!channelUID.isInGroup()) {
                            item.addGroupName(thingGroupItemName);
                        } else {
                            item.addGroupName(getChannelGroupItemName(thingGroupItemName, channelUID.getGroupId()));
                        }
                    }
                    item.addTags(channelType.getTags());
                    item.setCategory(channelType.getCategory());

                    // Set the label - use the thing defined label if it is set
                    if (channel.getLabel() != null) {
                        item.setLabel(channel.getLabel());
                    } else {
                        item.setLabel(channelType.getLabel());
                    }
                    addItemSafely(item);
                    addItemChannelLinkSafely(new ItemChannelLink(itemName, channelUID));
                }
            }
        } else {
            logger.warn("Could not enable channel '{}', because no channel type was found.", channelUID);
        }
    }

    /**
     * Returns a list of all group items (items with tag 'home-group').
     *
     * @return list of all group items (can not be null)
     */
    public Collection<GroupItem> getHomeGroups() {
        List<GroupItem> homeGroupItems = new ArrayList<>();
        for (Item item : this.itemRegistry.getAll()) {
            if (item instanceof GroupItem && ((GroupItem) item).hasTag(TAG_HOME_GROUP)) {
                homeGroupItems.add((GroupItem) item);
            }
        }
        return homeGroupItems;
    }

    /**
     * Returns a thing for a given UID.
     *
     * @return thing or null, if thing with UID does not exists
     */
    public Thing getThing(ThingUID thingUID) {
        return this.thingRegistry.get(thingUID);
    }

    /**
     * Returns all things.
     *
     * @return things
     */
    public Collection<Thing> getThings() {
        return thingRegistry.getAll();
    }

    /**
     * Removes the given item from the given group.
     *
     * @param itemName
     *            item name (must not be null)
     * @param groupItemName
     *            group item name (must not be null)
     */
    public void removeFromHomeGroup(String itemName, String groupItemName) {
        ActiveItem item = (ActiveItem) this.itemRegistry.get(itemName);
        item.removeGroupName(groupItemName);
        this.itemRegistry.update(item);
    }

    /**
     * Removes the thing identified by the given thing UID from the given group.
     *
     * @param thingUID
     *            thing UID (must not be null)
     * @param groupItemName
     *            group item name (must not be null)
     */
    public void removeFromHomeGroup(ThingUID thingUID, String groupItemName) {
        String linkedItem = getFirstLinkedItem(thingUID);
        if (linkedItem != null) {
            removeFromHomeGroup(linkedItem, groupItemName);
        }
    }

    /**
     * Removes the home group identified by the given itemName from the system.
     *
     * @param itemName
     *            name of group to be added
     */
    public void removeHomeGroup(String itemName) {
        itemRegistry.remove(itemName);
    }

    /**
     * Calls {@link ThingSetupManager#removeThing(ThingUID, boolean)} with force = false.
     */
    public void removeThing(ThingUID thingUID) {
        removeThing(thingUID, false);
    }

    /**
     * Remove a thing and all its links and items.
     * If this is a bridge, also remove child things by calling
     * removeThing recursively
     *
     * @param thingUID thing UID
     * @param force if the thing should be removed without asking the binding
     */
    public void removeThing(ThingUID thingUID, boolean force) {
        // Lookup the thing in the registry
        Thing thing = thingRegistry.get(thingUID);
        if (thing == null) {
            return;
        }

        // If this is a bridge, remove all child things, their items and links
        if (thing instanceof Bridge) {
            Bridge bridge = (Bridge) thing;
            for (Thing bridgeThing : bridge.getThings()) {
                ThingUID bridgeThingUID = bridgeThing.getUID();
                removeThing(bridgeThingUID, force);
            }
        }

        if (force) {
            thingRegistry.forceRemove(thingUID);
        } else {
            thingRegistry.remove(thingUID);
        }
    }

    /**
     * Sets the label for a given thing UID.
     *
     * @param thingUID
     *            thing UID (must not be null)
     * @param label
     *            label (can be null)
     */
    public void setLabel(ThingUID thingUID, String label) {
        Thing thing = thingRegistry.get(thingUID);
        GroupItem groupItem = thing.getLinkedItem();
        if (groupItem != null) {
            if (label != null && label.equals(groupItem.getLabel())) {
                return;
            }
            groupItem.setLabel(label);
            itemRegistry.update(groupItem);
        } else {
            throw new IllegalArgumentException("No item is linked with thing '" + thingUID.toString() + "'.");
        }
    }

    /**
     * Sets the given label for the home group identified by the given item name.
     *
     * @param itemName the name of the home group
     * @param label the new label for the home group
     */
    public void setHomeGroupLabel(String itemName, String label) {
        GroupItem groupItem = (GroupItem) itemRegistry.get(itemName);
        if (groupItem == null) {
            throw new IllegalArgumentException("No group item found with item name " + itemName);
        }
        groupItem.setLabel(label);
        itemRegistry.update(groupItem);
    }

    /**
     * Updates an item.
     *
     * @param item
     *            item (must not be null)
     */
    public void updateItem(Item item) {
        itemRegistry.update(item);
    }

    /**
     * Updates a thing.
     *
     * @param thing
     *            thing (must not be null)
     */
    public void updateThing(Thing thing) {
        this.thingRegistry.update(thing);
    }

    protected void addItemFactory(ItemFactory itemFactory) {
        this.itemFactories.add(itemFactory);
    }

    protected void addThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        this.thingHandlerFactories.add(thingHandlerFactory);
    }

    protected void removeItemFactory(ItemFactory itemFactory) {
        this.itemFactories.remove(itemFactory);
    }

    protected void removeThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        this.thingHandlerFactories.remove(thingHandlerFactory);
    }

    protected void setItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void setItemThingLinkRegistry(ItemThingLinkRegistry itemThingLinkRegistry) {
        this.itemThingLinkRegistry = itemThingLinkRegistry;
    }

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = thingTypeRegistry;
    }

    protected void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = null;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    protected void unsetItemThingLinkRegistry(ItemThingLinkRegistry itemThingLinkRegistry) {
        this.itemThingLinkRegistry = null;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    protected void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = null;
    }

    private Thing addThing(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID, String label, List<String> groupNames, boolean enableChannels) {
        return addThing(thingTypeUID, thingUID, configuration, bridgeUID, label, groupNames, enableChannels, null);
    }

    private Thing addThing(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID, String label, List<String> groupNames, boolean enableChannels,
            Map<String, String> properties) {

        Thing thing = createThing(thingUID, configuration, bridgeUID, thingTypeUID);

        if (thing == null) {
            logger.warn("Cannot create thing. No binding found that supports creating a thing" + " of type {}.",
                    thingTypeUID);
            return null;
        }

        if (properties != null) {
            for (String key : properties.keySet()) {
                thing.setProperty(key, properties.get(key));
            }
        }

        String itemName = toItemName(thing.getUID());
        GroupItem groupItem = new GroupItem(itemName);
        groupItem.addTag(TAG_THING);
        groupItem.setLabel(label);
        groupItem.addGroupNames(groupNames);
        addThingSafely(thing);
        addItemSafely(groupItem);
        addItemThingLinkSafely(new ItemThingLink(itemName, thing.getUID()));

        ThingType thingType = thingTypeRegistry.getThingType(thingTypeUID);
        if (thingType != null) {
            List<ChannelGroupDefinition> channelGroupDefinitions = thingType.getChannelGroupDefinitions();
            for (ChannelGroupDefinition channelGroupDefinition : channelGroupDefinitions) {
                GroupItem channelGroupItem = new GroupItem(
                        getChannelGroupItemName(itemName, channelGroupDefinition.getId()));
                channelGroupItem.addTag(TAG_CHANNEL_GROUP);
                channelGroupItem.addGroupName(itemName);
                addItemSafely(channelGroupItem);
            }
        }

        if (thingType != null && enableChannels) {
            List<Channel> channels = thing.getChannels();
            for (Channel channel : channels) {
                ChannelType channelType = TypeResolver.resolve(thingType.getChannelTypeUID(channel.getUID()));
                if (channelType != null && !channelType.isAdvanced()) {
                    // Enable the channel.
                    // Pass the channel label. This will be null if it's not set, and the enableChannel method will use
                    // the label from the channelType
                    enableChannel(channel.getUID());
                } else if (channelType == null) {
                    logger.warn("Could not enable channel '{}', because no channel type was found.", channel.getUID());
                }
            }
        }

        return thing;
    }

    private void addThingSafely(Thing thing) {
        ThingUID thingUID = thing.getUID();
        if (thingRegistry.get(thingUID) != null) {
            thingRegistry.remove(thingUID);
        }
        thingRegistry.add(thing);
    }

    private void addItemSafely(Item item) {
        String itemName = item.getName();
        if (itemRegistry.get(itemName) != null) {
            itemRegistry.remove(itemName);
        }
        itemRegistry.add(item);
    }

    private void addItemThingLinkSafely(ItemThingLink itemThingLink) {
        if (itemThingLinkRegistry.get(itemThingLink.getID()) == null) {
            itemThingLinkRegistry.add(itemThingLink);
        }
    }

    private void addItemChannelLinkSafely(ItemChannelLink itemChannelLink) {
        if (this.itemChannelLinkRegistry.get(itemChannelLink.getID()) == null) {
            this.itemChannelLinkRegistry.add(itemChannelLink);
        }
    }

    private Thing createThing(ThingUID thingUID, Configuration configuration, ThingUID bridgeUID,
            ThingTypeUID thingTypeUID) {
        for (ThingHandlerFactory thingHandlerFactory : this.thingHandlerFactories) {
            if (thingHandlerFactory.supportsThingType(thingTypeUID)) {
                Thing thing = thingHandlerFactory.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
                return thing;
            }
        }
        return null;
    }

    private String getChannelGroupItemName(String itemName, String channelGroupId) {
        return itemName + "_" + channelGroupId;
    }

    private ItemFactory getItemFactoryForItemType(String itemType) {
        for (ItemFactory itemFactory : this.itemFactories) {
            String[] supportedItemTypes = itemFactory.getSupportedItemTypes();
            for (int i = 0; i < supportedItemTypes.length; i++) {
                String supportedItemType = supportedItemTypes[i];
                if (supportedItemType.equals(itemType)) {
                    return itemFactory;
                }
            }
        }
        return null;
    }

    private String getThingGroupItemName(ChannelUID channelUID) {
        Collection<ItemThingLink> links = this.itemThingLinkRegistry.getAll();
        for (ItemThingLink link : links) {
            if (link.getUID().equals(channelUID.getThingUID())) {
                return link.getItemName();
            }
        }
        return null;
    }

    private String toItemName(UID uid) {
        String itemName = uid.getAsString().replaceAll("[^a-zA-Z0-9_]", "_");
        return itemName;
    }

    private String getFirstLinkedItem(UID uid) {
        for (ItemThingLink link : itemThingLinkRegistry.getAll()) {
            if (link.getUID().equals(uid)) {
                return link.getItemName();
            }
        }
        return null;
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return subscribedEventTypes;
    }

    @Override
    public EventFilter getEventFilter() {
        return eventFiter;
    }

    @Override
    public void receive(Event event) {
        if (event instanceof ThingRemovedEvent) {
            ThingRemovedEvent thingRemovedEvent = (ThingRemovedEvent) event;
            ThingUID thingUID = new ThingUID(thingRemovedEvent.getThing().UID);
            String itemName = toItemName(thingUID);
            if (itemRegistry.get(itemName) != null) {
                try {
                    itemRegistry.remove(itemName, true);
                    itemThingLinkRegistry.remove(AbstractLink.getIDFor(itemName, thingUID));
                    itemChannelLinkRegistry.removeLinksForThing(thingUID);
                } catch (Exception ex) {
                    logger.error("Coud not remove items and links for removed thing: " + ex.getMessage(), ex);
                }
            }
        }
    }
}
