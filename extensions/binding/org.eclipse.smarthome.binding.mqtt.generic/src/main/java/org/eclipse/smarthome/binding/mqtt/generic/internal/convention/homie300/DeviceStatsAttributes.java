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
package org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Homie 3.x Device statistic attributes
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class DeviceStatsAttributes {
    // The following attributes are part of the specification.
    // They serve limited use though and their location within the device tree is questionable.
    // We do not require those from the peer device, for now.

    // public int uptime = 0;
    // public float signal = 0;
    // public float cputemp = 0;
    // public float cpuload = 0;
    // public float battery = 0;
    // public int freeheap = 0;
    // public float supply = 0;

    // The interval time is like a heart-beat/keep-alive timer
    public int interval = 60; // In seconds
}
