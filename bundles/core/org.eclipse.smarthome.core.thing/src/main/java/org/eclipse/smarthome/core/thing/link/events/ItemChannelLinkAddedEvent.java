/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link.events;

import org.eclipse.smarthome.core.thing.link.dto.ItemChannelLinkDTO;

/**
 * A {@link ItemChannelLinkAddedEvent} notifies subscribers that an item channel link has been added.
 * Events must be created with the {@link LinkEventFactory}.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class ItemChannelLinkAddedEvent extends AbstractItemChannelLinkRegistryEvent {

    /**
     * The link added event type.
     */
    public final static String TYPE = ItemChannelLinkAddedEvent.class.getSimpleName();

    public ItemChannelLinkAddedEvent(String topic, String payload, ItemChannelLinkDTO link) {
        super(topic, payload, link);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        ItemChannelLinkDTO link = getLink();
        return "Link '" + link.itemName + "-" + link.channelUID + "' has been added.";
    }

}
