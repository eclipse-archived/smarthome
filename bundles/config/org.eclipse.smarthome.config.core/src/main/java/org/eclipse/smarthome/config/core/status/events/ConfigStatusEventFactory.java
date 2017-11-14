/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.status.events;

import java.util.Set;

import org.eclipse.smarthome.config.core.status.ConfigStatusInfo;
import org.eclipse.smarthome.core.events.AbstractEventFactory;
import org.eclipse.smarthome.core.events.Event;

import com.google.common.collect.Sets;

/**
 * The {@link ConfigStatusEventFactory} is the event factory implementation to create configuration status events, e.g.
 * for {@link ConfigStatusInfoEvent}.
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class ConfigStatusEventFactory extends AbstractEventFactory {

    private static final Set<String> SUPPORTED_EVENT_TYPES = Sets.newHashSet(ConfigStatusInfoEvent.TYPE);

    /**
     * Creates a new {@link ConfigStatusEventFactory}.
     */
    public ConfigStatusEventFactory() {
        super(SUPPORTED_EVENT_TYPES);
    }

    private Event createStatusInfoEvent(String topic, String payload) throws Exception {
        String[] topicElements = getTopicElements(topic);
        if (topicElements.length != 5) {
            throw new IllegalArgumentException("ConfigStatusInfoEvent creation failed, invalid topic: " + topic);
        }
        ConfigStatusInfo thingStatusInfo = deserializePayload(payload, ConfigStatusInfo.class);
        return new ConfigStatusInfoEvent(topic, thingStatusInfo);
    }

    @Override
    protected Event createEventByType(String eventType, String topic, String payload, String source) throws Exception {
        if (ConfigStatusInfoEvent.TYPE.equals(eventType)) {
            return createStatusInfoEvent(topic, payload);
        }
        throw new IllegalArgumentException(
                eventType + " not supported by " + ConfigStatusEventFactory.class.getSimpleName());
    }
}
