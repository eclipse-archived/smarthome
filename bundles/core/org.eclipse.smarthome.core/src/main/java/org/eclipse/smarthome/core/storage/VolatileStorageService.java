/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The {@link VolatileStorageService} returns {@link VolatileStorage}s
 * which stores their data in-memory.
 * 
 * @author Thomas.Eichstaedt-Engelen - Initial Contribution and API
 */
public class VolatileStorageService implements StorageService {
	
	@SuppressWarnings("rawtypes")
	Map<String, Storage> storages = new ConcurrentHashMap<String, Storage>();
	
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public <T> Storage<T> getStorage(String name) {
		if (!storages.containsKey(name)) {
			storages.put(name, new VolatileStorage<T>());
		}
		return storages.get(name);
	}
	
}
