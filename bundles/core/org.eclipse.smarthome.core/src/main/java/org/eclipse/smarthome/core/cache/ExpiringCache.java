/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.cache;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This is a simple expiring and reloading cache implementation.
 *
 * There must be provided an action in order to retrieve/calculate the value. This action will be called only if the
 * answer from the last calculation is not valid anymore, i.e. if it is expired.
 *
 * @author Christoph Weitkamp - Initial contribution and API.
 *
 * @param <V> the type of the value
 */
public class ExpiringCache<V> {
    private final long expiry;
    private final Supplier<V> action;
    @Nullable
    private V value;
    private long expiresAt;

    /**
     * Create a new instance.
     *
     * @param expiry the duration in milliseconds for how long the value stays valid
     * @param action the action to retrieve/calculate the value
     */
    public ExpiringCache(long expiry, Supplier<V> action) {
        if (action == null) {
            throw new IllegalArgumentException("ExpiringCacheItem cannot be created as action is null.");
        }

        this.expiry = TimeUnit.MILLISECONDS.toNanos(expiry);
        this.action = action;

        invalidateValue();
    }

    /**
     * Returns the value - possibly from the cache, if it is still valid.
     */
    @Nullable
    public synchronized V getValue() {
        if (value == null || isExpired()) {
            return refreshValue();
        }
        return value;
    }

    /**
     * Invalidates the value in the cache.
     */
    public synchronized void invalidateValue() {
        value = null;
        expiresAt = 0;
    }

    /**
     * Refreshes and returns the value in the cache.
     *
     * @return the new value
     */
    @Nullable
    public synchronized V refreshValue() {
        value = action.get();
        expiresAt = System.nanoTime() + expiry;
        return value;
    }

    /**
     * Checks if the value is expired.
     *
     * @return true if the value is expired
     */
    public boolean isExpired() {
        return expiresAt < System.nanoTime();
    }
}
