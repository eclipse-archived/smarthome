/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.storage;

import java.util.Collection;

/**
 * A Storage is the generic way to store key-value pairs in ESH. Each Storage
 * implementation can store its data differently, e.g in-memory or in-database.
 *
 * @author Thomas.Eichstaedt-Engelen - Initial Contribution and API
 * @author Kai Kreuzer - improved return values
 */
public interface Storage<T> {

    /**
     * Puts a key-value mapping into this Storage.
     * 
     * @param key the key to add
     * @param value the value to add
     * @return previous value for the key or null if no value was replaced
     */
    T put(String key, T value);

    /**
     * Removes the specified mapping from this map.
     *
     * @param key the mapping to remove
     * @return the removed value or null if no entry existed
     */
    T remove(String key);

    /**
     * Gets the value mapped to the key specified.
     * 
     * @param key the key
     * @return the mapped value, null if no match
     */
    T get(String key);

    /**
     * Gets all keys of this Storage.
     * 
     * @return the keys of this Storage
     */
    Collection<String> getKeys();

    /**
     * Gets all values of this Storage.
     * 
     * @return the values of this Storage
     */
    Collection<T> getValues();

}
