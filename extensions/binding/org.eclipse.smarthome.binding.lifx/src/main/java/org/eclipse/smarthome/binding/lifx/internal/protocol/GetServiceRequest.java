/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.protocol;

import java.nio.ByteBuffer;

/**
 * @author Tim Buckley - Initial Contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class GetServiceRequest extends Packet {

    public static final int TYPE = 0x02;

    // public static final int PROTOCOL_DEFAULT = 21504; // ??

    public GetServiceRequest() {
        setTagged(true);
        setAddressable(true);
    }

    @Override
    public int packetLength() {
        return 0;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        // empty
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(0);
    }

    @Override
    public int[] expectedResponses() {
        return new int[] { StateServiceResponse.TYPE }; // UDP packets cannot have responses
    }

}
