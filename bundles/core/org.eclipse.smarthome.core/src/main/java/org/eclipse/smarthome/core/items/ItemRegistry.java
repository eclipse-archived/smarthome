/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import java.util.Collection;

import org.eclipse.smarthome.core.common.registry.Registry;

/**
 * The ItemRegistry is the central place, where items are kept in memory and their state
 * is permanently tracked. So any code that requires the current state of items should use
 * this service (instead of trying to keep their own local copy of the items).
 *
 * Items are registered by {@link ItemProvider}s, which can provision them from any source
 * they like and also dynamically remove or add items.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public interface ItemRegistry extends Registry<Item, String> {

    /**
     * This method retrieves a single item from the registry.
     *
     * @param name the item name
     * @return the uniquely identified item
     * @throws ItemNotFoundException if no item matches the input
     */
    public Item getItem(String name) throws ItemNotFoundException;

    /**
     * This method retrieves a single item from the registry.
     * Search patterns and shortened versions are supported, if they uniquely identify an item
     *
     * @param name the item name, a part of the item name or a search pattern
     * @return the uniquely identified item
     * @throws ItemNotFoundException if no item matches the input
     * @throws ItemNotUniqueException if multiply items match the input
     */
    public Item getItemByPattern(String name) throws ItemNotFoundException, ItemNotUniqueException;

    /**
     * This method retrieves all items that are currently available in the registry
     *
     * @return a collection of all available items
     */
    public Collection<Item> getItems();

    /**
     * This method retrieves all items with the given type
     *
     * @param type
     *            - item type as defined by {@link ItemFactory}s
     * @return a collection of all items of the given type
     */
    public Collection<Item> getItemsOfType(String type);

    /**
     * This method retrieves all items that match a given search pattern
     *
     * @return a collection of all items matching the search pattern
     */
    public Collection<Item> getItems(String pattern);

    /**
     * Checks whether itemName matches the item name conventions.
     * Item names must only consist out of alpha-numerical characters and
     * underscores (_).
     *
     * @param itemName the item name to validate
     * @return true, if the name is valid
     */
    public boolean isValidItemName(String itemName);

    /**
     * Returns list of items which contains all of the given tags.
     *
     * @param tags
     *            - array of tags to be present on the returned items.
     * @return list of items which contains all of the given tags.
     */
    public Collection<Item> getItemsByTag(String... tags);

    /**
     * Returns list of items with a certain type containing all of the given tags.
     *
     * @param type
     *            - item type as defined by {@link ItemFactory}s
     * @param tags
     *            - array of tags to be present on the returned items.
     * @return list of items which contains all of the given tags.
     */
    public Collection<Item> getItemsByTagAndType(String type, String... tags);

    /**
     * Returns list of items which contains all of the given tags.
     *
     * @param typeFilter
     *            - subclass of {@link GenericItem} to filter the resulting list
     *            for.
     * @param tags
     *            - array of tags to be present on the returned items.
     * @return list of items which contains all of the given tags, which is
     *         filtered by the given type filter.
     */
    public <T extends GenericItem> Collection<T> getItemsByTag(Class<T> typeFilter, String... tags);

    /**
     * @see ManagedItemProvider#remove(String, boolean)
     */
    public Item remove(String itemName, boolean recursive);

}
