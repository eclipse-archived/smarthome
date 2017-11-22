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
 * A pseudo-uint64 field. Bytes will be stored directly in a long value, so
 * unexpected values will likely be shown if exposed to users. Most bit-level
 * operations should still work (addition, multiplication, shifting, etc).
 *
 * @author Tim Buckley
 */
public class UInt64Field extends Field<Long> {

    @Override
    public int defaultLength() {
        return 8;
    }

    @Override
    public Long value(ByteBuffer bytes) {
        return bytes.getLong();
    }

    @Override
    protected ByteBuffer bytesInternal(Long value) {
        return ByteBuffer.allocate(8).putLong(value);
    }

}
