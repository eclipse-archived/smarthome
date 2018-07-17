/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.lifx.internal.fields;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Tim Buckley
 */
@NonNullByDefault
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
