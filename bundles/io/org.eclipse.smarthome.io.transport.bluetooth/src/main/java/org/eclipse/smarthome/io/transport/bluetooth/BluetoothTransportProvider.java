/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth;

/**
 * This interface defines the interface between bluetooth providers, and the bluetooth transport
 * registry. Providers register with the registry so that bindings can use them
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public interface BluetoothTransportProvider {
    /**
     * Returns the providers default bluetooth adapter.
     * Normally a provider, will only implement a single adapter
     *
     * @return {@link BluetoothAdapter}
     */
    BluetoothAdapter getDefaultAdapter();

}
