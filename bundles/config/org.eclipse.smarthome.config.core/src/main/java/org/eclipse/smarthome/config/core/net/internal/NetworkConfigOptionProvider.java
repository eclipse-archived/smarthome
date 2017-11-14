/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.net.internal;

import java.net.Inet4Address;
import java.net.URI;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.net.CidrAddress;
import org.eclipse.smarthome.core.net.NetUtil;
import org.osgi.service.component.annotations.Component;

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

    @Override
    public Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale) {
        if (!uri.equals(CONFIG_URI)) {
            return null;
        }

        if (param.equals(PARAM_PRIMARY_ADDRESS)) {
            Stream<CidrAddress> ipv4Addresses = NetUtil.getAllInterfaceAddresses().stream()
                    .filter(a -> a.getAddress() instanceof Inet4Address);
            return ipv4Addresses.map(a -> new ParameterOption(a.toString(), a.toString())).collect(Collectors.toList());
        }
        return null;
    }
}
