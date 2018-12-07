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
package org.eclipse.smarthome.binding.hue.internal.discovery.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link BridgeParameters} class defines JSON object, which
 * contains bridge attributes like IP address. It is used for bridge
 * N-UPNP Discovery.
 *
 * @author Awelkiyar Wehabrebi - Initial contribution and API
 * @author Christoph Knauf - Refactorings
 */
@NonNullByDefault
public class BridgeParameters {
    public final String id;
    @SerializedName("internalipaddress")
    public final String internalIpAddress;
    @SerializedName("macaddress")
    public final String macAddress;
    public final String name;

    public BridgeParameters(String id, String internalIpAddress, String macAdress, String name) {
        this.id = id;
        this.internalIpAddress = internalIpAddress;
        this.macAddress = macAdress;
        this.name = name;
    }
}
