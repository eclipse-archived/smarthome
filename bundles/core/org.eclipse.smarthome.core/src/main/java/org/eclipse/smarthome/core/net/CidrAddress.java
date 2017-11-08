/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.net;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The CIDR (Class-less interdomain routing) notation is an IP address
 * and additionally ends with a slash followed by the network prefix length number.
 *
 * The toString() method will return a CIRDR representation, but the individual
 * address and prefix length can be accessed as well.
 *
 * Java has a class that exactly provides this {@link InterfaceAddress}, but unfortunately
 * no public constructor exists.
 *
 * @author David Graeff - Initial contribution
 */
public class CidrAddress {
    private final InetAddress address;
    private final int prefix;

    public CidrAddress(@NonNull InetAddress address, short networkPrefixLength) {
        this.address = address;
        this.prefix = networkPrefixLength;
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
    public boolean equals(Object o) {
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

}
