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
public class UInt8Field extends Field<Integer> {

    public UInt8Field() {
        super(1);
    }

    @Override
    public int defaultLength() {
        return 1;
    }

    @Override
    public Integer value(ByteBuffer bytes) {
        return (int) (bytes.get() & 0xFF);
    }

    @Override
    public ByteBuffer bytesInternal(Integer value) {
        return ByteBuffer.allocate(1).put((byte) (value & 0xFF));
    }

}
