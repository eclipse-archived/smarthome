/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.monitor.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.events.ItemRemovedEvent;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class EventLogger implements EventSubscriber {

    private final Map<String, Logger> eventLoggers = Maps.newHashMap();

    private final Set<String> subscribedEventTypes = ImmutableSet.of(EventSubscriber.ALL_EVENT_TYPES);

    /** Whether or not to skip logging events about states whose value did <b>not</b> change. */
    private boolean skipUnchangedStateEvents = false;

    /**
     * Asking the item registry for the previous state does not work because the state was already changed before
     * {@link #receive(Event)} is called.
     * So we should rather hold previous updates in a local cache.
     */
    private Map<String, State> stateCacheByItemName = new HashMap<String, State>();

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
        final Logger logger = getLogger(event.getType());
        logger.trace("Received event of type '{}' under the topic '{}' with payload: '{}'", event.getType(),
                event.getTopic(), event.getPayload());
        /*
         * If event is a state change, its value has not been changed, and the logger has been configured to skip
         * logging state updated whose value did not change, then the event is not logged.
         */
        if (!(event instanceof ItemStateEvent) || !skipUnchangedStateEvents || stateChanged((ItemStateEvent) event)) {
        logger.info(event.toString());
    }
        // if unchanged state events are not logged and an item is removed, also remove it from cache
        if (skipUnchangedStateEvents && event instanceof ItemRemovedEvent) {
            stateCacheByItemName.remove(((ItemRemovedEvent) event).getItem().name);
        }
    }

    private boolean stateChanged(ItemStateEvent event) {
        try {
            final State newState = event.getItemState();
            final State oldState = stateCacheByItemName.put(event.getItemName(), newState);
            return !newState.equals(oldState);
        } catch (Exception e) {
            // ignore exceptions and leave default behavior
        }
        return true; // default case: log always!
    }

    private Logger getLogger(String eventType) {
        final String loggerName = "smarthome.event." + eventType;
        Logger logger = eventLoggers.get(loggerName);
        if (logger == null) {
            logger = LoggerFactory.getLogger(loggerName);
            eventLoggers.put(loggerName, logger);
        }
        return logger;
    }

    protected void updateProperties(Map<String, Object> configProps) {
        final String value = (String) configProps.get("skipUnchangedStateEvents");
        skipUnchangedStateEvents = value != null && "true".equalsIgnoreCase(value.trim());
        if (!skipUnchangedStateEvents)
            stateCacheByItemName.clear(); // clear cache in case it has been deactivated
    }
}
