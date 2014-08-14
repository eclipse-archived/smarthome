/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link;

import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.core.thing.ChannelUID;


/**
 * {@link ItemChannelLinkRegistry} tracks all {@link ItemChannelLinkProvider}s
 * and aggregates all {@link ItemChannelLink}s.
 * 
 * @author Dennis Nobel - Initial contribution
 * 
 */
public class ItemChannelLinkRegistry extends AbstractRegistry<ItemChannelLink> {

    /**
     * Returns the item name, which is bound to the given channel UID.
     * 
     * @param channelUID
     *            channel UID
     * @return item name or null if no item is bound to the given channel UID
     */
    public String getBoundItem(ChannelUID channelUID) {
        for (ItemChannelLink itemChannelLink : getAll()) {
            if (itemChannelLink.getChannelUID().equals(channelUID)) {
                return itemChannelLink.getItemName();
            }
        }
        return null;
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

        for (ItemChannelLink itemChannelLink : getAll()) {
            if (itemChannelLink.getChannelUID().equals(channelUID)
                    && itemChannelLink.getItemName().equals(itemName)) {
                return true;
            }
        }

        return false;
    }
}
