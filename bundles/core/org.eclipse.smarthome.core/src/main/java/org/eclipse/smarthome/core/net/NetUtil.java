/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.net;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some utility functions related to network interfaces etc.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class NetUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetUtil.class);

    private NetUtil() {
    }

    /**
     * Get the first candidate for a local IPv4 host address (non loopback, non localhost).
     */
    public static String getLocalIpv4HostAddress() {
        try {
            String hostAddress = null;
            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                final NetworkInterface current = interfaces.nextElement();
                if (!current.isUp() || current.isLoopback() || current.isVirtual()) {
                    continue;
                }
                final Enumeration<InetAddress> addresses = current.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    final InetAddress current_addr = addresses.nextElement();
                    if (current_addr.isLoopbackAddress() || (current_addr instanceof Inet6Address)) {
                        continue;
                    }
                    if (hostAddress != null) {
                        LOGGER.warn("Found multiple local interfaces - ignoring {}", current_addr.getHostAddress());
                    } else {
                        hostAddress = current_addr.getHostAddress();
                    }
                }
            }
            return hostAddress;
        } catch (SocketException ex) {
            LOGGER.error("Could not retrieve network interface: " + ex.getMessage(), ex);
            return null;
        }
    }

}
