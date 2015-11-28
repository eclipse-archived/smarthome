/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth;

import java.util.List;

/**
 * This class provides an OSGI interface for a Bluetooth Transport Provider.
 * It allows the transport provider to register with the system, and for bindings, or
 * other users of Bluetooth to create a default adapter.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class BluetoothManager {
    static BluetoothTransportProvider provider;

    public void setProvider(BluetoothTransportProvider provider) {
        BluetoothManager.provider = provider;
    }

    public void unsetProvider(BluetoothTransportProvider provider) {
        BluetoothManager.provider = null;
    }

    /**
     * Returns the providers default bluetooth adapter.
     * Normally a provider, will only implement a single adapter
     *
     * @return {@link BluetoothAdapter}
     */
    public static BluetoothAdapter getDefaultAdapter() {
        if (provider == null) {
            return null;
        }

        return provider.getDefaultAdapter();
    }

    /**
     * Get connected devices for the specified profile.
     *
     * @param profile
     * @return
     */
    List<BluetoothDevice> getConnectedDevices(int profile) {
        return null;
    }

    /**
     * Get the current connection state of the profile to the remote device.
     *
     * @param device
     * @param profile
     * @return
     */
    int getConnectionState(BluetoothDevice device, int profile) {
        return 0;
    }

}
