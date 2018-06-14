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
package org.eclipse.smarthome.binding.homematic.handler;

import java.util.ArrayList;
import java.util.List;

/**
 * A very simple pool implementation that handles free port numbers used by the RPC server if multiple bridges are
 * configured.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SimplePortPool {
    private static int START_PORT = 9125;

    private List<PortInfo> availablePorts = new ArrayList<PortInfo>();

    /**
     * Adds the specified port to the pool an mark it as in use.
     */
    public void setInUse(int port) {
        PortInfo portInfo = new PortInfo();
        portInfo.port = port;
        portInfo.free = false;
        availablePorts.add(portInfo);
    }

    /**
     * Returns the next free port number.
     */
    public synchronized int getNextPort() {
        for (PortInfo portInfo : availablePorts) {
            if (portInfo.free) {
                portInfo.free = false;
                return portInfo.port;
            }
        }

        PortInfo portInfo = new PortInfo();
        while (isPortInUse(START_PORT++)) {
        }
        portInfo.port = START_PORT - 1;
        portInfo.free = false;
        availablePorts.add(portInfo);

        return portInfo.port;
    }

    /**
     * Returns true, if the specified port is not in use.
     */
    private boolean isPortInUse(int port) {
        for (PortInfo portInfo : availablePorts) {
            if (portInfo.port == port) {
                return !portInfo.free;
            }
        }
        return false;
    }

    /**
     * Releases a unused port number.
     */
    public synchronized void release(int port) {
        for (PortInfo portInfo : availablePorts) {
            if (portInfo.port == port) {
                portInfo.free = true;
            }
        }
    }

    private class PortInfo {
        int port;
        boolean free;
    }
}
