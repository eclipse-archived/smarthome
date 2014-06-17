package org.eclipse.smarthome.core.thing.link;

import java.util.Collection;

/**
 * The {@link ItemChannelLinkProvider} is responsible for providing item channel
 * links.
 * 
 * @author Dennis Nobel - Initial contribution
 * 
 */
public interface ItemChannelLinkProvider {

    /**
     * Provides a collection of item channel links.
     * 
     * @return the item channel links provided by the
     *         {@link ItemChannelLinkProvider}
     */
    Collection<ItemChannelLink> getItemChannelLinks();

    /**
     * Adds a {@link ItemChannelLinksChangeListener} which is notified if there
     * are changes concerning the item channel links provides by the
     * {@link ItemChannelLinkProvider}.
     * 
     * @param listener
     *            The listener to be added
     */
    void addItemChannelLinksChangeListener(ItemChannelLinksChangeListener listener);

    /**
     * Removes a {@link ItemChannelLinksChangeListener} which is notified if
     * there are changes concerning the item channel links provides by the
     * {@link ItemChannelLinkProvider}.
     * 
     * @param listener
     *            The listener to be removed.
     */
    void removeItemChannelLinksChangeListener(ItemChannelLinksChangeListener listener);
}
