/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sse.internal.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ItemRegistryChangeListener;
import org.eclipse.smarthome.io.rest.core.item.beans.ItemBean;
import org.eclipse.smarthome.io.rest.core.util.BeanMapper;
import org.eclipse.smarthome.io.rest.sse.EventType;
import org.eclipse.smarthome.io.rest.sse.SseResource;

/**
 * Listener responsible for broadcasting item registry events to all clients
 * subscribed to them.
 *
 * @author Ivan Iliev - Initial Contribution and API
 *
 */
public class ItemRegistryEventListener implements ItemRegistryChangeListener {

    private ItemRegistry itemRegistry;

    private SseResource sseResource;

    protected void setSseResource(SseResource sseResource) {
        this.sseResource = sseResource;
    }

    protected void unsetSseResource(SseResource sseResource) {
        this.sseResource = null;
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
        this.itemRegistry.addRegistryChangeListener(this);
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry.removeRegistryChangeListener(this);
        this.itemRegistry = null;
    }

    @Override
    public void added(Item element) {
        broadcastItemEvent(element.getName(), EventType.ITEM_ADDED, element);
    }

    @Override
    public void removed(Item element) {
        broadcastItemEvent(element.getName(), EventType.ITEM_REMOVED, element);
    }

    @Override
    public void updated(Item oldElement, Item element) {
        broadcastItemEvent(element.getName(), EventType.ITEM_UPDATED, oldElement, element);
    }

    @Override
    public void allItemsChanged(Collection<String> oldItemNames) {

    }

    private void broadcastItemEvent(String itemIdentifier, EventType eventType, Item... elements) {
        Object eventObject = null;
        if (elements != null && elements.length > 0) {
            List<ItemBean> itemBeans = new ArrayList<ItemBean>();

            for (Item item : elements) {
                itemBeans.add(BeanMapper.mapItemToBean(item, false, "/"));
            }

            eventObject = itemBeans;
        }

        sseResource.broadcastEvent(itemIdentifier, eventType, eventObject);
    }

}
