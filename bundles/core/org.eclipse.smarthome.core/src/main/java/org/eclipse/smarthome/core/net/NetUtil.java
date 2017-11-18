/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
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
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
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
 */
@Component(configurationPid = "org.eclipse.smarthome.network", property = { "service.pid=org.eclipse.smarthome.network",
        "service.config.description.uri=system:network", "service.config.label=Network Settings",
        "service.config.category=system" })
public class NetUtil implements NetworkAddressService {

    private static final String PRIMARY_ADDRESS = "primaryAddress";
    private static final Logger LOGGER = LoggerFactory.getLogger(NetUtil.class);

    private static final Pattern IPV4_PATTERN = Pattern
            .compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private String primaryAddress;

    @Activate
    protected void activate(Map<String, Object> props) {
        modified(props);
    }

    @Modified
    public synchronized void modified(Map<String, Object> config) {
        String primaryAddressConf = (String) config.get(PRIMARY_ADDRESS);
        if (primaryAddressConf == null || primaryAddressConf.isEmpty() || !isValidIPConfig(primaryAddressConf)) {
            // if none is specified we return the default one for backward compatibility
            primaryAddress = getFirstLocalIPv4Address();
        } else {
            primaryAddress = primaryAddressConf;
        }
    }

    @Override
    public String getPrimaryIpv4HostAddress() {
        String primaryIP;

        String[] addrString = primaryAddress.split("/");
        if (addrString.length > 1) {
            String ip = getIPv4inSubnet(primaryAddress);
            if (ip == null) {
                // an error has occurred, using first interface like nothing has been configured
                LOGGER.warn("Invalid address '{}', will use first interface instead.", primaryAddress);
                primaryIP = getFirstLocalIPv4Address();
            } else {
                primaryIP = ip;
            }
        } else {
            primaryIP = addrString[0];
        }

        return primaryIP;
    }

    /**
     * Deprecated: Please use the NetworkAddressService with getPrimaryIpv4HostAddress()
     *
     * Get the first candidate for a local IPv4 host address (non loopback, non localhost).
     */
    @Deprecated
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
            LOGGER.error("Could not retrieve network interface: {}", ex.getMessage(), ex);
            return null;
        }
    }

    private String getFirstLocalIPv4Address() {
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
            LOGGER.error("Could not retrieve network interface: {}", ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Get all broadcast addresses on the current host
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
     * Get the first candidate for a broadcast address
     *
     * @return broadcast address, null of no broadcast address is found
     */
    public static String getBroadcastAddress() {
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
                interfaceIPs.add(new CidrAddress(address, cidr.getNetworkPrefixLength()));
            }
        }

        return interfaceIPs;
    }

    /**
     * Converts a netmask in bits into a string representation
     * i.e. 24 bits -> 255.255.255.0
     *
     * @param prefixLength bits of the netmask
     * @return string representation of netmask (i.e. 255.255.255.0)
     */
    public static @NonNull String networkPrefixLengthToNetmask(int prefixLength) {
        if (prefixLength > 31 || prefixLength < 1) {
            throw new IllegalArgumentException("Network prefix length is not within bounds");
        }

        int ipv4Netmask = 0xFFFFFFFF;
        ipv4Netmask <<= (32 - prefixLength);

        byte[] octets = new byte[] { (byte) (ipv4Netmask >>> 24), (byte) (ipv4Netmask >>> 16),
                (byte) (ipv4Netmask >>> 8), (byte) ipv4Netmask };

        String result = "";
        for (int i = 0; i < 4; i++) {
            result += octets[i] & 0xff;
            if (i < 3) {
                result += ".";
            }
        }
        return result;
    }

    /**
     * Get the network address a specific ip address is in
     *
     * @param ipAddressString ipv4 address of the device (i.e. 192.168.5.1)
     * @param netMask netmask in bits (i.e. 24)
     * @return network a device is in (i.e. 192.168.5.0)
     *
     * @throws IllegalArgumentException if parameters are wrong
     */
    public static @NonNull String getIpv4NetAddress(@NonNull String ipAddressString, short netMask) {

        String errorString = "IP '" + ipAddressString + "' is not a valid IPv4 address";
        if (!isValidIPConfig(ipAddressString)) {
            throw new IllegalArgumentException(errorString);
        }
        if (netMask < 1 || netMask > 31) {
            throw new IllegalArgumentException("Netmask '" + netMask + "' is out of bounds (1-31)");
        }

        String subnetMaskString = networkPrefixLengthToNetmask(netMask);

        String[] netMaskOctets = subnetMaskString.split("\\.");
        String[] ipv4AddressOctets = ipAddressString.split("\\.");
        String netAddress = "";
        try {
            for (int i = 0; i < 4; i++) {
                netAddress += Integer.parseInt(ipv4AddressOctets[i]) & Integer.parseInt(netMaskOctets[i]);
                if (i < 3) {
                    netAddress += ".";
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorString);
        }

        return netAddress;
    }

    private String getIPv4inSubnet(String subnet) {
        try {
            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                final NetworkInterface current = interfaces.nextElement();
                if (!current.isUp() || current.isLoopback() || current.isVirtual()) {
                    continue;
                }

                for (InterfaceAddress ifAddr : current.getInterfaceAddresses()) {
                    InetAddress addr = ifAddr.getAddress();

                    if (addr.isLoopbackAddress() || (addr instanceof Inet6Address)) {
                        continue;
                    }

                    String ipv4Address = addr.getHostAddress();
                    String subNetString = getIpv4NetAddress(ipv4Address, ifAddr.getNetworkPrefixLength()) + "/"
                            + String.valueOf(ifAddr.getNetworkPrefixLength());

                    // use first IP within this subnet
                    if (subNetString.equals(subnet)) {
                        return ipv4Address;
                    }
                }
            }
        } catch (SocketException ex) {
            LOGGER.error("Could not retrieve network interface: {}", ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * Checks if the given String is a valid IPv4 Address
     * or IPv4 address in CIDR notation
     *
     * @param ipAddress in format xxx.xxx.xxx.xxx or xxx.xxx.xxx.xxx/xx
     * @return true if it is a valid address
     */
    public static boolean isValidIPConfig(String ipAddress) {

        if (ipAddress.contains("/")) {
            String parts[] = ipAddress.split("/");
            boolean ipMatches = IPV4_PATTERN.matcher(parts[0]).matches();

            int netMask = Integer.parseInt(parts[1]);
            boolean netMaskMatches = false;
            if (netMask > 0 || netMask < 32) {
                netMaskMatches = true;
            }

            if (ipMatches && netMaskMatches) {
                return true;
            }
        } else {
            return IPV4_PATTERN.matcher(ipAddress).matches();
        }
        return false;
    }

}
