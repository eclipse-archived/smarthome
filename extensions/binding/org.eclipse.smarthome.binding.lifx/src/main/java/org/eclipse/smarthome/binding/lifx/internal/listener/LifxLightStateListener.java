/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.listener;

import org.eclipse.smarthome.binding.lifx.internal.LifxLightState;
import org.eclipse.smarthome.binding.lifx.internal.fields.HSBK;
import org.eclipse.smarthome.binding.lifx.internal.protocol.PowerState;
import org.eclipse.smarthome.binding.lifx.internal.protocol.SignalStrength;
import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * The {@link LifxLightStateListener} is notified when the properties of a {@link LifxLightState} change.
 *
 * @author Wouter Born - Initial contribution
 */
public interface LifxLightStateListener {

    /**
     * Called when the colors property changes.
     *
     * @param oldColors the old colors value
     * @param newColors the new colors value
     */
    void handleColorsChange(HSBK[] oldColors, HSBK[] newColors);

    /**
     * Called when the power state property changes.
     *
     * @param oldPowerState the old power state value
     * @param newPowerState the new power state value
     */
    void handlePowerStateChange(PowerState oldPowerState, PowerState newPowerState);

    /**
     * Called when the infrared property changes.
     *
     * @param oldInfrared the old infrared value
     * @param newInfrared the new infrared value
     */
    void handleInfraredChange(PercentType oldInfrared, PercentType newInfrared);

    /**
     * Called when the signal strength property changes.
     *
     * @param oldSignalStrength the old signal strength value
     * @param newSignalStrength the new signal strength value
     */
    void handleSignalStrengthChange(SignalStrength oldSignalStrength, SignalStrength newSignalStrength);
}
