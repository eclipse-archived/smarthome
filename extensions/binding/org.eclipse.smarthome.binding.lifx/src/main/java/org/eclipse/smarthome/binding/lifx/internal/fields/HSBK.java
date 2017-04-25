/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.fields;

import static org.eclipse.smarthome.binding.lifx.internal.LifxUtils.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * @author Wouter Born - Add support for MultiZone light control
 */
public class HSBK {

    private static final String DEFAULT_PROPERTY_NAME = "hsbk";

    private int hue;
    private int saturation;
    private int brightness;
    private int kelvin;

    public HSBK(int hue, int saturation, int brightness, int kelvin) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
        this.kelvin = kelvin;
    }

    public HSBK(HSBK other) {
        this(other.hue, other.saturation, other.brightness, other.kelvin);
    }

    public HSBK(HSBType hsb, PercentType temperature) {
        setHSB(hsb);
        setTemperature(temperature);
    }

    public int getHue() {
        return hue;
    }

    public int getSaturation() {
        return saturation;
    }

    public int getBrightness() {
        return brightness;
    }

    public int getKelvin() {
        return kelvin;
    }

    public HSBType getHSB() {
        DecimalType hue = hueToDecimalType(this.hue);
        PercentType saturation = saturationToPercentType(this.saturation);
        PercentType brightness = brightnessToPercentType(this.brightness);
        return new HSBType(hue, saturation, brightness);
    }

    public PercentType getTemperature() {
        return kelvinToPercentType(kelvin);
    }

    public void setHSB(HSBType hsb) {
        setHue(hsb.getHue());
        setSaturation(hsb.getSaturation());
        setBrightness(hsb.getBrightness());
    }

    public void setHue(DecimalType hue) {
        this.hue = decimalTypeToHue(hue);
    }

    public void setSaturation(PercentType saturation) {
        this.saturation = percentTypeToSaturation(saturation);
    }

    public void setBrightness(PercentType brightness) {
        this.brightness = percentTypeToBrightness(brightness);
    }

    public void setTemperature(PercentType temperature) {
        kelvin = percentTypeToKelvin(temperature);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + hue;
        result = prime * result + saturation;
        result = prime * result + brightness;
        result = prime * result + kelvin;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HSBK other = (HSBK) obj;
        if (hue != other.hue) {
            return false;
        }
        if (saturation != other.saturation) {
            return false;
        }
        if (brightness != other.brightness) {
            return false;
        }
        if (kelvin != other.kelvin) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return toString(DEFAULT_PROPERTY_NAME);
    }

    public String toString(String propertyName) {
        return String.format("%s=%d,%d,%d,%d", propertyName, hue, saturation, brightness, kelvin);
    }
}
