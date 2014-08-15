/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.common.registry.AbstractManagedProvider;
import org.eclipse.smarthome.core.items.ManagedItemProvider.PersistedItem;
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
public class ManagedItemProvider extends AbstractManagedProvider<Item, String, PersistedItem> implements ItemProvider {

    public static class PersistedItem {

        public String baseItemType;

        public List<String> groupNames;

        public String itemType;

        public PersistedItem(String itemType, List<String> groupNames) {
            this(itemType, groupNames, null);
        }

        public PersistedItem(String itemType, List<String> groupNames, String baseItemType) {
            this.itemType = itemType;
            this.groupNames = groupNames;
            this.baseItemType = baseItemType;
        }
    }

    private static final String ITEM_TYPE_GROUP = "Group";

    private static final Logger logger = LoggerFactory.getLogger(ManagedItemProvider.class);

    private Collection<ItemFactory> itemFactories = new CopyOnWriteArrayList<ItemFactory>();

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

    protected void addItemFactory(ItemFactory itemFactory) {
        itemFactories.add(itemFactory);
    }

    @Override
    protected String getKey(Item element) {
        return element.getName();
    }

    @Override
    protected String getStorageName() {
        return Item.class.getName();
    }

    @Override
    protected String keyToString(String key) {
        return key;
    }

    protected void removeItemFactory(ItemFactory itemFactory) {
        itemFactories.remove(itemFactory);
    }

    @Override
    protected Item toElement(String itemName, PersistedItem persistedItem) {
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

    @Override
    protected PersistedItem toPersistableElement(Item item) {

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

}
