/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
