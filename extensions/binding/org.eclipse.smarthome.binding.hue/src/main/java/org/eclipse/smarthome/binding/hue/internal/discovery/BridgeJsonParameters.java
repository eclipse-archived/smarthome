/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.discovery;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link BridgeJsonParameters} class defines JSON object, which
 * contains bridge attributes like IP address. It is used for bridge
 * N-UPNP Discovery.
 *
 * @author Awelkiyar Wehabrebi - Initial contribution and API
 * @author Christoph Knauf - Refactorings
 */
public class BridgeJsonParameters {

    private final String id;
    @SerializedName("internalipaddress")
    private final String internalIpAddress;
    @SerializedName("macaddress")
    private final String macAddress;
    private final String name;

    public BridgeJsonParameters(String id, String internalIpAddress, String macAdress, String name) {
        this.id = id;
        this.internalIpAddress = internalIpAddress;
        this.macAddress = macAdress;
        this.name = name;
    }

    public String getInternalIpAddress() {
        return internalIpAddress;
    }

    public String getId() {
        return id;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getName() {
        return name;
    }

}
