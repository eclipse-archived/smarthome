/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.setup;

/**
 * Enum containing all possible flow context keys.
 * 
 * @author Oliver Libutzki
 * 
 */
public enum HueBridgeContextKey {

    IP("ipAddress"), USERNAME("userName"), BRIDGE_SERIAL_NUMBER(
            "bridgeSerialNumber");

    private String key;

    private HueBridgeContextKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
