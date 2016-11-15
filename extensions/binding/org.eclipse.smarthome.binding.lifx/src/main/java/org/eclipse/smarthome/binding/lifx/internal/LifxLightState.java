/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.binding.lifx.internal.listener.LifxLightStateListener;
import org.eclipse.smarthome.binding.lifx.internal.protocol.PowerState;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * The {@link LifxLightState} stores the properties that represent the state of a light.
 *
 * @author Wouter Born - Extracted class from LifxLightHandler, added listener logic
 */
public class LifxLightState {

    private PowerState powerState;
    private HSBType hsb;
    private PercentType temperature;

    private long lastChange;

    private List<LifxLightStateListener> listeners = new CopyOnWriteArrayList<>();

    public void copy(LifxLightState other) {
        this.powerState = other.getPowerState();
        this.hsb = other.getHSB();
        this.temperature = other.getTemperature();
    }

    public PowerState getPowerState() {
        return powerState;
    }

    public HSBType getHSB() {
        return hsb;
    }

    public PercentType getTemperature() {
        return temperature;
    }

    public void setHSB(HSBType newHSB) {
        HSBType oldHSB = this.hsb;
        this.hsb = newHSB;
        updateLastChange();
        for (LifxLightStateListener listener : listeners) {
            listener.handleHSBChange(oldHSB, newHSB);
        }
    }

    public void setPowerState(OnOffType onOffType) {
        setPowerState(onOffType == OnOffType.ON ? PowerState.ON : PowerState.OFF);
    }

    public void setPowerState(PowerState newPowerState) {
        PowerState oldPowerState = this.powerState;
        this.powerState = newPowerState;
        updateLastChange();
        for (LifxLightStateListener listener : listeners) {
            listener.handlePowerStateChange(oldPowerState, newPowerState);
        }
    }

    public void setTemperature(PercentType newTemperature) {
        PercentType oldTemperature = this.temperature;
        this.temperature = newTemperature;
        updateLastChange();
        for (LifxLightStateListener listener : listeners) {
            listener.handleTemperatureChange(oldTemperature, newTemperature);
        }
    }

    private void updateLastChange() {
        lastChange = System.currentTimeMillis();
    }

    public long getMillisSinceLastChange() {
        return System.currentTimeMillis() - lastChange;
    }

    public void addListener(LifxLightStateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(LifxLightStateListener listener) {
        listeners.remove(listener);
    }

}
