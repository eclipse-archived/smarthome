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
package org.eclipse.smarthome.binding.onewire.internal.device;

/**
 * The {@link OwSensorType} defines all known sensor types
 *
 * @author Jan N. Klug - Initial contribution
 */

public enum OwSensorType {
    DS18S20,
    DS18B20,
    DS2401,
    DS2405,
    DS2406,
    DS2408,
    DS2413,
    DS2431,
    DS2438,
    DS2450,
    MS_TH,
    MS_TH_S,
    MS_TV,
    AMS,
    AMS_S,
    BMS,
    BMS_S,
    UNKNOWN;
}
