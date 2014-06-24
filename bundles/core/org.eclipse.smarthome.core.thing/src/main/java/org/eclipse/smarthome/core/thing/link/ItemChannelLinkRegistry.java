/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


/**
 * {@link ItemChannelLinkRegistry} tracks all {@link ItemChannelLinkProvider}s
 * and aggregates all {@link ItemChannelLink}s.
 * 
 * @author Dennis Nobel - Initial contribution
 * 
 */
public class ItemChannelLinkRegistry implements ItemChannelLinksChangeListener {

    private final static Logger logger = LoggerFactory.getLogger(ItemChannelLinkRegistry.class
            .getName());

    private Map<ItemChannelLinkProvider, Collection<ItemChannelLink>> itemChannelLinkMap = new ConcurrentHashMap<>();

    /**
     * Returns the item name, which is bound to the given channel UID.
     * 
     * @param channelUID
     *            channel UID
     * @return item name or null if no item is bound to the given channel UID
     */
    public String getBoundItem(ChannelUID channelUID) {
        for (ItemChannelLink itemChannelLink : getItemChannelLinks()) {
            if (itemChannelLink.getChannelUID().equals(channelUID)) {
                return itemChannelLink.getItemName();
            }
        }
        return null;
    }

    /**
     * Returns all item channel links.
     * 
     * @return all item channel links
     */
    public List<ItemChannelLink> getItemChannelLinks() {
        return Lists.newArrayList(Iterables.concat(itemChannelLinkMap.values()));
    }

    /**
     * Returns if an item for a given item is linked to a channel for a given
     * channel UID.
     * 
     * @param itemName
     *            item name
     * @param channelUID
     *            channel UID
     * @return true if linked, false otherwise
     */
    public boolean isLinked(String itemName, ChannelUID channelUID) {

        for (ItemChannelLink itemChannelLink : getItemChannelLinks()) {
            if (itemChannelLink.getChannelUID().equals(channelUID)
                    && itemChannelLink.getItemName().equals(itemName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void itemChannelLinkAdded(ItemChannelLinkProvider provider,
            ItemChannelLink itemChannelLink) {
        Collection<ItemChannelLink> itemChannelLinks = itemChannelLinkMap.get(provider);
        if (itemChannelLinks != null) {
            itemChannelLinks.add(itemChannelLink);
        }
    }

    @Override
    public void itemChannelLinkRemoved(ItemChannelLinkProvider provider,
            ItemChannelLink itemChannelLink) {
        Collection<ItemChannelLink> itemChannelLinks = itemChannelLinkMap.get(provider);
        if (itemChannelLinks != null) {
            itemChannelLinks.remove(itemChannelLink);
        }
    }

    protected void addItemChannelLinkProvider(ItemChannelLinkProvider itemChannelLinkProvider) {
        // only add this provider if it does not already exist
        if (!itemChannelLinkMap.containsKey(itemChannelLinkProvider)) {
            Collection<ItemChannelLink> itemChannelLinks = new CopyOnWriteArraySet<ItemChannelLink>(
                    itemChannelLinkProvider.getItemChannelLinks());
            itemChannelLinkMap.put(itemChannelLinkProvider, itemChannelLinks);
            itemChannelLinkProvider.addItemChannelLinksChangeListener(this);
            logger.debug("ItemChannelLinkProvider '{}' has been added.", itemChannelLinkProvider
                    .getClass().getName());
        }
    }

    protected void removeItemChannelLinkProvider(ItemChannelLinkProvider itemChannelLinkProvider) {
        if (itemChannelLinkMap.containsKey(itemChannelLinkProvider)) {
            itemChannelLinkMap.remove(itemChannelLinkProvider);
            itemChannelLinkProvider.removeItemChannelLinksChangeListener(this);
            logger.debug("ItemChannelLinkProvider '{}' has been removed.", itemChannelLinkProvider
                    .getClass().getName());
        }
    }

}
