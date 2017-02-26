/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth;

import java.util.UUID;

/**
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public interface BluetoothProfile {
    // Profile connection states
    public static final int STATE_CONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_DISCONNECTED = 2;
    public static final int STATE_DISCONNECTING = 3;

    // Bluetooth profile UUID definitions
    public final static UUID PROFILE_GATT = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_A2DP_SOURCE = UUID.fromString("0000110a-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_A2DP_SINK = UUID.fromString("0000110b-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_A2DP = UUID.fromString("0000110d-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_AVRCP_REMOTE = UUID.fromString("0000110c-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_CORDLESS_TELEPHONE = UUID.fromString("00001109-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_DID_PNPINFO = UUID.fromString("00001200-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_HEADSET = UUID.fromString("00001108-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_HFP = UUID.fromString("0000111e-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_HFP_AUDIOGATEWAY = UUID.fromString("0000111f-0000-1000-8000-00805f9b34fb");

    /**
     * Get the current connection state
     *
     * @param device
     * @return
     */
    abstract int getConnectionState(BluetoothDevice device);
}
