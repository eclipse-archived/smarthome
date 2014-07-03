/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import java.util.Collection;

/**
 * This is a listener interface which should be implemented where ever the item registry is
 * used in order to be notified of any dynamic changes in the provided items.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public interface ItemRegistryChangeListener {

	/**
	 * Notifies the listener that all items in the registry have changed and thus should be reloaded.
	 * 
	 * @param oldItemNames a collection of all previous item names, so that references can be removed
	 */
	public void allItemsChanged(Collection<String> oldItemNames);

	/**
	 * Notifies the listener that a single item has been added
	 * 
	 * @param item the item that has been added
	 */
	public void itemAdded(Item item);
	
	/**
	 * Notifies the listener that a single item has been removed
	 * 
	 * @param item the item that has been removed
	 */
	public void itemRemoved(Item item);
	
    /**
     * Notifies the listener that a single item has been updated
     * 
     * @param oldItem
     *            the item that has been updated
     * @param oldItem
     *            the item that has been updated
     */
    public void itemUpdated(Item oldItem, Item item);

}
