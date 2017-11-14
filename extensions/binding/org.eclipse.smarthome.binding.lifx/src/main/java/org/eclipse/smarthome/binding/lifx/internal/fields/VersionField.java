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
 * @author Wouter Born - Add Thing properties
 */
public class VersionField extends Field<Version> {

    @Override
    public int defaultLength() {
        return 4;
    }

    @Override
    public Version value(ByteBuffer bytes) {
        long value = bytes.getInt() & 0xFFFFFFFFL;
        long major = (value >> 16) & 0xFFL;
        long minor = value & 0xFFL;
        return new Version(major, minor);
    }

    @Override
    public ByteBuffer bytesInternal(Version value) {
        return ByteBuffer.allocate(4).putInt((int) (((value.getMajor() << 16) | value.getMinor()) & 0xFFFFFFFFL));
    }

}
