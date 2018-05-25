/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.items;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
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
    public @NonNull Item getItem(String name) throws ItemNotFoundException;

    /**
     * This method retrieves a single item from the registry.
     * Search patterns and shortened versions are supported, if they uniquely identify an item
     *
     * @param name the item name, a part of the item name or a search pattern
     * @return the uniquely identified item
     * @throws ItemNotFoundException if no item matches the input
     * @throws ItemNotUniqueException if multiply items match the input
     */
    public @NonNull Item getItemByPattern(@NonNull String name) throws ItemNotFoundException, ItemNotUniqueException;

    /**
     * This method retrieves all items that are currently available in the registry
     *
     * @return a collection of all available items
     */
    public @NonNull Collection<@NonNull Item> getItems();

    /**
     * This method retrieves all items with the given type
     *
     * @param type
     *            - item type as defined by {@link ItemFactory}s
     * @return a collection of all items of the given type
     */
    public @NonNull Collection<Item> getItemsOfType(@NonNull String type);

    /**
     * This method retrieves all items that match a given search pattern
     *
     * @return a collection of all items matching the search pattern
     */
    public @NonNull Collection<@NonNull Item> getItems(@NonNull String pattern);

    /**
     * Returns list of items which contains all of the given tags.
     *
     * @param tags
     *            - array of tags to be present on the returned items.
     * @return list of items which contains all of the given tags.
     */
    public @NonNull Collection<Item> getItemsByTag(@NonNull String... tags);

    /**
     * Returns list of items with a certain type containing all of the given tags.
     *
     * @param type
     *            - item type as defined by {@link ItemFactory}s
     * @param tags
     *            - array of tags to be present on the returned items.
     * @return list of items which contains all of the given tags.
     */
    public @NonNull Collection<Item> getItemsByTagAndType(@NonNull String type, @NonNull String... tags);

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
    public @NonNull <T extends GenericItem> Collection<T> getItemsByTag(@NonNull Class<T> typeFilter,
            @NonNull String... tags);

    /**
     * @see ManagedItemProvider#remove(String, boolean)
     */
    public @Nullable Item remove(@NonNull String itemName, boolean recursive);

    /**
     * Add a hook to be informed before adding/after removing items.
     *
     * @param hook
     */
    void addRegistryHook(RegistryHook<Item> hook);

    /**
     * Remove the hook again.
     *
     * @param hook
     */
    void removeRegistryHook(RegistryHook<Item> hook);

    /**
     * Adds a tag to the item.
     *
     * @param itemName the name of the item
     * @param tag the tag to be added
     * @return {@code true} if the collection of tags was modified by this operation
     */
    boolean addTag(String itemName, String tag);

    /**
     * Adds the given tags to the item.
     *
     * @param itemName the name of the item
     * @param tags the tags to be added
     * @return {@code true} if the collection of tags was modified by this operation
     */
    boolean addTags(String itemName, Collection<String> tags);

    /**
     * Removes a tag from the item.
     *
     * @param itemName the name of the item
     * @param tag the tag to be removed
     * @return {@code true} if the collection of tags was modified by this operation
     */
    boolean removeTag(String itemName, String tag);

    /**
     * Removes the given tags from the item.
     *
     * @param itemName the name of the item
     * @param tags the tags to be removed
     * @return {@code true} if the collection of tags was modified by this operation
     */
    boolean removeTags(String itemName, Collection<String> tags);

    /**
     * Removes all tags from the item.
     *
     * @param itemName the name of the item
     */
    void removeTags(String itemName);

}
