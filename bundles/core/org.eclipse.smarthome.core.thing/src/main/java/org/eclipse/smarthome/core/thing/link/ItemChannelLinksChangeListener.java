/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link;

/**
 * 
 * This is a listener interface which should be implemented wherever item
 * channel link providers or the item channel link registry are used in order to
 * be notified of any dynamic changes in the provided item channel links.
 * 
 * @author Dennis Nobel - Initial contribution
 * 
 */
public interface ItemChannelLinksChangeListener {

    /**
     * Notifies the listener that a single item channel link has been added
     * 
     * @param provider
     *            the concerned item channel link provider
     * @param itemChannelLink
     *            the item channel link that has been added
     */
    public void itemChannelLinkAdded(ItemChannelLinkProvider provider,
            ItemChannelLink itemChannelLink);

    /**
     * Notifies the listener that a single item channel link has been removed
     * 
     * @param provider
     *            the concerned item channel link provider
     * @param itemChannelLink
     *            the item channel link that has been removed
     */
    public void itemChannelLinkRemoved(ItemChannelLinkProvider provider,
            ItemChannelLink itemChannelLink);
}
