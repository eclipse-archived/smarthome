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
 * Defines an abstract field that can be used to convert between native
 * datatypes and a LIFX-compatible byte representation.
 *
 * @param <T> the field datatype
 *
 * @author Tim Buckley
 */
public abstract class Field<T> {

    protected final int length;

    public int getLength() {
        return length;
    }

    /**
     * Creates a new Field instance using the default length.
     */
    public Field() {
        length = defaultLength();
    }

    /**
     * Creates a new Field instance using the specified length.
     *
     * @param length the field length to use
     */
    public Field(int length) {
        this.length = length;
    }

    /**
     * Returns a default field length. Client classes should always use
     * the {@code length} field (via {@code getLength()} to get the actual field
     * length.
     *
     * @return the default length of this field to use if none is specified
     */
    public abstract int defaultLength();

    /**
     * Converts the given ByteBuffer to a native datatype. The actual behavior
     * of this method is left to the implementation.
     *
     * @param bytes the buffer to convert
     * @return a native representation of the contents of the buffer
     */
    public abstract T value(ByteBuffer bytes);

    /**
     * Converts the given value to a ByteBuffer. Actual behavior is determined
     * by the concrete implementation.
     *
     * @param value the value to convert
     * @return a buffer containing a representation of the value
     */
    public ByteBuffer bytes(T value) {
        ByteBuffer buf = bytesInternal(value);
        buf.rewind();
        return buf;
    }

    /**
     * Called by {@link #bytes(Object)} to create a ByteBuffer containing an
     * encoded representation of the given value. The buffer will be have
     * {@link ByteBuffer#rewind()} called automatically by {@code bytes()}.
     *
     * @param value the value to convert
     * @return a ByteBuffer containing the converted value
     */
    protected abstract ByteBuffer bytesInternal(T value);

    /**
     * Returns a {@link LittleField} wrapping this field, effectively converting
     * it to little endian.
     *
     * @return a little-endian version of this field
     */
    public Field<T> little() {
        return new LittleField<>(this);
    }

}
