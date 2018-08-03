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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.Duration;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.eclipse.smarthome.core.net.CidrAddress;
import org.eclipse.smarthome.core.net.NetworkAddressChangeListener;
import org.eclipse.smarthome.io.transport.mdns.MDNSClient;
import org.eclipse.smarthome.io.transport.mdns.ServiceDescription;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class starts the JmDNS and implements interface to register and unregister services.
 *
 * @author Victor Belov - Initial contribution
 * @author Gary Tse - Add NetworkAddressChangeListener to handle interface changes
 *
 */
@Component(immediate = true, configurationPid = "org.eclipse.smarthome.TransportMDNS", property = {
        "service.pid=org.eclipse.smarthome.TransportMDNS", "service.config.description.uri=system:transport-mdns",
        "service.config.label=Transport MDNS Settings", "service.config.category=system" })
public class MDNSClientImpl implements MDNSClient, NetworkAddressChangeListener {
    private final Logger logger = LoggerFactory.getLogger(MDNSClientImpl.class);

    private static final String CONFIG_USER_ONLY_ONE_ADDRESS = "useOnlyOneAddress";
    private static final String CONFIG_IGNORE_IPV6 = "ignoreIPv6";

    private final ConcurrentMap<InetAddress, JmDNS> jmdnsInstances = new ConcurrentHashMap<>();

    private final ConcurrentSkipListSet<ServiceDescription> activeServices = new ConcurrentSkipListSet<>();

    private boolean useOnlyOneAddress;
    private boolean ignoreIPv6;

    private Set<InetAddress> getAllInetAddresses() {
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
                if (!iface.isUp() || iface.isLoopback() || iface.isPointToPoint()) {
                    continue;
                }
            } catch (final SocketException ex) {
                continue;
            }
            final Enumeration<InetAddress> itAddresses = iface.getInetAddresses();
            boolean ipv4addressAdded = false;
            boolean ipv6addressAdded = false;
            while (itAddresses.hasMoreElements()) {
                final InetAddress address = itAddresses.nextElement();
                if (address.isLoopbackAddress() || address.isLinkLocalAddress()
                        || (ignoreIPv6 && address instanceof Inet6Address)) {
                    continue;
                }
                if (useOnlyOneAddress) {
                    // add only one address per interface and family
                    if (address instanceof Inet4Address) {
                        if (!ipv4addressAdded) {
                            addresses.add(address);
                            ipv4addressAdded = true;
                        }
                    } else if (address instanceof Inet6Address) {
                        if (!ipv6addressAdded) {
                            addresses.add(address);
                            ipv6addressAdded = true;
                        }
                    }
                } else {
                    addresses.add(address);
                }
            }
        }
        return addresses;
    }

    @Override
    public Set<JmDNS> getClientInstances() {
        return new HashSet<>(jmdnsInstances.values());
    }

    @Activate
    protected void activate(Map<String, Object> parameters) {
        getConfigParameters(parameters);
        start();
    }

    private void start() {
        for (InetAddress address : getAllInetAddresses()) {
            createJmDNSByAddress(address);
        }
        for (ServiceDescription description : activeServices) {
            try {
                registerService(description);
            } catch (IOException e) {
                // ignore so far
            }
        }
    }

    @Modified
    protected void modified(Map<String, Object> parameters) {
        deactivate();
        activate(parameters);
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
        activeServices.add(description);
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
        activeServices.remove(description);
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
        activeServices.clear();
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
        deactivate();
        start();
    }

    private void getConfigParameters(Map<String, Object> parameters) {
        useOnlyOneAddress = getConfigParameter(parameters, CONFIG_USER_ONLY_ONE_ADDRESS, false);
        ignoreIPv6 = getConfigParameter(parameters, CONFIG_IGNORE_IPV6, false);
    }

    private boolean getConfigParameter(Map<String, Object> parameters, String parameter, boolean defaultValue) {
        if (parameters == null) {
            return defaultValue;
        }
        Object value = parameters.get(parameter);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.valueOf((String) value);
        } else {
            logger.warn("ignoring invalid type {} for parameter {}, using default value {} instead",
                    value.getClass().getName(), parameter, defaultValue);
            return defaultValue;
        }
    }
}
