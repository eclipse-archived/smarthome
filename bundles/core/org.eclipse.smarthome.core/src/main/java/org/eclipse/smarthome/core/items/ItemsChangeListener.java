/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import java.util.Collection;

import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;

/**
 * This is a listener interface which should be implemented where ever item
 * providers or the item registry are used in order to be notified of any
 * dynamic changes in the provided items.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public interface ItemsChangeListener extends ProviderChangeListener<Item> {

    /**
     * Notifies the listener that all items of a provider have changed and thus
     * should be reloaded.
     *
     * @param provider
     *            the concerned item provider
     * @param oldItemNames
     *            a collection of all previous item names, so that references
     *            can be removed
     */
    public void allItemsChanged(ItemProvider provider, Collection<String> oldItemNames);
}
