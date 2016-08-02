/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.eclipse.smarthome.io.transport.mdns.MDNSClient;
import org.eclipse.smarthome.io.transport.mdns.ServiceDescription;
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

    /**
     * {@inheritDoc}
     */
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
        close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addServiceListener(String type, ServiceListener listener) {
        for (JmDNS instance : jmdnsInstances) {
            instance.addServiceListener(type, listener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeServiceListener(String type, ServiceListener listener) {
        for (JmDNS instance : jmdnsInstances) {
            instance.removeServiceListener(type, listener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerService(ServiceDescription description) throws IOException {
        for (JmDNS instance : jmdnsInstances) {
            logger.debug("Registering new service {} at {}:{} ({})", description.serviceType,
                    instance.getInetAddress().getHostAddress(), description.servicePort, instance.getName());
            // Create one ServiceInfo object for each JmDNS instance
            ServiceInfo serviceInfo = ServiceInfo.create(description.serviceType, description.serviceName,
                    description.servicePort, 0, 0, description.serviceProperties);
            instance.registerService(serviceInfo);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterService(ServiceDescription description) {
        for (JmDNS instance : jmdnsInstances) {
            try {
                logger.debug("Unregistering service {} at {}:{} ({})", description.serviceType,
                        instance.getInetAddress().getHostAddress(), description.servicePort, instance.getName());
            } catch (IOException e) {
                logger.debug("Unregistering service {} ({})", description.serviceType, instance.getName());
            }
            ServiceInfo serviceInfo = ServiceInfo.create(description.serviceType, description.serviceName,
                    description.servicePort, 0, 0, description.serviceProperties);
            instance.unregisterService(serviceInfo);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterAllServices() {
        for (JmDNS instance : jmdnsInstances) {
            instance.unregisterAllServices();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceInfo[] list(String type) {
        ServiceInfo[] services = new ServiceInfo[0];
        for (JmDNS instance : jmdnsInstances) {
            services = concatenate(services, instance.list(type));
        }
        return services;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        for (JmDNS jmdns : jmdnsInstances) {
            try {
                jmdns.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Concatenate two arrays of ServiceInfo
     *
     * @param a: the first array
     * @param b: the second array
     * @return an array of ServiceInfo
     */
    private ServiceInfo[] concatenate(ServiceInfo[] a, ServiceInfo[] b) {
        int aLen = a.length;
        int bLen = b.length;

        ServiceInfo[] c = new ServiceInfo[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }
}
