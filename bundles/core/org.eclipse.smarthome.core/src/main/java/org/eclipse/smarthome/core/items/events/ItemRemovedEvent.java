/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items.events;

import org.eclipse.smarthome.core.items.dto.ItemDTO;

/**
 * An {@link ItemRemovedEvent} notifies subscribers that an item has been removed.
 * Item removed events must be created with the {@link ItemEventFactory}.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
public class ItemRemovedEvent extends AbstractItemRegistryEvent {

    /**
     * The item removed event type.
     */
    public final static String TYPE = ItemRemovedEvent.class.getSimpleName();

    /**
     * Constructs a new item removed event object.
     *
     * @param topic the topic
     * @param payload the payload
     * @param item the item data transfer object
     */
    protected ItemRemovedEvent(String topic, String payload, ItemDTO item) {
        super(topic, payload, null, item);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "Item '" + getItem().name + "' has been removed.";
    }

}
