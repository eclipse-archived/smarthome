/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.cache;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple expiring and reloading multiple key-value-pair cache implementation. The value expires after the
 * specified duration has passed since the item was created, or the most recent replacement of the value.
 *
 * @author Christoph Weitkamp - Initial contribution and API.
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 */
public class ExpiringCacheMap<K, V> {

    private final Logger logger = LoggerFactory.getLogger(ExpiringCacheMap.class);

    private final long expiry;
    private final ConcurrentMap<K, ExpiringCache<V>> items;

    /**
     * Creates a new instance.
     * 
     * @param expiry the duration in milliseconds for how long the value stays valid
     */
    public ExpiringCacheMap(long expiry) {
        this.expiry = expiry;
        this.items = new ConcurrentHashMap<>();
    }

    /**
     * Creates an {@link ExpiringCache} and adds it to the cache.
     * 
     * @param key the key with which the specified value is to be associated
     * @param action the action for the item to be associated with the specified key to retrieve/calculate the value
     */
    public void put(K key, Supplier<V> action) {
        put(key, new ExpiringCache<>(expiry, action));
    }

    /**
     * Adds an {@link ExpiringCache} to the cache.
     * 
     * @param key the key with which the specified value is to be associated
     * @param item the item to be associated with the specified key
     */
    public void put(K key, ExpiringCache<V> item) {
        if (key == null) {
            throw new IllegalArgumentException("Item cannot be added as key is null.");
        }
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be added as item is null.");
        }

        items.put(key, item);
    }

    /**
     * Checks if the key is present in the cache.
     * 
     * @param key the key whose presence in the cache is to be tested
     * @return true if the cache contains a value for the specified key
     */
    public boolean containsKey(K key) {
        return items.containsKey(key);
    }

    /**
     * Removes the item associated with the given key from the cache.
     * 
     * @param key the key whose associated value is to be removed
     */
    public void remove(K key) {
        items.remove(key);
    }

    /**
     * Discards all items from the cache.
     */
    public void clear() {
        items.clear();
    }

    /**
     * Returns a set of all keys.
     * 
     * @return the set of all keys
     */
    public synchronized Set<K> keys() {
        final Set<K> keys = new LinkedHashSet<>();
        for (final K key : items.keySet()) {
            keys.add(key);
        }
        return keys;
    }

    /**
     * Returns the value associated with the given key - possibly from the cache, if it is still valid.
     * 
     * @param key the key whose associated value is to be returned
     * @return the value associated with the given key, or null if there is no cached value for the given key
     */
    public V get(K key) {
        final ExpiringCache<V> item = items.get(key);
        if (item == null) {
            logger.debug("No item for key '{}' found", key);
            return null;
        } else {
            return item.getValue();
        }
    }

    /**
     * Returns a collection of all values - possibly from the cache, if they are still valid.
     * 
     * @return the collection of all values
     */
    public synchronized Collection<V> values() {
        final Collection<V> values = new LinkedList<>();
        for (final ExpiringCache<V> item : items.values()) {
            values.add(item.getValue());
        }
        return values;
    }

    /**
     * Invalidates the value associated with the given key in the cache.
     * 
     * @param key the key whose associated value is to be invalidated
     */
    public synchronized void invalidate(K key) {
        final ExpiringCache<V> item = items.get(key);
        if (item == null) {
            logger.debug("No item for key '{}' found", key);
        } else {
            item.invalidateValue();
        }
    }

    /**
     * Invalidates all values in the cache.
     */
    public synchronized void invalidateAll() {
        items.values().forEach(item -> item.invalidateValue());
    }

    /**
     * Refreshes and returns the value associated with the given key in the cache.
     * 
     * @param key the key whose associated value is to be refreshed
     * @return the value associated with the given key, or null if there is no cached value for the given key
     */
    public synchronized V refresh(K key) {
        final ExpiringCache<V> item = items.get(key);
        if (item == null) {
            logger.debug("No item for key '{}' found", key);
            return null;
        } else {
            return item.refreshValue();
        }
    }

    /**
     * Refreshes and returns a collection of all new values in the cache.
     * 
     * @return the collection of all values
     */
    public synchronized Collection<V> refreshAll() {
        final Collection<V> values = new LinkedList<>();
        for (final ExpiringCache<V> item : items.values()) {
            values.add(item.refreshValue());
        }
        return values;
    }
}
