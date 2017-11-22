/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
