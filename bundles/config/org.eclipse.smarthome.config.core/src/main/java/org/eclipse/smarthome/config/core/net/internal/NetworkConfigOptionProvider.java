/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.net.internal;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.net.NetUtil;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a list of IPv4 addresses of the local machine and shows the user which interface belongs to which IP address
 *
 * @author Stefan Triller - initial contribution
 *
 */
@Component
public class NetworkConfigOptionProvider implements ConfigOptionProvider {

    static final URI CONFIG_URI = URI.create("system:network");
    static final String PARAM_PRIMARY_ADDRESS = "primaryAddress";

    private final Logger logger = LoggerFactory.getLogger(NetworkConfigOptionProvider.class);

    @Override
    public Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale) {
        if (!uri.equals(CONFIG_URI)) {
            return null;
        }

        if (param.equals(PARAM_PRIMARY_ADDRESS)) {
            return getIPv4Addresses();
        }
        return null;
    }

    private List<ParameterOption> getIPv4Addresses() {
        List<ParameterOption> interfaceOptions = new ArrayList<>();

        Set<String> subnets = new HashSet<>();

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

                    @SuppressWarnings("null")
                    @NonNull
                    String ipv4Address = addr.getHostAddress();
                    try {
                        String subNetString = NetUtil.getIpv4NetAddress(ipv4Address, ifAddr.getNetworkPrefixLength())
                                + "/" + String.valueOf(ifAddr.getNetworkPrefixLength());
                        subnets.add(subNetString);
                    } catch (IllegalArgumentException ex) {
                        logger.error("Could not calculate network address: {} Ignoring IP {}", ex.getMessage(),
                                ipv4Address, ex);
                    }
                }
            }
        } catch (SocketException ex) {
            logger.error("Could not retrieve network interface: {}", ex.getMessage(), ex);
            return null;
        }

        for (String subnet : subnets) {
            ParameterOption po = new ParameterOption(subnet, subnet);
            interfaceOptions.add(po);
        }

        return interfaceOptions;
    }

}
