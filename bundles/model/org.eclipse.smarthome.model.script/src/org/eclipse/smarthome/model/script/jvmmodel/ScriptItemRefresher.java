/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.jvmmodel;

import java.util.Collection;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ItemRegistryChangeListener;
import org.eclipse.smarthome.model.core.ModelRepository;

/**
 * The {@link ScriptItemRefresher} is responsible for reloading script resources every time an item is added or removed.
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class ScriptItemRefresher implements ItemRegistryChangeListener {

	ModelRepository modelRepository;
	private ItemRegistry itemRegistry;


	public void setModelRepository(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	public void unsetModelRepository(ModelRepository modelRepository) {
		this.modelRepository = null;
	}

	public void setItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = itemRegistry;
		this.itemRegistry.addRegistryChangeListener(this);
	}
	
	public void unsetItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry.removeRegistryChangeListener(this);
		this.itemRegistry = null;
	}

	@Override
	public void added(Item element) {
		modelRepository.reloadAllModelsOfType("script");
	}

	@Override
	public void removed(Item element) {
		modelRepository.reloadAllModelsOfType("script");
	}

	@Override
	public void updated(Item oldElement, Item element) {

	}

	@Override
	public void allItemsChanged(Collection<String> oldItemNames) {
		modelRepository.reloadAllModelsOfType("script");
	}
	
}
