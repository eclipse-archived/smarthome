/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@link ManagedItemChannelLinkProvider} is responsible for managed
 * {@link ItemChannelLink}s at runtime.
 * 
 * @author Dennis Nobel - Initial contribution
 * 
 */
public class ManagedItemChannelLinkProvider implements ItemChannelLinkProvider {

    private final static Logger logger = LoggerFactory
            .getLogger(ManagedItemChannelLinkProvider.class);

    private Collection<ItemChannelLinksChangeListener> itemChannelLinksChangeListeners = new CopyOnWriteArrayList<>();
    private Storage<ItemChannelLink> storage;

    /**
     * Adds an {@link ItemChannelLink}.
     * 
     * @param itemChannelLink
     *            item channel link
     */
    public void addItemChannelLink(ItemChannelLink itemChannelLink) {
        logger.info("Adding item channel link to managed item channel link provider '{}'.",
                itemChannelLink.toString());
        ItemChannelLink oldItemChannelLink = storage.put(itemChannelLink.getID(), itemChannelLink);
        if (oldItemChannelLink != null) {
            notifyItemChannelLinksChangeListenerAboutRemovedItemChannelLink(oldItemChannelLink);
        }
        notifyItemChannelLinksChangeListenerAboutAddedItemChannelLink(itemChannelLink);
    }

    @Override
    public void addItemChannelLinksChangeListener(ItemChannelLinksChangeListener listener) {
        itemChannelLinksChangeListeners.add(listener);
    }

    /**
     * Returns all managed {@link ItemChannelLink}s.
     * 
     * @return all managed item channel links
     */
    @Override
    public Collection<ItemChannelLink> getItemChannelLinks() {
        return storage.getValues();
    }

    /**
     * Removes an {@link ItemChannelLink}.
     * 
     * @param itemChannelLink
     *            item channel link
     * @return the removed item channel link or null if no link was removed
     */
    public ItemChannelLink removeItemChannelLink(ItemChannelLink itemChannelLink) {
        logger.debug("Removing itemChannelLink from managed itemChannelLink provider '{}'.",
                itemChannelLink.toString());
        ItemChannelLink removedItemChannelLink = storage.remove(itemChannelLink.getID());
        if (removedItemChannelLink != null) {
            notifyItemChannelLinksChangeListenerAboutRemovedItemChannelLink(removedItemChannelLink);
        }
        return removedItemChannelLink;
    }

    @Override
    public void removeItemChannelLinksChangeListener(ItemChannelLinksChangeListener listener) {
        itemChannelLinksChangeListeners.remove(listener);
    }

    private void notifyItemChannelLinksChangeListenerAboutAddedItemChannelLink(
            ItemChannelLink itemChannelLink) {
        for (ItemChannelLinksChangeListener itemChannelLinksChangeListener : this.itemChannelLinksChangeListeners) {
            itemChannelLinksChangeListener.itemChannelLinkAdded(this, itemChannelLink);
        }
    }

    private void notifyItemChannelLinksChangeListenerAboutRemovedItemChannelLink(
            ItemChannelLink itemChannelLink) {
        for (ItemChannelLinksChangeListener itemChannelLinksChangeListener : this.itemChannelLinksChangeListeners) {
            itemChannelLinksChangeListener.itemChannelLinkRemoved(this, itemChannelLink);
        }
    }

    protected void setStorageService(StorageService storageService) {
        this.storage = storageService.getStorage(ItemChannelLink.class.getName(), this.getClass()
                .getClassLoader());
    }

    protected void unsetStorageService(StorageService storageService) {
        this.storage = null;
    }

}
