/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.discovery;

/**
 * The {@link BridgeJsonParameters} class defines JSON object, which
 * contains bridge attributes like IP address. It is used for bridge
 * N-UPNP Discovery.
 *
 * @author Awelkiyar Wehabrebi
 *
 */
public class BridgeJsonParameters {

    // hue bridge parameters
    private final String id;
    private final String internalipaddress;
    private final String macaddress;
    private final String name;

    public BridgeJsonParameters(String id, String internalipaddress, String macaddress, String name) {
        this.id = id;
        this.internalipaddress = internalipaddress;
        this.macaddress = macaddress;
        this.name = name;
    }

    public String getInternalipaddress() {
        return internalipaddress;
    }

    public String getId() {
        return id;
    }

    public String getMacaddress() {
        return macaddress;
    }

    public String getName() {
        return name;
    }

}
