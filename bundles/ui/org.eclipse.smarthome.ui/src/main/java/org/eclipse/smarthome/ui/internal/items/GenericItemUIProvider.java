/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.internal.items;

import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.ui.items.ItemUIProvider;
import org.eclipse.smarthome.model.items.ModelItem;
import org.eclipse.smarthome.model.items.ItemModel;
import org.eclipse.smarthome.model.sitemap.Widget;

public class GenericItemUIProvider implements ItemUIProvider {

	private ModelRepository modelRepository = null;

	public void setModelRepository(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	public void unsetModelRepository(ModelRepository modelRepository) {
		this.modelRepository = null;
	}

	public String getIcon(String itemName) {
		ModelItem item = getItem(itemName);
		return item != null ? item.getIcon() : null;
	}

	public String getLabel(String itemName) {
		ModelItem item = getItem(itemName);
		return item != null ? item.getLabel() : null;
	}

	public Widget getWidget(String itemName) {
		return null;
	}

	public Widget getDefaultWidget(Class<? extends org.eclipse.smarthome.core.items.Item> itemType, String itemName) {
		return null;
	}

	public ModelItem getItem(String itemName) {
		if (itemName != null && modelRepository != null) {
			for (String modelName : modelRepository.getAllModelNamesOfType("items")) {
				ItemModel model = (ItemModel) modelRepository.getModel(modelName);
				for (ModelItem item : model.getItems()) {
					if (itemName.equals(item.getName()))
						return item;
				}
			}
		}
		return null;
	}

}
