/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.storage;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.core.storage.Storage;


/**
 * A {@link Storage} implementation which stores it's data in-memory.
 *  
 * @author Thomas.Eichstaedt-Engelen - Initial Contribution and API
 */
public class VolatileStorage<T> implements Storage<T> {
	
	Map<String, T> storage = new ConcurrentHashMap<String, T>();
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean put(String key, T value) {
		return storage.put(key, value) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(String key) {
		return storage.remove(key) != null;
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
