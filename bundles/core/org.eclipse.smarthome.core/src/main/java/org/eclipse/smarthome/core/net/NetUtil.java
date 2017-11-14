/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some utility functions related to network interfaces etc.
 *
 * @author Markus Rathgeb - Initial contribution and API
 * @author Mark Herwege - Added methods to find broadcast address(es)
 * @author Stefan Triller - Converted to OSGi service with primary ipv4 conf
 * @author David Graeff - Make IPv6 a valid choice as well
 */
@Component(configurationPid = "org.eclipse.smarthome.network", property = { "service.pid=org.eclipse.smarthome.network",
        "service.config.description.uri=system:network", "service.config.label=Network Settings",
        "service.config.category=system" })
@NonNullByDefault
public class NetUtil implements NetworkAddressService {

    private static final String PRIMARY_ADDRESS = "primaryAddress";
    private static final Logger LOGGER = LoggerFactory.getLogger(NetUtil.class);

    private @Nullable CidrAddress primaryAddress;

    @Activate
    protected void activate(Map<String, Object> props) {
        modified(props);
    }

    @Modified
    public synchronized void modified(Map<String, Object> config) {
        final @Nullable String primaryAddressConf = (String) config.get(PRIMARY_ADDRESS);
        if (primaryAddressConf != null) {
            try {
                primaryAddress = new CidrAddress(primaryAddressConf);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Primary address configuration value invalid", e);
            }
        }
    }

    /**
     * Deprecated: Please use the NetworkAddressService with getPrimaryIpv4HostAddress()
     *
     * Get the first candidate for a local IPv4 host address (non loopback, non localhost).
     */
    @Deprecated
    public static @Nullable String getLocalIpv4HostAddress() {
        return new NetUtil().getPrimaryHostAddress();
    }

    @Deprecated
    @Override
    public @Nullable String getPrimaryIpv4HostAddress() {
        return getPrimaryHostAddress();
    }

    /**
     * Returned an IP that is either the same as the one configured or an IP on the same
     * network interface in the same IP subnet range. This can be an IPv4 or IPv6.
     *
     * If nothing is configured so far, we just return the first assigned address.
     * Loopback addresses are not considered. If no network interface is online, we
     */
    @Override
    public @Nullable String getPrimaryHostAddress() {
        Collection<CidrAddress> addresses = getAllInterfaceAddresses();
        if (addresses.size() == 0) {
            return null;
        }
        final CidrAddress primary = primaryAddress;
        if (primary != null) {
            Optional<CidrAddress> first = addresses.stream().filter(a -> primary.isInRange(a)).findFirst();
            if (first.isPresent()) {
                return first.get().getAddress().getHostAddress();
            }
        }
        LOGGER.warn("Invalid address '{}', will use first interface instead.", primaryAddress);
        return addresses.iterator().next().getAddress().getHostAddress();
    }

    /**
     * Get all IPv4 broadcast addresses on the current host
     *
     * @return list of broadcast addresses, empty list if no broadcast addresses found
     */
    public static List<String> getAllBroadcastAddresses() {
        List<String> broadcastAddresses = new LinkedList<String>();
        try {
            final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                final NetworkInterface networkInterface = networkInterfaces.nextElement();
                final List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
                for (InterfaceAddress interfaceAddress : interfaceAddresses) {
                    final InetAddress addr = interfaceAddress.getAddress();
                    if (!addr.isLinkLocalAddress() && !addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        broadcastAddresses.add(interfaceAddress.getBroadcast().getHostAddress());
                    }
                }
            }
        } catch (SocketException ex) {
            LOGGER.error("Could not find broadcast address: {}", ex.getMessage(), ex);
        }
        return broadcastAddresses;
    }

    /**
     * Get the first candidate for a IPv4 broadcast address
     *
     * @return broadcast address, null of no broadcast address is found
     */
    public static @Nullable String getBroadcastAddress() {
        final List<String> broadcastAddresses = getAllBroadcastAddresses();
        if (!broadcastAddresses.isEmpty()) {
            return broadcastAddresses.get(0);
        } else {
            return null;
        }
    }

    /**
     * Gets every IPv4+IPv6 Address on each Interface except the loopback interface.
     * The Address format is in the CIDR notation which is ip/prefix-length e.g. 129.31.31.1/24.
     *
     * Example to get a list of only IPv4 addresses in string representation:
     * List<String> l = getAllInterfaceAddresses().stream().filter(a->a.getAddress() instanceof
     * Inet4Address).map(a->a.getAddress().getHostAddress()).collect(Collectors.toList());
     *
     * @return The collected IPv4 and IPv6 Addresses
     */
    public static Collection<CidrAddress> getAllInterfaceAddresses() {
        Collection<CidrAddress> interfaceIPs = new ArrayList<>();
        Enumeration<NetworkInterface> en;
        try {
            en = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            LOGGER.error("Could not find interface IP addresses: {}", ex.getMessage(), ex);
            return interfaceIPs;
        }

        while (en.hasMoreElements()) {
            NetworkInterface networkInterface = en.nextElement();

            try {
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
            } catch (SocketException ignored) {
                continue;
            }

            for (InterfaceAddress cidr : networkInterface.getInterfaceAddresses()) {
                final InetAddress address = cidr.getAddress();
                assert address != null; // NetworkInterface.getInterfaceAddresses() should return only non-null
                                        // addresses
                if (address.isLoopbackAddress()) {
                    continue;
                }
                interfaceIPs.add(new CidrAddress(address, cidr.getNetworkPrefixLength()));
            }
        }

        return interfaceIPs;
    }
}
