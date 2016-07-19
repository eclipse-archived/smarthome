/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.monitor.internal;

import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class EventLogger implements EventSubscriber {

    private final Map<String, Logger> eventLoggers = Maps.newHashMap();

    private final Set<String> subscribedEventTypes = ImmutableSet.of(EventSubscriber.ALL_EVENT_TYPES);

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
        Logger logger = getLogger(event.getType());
        logger.trace("Received event of type '{}' under the topic '{}' with payload: '{}'", event.getType(),
                event.getTopic(), event.getPayload());
        logger.info(event.toString());
    }

    private Logger getLogger(String eventType) {
        String loggerName = "smarthome.event." + eventType;
        Logger logger = eventLoggers.get(loggerName);
        if (logger == null) {
            logger = LoggerFactory.getLogger(loggerName);
            eventLoggers.put(loggerName, logger);
        }
        return logger;
    }

}
