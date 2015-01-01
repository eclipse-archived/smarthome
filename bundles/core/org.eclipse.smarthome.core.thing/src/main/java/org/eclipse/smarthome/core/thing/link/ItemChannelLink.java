/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.ChannelUID;

/**
 * {@link ItemChannelLink} defines a link between an {@link Item} and a
 * {@link Channel}.
 * 
 * @author Dennis Nobel - Initial contribution, Added getIDFor method
 * @author Jochen Hiller - Bugfix 455434: added default constructor, object is now mutable
 */
public class ItemChannelLink {

    private String itemName;

    private ChannelUID channelUID;

    /**
     * Default constructor in package scope only. Will allow to instantiate this
     * class by reflection. Not intended to be used for normal instantiation.
     */
    ItemChannelLink() {
        this.itemName = null;
        this.channelUID = null;
    }

    public ItemChannelLink(String itemName, ChannelUID channelUID) {
        this.itemName = itemName;
        this.channelUID = channelUID;
    }

    public String getItemName() {
        return itemName;
    }

    public ChannelUID getChannelUID() {
        return channelUID;
    }

    public String getID() {
        return getIDFor(itemName, channelUID);
    }

    @Override
    public String toString() {
        return getID();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
        	return false;
        }
        if (!(obj instanceof ItemChannelLink)) {
        	return false;
        }
        ItemChannelLink other = (ItemChannelLink) obj;
        if (!this.itemName.equals(other.itemName)) {
        	return false;
        }
        if (!this.channelUID.equals(other.channelUID)) {
        	return false;
        }

        return true;
    }
    
    @Override
    public int hashCode() {
        return (int)this.itemName.hashCode() *
                this.channelUID.hashCode();
    }
    
    /**
     * Returns the item channel link ID for a given item name and channel UID
     * 
     * @param itemName
     *            item name
     * @param channelUID
     *            channel UID
     * @return the item channel link ID
     */
    public static String getIDFor(String itemName, ChannelUID channelUID) {
        return itemName + " -> " + channelUID.toString();
    }

}
