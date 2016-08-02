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
public class ByteField extends Field<ByteBuffer> {

    public ByteField() {
    }

    public ByteField(int length) {
        super(length);
    }

    @Override
    public int defaultLength() {
        return 2;
    }

    @Override
    public ByteBuffer value(ByteBuffer bytes) {
        byte[] data = new byte[length];
        bytes.get(data);

        return ByteBuffer.wrap(data);
    }

    @Override
    public ByteBuffer bytesInternal(ByteBuffer value) {
        return value;
    }

}
