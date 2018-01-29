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
package org.eclipse.smarthome.binding.dmx.internal.dmxoverethernet;

/**
 * The {@link ArtnetNode} represents a sending or receiving node with address and port
 * default address is set to 5568 for sACN/E1.31
 *
 * @author Jan N. Klug - Initial contribution
 *
 */

public class SacnNode extends IpNode {
    public static final int DEFAULT_PORT = 5568;

    /**
     * constructor with address
     *
     * @param addrString address in format address[:port]
     */
    public SacnNode(String addrString) {
        super(addrString);
        if (port == 0) {
            port = DEFAULT_PORT;
        }
    }

    /**
     * create a SacnNode with a broadcast-address
     *
     * @param universeId the universe to create the node for
     * @return the multicast SacnNode
     */
    public static SacnNode getBroadcastNode(int universeId) {
        return new SacnNode(String.format("239.255.%d.%d", universeId / 256, universeId % 256));
    }
}
