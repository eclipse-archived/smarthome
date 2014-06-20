/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.storage.mapdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.smarthome.core.storage.Storage;
import org.mapdb.DB;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas.Eichstaedt-Engelen - Initial Contribution and API
 */
public class MapDbStorage<T> implements Storage<T> {

	private static final String VALUE_SEPARATOR = "@@@";

	private static final Logger logger = 
		LoggerFactory.getLogger(MapDbStorage.class);
	
	private DB db;
	private BundleContext consumerContext;
	private Map<String, String> map;
	
	private transient ObjectMapper mapper = new ObjectMapper();
	
	
	public MapDbStorage(DB db, BundleContext consumerContext, String name) {
		this.db = db;
		this.consumerContext = consumerContext;
		this.map = db.createTreeMap(name).makeOrGet();
	}
	
	
	@Override
	public T put(String key, T value) {
		String previousValue = map.put(key, serialize(value));
		db.commit();
		return deserialize(previousValue);
	}

	@Override
	public T remove(String key) {
		String removedElement = map.remove(key);
		db.commit();
		return deserialize(removedElement);
	}

	@Override
	public T get(String key) {
		return deserialize(map.get(key));
	}

	@Override
	public Collection<String> getKeys() {
		return map.keySet();
	}
	
	@Override
	public Collection<T> getValues() {
		Collection<T> values = new ArrayList<T>();
		for (String key : getKeys()) {
			values.add(get(key));
		}
		return values;
	}
	
	private String serialize(T value) {
		
		if (value == null) {
			throw new IllegalArgumentException("Cannot serialize NULL");
		}
		
		try {
			String valueTypeName = value.getClass().getName();
			String valueAsString = mapper.writeValueAsString(value);
			String concatValue = valueTypeName + VALUE_SEPARATOR + valueAsString;
			
			logger.debug("serialized value '{}' to MapDB", concatValue);
			return concatValue;
		} catch (IOException e) {
			logger.warn("Couldn't serialize value '{}'. Root cause is: {}", value, e.getMessage());
		}
		
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	public T deserialize(String json) {
		T value = null;
		try {
			String[] concatValue = json.split(VALUE_SEPARATOR);
			String valueTypeName = concatValue[0];
			String valueAsString = concatValue[1];

			Class<T> loadedValueType = (Class<T>)
				consumerContext.getBundle().loadClass(valueTypeName);
			value = mapper.readValue(valueAsString, loadedValueType);
		} catch (Exception e) {
			logger.warn("Couldn't deserialize value '{}'", json);
		}
		
		return value;
	}
	
}
