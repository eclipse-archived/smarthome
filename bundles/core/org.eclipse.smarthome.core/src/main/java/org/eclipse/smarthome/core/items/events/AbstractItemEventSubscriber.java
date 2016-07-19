/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items.events;

import java.util.Set;

import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link AbstractItemEventSubscriber} defines an abstract implementation of the {@link EventSubscriber} interface
 * for receiving {@link ItemStateEvent}s and {@link ItemCommandEvent}s from the Eclipse SmartHome event bus. </p>
 * 
 * A subclass can implement the methods {@link #receiveUpdate(ItemStateEvent)} and
 * {@link #receiveCommand(ItemCommandEvent)} in order to receive and handle such events.
 * 
 * @author Stefan Bußweiler - Initial contribution
 */
public abstract class AbstractItemEventSubscriber implements EventSubscriber {

    private final Set<String> subscribedEventTypes = ImmutableSet.of(ItemStateEvent.TYPE, ItemCommandEvent.TYPE);
    
    @Override
    public Set<String> getSubscribedEventTypes() {
        return subscribedEventTypes;
    }

    @Override
    public EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(Event event) {
        if (event instanceof ItemStateEvent) {
            receiveUpdate((ItemStateEvent) event);
        } else if (event instanceof ItemCommandEvent) {
            receiveCommand((ItemCommandEvent) event);
        }
    }

    /**
     * Callback method for receiving item command events from the Eclipse SmartHome event bus.
     * 
     * @param commandEvent the item command event
     */
    protected void receiveCommand(ItemCommandEvent commandEvent) {
        // Default implementation: do nothing.
        // Can be implemented by subclass in order to handle item commands.
    }

    /**
     * Callback method for receiving item update events from the Eclipse SmartHome event bus.
     * 
     * @param updateEvent the item update event
     */
    protected void receiveUpdate(ItemStateEvent updateEvent) {
        // Default implementation: do nothing.
        // Can be implemented by subclass in order to handle item updates.
    }

}
