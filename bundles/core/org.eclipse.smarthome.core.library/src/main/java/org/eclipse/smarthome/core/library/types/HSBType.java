/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import java.math.BigDecimal;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.ComplexType;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.State;

/**
 * The HSBType is a complex type with constituents for hue, saturation and
 * brightness and can be used for color items.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class HSBType extends PercentType implements ComplexType, State, Command {

    private static final long serialVersionUID = 322902950356613226L;

    // constants for the constituents
    static final public String KEY_HUE = "h";
    static final public String KEY_SATURATION = "s";
    static final public String KEY_BRIGHTNESS = "b";

    // constants for colors
    static final public HSBType BLACK = new HSBType("0,0,0");
    static final public HSBType WHITE = new HSBType("0,0,100");
    static final public HSBType RED = new HSBType("0,100,100");
    static final public HSBType GREEN = new HSBType("120,100,100");
    static final public HSBType BLUE = new HSBType("240,100,100");

    protected BigDecimal hue;
    protected BigDecimal saturation;

    public HSBType(DecimalType h, PercentType s, PercentType b) {
        this.hue = h.toBigDecimal();
        this.saturation = s.toBigDecimal();
        this.value = b.toBigDecimal();
    }

    public HSBType(String value) {
        if (value != null) {
            String[] constituents = value.split(",");
            if (constituents.length == 3) {
                this.hue = new BigDecimal(constituents[0]);
                this.saturation = new BigDecimal(constituents[1]);
                this.value = new BigDecimal(constituents[2]);
            } else {
                throw new IllegalArgumentException(value + " is not a valid HSBType syntax");
            }
        } else {
            throw new IllegalArgumentException("Constructor argument must not be null");
        }
    }

    public static HSBType valueOf(String value) {
        return new HSBType(value);
    }

    @Override
    public SortedMap<String, PrimitiveType> getConstituents() {
        TreeMap<String, PrimitiveType> map = new TreeMap<String, PrimitiveType>();
        map.put(KEY_HUE, getHue());
        map.put(KEY_SATURATION, getSaturation());
        map.put(KEY_BRIGHTNESS, getBrightness());
        return map;
    }

    public DecimalType getHue() {
        return new DecimalType(hue);
    }

    public PercentType getSaturation() {
        return new PercentType(saturation);
    }

    public PercentType getBrightness() {
        return new PercentType(value);
    }

    public PercentType getRed() {
        return toRGB()[0];
    }

    public PercentType getGreen() {
        return toRGB()[1];
    }

    public PercentType getBlue() {
        return toRGB()[2];
    }

    /**
     * Returns the RGB value representing the color in the default sRGB
     * color model.
     * (Bits 24-31 are alpha, 16-23 are red, 8-15 are green, 0-7 are blue).
     *
     * @return the RGB value of the color in the default sRGB color model
     */
    public int getRGB() {
        PercentType[] rgb = toRGB();
        return ((0xFF) << 24) | ((convertPercentToByte(rgb[0]) & 0xFF) << 16)
                | ((convertPercentToByte(rgb[1]) & 0xFF) << 8) | ((convertPercentToByte(rgb[2]) & 0xFF) << 0);
    }

    @Override
    public String toString() {
        return getHue() + "," + getSaturation() + "," + getBrightness();
    }

    @Override
    public int hashCode() {
        int tmp = 10000 * (getHue() == null ? 0 : getHue().hashCode());
        tmp += 100 * (getSaturation() == null ? 0 : getSaturation().hashCode());
        tmp += (getBrightness() == null ? 0 : getBrightness().hashCode());
        return tmp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof HSBType))
            return false;
        HSBType other = (HSBType) obj;
        if ((getHue() != null && other.getHue() == null) || (getHue() == null && other.getHue() != null)
                || (getSaturation() != null && other.getSaturation() == null)
                || (getSaturation() == null && other.getSaturation() != null)
                || (getBrightness() != null && other.getBrightness() == null)
                || (getBrightness() == null && other.getBrightness() != null)) {
            return false;
        }
        if (!getHue().equals(other.getHue()) || !getSaturation().equals(other.getSaturation())
                || !getBrightness().equals(other.getBrightness())) {
            return false;
        }
        return true;
    }

    public PercentType[] toRGB() {
        PercentType red = null;
        PercentType green = null;
        PercentType blue = null;

        BigDecimal h = hue.divide(new BigDecimal(100), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal s = saturation.divide(new BigDecimal(100));

        int h_int = h.multiply(new BigDecimal(5)).divide(new BigDecimal(3), 10, BigDecimal.ROUND_HALF_UP).intValue();
        BigDecimal f = h.multiply(new BigDecimal(5)).divide(new BigDecimal(3), 10, BigDecimal.ROUND_HALF_UP)
                .remainder(BigDecimal.ONE);
        PercentType a = new PercentType(value.multiply(BigDecimal.ONE.subtract(s)));
        PercentType b = new PercentType(value.multiply(BigDecimal.ONE.subtract(s.multiply(f))));
        PercentType c = new PercentType(
                value.multiply(BigDecimal.ONE.subtract((BigDecimal.ONE.subtract(f)).multiply(s))));

        if (h_int == 0 || h_int == 6) {
            red = getBrightness();
            green = c;
            blue = a;
        } else if (h_int == 1) {
            red = b;
            green = getBrightness();
            blue = a;
        } else if (h_int == 2) {
            red = a;
            green = getBrightness();
            blue = c;
        } else if (h_int == 3) {
            red = a;
            green = b;
            blue = getBrightness();
        } else if (h_int == 4) {
            red = c;
            green = a;
            blue = getBrightness();
        } else if (h_int == 5) {
            red = getBrightness();
            green = a;
            blue = b;
        } else {
            throw new RuntimeException();
        }
        return new PercentType[] { red, green, blue };
    }

    private int convertPercentToByte(PercentType percent) {
        return percent.value.multiply(BigDecimal.valueOf(255))
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP).intValue();
    }

}
