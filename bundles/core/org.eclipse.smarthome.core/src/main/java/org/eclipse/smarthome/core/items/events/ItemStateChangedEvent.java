/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items.events;

import org.eclipse.smarthome.core.events.AbstractEvent;
import org.eclipse.smarthome.core.types.State;

/**
 * {@link ItemStateChangedEvent}s can be used to deliver item state changes through the Eclipse SmartHome event bus. In
 * contrast to the {@link ItemStateEvent} the {@link ItemStateChangedEvent} is only sent if the state changed.
 * State events must be created with the {@link ItemEventFactory}.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class ItemStateChangedEvent extends AbstractEvent {

    /**
     * The item state changed event type.
     */
    public final static String TYPE = ItemStateChangedEvent.class.getSimpleName();

    private final String itemName;

    private final State itemState;

    private final State oldItemState;

    /**
     * Constructs a new item state changed event.
     *
     * @param topic the topic
     * @param payload the payload
     * @param itemName the item name
     * @param newItemState the new item state
     * @param oldItemState the old item state
     */
    protected ItemStateChangedEvent(String topic, String payload, String itemName, State newItemState,
            State oldItemState) {
        super(topic, payload, null);
        this.itemName = itemName;
        this.itemState = newItemState;
        this.oldItemState = oldItemState;
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

    /**
     * Gets the old item state.
     *
     * @return the old item state
     */
    public State getOldItemState() {
        return oldItemState;
    }

    @Override
    public String toString() {
        return itemName + " changed from " + oldItemState.toString() + " to " + itemState.toString();
    }

}
