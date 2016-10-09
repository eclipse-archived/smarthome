/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import java.util.Arrays;

import org.eclipse.smarthome.core.internal.PortableBase64;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.State;

/**
 * This type can be used for all binary data such as images, documents, sounds etc.
 * Note that it is NOT adequate for any kind of streams, but only for fixed-size data.
 *
 * @author Kai Kreuzer
 *
 */
public class RawType implements PrimitiveType, State {

    protected byte[] bytes;

    public RawType() {
        this(new byte[0]);
    }

    public RawType(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public static RawType valueOf(String value) {
        return new RawType(PortableBase64.getDecoder().decode(value));
    }

    @Override
    public String toString() {
        return String.format("raw type: %d bytes", bytes.length);
    }

    @Override
    public String toFullString() {
        return PortableBase64.getEncoder().encode(bytes);
    }

    @Override
    public String format(String pattern) {
        return toFullString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(bytes);
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
        RawType other = (RawType) obj;
        if (!Arrays.equals(bytes, other.bytes)) {
            return false;
        }
        return true;
    }

}
