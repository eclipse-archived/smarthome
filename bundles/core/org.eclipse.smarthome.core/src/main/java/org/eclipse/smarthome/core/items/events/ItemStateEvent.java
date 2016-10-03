/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items.events;

import org.eclipse.smarthome.core.events.AbstractEvent;
import org.eclipse.smarthome.core.types.State;

/**
 * {@link ItemStateEvent}s can be used to deliver item status updates through the Eclipse SmartHome event bus.
 * State events must be created with the {@link ItemEventFactory}.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
public class ItemStateEvent extends AbstractEvent {

    /**
     * The item state event type.
     */
    public final static String TYPE = ItemStateEvent.class.getSimpleName();

    private final String itemName;

    private final State itemState;

    /**
     * Constructs a new item state event.
     *
     * @param topic the topic
     * @param payload the payload
     * @param itemName the item name
     * @param itemState the item state
     * @param source the source, can be null
     */
    protected ItemStateEvent(String topic, String payload, String itemName, State itemState, String source) {
        super(topic, payload, source);
        this.itemName = itemName;
        this.itemState = itemState;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Gets the item name.
     *
     * @return the item name
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * Gets the item state.
     *
     * @return the item state
     */
    public State getItemState() {
        return itemState;
    }

    @Override
    public String toString() {
        return String.format("%s updated to %s", itemName, itemState);
    }

}
