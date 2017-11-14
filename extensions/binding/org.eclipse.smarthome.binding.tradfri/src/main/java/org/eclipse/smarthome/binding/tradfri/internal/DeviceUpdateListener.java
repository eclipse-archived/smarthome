/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri.internal;

import org.eclipse.smarthome.binding.tradfri.handler.TradfriGatewayHandler;

import com.google.gson.JsonObject;

/**
 * {@link DeviceUpdateListener} can register on the {@link TradfriGatewayHandler} to be
 * informed about details about devices.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public interface DeviceUpdateListener {

    /**
     * This method is called when new device information is received.
     *
     * @param instanceId The instance id of the device
     * @param data the json data describing the device
     */
    public void onUpdate(String instanceId, JsonObject data);
}
