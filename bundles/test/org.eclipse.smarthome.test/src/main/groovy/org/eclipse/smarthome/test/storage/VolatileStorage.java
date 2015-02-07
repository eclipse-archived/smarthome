/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.test.storage;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.core.storage.Storage;

/**
 * A {@link Storage} implementation which stores it's data in-memory.
 * 
 * @author Thomas.Eichstaedt-Engelen - Initial Contribution and API
 * @author Kai Kreuzer - improved return values
 */
public class VolatileStorage<T> implements Storage<T> {

    Map<String, T> storage = new ConcurrentHashMap<String, T>();

    /**
     * {@inheritDoc}
     */
    @Override
    public T put(String key, T value) {
        return storage.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T remove(String key) {
        return storage.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get(String key) {
        return storage.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getKeys() {
        return storage.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<T> getValues() {
        return storage.values();
    }

}
