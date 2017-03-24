/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.TemperatureControlSensorTransmitter;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonResponseContainer.impl.TemperatureControlStatus;

/**
 * The {@link TemperatureControlStatusListener} can be implemented to get informed by configuration and status changes.
 * <br>
 * It also can be implemented as discovery, than the id have to be {@link #DISCOVERY}.
 *
 * @author Michael Ochel
 * @author Matthias Siegele
 *
 */
public interface TemperatureControlStatusListener {

    /**
     * The id for discovery.
     */
    static Integer DISCOVERY = -2;

    /**
     * Will be called, if the configuration of the {@link TemperatureControlStatus} has changed.
     *
     * @param tempControlStatus that has changed
     */
    void configChanged(TemperatureControlStatus tempControlStatus);

    /**
     * Will be called, if the target temperature has changed.
     *
     * @param newValue of the target temperature
     */
    void onTargetTemperatureChanged(Float newValue);

    /**
     * Will be called, if the control value has changed.
     *
     * @param newValue of the control value
     */
    void onControlValueChanged(Integer newValue);

    /**
     * Registers a {@link TemperatureControlSensorTransmitter}.
     *
     * @param temperatureSensorTransmitter to register
     */
    void registerTemperatureSensorTransmitter(TemperatureControlSensorTransmitter temperatureSensorTransmitter);

    /**
     * Returns the id of this {@link TemperatureControlStatusListener}.
     *
     * @return id
     */
    Integer getTemperationControlStatusListenrID();
}
