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
 * @author Karel Goderis
 */
public class MACAddressField extends Field<MACAddress> {

    public MACAddressField() {
        super(8);
    }

    @Override
    public int defaultLength() {
        return 8;
    }

    @Override
    public MACAddress value(ByteBuffer bytes) {
        byte[] data = new byte[length];
        bytes.get(data);

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.limit(length - 2);
        return new MACAddress(buffer);
    }

    @Override
    protected ByteBuffer bytesInternal(MACAddress value) {
        return value.getBytes().duplicate();
    }

    @Override
    public ByteBuffer bytes(MACAddress value) {
        byte[] data = new byte[length];
        ByteBuffer bytes = bytesInternal(value);
        bytes.rewind();
        bytes.get(data, 0, bytes.limit());
        ByteBuffer buf = ByteBuffer.wrap(data);
        buf.rewind();
        return buf;
    }

}
