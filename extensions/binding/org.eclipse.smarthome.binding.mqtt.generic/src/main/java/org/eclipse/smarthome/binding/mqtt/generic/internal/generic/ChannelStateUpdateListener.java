/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.mqtt.generic.internal.generic;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface ChannelStateUpdateListener {
    /**
     * A new value got published on a configured MQTT topic associated with the given channel uid.
     *
     * @param channelUID The channel uid
     * @param value The new value. Doesn't necessarily need to be different than the value before.
     */
    void updateChannelState(ChannelUID channelUID, State value);

    /**
     * A new value got published on a configured MQTT topic associated with the given channel uid.
     * The channel is configured to post the new state as command.
     *
     * @param channelUID The channel uid
     * @param value The new value. Doesn't necessarily need to be different than the value before.
     */
    void postChannelState(ChannelUID channelUID, Command value);

    /**
     * A new value got published on a configured MQTT topic associated with the given channel uid.
     * The channel is of kind Trigger.
     *
     * @param channelUID The channel uid
     * @param value The new value. Doesn't necessarily need to be different than the value before.
     */
    void triggerChannel(ChannelUID channelUID, String eventPayload);
}
