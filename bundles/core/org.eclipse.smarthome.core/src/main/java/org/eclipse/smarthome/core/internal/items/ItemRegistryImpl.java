/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemNotUniqueException;
import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ItemsChangeListener;
import org.eclipse.smarthome.core.items.ManagedItemProvider;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.types.StateDescriptionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main implementing class of the {@link ItemRegistry} interface. It
 * keeps track of all declared items of all item providers and keeps their
 * current state in memory. This is the central point where states are kept and
 * thus it is a core part for all stateful services.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Stefan Bußweiler - Migration to new event mechanism
 *
 */
public class ItemRegistryImpl extends AbstractRegistry<Item, String>implements ItemRegistry, ItemsChangeListener {

    private final Logger logger = LoggerFactory.getLogger(ItemRegistryImpl.class);

    protected List<StateDescriptionProvider> stateDescriptionProviders = new CopyOnWriteArrayList<>();

    @Override
    public void allItemsChanged(ItemProvider provider, Collection<String> oldItemNames) {

        Map<String, Item> oldItemsMap = new HashMap<>();
        Collection<Item> oldItems = elementMap.get(provider);

        // if the provider did not provide any old item names, we check if we
        // know them and pass them further on to our listeners
        if (oldItemNames == null || oldItemNames.isEmpty()) {
            oldItemNames = new HashSet<String>();
            if (oldItems != null && oldItems.size() > 0) {
                for (Item oldItem : oldItems) {
                    oldItemsMap.put(oldItem.getName(), oldItem);
                }
            }
        } else {
            for (Item item : oldItems) {
                if (oldItemNames.contains(item.getName())) {
                    oldItemsMap.put(item.getName(), item);
                }
            }
        }

        Collection<Item> providedItems = provider.getAll();
        List<Item> items = new CopyOnWriteArrayList<Item>();
        elementMap.put(provider, items);
        for (Item item : providedItems) {
            Item oldItem = oldItemsMap.get(item.getName());
            if (oldItem == null) {
                // it is a new item
                try {
                    onAddElement(item);
                    items.add(item);
                    for (RegistryChangeListener<Item> listener : listeners) {
                        listener.added(item);
                    }
                } catch (IllegalArgumentException ex) {
                    logger.warn("Could not add item: " + ex.getMessage(), ex);
                }
            } else if (!oldItem.equals(item)) {
                // it is a modified item
                try {
                    onAddElement(item);
                    items.add(item);
                    for (RegistryChangeListener<Item> listener : listeners) {
                        listener.updated(oldItem, item);
                    }
                } catch (IllegalArgumentException ex) {
                    logger.warn("Could not add item: " + ex.getMessage(), ex);
                }
            } else {
                // it has not been modified, so keep the old instance
                items.add(oldItem);
            }
            oldItemsMap.remove(item.getName());
        }

        // send a remove notification for all remaining old items
        for (Item removedItem : oldItemsMap.values()) {
            for (RegistryChangeListener<Item> listener : listeners) {
                listener.removed(removedItem);
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.internal.items.ItemRegistry#getItem(java.lang
     * .String)
     */
    @Override
    public Item getItem(String name) throws ItemNotFoundException {

        for (Item item : getItems()) {
            if (item.getName().equals(name)) {
                return item;
            }
        }

        throw new ItemNotFoundException(name);
    }

    @Override
    public Item get(String itemName) {
        try {
            return getItem(itemName);
        } catch (ItemNotFoundException ignored) {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.internal.items.ItemRegistry#getItemByPattern
     * (java.lang.String)
     */
    @Override
    public Item getItemByPattern(String name) throws ItemNotFoundException, ItemNotUniqueException {
        Collection<Item> items = getItems(name);

        if (items.isEmpty()) {
            throw new ItemNotFoundException(name);
        }

        if (items.size() > 1) {
            throw new ItemNotUniqueException(name, items);
        }

        return items.iterator().next();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.internal.items.ItemRegistry#getItems()
     */
    @Override
    public Collection<Item> getItems() {
        return getAll();
    }

    @Override
    public Collection<Item> getItemsOfType(String type) {
        Collection<Item> matchedItems = new ArrayList<Item>();

        for (Item item : getItems()) {
            if (item.getType().equals(type)) {
                matchedItems.add(item);
            }
        }

        return matchedItems;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.internal.items.ItemRegistry#getItems(java.
     * lang.String)
     */
    @Override
    public Collection<Item> getItems(String pattern) {
        String regex = pattern.replace("?", ".?").replace("*", ".*?");
        Collection<Item> matchedItems = new ArrayList<Item>();

        for (Item item : getItems()) {
            if (item.getName().matches(regex)) {
                matchedItems.add(item);
            }
        }

        return matchedItems;
    }

    @Override
    public boolean isValidItemName(String name) {
        return name.matches("[a-zA-Z0-9_]*");
    }

    private void addToGroupItems(Item item, List<String> groupItemNames) {
        for (String groupName : groupItemNames) {
            try {
                Item groupItem = getItem(groupName);
                if (groupItem instanceof GroupItem) {
                    ((GroupItem) groupItem).addMember(item);
                }
            } catch (ItemNotFoundException e) {
                // the group might not yet be registered, let's ignore this
            }
        }
    }

    /**
     * An item should be initialized, which means that the event publisher is
     * injected and its implementation is notified that it has just been
     * created, so it can perform any task it needs to do after its creation.
     *
     * @param item
     *            the item to initialize
     * @throws IllegalArgumentException if the item has no valid name
     */
    private void initializeItem(Item item) throws IllegalArgumentException {
        if (isValidItemName(item.getName())) {
            if (item instanceof GenericItem) {
                GenericItem genericItem = (GenericItem) item;
                genericItem.setEventPublisher(eventPublisher);
                genericItem.setStateDescriptionProviders(stateDescriptionProviders);
                genericItem.initialize();
            }

            if (item instanceof GroupItem) {
                // fill group with its members
                addMembersToGroupItem((GroupItem) item);
            }

            // add the item to all relevant groups
            addToGroupItems(item, item.getGroupNames());
        } else {
            throw new IllegalArgumentException(
                    "Ignoring item '" + item.getName() + "' as it does not comply with" + " the naming convention.");
        }
    }

    private void addMembersToGroupItem(GroupItem groupItem) {
        for (Item i : getItems()) {
            if (i.getGroupNames().contains(groupItem.getName())) {
                groupItem.addMember(i);
            }
        }
    }

    private void removeFromGroupItems(Item item, List<String> groupItemNames) {
        for (String groupName : groupItemNames) {
            try {
                Item groupItem = getItem(groupName);
                if (groupItem instanceof GroupItem) {
                    ((GroupItem) groupItem).removeMember(item);
                }
            } catch (ItemNotFoundException e) {
                // the group might not yet be registered, let's ignore this
            }
        }
    }

    @Override
    protected void onAddElement(Item element) throws IllegalArgumentException {
        initializeItem(element);
    }

    @Override
    protected void onRemoveElement(Item element) {
        removeFromGroupItems(element, element.getGroupNames());
    }

    @Override
    protected void onUpdateElement(Item oldItem, Item item) {
        removeFromGroupItems(oldItem, oldItem.getGroupNames());
        addToGroupItems(item, item.getGroupNames());
        if (item instanceof GroupItem) {
            addMembersToGroupItem((GroupItem) item);
        }
    }

    @Override
    protected void setEventPublisher(EventPublisher eventPublisher) {
        super.setEventPublisher(eventPublisher);
        for (Item item : getItems()) {
            ((GenericItem) item).setEventPublisher(eventPublisher);
        }
    }

    @Override
    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        super.unsetEventPublisher(eventPublisher);
        for (Item item : getItems()) {
            ((GenericItem) item).setEventPublisher(null);
        }
    }

    protected void addStateDescriptionProvider(StateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProviders.add(stateDescriptionProvider);
        for (Item item : getItems()) {
            ((GenericItem) item).setStateDescriptionProviders(stateDescriptionProviders);
        }
    }

    protected void removeStateDescriptionProvider(StateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProviders.remove(stateDescriptionProvider);
        for (Item item : getItems()) {
            ((GenericItem) item).setStateDescriptionProviders(stateDescriptionProviders);
        }
    }

    @Override
    public Collection<Item> getItemsByTag(String... tags) {
        List<Item> filteredItems = new ArrayList<Item>();
        for (Item item : getItems()) {
            if (itemHasTags(item, tags)) {
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }

    private boolean itemHasTags(Item item, String... tags) {
        for (String tag : tags) {
            if (!item.hasTag(tag)) {
                return false;
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends GenericItem> Collection<T> getItemsByTag(Class<T> typeFilter, String... tags) {
        Collection<T> filteredItems = new ArrayList<T>();

        Collection<Item> items = getItemsByTag(tags);
        for (Item item : items) {
            if (typeFilter.isInstance(item)) {
                filteredItems.add((T) filteredItems);
            }
        }
        return filteredItems;
    }

    @Override
    public Collection<Item> getItemsByTagAndType(String type, String... tags) {
        List<Item> filteredItems = new ArrayList<Item>();
        for (Item item : getItemsOfType(type)) {
            if (itemHasTags(item, tags)) {
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }

    @Override
    public Item remove(String itemName, boolean recursive) {
        if (this.managedProvider != null) {
            return ((ManagedItemProvider) this.managedProvider).remove(itemName, recursive);
        } else {
            throw new IllegalStateException("ManagedProvider is not available");
        }
    }

    @Override
    protected void notifyListenersAboutAddedElement(Item element) {
        super.notifyListenersAboutAddedElement(element);
        postEvent(ItemEventFactory.createAddedEvent(element));
    }

    @Override
    protected void notifyListenersAboutRemovedElement(Item element) {
        super.notifyListenersAboutRemovedElement(element);
        postEvent(ItemEventFactory.createRemovedEvent(element));
    }

    @Override
    protected void notifyListenersAboutUpdatedElement(Item oldElement, Item element) {
        super.notifyListenersAboutUpdatedElement(oldElement, element);
        postEvent(ItemEventFactory.createUpdateEvent(element, oldElement));
    }

}
