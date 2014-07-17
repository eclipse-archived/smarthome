/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import static org.eclipse.smarthome.core.internal.CoreActivator.getContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ManagedItemProvider} is an OSGi service, that allows to add or remove
 * items at runtime by calling {@link ManagedItemProvider#addItem(Item)} or
 * {@link ManagedItemProvider#removeItem(Item)}. An added item is automatically
 * exposed to the {@link ItemRegistry}. Persistence of added Items is handled by
 * a {@link StorageService}. Items are being restored using the given
 * {@link ItemFactory}s.
 * 
 * @author Dennis Nobel - Initial contribution, added support for GroupItems
 * @author Thomas Eichstaedt-Engelen
 * @author Kai Kreuzer - improved return values
 */
public class ManagedItemProvider extends AbstractItemProvider {

    private static final String ITEM_TYPE_GROUP = "Group";

    private static final Logger logger = LoggerFactory.getLogger(ManagedItemProvider.class);

    private Storage<PersistedItem> itemStorage;
    private Collection<ItemFactory> itemFactories = new CopyOnWriteArrayList<ItemFactory>();

    
    public void addItemFactory(ItemFactory itemFactory) {
        itemFactories.add(itemFactory);
    }

    public void removeItemFactory(ItemFactory itemFactory) {
        itemFactories.remove(itemFactory);
    }
    
    protected void setStorageService(StorageService storageService) {
        this.itemStorage = storageService.getStorage(getContext(), Item.class.getName());
    }

    protected void unsetStorageService(StorageService storageService) {
        this.itemStorage = null;
    }


    public Item addItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot add null Item.");
        }

        PersistedItem persistedItem = createPeristedItem(item);
        PersistedItem oldPersistedItem = itemStorage.put(item.getName(), persistedItem);
        Item oldItem = null;
        if (oldPersistedItem != null) {
            oldItem = restoreItem(oldPersistedItem, item.getName());
            notifyItemChangeListenersAboutRemovedItem(oldItem);
        }
        notifyItemChangeListenersAboutAddedItem(item);

        return oldItem;
    }

    private PersistedItem createPeristedItem(Item item) {
        PersistedItem persistedItem;
        String itemType = toItemFactoryName(item);

        if (item instanceof GroupItem) {
            String baseItemType = null;
            GenericItem baseItem = ((GroupItem) item).getBaseItem();
            if (baseItem != null) {
                baseItemType = toItemFactoryName(baseItem);
            }
            persistedItem = new PersistedItem(ITEM_TYPE_GROUP, item.getGroupNames(), baseItemType);
        } else {
            persistedItem = new PersistedItem(itemType, item.getGroupNames());
        }
        return persistedItem;
    }

    /**
     * Translates the Items class simple name into a type name understandable by
     * the {@link ItemFactory}s.
     * 
     * @param item
     *            the Item to translate the name
     * @return the translated ItemTypeName understandable by the
     *         {@link ItemFactory}s
     */
    private String toItemFactoryName(Item item) {
        return item.getType();
    }

    public Item removeItem(String itemName) {
        if (itemName == null) {
            throw new IllegalArgumentException("Cannot remove null Item");
        }

        PersistedItem removedPersistedItem = itemStorage.remove(itemName);
        if (removedPersistedItem != null) {
            Item removedItem = restoreItem(removedPersistedItem, itemName);
            notifyItemChangeListenersAboutRemovedItem(removedItem);
            return removedItem;
        }
        return null;
    }

    /**
     * Updates an item and returns the old item instance.
     * 
     * @param item
     *            item (must not be null)
     * @return old item
     * @throws IllegalArgumentException
     *             is thrown, when the item is null, or there exists no item
     *             with the given name
     */
    public Item updateItem(Item item) throws IllegalArgumentException {

        if (item == null) {
            throw new IllegalArgumentException("Cannot update null Item.");
        }

        String itemName = item.getName();

        PersistedItem persistedItem = itemStorage.get(itemName);
        if (persistedItem != null) {
            Item oldItem = restoreItem(persistedItem, itemName);
            PersistedItem peristedItem = createPeristedItem(item);
            itemStorage.put(itemName, peristedItem);
            notifyItemChangeListenersAboutUpdatedItem(oldItem, item);
            return oldItem;
        } else {
            throw new IllegalArgumentException("Item with name " + item.getName()
                    + " could not be found for update.");
        }

    }

    /**
     * Returns all Items of this {@link ItemProvider} being restored from the
     * underlying {@link StorageService} and instantiated using the appropriate
     * {@link ItemFactory}s.
     * 
     * {@inheritDoc}
     */
    @Override
    public Collection<Item> getItems() {
        Collection<Item> storedItems = new ArrayList<Item>();
        for (String itemName : itemStorage.getKeys()) {
            PersistedItem persistedItem = itemStorage.get(itemName);
            storedItems.add(restoreItem(persistedItem, itemName));
        }
        return storedItems;
    }

    private Item restoreItem(PersistedItem persistedItem, String itemName) {
        GenericItem item = null;

        if (persistedItem.itemType.equals(ITEM_TYPE_GROUP)) {
            if (persistedItem.baseItemType != null) {
                GenericItem baseItem = createItem(persistedItem.baseItemType, itemName);
                item = new GroupItem(itemName, baseItem);
            } else {
                item = new GroupItem(itemName);
            }
        } else {
            item = createItem(persistedItem.itemType, itemName);
        }

        List<String> groupNames = persistedItem.groupNames;
        if (item != null && groupNames != null) {
            for (String groupName : groupNames) {
                item.addGroupName(groupName);
            }
        }

        if (item == null) {
            logger.debug(
                    "Couldn't restore item '{}' of type '{}' ~ there is no appropriate ItemFactory available.",
                    itemName, persistedItem.itemType);
        }
        return item;
    }

    private GenericItem createItem(String itemType, String itemName) {

        for (ItemFactory factory : this.itemFactories) {
            GenericItem item = factory.createItem(itemType, itemName);
            if (item != null) {
                return item;
            }
        }

        logger.debug("Couldn't find ItemFactory for item '{}' of type '{}'", itemName, itemType);

        return null;
    }
    
    
    private class PersistedItem {

        public PersistedItem(String itemType, List<String> groupNames) {
            this(itemType, groupNames, null);
        }

        public PersistedItem(String itemType, List<String> groupNames, String baseItemType) {
            this.itemType = itemType;
            this.groupNames = groupNames;
            this.baseItemType = baseItemType;
        }

        public String itemType;
        public List<String> groupNames;
        public String baseItemType;
    }
    
}
