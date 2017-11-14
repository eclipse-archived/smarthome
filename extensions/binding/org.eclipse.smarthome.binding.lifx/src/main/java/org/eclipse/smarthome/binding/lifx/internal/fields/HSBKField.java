/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.fields;

import java.nio.ByteBuffer;

/**
 * @author Wouter Born - Add support for MultiZone light control
 */
public class HSBKField extends Field<HSBK> {

    public static final Field<Integer> FIELD_HUE = new UInt16Field().little();
    public static final Field<Integer> FIELD_SATURATION = new UInt16Field().little();
    public static final Field<Integer> FIELD_BRIGHTNESS = new UInt16Field().little();
    public static final Field<Integer> FIELD_KELVIN = new UInt16Field().little();

    @Override
    public int defaultLength() {
        return 8;
    }

    @Override
    public HSBK value(ByteBuffer bytes) {
        int hue = FIELD_HUE.value(bytes);
        int saturation = FIELD_SATURATION.value(bytes);
        int brightness = FIELD_BRIGHTNESS.value(bytes);
        int kelvin = FIELD_KELVIN.value(bytes);

        return new HSBK(hue, saturation, brightness, kelvin);
    }

    @Override
    protected ByteBuffer bytesInternal(HSBK value) {
        return ByteBuffer.allocate(defaultLength()).put(FIELD_HUE.bytes(value.getHue()))
                .put(FIELD_SATURATION.bytes(value.getSaturation())).put(FIELD_BRIGHTNESS.bytes(value.getBrightness()))
                .put(FIELD_KELVIN.bytes(value.getKelvin()));
    }

}
