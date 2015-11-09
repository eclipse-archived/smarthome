/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.status.events;

import java.util.Set;

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

    @Override
    protected Event createEventByType(String eventType, String topic, String payload, String source) throws Exception {
        if (ConfigStatusInfoEvent.TYPE.equals(eventType)) {
            return new ConfigStatusInfoEvent(topic, payload, source);
        }
        throw new IllegalArgumentException(
                eventType + " not supported by " + ConfigStatusEventFactory.class.getSimpleName());
    }
}
