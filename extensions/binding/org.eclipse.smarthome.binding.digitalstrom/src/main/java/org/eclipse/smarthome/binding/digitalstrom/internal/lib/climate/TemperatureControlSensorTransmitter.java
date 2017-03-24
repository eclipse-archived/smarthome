/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate;

/**
 * The {@link TemperatureControlSensorTransmitter} can be implement by subclasses to implement a
 * transmitter which can be used to push the target temperature or control value to a digitalSTROM zone.
 *
 * @author Michael Ochel - initial contributer
 * @author Matthias Siegele - initial contributer
 */
public interface TemperatureControlSensorTransmitter {

    /**
     * Maximal temperature, which can be set as target temperature in digitalSTROM.
     */
    static float MAX_TEMP = 50f;

    /**
     * Minimal temperature, which can be set as target temperature in digitalSTROM.
     */
    static float MIN_TEMP = -43.15f;

    /**
     * Maximal control value, which can be set as target temperature in digitalSTROM.
     */
    static float MAX_CONTROLL_VALUE = 100f;

    /**
     * Minimal control value, which can be set as target temperature in digitalSTROM.
     */
    static float MIN_CONTROLL_VALUE = 0f;

    /**
     * Pushes a new target temperature to a digitalSTROM zone.
     *
     * @param zoneID (must not be null)
     * @param newValue (must not be null)
     * @return true, if the push was successfully
     */
    boolean pushTargetTemperature(Integer zoneID, Float newValue);

    /**
     * Pushes a new control value to a digitalSTROM zone.
     *
     * @param zoneID (must not be null)
     * @param newValue (must not be null)
     * @return true, if the push was successfully
     */
    boolean pushControlValue(Integer zoneID, Float newValue);
}
