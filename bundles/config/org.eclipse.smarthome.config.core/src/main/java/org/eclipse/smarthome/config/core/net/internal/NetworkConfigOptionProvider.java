/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
