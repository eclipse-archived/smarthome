/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.ui.internal.items;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.items.ItemUIProvider;

/**
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class GenericItemUIProvider implements ItemUIProvider {

    private ItemRegistry itemRegistry = null;

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    @Override
    public String getCategory(String itemName) {
        if (itemRegistry != null) {
            Item item = itemRegistry.get(itemName);
            return item != null ? item.getCategory() : null;
        }
        return null;
    }

    @Override
    public String getLabel(String itemName) {
        if (itemRegistry != null) {
            Item item = itemRegistry.get(itemName);
            return item != null ? item.getLabel() : null;
        }
        return null;
    }

    @Override
    public Widget getWidget(String itemName) {
        return null;
    }

    @Override
    public Widget getDefaultWidget(Class<? extends org.eclipse.smarthome.core.items.Item> itemType, String itemName) {
        return null;
    }

}
