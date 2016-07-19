/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
public class UInt32Field extends Field<Long> {

    @Override
    public int defaultLength() {
        return 4;
    }

    @Override
    public Long value(ByteBuffer bytes) {
        return bytes.getInt() & 0xFFFFFFFFL;
    }

    @Override
    public ByteBuffer bytesInternal(Long value) {
        return ByteBuffer.allocate(4).putInt((int) (value & 0xFFFFFFFFL));
    }

}
