/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth;

import org.eclipse.smarthome.io.transport.bluetooth.events.BluetoothEvent;

/**
 * This interface defines an event interface for all BluetoothEvents
 *
 * A listener can subscribe to events on an adaptor to receive notifications of certain events.
 * Each event is a specific class overridden from the {@link BluetoothEvent} base class.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public interface BluetoothEventListener {
    void handleBluetoothEvent(BluetoothEvent event);
}
