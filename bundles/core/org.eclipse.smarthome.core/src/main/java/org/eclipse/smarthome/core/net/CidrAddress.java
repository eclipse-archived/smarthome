/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.net;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The CIDR (Class-less interdomain routing) notation is an IP address
 * and additionally ends with a slash followed by the network prefix length number.
 *
 * The toString() method will return a CIDR representation, but the individual
 * address and prefix length can be accessed as well.
 *
 * Java has a class that exactly provides this {@link InterfaceAddress}, but unfortunately
 * no public constructor exists.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class CidrAddress {
    private final InetAddress address;
    private final int prefix;

    @SuppressWarnings("null")
    private static final Pattern IPV4PATTERN = Pattern
            .compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    @SuppressWarnings("null")
    private static final Pattern IPV6PATTERN = Pattern
            .compile("^(([0-9a-fA-F]{1,4}:){7}([0-9a-fA-F]){1,4}(%[\\S]*)?)$");

    /**
     * Creates a CIDR notation address by providing an IP and a prefix length.
     *
     * @param address An IP address
     * @param networkPrefixLength A prefix length. The length is checked to be valid for the given IP
     */
    public CidrAddress(InetAddress address, int networkPrefixLength) {
        this.address = address;
        checkPrefix(networkPrefixLength);
        this.prefix = networkPrefixLength;
    }

    /**
     * Creates a CIDR address instance by providing a CIDR notation string
     *
     * @param cidrNotation A CIDR notation string like 192.168.0.1/24 or 2001:db8:0:0:8:800:200c:417a/33.
     *            The prefix part is optional. IPv6 addresses can have an optional interface index, separated
     *            by %. For instance 2001:DB8:0:0:8:800:200C:417A%eth0.
     * @throws IllegalArgumentException
     */
    public CidrAddress(String cidrNotation) throws IllegalArgumentException {
        if (StringUtils.isBlank(cidrNotation)) {
            throw new IllegalArgumentException("cidrNotation cannot be null or empty");
        }
        String[] parts = cidrNotation.split("/");
        @SuppressWarnings("null")
        final @NonNull String IP = parts[0];

        if (!isIpAddress(IP)) {
            throw new IllegalArgumentException("IP address is not valid");
        }
        try {
            address = InetAddress.getByName(IP);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("IP address is not valid");
        }

        if (parts.length == 2) {
            prefix = Integer.valueOf(parts[1]);
            checkPrefix(prefix);
        } else if (parts.length == 1) {
            prefix = 0;
        } else {
            throw new IllegalArgumentException("Not a valid CIDR notation");
        }
    }

    /**
     * Return true if the given string is a valid IPv4 or IPv6 address.
     *
     * Be aware that only full IP addresses (4 segments for IPv4 and 6 segments for IPv6)
     * are considered as correct input.
     * For instance "::" although a valid IPv6 address per specification, will
     * not be recognised as valid by this method.
     *
     * @param ipAddress An address like 129.12.3.1 or "2001:DB8:0:0:8:800:200C:417A%eth0"
     * @return
     */
    public static boolean isIpAddress(String ipAddress) {
        Matcher m1 = IPV4PATTERN.matcher(ipAddress);
        if (m1.matches()) {
            return true;
        }
        Matcher m2 = IPV6PATTERN.matcher(ipAddress);
        return m2.matches();
    }

    private void checkPrefix(int prefix) throws IllegalArgumentException {
        if (prefix < 0 || prefix > address.getAddress().length * 8) {
            throw new IllegalArgumentException("Prefix not in range of the given IP");
        }
    }

    @Override
    public String toString() {
        if (prefix == 0) {
            return address.getHostAddress();
        } else {
            return address.getHostAddress() + "/" + String.valueOf(prefix);
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof CidrAddress)) {
            return false;
        }
        CidrAddress c = (CidrAddress) o;
        return c.getAddress().equals(getAddress()) && c.getPrefix() == getPrefix();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress().hashCode(), getPrefix());
    }

    public int getPrefix() {
        return prefix;
    }

    public InetAddress getAddress() {
        return address;
    }

    private byte[] and(byte dest[], byte mask[]) {
        for (int i = 0; i < dest.length; ++i) {
            dest[i] &= mask[i];
        }
        return dest;
    }

    private byte[] mask(byte dest[], int prefix) {
        if (prefix == 0) {
            prefix = dest.length * 8;
        }
        for (int i = 0; i < dest.length; ++i) {
            if (prefix >= 8) {
                dest[i] = (byte) 0xff;
            } else if (prefix > 0) {
                dest[i] = (byte) (0xff << prefix);
            } else if (prefix < 0) {
                dest[i] = 0;
            }
            prefix -= 8;
        }
        return dest;
    }

    /**
     * Return true if this IP address with the given network prefix
     * is in the same subnet as given by the interfaceAddress parameter.
     *
     * @param interfaceAddress An IP address/Prefix length, given in CIDR notation
     * @return Return true if in range. Will definitely return false if the IPs are of different versions
     */
    public boolean isInRange(CidrAddress interfaceAddress) {
        if (interfaceAddress.getAddress() instanceof Inet6Address && !(address instanceof Inet6Address)) {
            return false;
        }

        final int iipPrefix = interfaceAddress.prefix;
        final InetAddress iip = interfaceAddress.getAddress();
        byte maskedIP[] = and(address.getAddress(), mask(new byte[address.getAddress().length], prefix));
        byte maskedIIP[] = and(iip.getAddress(), mask(new byte[address.getAddress().length], iipPrefix));
        return Arrays.equals(maskedIP, maskedIIP);
    }
}
