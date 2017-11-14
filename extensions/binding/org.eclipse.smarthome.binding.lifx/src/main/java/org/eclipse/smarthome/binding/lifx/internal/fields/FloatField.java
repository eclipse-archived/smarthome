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
 * @author Tim Buckley
 */
public class FloatField extends Field<Float> {

    @Override
    public int defaultLength() {
        return 4;
    }

    @Override
    public Float value(ByteBuffer bytes) {
        return bytes.getFloat();
    }

    @Override
    protected ByteBuffer bytesInternal(Float value) {
        return ByteBuffer.allocate(4).putFloat(value);
    }

}
