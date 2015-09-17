/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.items;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.model.sitemap.Widget;

/**
 * This interface describes the methods that need to be implemented by a provider that
 * wants to define the appearance of an item in the UI.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public interface ItemUIProvider {

    /**
     * Returns the category to use.
     *
     * @param itemName the name of the item to return the icon for
     * @return the name of the category to use or null if undefined.
     */
    public String getCategory(String itemName);

    /**
     * Returns the label text to be used for an item in the UI.
     *
     * @param item the name of the item to return the label text for
     * @return the label text to be used in the UI or null if undefined.
     */
    public String getLabel(String itemName);

    /**
     * Provides a default widget for a given item (class). This is used whenever
     * the UI needs to be created dynamically and there is no other source
     * of information about the widgets.
     *
     * @param itemType the class of the item
     * @param itemName the item name to get the default widget for
     *
     * @return a widget implementation that can be used for the given item
     */
    public Widget getDefaultWidget(Class<? extends Item> itemType, String itemName);

    /**
     * <p>
     * Provides a widget for a given item. This can be used to overwrite the widget listed in the sitemap. A use case
     * for this is that the sitemap defines merely the parent-child-relation of widgets, but the concrete widget to be
     * used for rendering might be selected dynamically at runtime.
     * </p>
     * <p>
     * If the sitemap widget should not be overridden, this method must return <code>null</code>.
     * </p>
     *
     * @param itemName the item name to get the widget for
     * @return a widget to use for the given item or <code>null</code> if sitemap should not be overridden.
     */
    public Widget getWidget(String itemName);
}
