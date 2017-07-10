/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import static org.eclipse.smarthome.binding.lifx.LifxBindingConstants.DEFAULT_COLOR;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.binding.lifx.internal.fields.HSBK;
import org.eclipse.smarthome.binding.lifx.internal.listener.LifxLightStateListener;
import org.eclipse.smarthome.binding.lifx.internal.protocol.PowerState;
import org.eclipse.smarthome.binding.lifx.internal.protocol.SignalStrength;
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
    private HSBK[] colors;
    private PercentType infrared;
    private SignalStrength signalStrength;

    private LocalDateTime lastChange = LocalDateTime.MIN;

    private List<LifxLightStateListener> listeners = new CopyOnWriteArrayList<>();

    public void copy(LifxLightState other) {
        this.powerState = other.getPowerState();
        this.colors = other.getColors();
        this.infrared = other.getInfrared();
        this.signalStrength = other.getSignalStrength();
    }

    public PowerState getPowerState() {
        return powerState;
    }

    public HSBK getColor() {
        return colors != null && colors.length > 0 ? new HSBK(colors[0]) : null;
    }

    public HSBK getColor(int zoneIndex) {
        return colors != null && zoneIndex < colors.length ? new HSBK(colors[zoneIndex]) : null;
    }

    public HSBK getNullSafeColor() {
        HSBK color = getColor();
        return color != null ? color : new HSBK(DEFAULT_COLOR);
    }

    public HSBK getNullSafeColor(int zoneIndex) {
        HSBK color = getColor(zoneIndex);
        return color != null ? color : new HSBK(DEFAULT_COLOR);
    }

    public HSBK[] getColors() {
        if (colors == null) {
            return null;
        }

        HSBK[] colorsCopy = new HSBK[colors.length];
        for (int i = 0; i < colors.length; i++) {
            colorsCopy[i] = colors[i] != null ? new HSBK(colors[i]) : null;
        }
        return colorsCopy;
    }

    public HSBK[] getNullSafeColors() {
        HSBK[] colors = getColors();
        return colors != null ? colors : new HSBK[] { new HSBK(DEFAULT_COLOR) };
    }

    public PercentType getInfrared() {
        return infrared;
    }

    public SignalStrength getSignalStrength() {
        return signalStrength;
    }

    public void setColor(HSBType newHSB) {
        HSBK newColor = getNullSafeColor();
        newColor.setHSB(newHSB);
        setColor(newColor);
    }

    public void setColor(HSBType newHSB, int zoneIndex) {
        HSBK newColor = getNullSafeColor(zoneIndex);
        newColor.setHSB(newHSB);
        setColor(newColor, zoneIndex);
    }

    public void setBrightness(PercentType brightness) {
        HSBK[] newColors = getNullSafeColors();
        for (HSBK newColor : newColors) {
            newColor.setBrightness(brightness);
        }
        setColors(newColors);
    }

    public void setBrightness(PercentType brightness, int zoneIndex) {
        HSBK newColor = getNullSafeColor(zoneIndex);
        newColor.setBrightness(brightness);
        setColor(newColor, zoneIndex);
    }

    public void setColor(HSBK newColor) {
        HSBK[] newColors = getNullSafeColors();
        Arrays.fill(newColors, newColor);
        setColors(newColors);
    }

    public void setColor(HSBK newColor, int zoneIndex) {
        HSBK[] newColors = getNullSafeColors();
        newColors[zoneIndex] = newColor;
        setColors(newColors);
    }

    public void setColors(HSBK[] newColors) {
        HSBK[] oldColors = this.colors;
        this.colors = newColors;
        updateLastChange();
        for (LifxLightStateListener listener : listeners) {
            listener.handleColorsChange(oldColors, newColors);
        }
    }

    public void setPowerState(OnOffType newOnOff) {
        setPowerState(PowerState.fromOnOffType(newOnOff));
    }

    public void setPowerState(PowerState newPowerState) {
        PowerState oldPowerState = this.powerState;
        this.powerState = newPowerState;
        updateLastChange();
        for (LifxLightStateListener listener : listeners) {
            listener.handlePowerStateChange(oldPowerState, newPowerState);
        }
    }

    public void setTemperature(PercentType temperature) {
        HSBK[] newColors = getNullSafeColors();
        for (HSBK newColor : newColors) {
            newColor.setTemperature(temperature);
        }
        setColors(newColors);
    }

    public void setTemperature(PercentType temperature, int zoneIndex) {
        HSBK newColor = getNullSafeColor(zoneIndex);
        newColor.setTemperature(temperature);
        setColor(newColor, zoneIndex);
    }

    public void setInfrared(PercentType newInfrared) {
        PercentType oldInfrared = this.infrared;
        this.infrared = newInfrared;
        updateLastChange();
        for (LifxLightStateListener listener : listeners) {
            listener.handleInfraredChange(oldInfrared, newInfrared);
        }
    }

    public void setSignalStrength(SignalStrength newSignalStrength) {
        SignalStrength oldSignalStrength = this.signalStrength;
        this.signalStrength = newSignalStrength;
        updateLastChange();
        for (LifxLightStateListener listener : listeners) {
            listener.handleSignalStrengthChange(oldSignalStrength, newSignalStrength);
        }
    }

    private void updateLastChange() {
        lastChange = LocalDateTime.now();
    }

    public Duration getDurationSinceLastChange() {
        return Duration.between(lastChange, LocalDateTime.now());
    }

    public void addListener(LifxLightStateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(LifxLightStateListener listener) {
        listeners.remove(listener);
    }

}
