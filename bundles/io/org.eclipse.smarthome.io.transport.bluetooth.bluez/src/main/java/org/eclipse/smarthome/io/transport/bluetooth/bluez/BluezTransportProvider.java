/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth.bluez;

import org.eclipse.smarthome.io.transport.bluetooth.BluetoothAdapter;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothTransportProvider;

/**
 * Implementation of BluetoothTransportProvider for BlueZ
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class BluezTransportProvider implements BluetoothTransportProvider {

    @Override
    public BluetoothAdapter getDefaultAdapter() {
        // TODO: This should search for the adapter using the interface manager
        return new BluezBluetoothAdapter("hci0");
    }

}
