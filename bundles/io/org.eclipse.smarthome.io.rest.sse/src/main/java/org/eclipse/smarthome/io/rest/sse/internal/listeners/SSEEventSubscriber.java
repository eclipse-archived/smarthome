/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sse.internal.listeners;

import java.util.Set;

import org.eclipse.smarthome.config.discovery.inbox.events.InboxAddedEvent;
import org.eclipse.smarthome.config.discovery.inbox.events.InboxRemovedEvent;
import org.eclipse.smarthome.config.discovery.inbox.events.InboxUpdatedEvent;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.events.ItemAddedEvent;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemRemovedEvent;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.items.events.ItemUpdatedEvent;
import org.eclipse.smarthome.core.thing.events.ThingAddedEvent;
import org.eclipse.smarthome.core.thing.events.ThingRemovedEvent;
import org.eclipse.smarthome.core.thing.events.ThingStatusInfoEvent;
import org.eclipse.smarthome.core.thing.events.ThingUpdatedEvent;
import org.eclipse.smarthome.io.rest.sse.EventType;
import org.eclipse.smarthome.io.rest.sse.SseResource;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link SSEEventSubscriber} is responsible for broadcasting Eclipse SmartHome events
 * to currently listening SSE clients.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
public class SSEEventSubscriber implements EventSubscriber {

    private final Set<String> subscribedEventTypes = ImmutableSet.of(
            ItemAddedEvent.TYPE, ItemRemovedEvent.TYPE, ItemUpdatedEvent.TYPE,
            ItemCommandEvent.TYPE, ItemStateEvent.TYPE, 
            InboxAddedEvent.TYPE, InboxRemovedEvent.TYPE, InboxUpdatedEvent.TYPE,
            ThingAddedEvent.TYPE, ThingRemovedEvent.TYPE, ThingUpdatedEvent.TYPE,
            ThingStatusInfoEvent.TYPE);

    private SseResource sseResource;

    protected void setSseResource(SseResource sseResource) {
        this.sseResource = sseResource;
    }

    protected void unsetSseResource(SseResource sseResource) {
        this.sseResource = null;
    }

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
        if (event instanceof ThingAddedEvent) {
            sseResource.broadcastEvent(EventType.THING_ADDED, event);
        } else if (event instanceof ThingRemovedEvent) {
            sseResource.broadcastEvent(EventType.THING_REMOVED, event);
        } else if (event instanceof ThingUpdatedEvent) {
            sseResource.broadcastEvent(EventType.THING_UPDATED, event);
        } else if (event instanceof ThingStatusInfoEvent) {
            sseResource.broadcastEvent(EventType.THING_STATUS_UPDATED, event);
        } else if (event instanceof ItemAddedEvent) {
            sseResource.broadcastEvent(EventType.ITEM_ADDED, event);
        } else if (event instanceof ItemRemovedEvent) {
            sseResource.broadcastEvent(EventType.ITEM_REMOVED, event);
        } else if (event instanceof ItemUpdatedEvent) {
            sseResource.broadcastEvent(EventType.ITEM_UPDATED, event);
        } else if (event instanceof ItemCommandEvent) {
            sseResource.broadcastEvent(EventType.COMMAND, event);
        } else if (event instanceof ItemStateEvent) {
            sseResource.broadcastEvent(EventType.UPDATE, event);
        } else if (event instanceof InboxAddedEvent) {
            sseResource.broadcastEvent(EventType.INBOX_THING_ADDED, event);
        } else if (event instanceof InboxRemovedEvent) {
            sseResource.broadcastEvent(EventType.INBOX_THING_REMOVED, event);
        } else if (event instanceof InboxUpdatedEvent) {
            sseResource.broadcastEvent(EventType.INBOX_THING_UPDATED, event);
        }
    }

}
