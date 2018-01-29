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
package org.eclipse.smarthome.binding.lifx.internal.protocol;

import java.nio.ByteBuffer;

/**
 * @author Tim Buckley - Initial Contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class GenericPacket extends Packet {

    @Override
    public int packetType() {
        return 0;
    }

    @Override
    protected int packetLength() {
        return 0;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(0);
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }

}
