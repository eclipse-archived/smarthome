package org.eclipse.smarthome.core.thing.link;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageSelector;
import org.eclipse.smarthome.core.storage.StorageSelector.StorageSelectionListener;
import org.eclipse.smarthome.core.storage.StorageService;
import org.eclipse.smarthome.core.thing.internal.Activator;
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
public class ManagedItemChannelLinkProvider implements ItemChannelLinkProvider,
        StorageSelectionListener<ItemChannelLink> {

    private final static Logger logger = LoggerFactory
            .getLogger(ManagedItemChannelLinkProvider.class);

    private Collection<ItemChannelLinksChangeListener> itemChannelLinksChangeListeners = new CopyOnWriteArrayList<>();
    private Storage<ItemChannelLink> storage;
    private StorageSelector<ItemChannelLink> storageSelector;

    public ManagedItemChannelLinkProvider() {
        this.storageSelector = new StorageSelector<>(Activator.getContext(),
                ItemChannelLink.class.getName(), this);
    }

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

    @Override
    public void storageSelected(Storage<ItemChannelLink> storage) {
        this.storage = storage;
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

    protected void addStorageService(StorageService storageService) {
        this.storageSelector.addStorageService(storageService);
    }

    protected void removeStorageService(StorageService storageService) {
        this.storageSelector.removeStorageService(storageService);
    }

}
