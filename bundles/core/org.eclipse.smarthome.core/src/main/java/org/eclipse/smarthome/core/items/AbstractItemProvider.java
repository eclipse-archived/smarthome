/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Abstract base class for {@link ItemProvider}s. In particular it handles
 * the management and notification of {@link ItemsChangeListener}.  
 * 
 * @author Thomas.Eichstaedt-Engelen - Initial Contribution
 */
public abstract class AbstractItemProvider implements ItemProvider {

	/** keeps track of all item change listeners */
	protected Collection<ItemsChangeListener> itemsChangeListeners = new CopyOnWriteArrayList<ItemsChangeListener>();

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addItemChangeListener(ItemsChangeListener listener) {
		itemsChangeListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeItemChangeListener(ItemsChangeListener listener) {
		itemsChangeListeners.remove(listener);
	}
	

    protected void notifyItemChangeListenersAboutAddedItem(Item item) {
        for (ItemsChangeListener itemsChangeListener : itemsChangeListeners) {
            itemsChangeListener.itemAdded(this, item);
        }
    }

    protected void notifyItemChangeListenersAboutRemovedItem(Item item) {
        for (ItemsChangeListener itemsChangeListener : itemsChangeListeners) {
            itemsChangeListener.itemRemoved(this, item);
        }
    }

    protected void notifyItemChangeListenersAboutAllItemsChanged(Collection<String> oldItemNames) {
        for (ItemsChangeListener itemsChangeListener : itemsChangeListeners) {
            itemsChangeListener.allItemsChanged(this, oldItemNames);
        }
    }

}
