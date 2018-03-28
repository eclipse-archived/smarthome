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
package org.eclipse.smarthome.binding.mqtt.internal;

import org.eclipse.smarthome.binding.mqtt.handler.MqttThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * Represents the value of the {@link MqttThingHandler}. This way we don't need to
 * implement different but quite similar handler classes, but have one handler class
 * and different and easily expendable value implementations.
 *
 * @author David Graeff - Initial contribution
 */
public interface AbstractMqttThingValue {
    /**
     * Returns the current value state.
     */
    public State getValue();

    /**
     * Updates the internal value state with the given command.
     *
     * @param command The command to update the internal value.
     * @return A string representation of the value to be send to Mqtt.
     */
    public String update(Command command) throws IllegalArgumentException;

    /**
     * Updates the internal value with the received Mqtt value.
     *
     * @param updatedValue
     */
    public State update(String updatedValue) throws IllegalArgumentException;
}
