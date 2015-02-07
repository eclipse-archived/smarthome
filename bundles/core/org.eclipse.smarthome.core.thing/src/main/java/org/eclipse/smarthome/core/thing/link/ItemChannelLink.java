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
 * {@link ItemChannelLink} defines a link between an {@link Item} and a {@link Channel}.
 *
 * @author Dennis Nobel - Initial contribution, Added getIDFor method
 * @author Jochen Hiller - Bugfix 455434: added default constructor, object is now mutable
 */
public class ItemChannelLink extends AbstractLink {

    private ChannelUID channelUID;

    /**
     * Default constructor in package scope only. Will allow to instantiate this
     * class by reflection. Not intended to be used for normal instantiation.
     */
    ItemChannelLink() {
        super();
        this.channelUID = null;
    }

    public ItemChannelLink(String itemName, ChannelUID channelUID) {
        super(itemName);
        this.channelUID = channelUID;
    }

    @Override
    public ChannelUID getUID() {
        return this.channelUID;
    }

}
