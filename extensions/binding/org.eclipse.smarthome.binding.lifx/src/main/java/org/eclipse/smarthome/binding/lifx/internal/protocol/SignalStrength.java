/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.protocol;

/**
 * The signal strength of a light.
 *
 * @author Wouter Born - Add signal strength channel
 */
public class SignalStrength {

    private double milliWatts;

    public SignalStrength(double milliWatts) {
        this.milliWatts = milliWatts;
    }

    /**
     * Returns the signal strength.
     *
     * @return the signal strength in milliwatts (mW).
     */
    public double getMilliWatts() {
        return milliWatts;
    }

    /**
     * Returns the signal strength as a quality percentage:
     * <ul>
     * <li>RSSI <= -100: returns 0
     * <li>-100 < RSSI < -50: returns a value between 0 and 1 (linearly distributed)
     * <li>RSSI >= -50: returns 1
     * <ul>
     *
     * @return a value between 0 and 1. 0 being worst strength and 1
     *         being best strength.
     */
    public double toQualityPercentage() {
        return Math.min(100, Math.max(0, 2 * (toRSSI() + 100))) / 100;
    }

    /**
     * Returns the signal strength as a quality rating.
     *
     * @return one of the values: 0, 1, 2, 3 or 4. 0 being worst strength and 4
     *         being best strength.
     */
    public byte toQualityRating() {
        return (byte) Math.round(toQualityPercentage() * 4);
    }

    /**
     * Returns the received signal strength indicator (RSSI).
     *
     * @return a value <= 0. 0 being best strength and more negative values indicate worser strength.
     */
    public double toRSSI() {
        return 10 * Math.log10(milliWatts);
    }

    @Override
    public String toString() {
        return "SignalStrength [milliWatts=" + milliWatts + ", rssi=" + Math.round(toRSSI()) + "]";
    }

}
