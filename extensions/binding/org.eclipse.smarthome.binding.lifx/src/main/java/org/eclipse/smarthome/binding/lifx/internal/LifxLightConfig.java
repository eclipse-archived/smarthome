/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import java.net.InetSocketAddress;
import java.time.Duration;

import org.eclipse.smarthome.binding.lifx.LifxBindingConstants;
import org.eclipse.smarthome.binding.lifx.internal.fields.MACAddress;

/**
 * Configuration class for LIFX lights.
 *
 * @author Wouter Born - Initial contribution
 */
public class LifxLightConfig {

    private String deviceId;
    private String host;
    private long fadetime = 300; // milliseconds

    public MACAddress getMACAddress() {
        return deviceId == null ? null : new MACAddress(deviceId, true);
    }

    public InetSocketAddress getHost() {
        return host == null ? null : new InetSocketAddress(host, LifxBindingConstants.UNICAST_PORT);
    }

    public Duration getFadeTime() {
        return Duration.ofMillis(fadetime);
    }

}
