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
public class UInt16Field extends Field<Integer> {

    @Override
    public int defaultLength() {
        return 2;
    }

    @Override
    public Integer value(ByteBuffer bytes) {
        return bytes.getShort() & 0xFFFF;
    }

    @Override
    public ByteBuffer bytesInternal(Integer value) {
        return ByteBuffer.allocate(2).putShort((short) (value & 0xFFFF));
    }

}
