/**
* Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*/
package org.eclipse.smarthome.core.thing;

/**
 * The {@link DefaultPropertyKey} enumeration defines all keys for default properties of a {@link Thing}.
 * 
 * @author Thomas Höfer - initial contribution
 */
public enum DefaultPropertyKey {

    VENDOR("vendor"), 
    MODEL("model"), 
    SERIAL_NUMBER("serialNumber"), 
    HARDWARE_VERSION("hardwareVersion"), 
    FIRMWARE_VERSION("firmwareVersion");

    public final String name;

    private DefaultPropertyKey(String name) {
        this.name = name;
    }
}