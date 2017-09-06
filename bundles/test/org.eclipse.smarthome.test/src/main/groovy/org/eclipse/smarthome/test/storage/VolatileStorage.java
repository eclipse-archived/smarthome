/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.test.storage;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.storage.Storage;

/**
 * A {@link Storage} implementation which stores it's data in-memory.
 *
 * @author Thomas.Eichstaedt-Engelen - Initial Contribution and API
 * @author Kai Kreuzer - improved return values
 */
public class VolatileStorage<T> implements Storage<T> {

    Map<String, T> storage = new ConcurrentHashMap<String, T>();

    @Override
    public T put(String key, T value) {
        return storage.put(key, value);
    }

    @Override
    public T remove(String key) {
        return storage.remove(key);
    }

    @Override
    public boolean containsKey(final @NonNull String key) {
        return storage.containsKey(key);
    }

    @Override
    public T get(String key) {
        return storage.get(key);
    }

    @Override
    public Collection<String> getKeys() {
        return storage.keySet();
    }

    @Override
    public Collection<T> getValues() {
        return storage.values();
    }

}
