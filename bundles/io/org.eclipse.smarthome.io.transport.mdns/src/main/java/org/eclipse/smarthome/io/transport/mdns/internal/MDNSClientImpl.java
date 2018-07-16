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
package org.eclipse.smarthome.io.transport.mdns.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.Duration;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.net.CidrAddress;
import org.eclipse.smarthome.core.net.NetworkAddressChangeListener;
import org.eclipse.smarthome.io.transport.mdns.MDNSClient;
import org.eclipse.smarthome.io.transport.mdns.ServiceDescription;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class starts the JmDNS and implements interface to register and unregister services.
 *
 * @author Victor Belov - Initial contribution
 * @author Gary Tse - Add NetworkAddressChangeListener to handle interface changes
 *
 */
@Component(immediate = true)
public class MDNSClientImpl implements MDNSClient, NetworkAddressChangeListener {
    private final Logger logger = LoggerFactory.getLogger(MDNSClientImpl.class);

    private final ConcurrentMap<InetAddress, JmDNS> jmdnsInstances = new ConcurrentHashMap<>();

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
        return new HashSet<>(jmdnsInstances.values());
    }

    @Activate
    public void activate() {
        for (InetAddress address : getAllInetAddresses()) {
            createJmDNSByAddress(address);
        }
    }

    @Deactivate
    public void deactivate() {
        close();
    }

    @Override
    public void addServiceListener(String type, ServiceListener listener) {
        jmdnsInstances.values().forEach(jmdns -> jmdns.addServiceListener(type, listener));
    }

    @Override
    public void removeServiceListener(String type, ServiceListener listener) {
        jmdnsInstances.values().forEach(jmdns -> jmdns.removeServiceListener(type, listener));
    }

    @Override
    public void registerService(ServiceDescription description) throws IOException {
        for (JmDNS instance : jmdnsInstances.values()) {
            logger.debug("Registering new service {} at {}:{} ({})", description.serviceType,
                    instance.getInetAddress().getHostAddress(), description.servicePort, instance.getName());
            // Create one ServiceInfo object for each JmDNS instance
            ServiceInfo serviceInfo = ServiceInfo.create(description.serviceType, description.serviceName,
                    description.servicePort, 0, 0, description.serviceProperties);
            instance.registerService(serviceInfo);
        }
    }

    @Override
    public void unregisterService(ServiceDescription description) {
        for (JmDNS instance : jmdnsInstances.values()) {
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

    @Override
    public void unregisterAllServices() {
        for (JmDNS instance : jmdnsInstances.values()) {
            instance.unregisterAllServices();
        }
    }

    @Override
    public ServiceInfo[] list(String type) {
        ServiceInfo[] services = new ServiceInfo[0];
        for (JmDNS instance : jmdnsInstances.values()) {
            services = concatenate(services, instance.list(type));
        }
        return services;
    }

    @Override
    public ServiceInfo[] list(String type, Duration timeout) {
        ServiceInfo[] services = new ServiceInfo[0];
        for (JmDNS instance : jmdnsInstances.values()) {
            services = concatenate(services, instance.list(type, timeout.toMillis()));
        }
        return services;
    }

    @Override
    public void close() {
        for (JmDNS jmdns : jmdnsInstances.values()) {
            closeQuietly(jmdns);
        }
    }

    private void closeQuietly(JmDNS jmdns) {
        try {
            jmdns.close();
        } catch (IOException e) {
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

    private void createJmDNSByAddress(InetAddress address) {
        try {
            JmDNS jmdns = JmDNS.create(address, "JmDNS-" + address.toString());
            jmdnsInstances.put(address, jmdns);
            logger.debug("mDNS service has been started ({} for IP {})", jmdns.getName(), address.getHostAddress());
        } catch (IOException e) {
            logger.debug("JmDNS instantiation failed ({})!", address.getHostAddress());
        }
    }

    @Override
    public void onChanged(List<CidrAddress> added, List<CidrAddress> removed) {
        // remove jmdns instances that no longer exist due to interface changed
        for (CidrAddress cidrAddress : removed) {
            InetAddress inetAddr = cidrAddress.getAddress();
            if (jmdnsInstances.containsKey(inetAddr)) {
                JmDNS jmdns = jmdnsInstances.get(inetAddr);
                closeQuietly(jmdns);
                jmdnsInstances.remove(inetAddr);
                logger.debug("mDNS service has been removed ({} for IP {})", jmdns.getName(),
                        inetAddr.getHostAddress());
            }
        }

        // add the new addresses, just like activate
        for (CidrAddress cidrAddress : added) {
            InetAddress address = cidrAddress.getAddress();

            // skip the loopback or link local addresses
            if (address.isLoopbackAddress() || address.isLinkLocalAddress()) {
                continue;
            }
            createJmDNSByAddress(address);
        }
    }

    @Override
    public void onPrimaryAddressChanged(@Nullable String oldPrimaryAddress, @Nullable String newPrimaryAddress) {
        // Intentionally left blank since the implementation is not interested in this information
    }
}
