/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.items;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemNotUniqueException;
import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ItemUtil;
import org.eclipse.smarthome.core.items.ManagedItemProvider;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.types.StateDescriptionProvider;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

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
@Component(immediate = true)
public class ItemRegistryImpl extends AbstractRegistry<Item, String, ItemProvider> implements ItemRegistry {

    private final List<StateDescriptionProvider> stateDescriptionProviders = Collections
            .synchronizedList(new ArrayList<StateDescriptionProvider>());

    public ItemRegistryImpl() {
        super(ItemProvider.class);
    }

    @Override
    public Item getItem(String name) throws ItemNotFoundException {
        final Item item = get(name);
        if (item == null) {
            throw new ItemNotFoundException(name);
        } else {
            return item;
        }
    }

    @Override
    public Item getItemByPattern(String name) throws ItemNotFoundException, ItemNotUniqueException {
        Collection<Item> items = getItems(name);

        if (items.isEmpty()) {
            throw new ItemNotFoundException(name);
        }

        if (items.size() > 1) {
            throw new ItemNotUniqueException(name, items);
        }

        Item item = items.iterator().next();

        if (item == null) {
            throw new ItemNotFoundException(name);
        } else {
            return item;
        }
    }

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

    private void addToGroupItems(Item item, List<String> groupItemNames) {
        for (String groupName : groupItemNames) {
            if (groupName != null) {
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
    }

    private void replaceInGroupItems(Item oldItem, Item newItem, List<String> groupItemNames) {
        for (String groupName : groupItemNames) {
            if (groupName != null) {
                try {
                    Item groupItem = getItem(groupName);
                    if (groupItem instanceof GroupItem) {
                        ((GroupItem) groupItem).replaceMember(oldItem, newItem);
                    }
                } catch (ItemNotFoundException e) {
                    // the group might not yet be registered, let's ignore this
                }
            }
        }
    }

    /**
     * An item should be initialized, which means that the event publisher is
     * injected and its implementation is notified that it has just been
     * created, so it can perform any task it needs to do after its creation.
     *
     * @param item the item to initialize
     * @throws IllegalArgumentException if the item has no valid name
     */
    private void initializeItem(Item item) throws IllegalArgumentException {
        ItemUtil.assertValidItemName(item.getName());

        injectServices(item);

        if (item instanceof GroupItem) {
            // fill group with its members
            addMembersToGroupItem((GroupItem) item);
        }

        // add the item to all relevant groups
        addToGroupItems(item, item.getGroupNames());
    }

    private void injectServices(Item item) {
        if (item instanceof GenericItem) {
            GenericItem genericItem = (GenericItem) item;
            genericItem.setEventPublisher(eventPublisher);
            genericItem.setStateDescriptionProviders(stateDescriptionProviders);
        }
    }

    private void clearServices(Item item) {
        if (item instanceof GenericItem) {
            GenericItem genericItem = (GenericItem) item;
            genericItem.setEventPublisher(null);
            genericItem.setStateDescriptionProviders(null);
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
            if (groupName != null) {
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
    }

    @Override
    protected void onAddElement(Item element) throws IllegalArgumentException {
        initializeItem(element);
    }

    @Override
    protected void onRemoveElement(Item element) {
        clearServices(element);
        removeFromGroupItems(element, element.getGroupNames());
    }

    @Override
    protected void onUpdateElement(Item oldItem, Item item) {
        clearServices(oldItem);
        injectServices(item);

        List<String> oldNames = oldItem.getGroupNames();
        List<String> newNames = item.getGroupNames();
        List<String> commonNames = oldNames.stream().filter(name -> newNames.contains(name)).collect(toList());

        removeFromGroupItems(oldItem, oldNames.stream().filter(name -> !commonNames.contains(name)).collect(toList()));
        replaceInGroupItems(oldItem, item, commonNames);
        addToGroupItems(item, newNames.stream().filter(name -> !commonNames.contains(name)).collect(toList()));
        if (item instanceof GroupItem) {
            addMembersToGroupItem((GroupItem) item);
        }
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
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
                filteredItems.add((T) item);
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

    protected void activate(final ComponentContext componentContext) {
        super.activate(componentContext.getBundleContext());
    }

    @Override
    protected void deactivate() {
        super.deactivate();
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addStateDescriptionProvider(StateDescriptionProvider provider) {
        synchronized (stateDescriptionProviders) {
            stateDescriptionProviders.add(provider);

            Collections.sort(stateDescriptionProviders, new Comparator<StateDescriptionProvider>() {
                // sort providers by service ranking in a descending order
                @Override
                public int compare(StateDescriptionProvider provider1, StateDescriptionProvider provider2) {
                    return provider2.getRank().compareTo(provider1.getRank());
                }
            });

            for (Item item : getItems()) {
                ((GenericItem) item).setStateDescriptionProviders(stateDescriptionProviders);
            }
        }
    }

    protected void removeStateDescriptionProvider(StateDescriptionProvider provider) {
        stateDescriptionProviders.remove(provider);
        for (Item item : getItems()) {
            ((GenericItem) item).setStateDescriptionProviders(stateDescriptionProviders);
        }
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setManagedProvider(ManagedItemProvider provider) {
        super.setManagedProvider(provider);
    }

    protected void unsetManagedProvider(ManagedItemProvider provider) {
        super.unsetManagedProvider(provider);
    }

}
