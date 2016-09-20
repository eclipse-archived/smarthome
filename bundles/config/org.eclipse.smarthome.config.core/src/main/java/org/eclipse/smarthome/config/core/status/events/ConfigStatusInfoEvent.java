/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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

    private final ConfigStatusInfo configStatusInfo;

    private static final Gson gson = new Gson();

    /**
     * Creates a new {@link ConfigStatusInfoEvent}.
     *
     * @param topic the topic of the event
     * @param configStatusInfo the corresponding configuration status information to be put as payload into the event
     */
    public ConfigStatusInfoEvent(String topic, ConfigStatusInfo configStatusInfo) {
        super(topic, gson.toJson(configStatusInfo), null);
        this.configStatusInfo = configStatusInfo;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return configStatusInfo.toString();
    }
}
