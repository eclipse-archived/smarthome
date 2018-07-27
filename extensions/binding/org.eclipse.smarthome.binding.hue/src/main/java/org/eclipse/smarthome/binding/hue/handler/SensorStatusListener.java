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
package org.eclipse.smarthome.binding.hue.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.internal.FullLight;
import org.eclipse.smarthome.binding.hue.internal.FullSensor;
import org.eclipse.smarthome.binding.hue.internal.HueBridge;

/**
 * The {@link SensorStatusListener} is notified when a sensor status has changed or a sensor has been removed or added.
 *
 * @author Samuel Leisering - Sensor Support
 *
 */
@NonNullByDefault
public interface SensorStatusListener {

    /**
     * This method is called whenever the state of the given light has changed. The new state can be obtained by
     * {@link FullLight#getState()}.
     *
     * @param bridge The bridge the changed light is connected to.
     * @param light The light which received the state update.
     */
    void onSensorStateChanged(@Nullable HueBridge bridge, FullSensor sensor);

    /**
     * This method is called whenever a sensor is removed.
     *
     * @param bridge The bridge the removed sensor was connected to.
     * @param light The removed sensor
     */
    void onSensorRemoved(@Nullable HueBridge bridge, FullSensor sensor);

    /**
     * This method is called whenever a sensor is added.
     *
     * @param bridge The bridge the added sensor was connected to.
     * @param light The added sensor
     */
    void onSensorAdded(@Nullable HueBridge bridge, FullSensor sensor);
}
