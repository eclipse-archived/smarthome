/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.listener;

import org.eclipse.smarthome.binding.lifx.internal.LifxLightState;
import org.eclipse.smarthome.binding.lifx.internal.protocol.PowerState;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * The {@link LifxLightStateListener} is notified when the properties of a {@link LifxLightState} change.
 *
 * @author Wouter Born - Initial contribution
 */
public interface LifxLightStateListener {

    /**
     * Called when the HSB property changes.
     *
     * @param oldHSB the old HSB value
     * @param newHSB the new HSB value
     */
    void handleHSBChange(HSBType oldHSB, HSBType newHSB);

    /**
     * Called when the power state property changes.
     *
     * @param oldPowerState the old power state value
     * @param newPowerState the new power state value
     */
    void handlePowerStateChange(PowerState oldPowerState, PowerState newPowerState);

    /**
     * Called when the temperature property changes.
     *
     * @param oldTemperature the old temperature value
     * @param newTemperature the new temperature value
     */
    void handleTemperatureChange(PercentType oldTemperature, PercentType newTemperature);

}
