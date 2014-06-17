package org.eclipse.smarthome.core.thing.link;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.ChannelUID;

/**
 * {@link ItemChannelLink} defines a link between an {@link Item} and a
 * {@link Channel}.
 * 
 * @author Dennis Nobel - Initial contribution
 */
public class ItemChannelLink {

    private final String itemName;

    private final ChannelUID channelUID;

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
        return itemName + " -> " + getChannelUID().toString();
    }

    @Override
    public String toString() {
        return getID();
    }

}
