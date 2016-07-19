/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sse.internal.listeners;

import java.util.Set;

import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.io.rest.sse.SseResource;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link SseEventSubscriber} is responsible for broadcasting Eclipse SmartHome events
 * to currently listening SSE clients.
 * 
 * @author Stefan Bußweiler - Initial contribution
 */
public class SseEventSubscriber implements EventSubscriber {

    private final Set<String> subscribedEventTypes = ImmutableSet.of(EventSubscriber.ALL_EVENT_TYPES);

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
        sseResource.broadcastEvent(event);
    }

}
