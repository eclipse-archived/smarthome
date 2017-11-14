/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sitemap.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.StateChangeListener;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.rest.core.item.EnrichedItemDTOMapper;
import org.eclipse.smarthome.io.rest.sitemap.SitemapSubscriptionService.SitemapSubscriptionCallback;
import org.eclipse.smarthome.model.sitemap.Frame;
import org.eclipse.smarthome.model.sitemap.VisibilityRule;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;

/**
 * This is a class that listens on item state change events and creates sitemap events for a dedicated sitemap page.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class PageChangeListener implements StateChangeListener {

    private final String sitemapName;
    private final String pageId;
    private final ItemUIRegistry itemUIRegistry;
    private EList<Widget> widgets;
    private Set<Item> items;
    private final List<SitemapSubscriptionCallback> callbacks = Collections
            .synchronizedList(new ArrayList<SitemapSubscriptionCallback>());
    private Set<SitemapSubscriptionCallback> distinctCallbacks = Collections.emptySet();

    /**
     * Creates a new instance.
     *
     * @param sitemapName the sitemap name of the page
     * @param pageId the id of the page for which events are created
     * @param itemUIRegistry the ItemUIRegistry which is needed for the functionality
     * @param widgets the list of widgets that are part of the page.
     */
    public PageChangeListener(String sitemapName, String pageId, ItemUIRegistry itemUIRegistry, EList<Widget> widgets) {
        this.sitemapName = sitemapName;
        this.pageId = pageId;
        this.itemUIRegistry = itemUIRegistry;

        updateItemsAndWidgets(widgets);
    }

    private void updateItemsAndWidgets(EList<Widget> widgets) {
        if (this.widgets != null) {
            // cleanup statechange listeners in case widgets were removed
            items = getAllItems(this.widgets);
            for (Item item : items) {
                if (item instanceof GenericItem) {
                    ((GenericItem) item).removeStateChangeListener(this);
                }
            }
        }

        this.widgets = widgets;
        items = getAllItems(widgets);
        for (Item item : items) {
            if (item instanceof GenericItem) {
                ((GenericItem) item).addStateChangeListener(this);
            }
        }
    }

    public String getSitemapName() {
        return sitemapName;
    }

    public String getPageId() {
        return pageId;
    }

    public void addCallback(SitemapSubscriptionCallback callback) {
        callbacks.add(callback);
        // we transform the list of callbacks to a set in order to remove duplicates
        distinctCallbacks = new HashSet<>(callbacks);
    }

    public void removeCallback(SitemapSubscriptionCallback callback) {
        callbacks.remove(callback);
        distinctCallbacks = new HashSet<>(callbacks);
    }

    /**
     * Disposes this instance and releases all resources.
     */
    public void dispose() {
        for (Item item : items) {
            if (item instanceof GenericItem) {
                ((GenericItem) item).removeStateChangeListener(this);
            } else if (item instanceof GroupItem) {
                ((GroupItem) item).removeStateChangeListener(this);
            }
        }
    }

    /**
     * Collects all items that are represented by a given list of widgets
     *
     * @param widgets
     *            the widget list to get the items for added to all bundles containing REST resources
     * @return all items that are represented by the list of widgets
     */
    private Set<Item> getAllItems(EList<Widget> widgets) {
        Set<Item> items = new HashSet<Item>();
        if (itemUIRegistry != null) {
            for (Widget widget : widgets) {
                addItemWithName(items, widget.getItem());
                if (widget instanceof Frame) {
                    items.addAll(getAllItems(((Frame) widget).getChildren()));
                }
                // now scan visibility rules
                for (VisibilityRule vr : widget.getVisibility()) {
                    addItemWithName(items, vr.getItem());
                }
            }
        }
        return items;
    }

    private void addItemWithName(Set<Item> items, String itemName) {
        if (itemName != null) {
            try {
                Item item = itemUIRegistry.getItem(itemName);
                items.add(item);
            } catch (ItemNotFoundException e) {
                // ignore
            }
        }
    }

    @Override
    public void stateChanged(Item item, State oldState, State newState) {
        // For all items except group, send an event only when the event state is changed.
        if (item instanceof GroupItem) {
            return;
        }
        Set<SitemapEvent> events = constructSitemapEvents(item, widgets);
        for (SitemapEvent event : events) {
            for (SitemapSubscriptionCallback callback : distinctCallbacks) {
                callback.onEvent(event);
            }
        }
    }

    @Override
    public void stateUpdated(Item item, State state) {
        // For group item only, send an event each time the event state is updated.
        // It allows updating the group label while the group state is unchanged,
        // for example the count in label for Group:Switch:OR
        if (!(item instanceof GroupItem)) {
            return;
        }
        Set<SitemapEvent> events = constructSitemapEvents(item, widgets);
        for (SitemapEvent event : events) {
            for (SitemapSubscriptionCallback callback : distinctCallbacks) {
                callback.onEvent(event);
            }
        }
    }

    private Set<SitemapEvent> constructSitemapEvents(Item item, List<Widget> widgets) {
        Set<SitemapEvent> events = new HashSet<>();
        for (Widget w : widgets) {
            if (w instanceof Frame) {
                events.addAll(constructSitemapEvents(item, itemUIRegistry.getChildren((Frame) w)));
            }

            if ((w.getItem() != null && w.getItem().equals(item.getName())) || definesVisibility(w, item.getName())) {
                SitemapWidgetEvent event = new SitemapWidgetEvent();
                event.sitemapName = sitemapName;
                event.pageId = pageId;
                event.label = itemUIRegistry.getLabel(w);
                event.labelcolor = itemUIRegistry.getLabelColor(w);
                event.valuecolor = itemUIRegistry.getValueColor(w);
                event.widgetId = itemUIRegistry.getWidgetId(w);
                event.visibility = itemUIRegistry.getVisiblity(w);
                event.item = EnrichedItemDTOMapper.map(item, false, null, null);

                // adjust the state according to the widget type
                event.item.state = itemUIRegistry.getState(w).toFullString();

                events.add(event);
            }
        }
        return events;
    }

    private boolean definesVisibility(Widget w, String name) {
        for (VisibilityRule vr : w.getVisibility()) {
            if (name.equals(vr.getItem())) {
                return true;
            }
        }
        return false;
    }

    public void sitemapContentChanged() {
        SitemapChangedEvent changeEvent = new SitemapChangedEvent();
        changeEvent.pageId = pageId;
        changeEvent.sitemapName = sitemapName;
        for (SitemapSubscriptionCallback callback : distinctCallbacks) {
            callback.onEvent(changeEvent);
        }
    }

}
