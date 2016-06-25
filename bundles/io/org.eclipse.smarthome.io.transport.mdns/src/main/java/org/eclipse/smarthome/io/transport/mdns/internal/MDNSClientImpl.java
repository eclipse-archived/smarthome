/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mdns.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.jmdns.JmDNS;

import org.eclipse.smarthome.io.transport.mdns.MDNSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class starts the JmDNS and implements interface to register and unregister services.
 *
 * @author Victor Belov
 *
 */
public class MDNSClientImpl implements MDNSClient {
    private final Logger logger = LoggerFactory.getLogger(MDNSClientImpl.class);

    private Set<JmDNS> jmdnsInstances = new CopyOnWriteArraySet<>();

    private static Set<InetAddress> getAllInetAddresses() {
        final Set<InetAddress> addresses = new HashSet<>();
        Enumeration<NetworkInterface> itInterfaces;
        try {
            itInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (final SocketException e) {
            return addresses;
        }
        while (itInterfaces.hasMoreElements()) {
            final NetworkInterface iface = itInterfaces.nextElement();
            try {
                if (!iface.isUp() || iface.isLoopback()) {
                    continue;
                }
            } catch (final SocketException ex) {
                continue;
            }
            final Enumeration<InetAddress> itAddresses = iface.getInetAddresses();
            while (itAddresses.hasMoreElements()) {
                final InetAddress address = itAddresses.nextElement();
                if (address.isLoopbackAddress() || address.isLinkLocalAddress()) {
                    continue;
                }
                addresses.add(address);
            }
        }
        return addresses;
    }

    @Override
    public Set<JmDNS> getClientInstances() {
        return jmdnsInstances;
    }

    public void activate() {
        for (InetAddress address : getAllInetAddresses()) {
            try {
                JmDNS jmdns = JmDNS.create(address, "JmDNS-IP-" + (jmdnsInstances.size() + 1));
                jmdnsInstances.add(jmdns);
                logger.debug("mDNS service has been started ({} for IP {})", jmdns.getName(), address.getHostAddress());
            } catch (IOException e) {
                logger.debug("JmDNS instanciation failed ({})!", address.getHostAddress());
            }
        }
        if (jmdnsInstances.isEmpty()) {
            // we must cancel the activation of this component here
            throw new IllegalStateException("No mDNS service has been started");
        }
    }

    public void deactivate() {
        for (JmDNS jmdns : jmdnsInstances) {
            try {
                jmdns.close();
            } catch (IOException e) {
            }
        }
    }
}
