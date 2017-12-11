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
package org.eclipse.smarthome.binding.fsinternetradio.test;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Look up a free TCP/IP port on localhost for test execution. This ensures collision free parallel test execution using
 * embedded jetty on dynamic ports.
 *
 * @author Henning Treu - initial contribution
 *
 */
public class TestPortUtil {

    /**
     * Returns a free port number on localhost.
     *
     * Heavily inspired from org.eclipse.jdt.launching.SocketUtil (to avoid a dependency to JDT just because of this).
     * Slightly improved with close() missing in JDT. And throws exception instead of returning -1.
     *
     * @return a free port number on localhost
     * @throws IllegalStateException if unable to find a free port
     */
    public static int findFreePort() {
        try (final ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (final IOException ex) {
            throw new IllegalStateException("Could not find a free TCP/IP port to start embedded Jetty HTTP Server on",
                    ex);
        }
    }
}
