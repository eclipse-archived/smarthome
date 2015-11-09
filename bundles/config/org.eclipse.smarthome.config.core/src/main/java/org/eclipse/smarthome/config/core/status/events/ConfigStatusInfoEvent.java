/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.status.events;

import org.eclipse.smarthome.config.core.status.ConfigStatusInfo;
import org.eclipse.smarthome.core.events.AbstractEvent;

import com.google.gson.Gson;

/**
 * Event for configuration status information.
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class ConfigStatusInfoEvent extends AbstractEvent {

    static final String TYPE = "ConfigStatusInfoEvent";

    private static final Gson gson = new Gson();

    /**
     * Creates a new {@link ConfigStatusInfoEvent}.
     *
     * @param topic the topic of the event
     * @param configStatusInfo the corresponding configuration status information to be put as payload into the event
     * @param source the source of the event
     */
    public ConfigStatusInfoEvent(String topic, ConfigStatusInfo configStatusInfo, String source) {
        this(topic, gson.toJson(configStatusInfo), source);
    }

    /**
     * Creates a new {@link ConfigStatusInfoEvent}.
     *
     * @param topic the topic of the event
     * @param payload the payload of the event
     * @param source the source of the event
     */
    ConfigStatusInfoEvent(String topic, String payload, String source) {
        super(topic, payload, source);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
