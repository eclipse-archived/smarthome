/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BleBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial contribution
 */
public class BleBindingConstants {

    public static final String BINDING_ID = "ble";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "sample");

    // List of all Channel ids
    public static final String BLE_CHANNEL_RSSI = "rssi";

    public static final String XMLPROPERTY_BLE_FILTER = "bleFilter";

    public static final String PROPERTY_TXPOWER = "ble_txpower";
    public static final String PROPERTY_MAXCONNECTIONS = "ble_maxconnections";

    public static final String CONFIGURATION_ADDRESS = "ble_address";

    public static final long bleUuid = 0x800000805f9b34fbL;

}
