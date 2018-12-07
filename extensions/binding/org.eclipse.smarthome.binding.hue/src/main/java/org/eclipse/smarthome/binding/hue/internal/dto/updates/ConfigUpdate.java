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
package org.eclipse.smarthome.binding.hue.internal.dto.updates;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.internal.utils.Util;

/**
 * Contains all fields that can be updated on the configuration rest endpoint.
 *
 * @author David Graeff - Initial contribution, rewritten
 */
@NonNullByDefault
public class ConfigUpdate {
    /**
     * Set the address of the proxy or null if there is no proxy.
     *
     * @param ip ip of proxy
     * @return this object for chaining calls
     */
    public ConfigUpdate setProxyAddress(@Nullable String ip) {
        if (ip != null && Util.stringSize(ip) > 40) {
            throw new IllegalArgumentException("Bridge proxy address can be at most 40 characters long");
        }
        proxyaddress = ip == null ? "none" : ip;
        return this;
    }

    public @Nullable Integer proxyport;
    public @Nullable String name;
    public @Nullable Boolean linkbutton;
    public @Nullable String proxyaddress;
    public @Nullable String ipaddress;
    public @Nullable String netmask;
    public @Nullable String gateway;
    public @Nullable Boolean dhcp;
}
