/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.DeviceStatusManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.ChangeableDeviceConfigEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdate;

/**
 * The {@link DeviceStatusListener} is notified, if a {@link Device} status has changed, if a scene configuration is
 * added
 * to a {@link Device} or if a device has been added or removed.
 * <p>
 * By implementation with the id {@link #DEVICE_DISCOVERY} this listener can be used as a device discovery to get
 * informed, if a new {@link Device}s is added or removed from the digitalSTROM-System.<br>
 * For that the {@link DeviceStatusListener} has to be registered on the
 * {@link DeviceStatusManager#registerDeviceListener(DeviceStatusListener)}. Then the {@link DeviceStatusListener} gets
 * informed by the methods {@link #onDeviceAdded(Device)} and {@link #onDeviceRemoved(Device)}.
 * </p>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public interface DeviceStatusListener {

    /**
     * ID of the device discovery listener.
     */
    public final static String DEVICE_DISCOVERY = "DeviceDiscovery";

    /**
     * This method is called whenever the state of the {@link Device} has changed and passes the new device state as an
     * {@link DeviceStateUpdate} object.
     *
     * @param deviceStateUpdate
     */
    public void onDeviceStateChanged(DeviceStateUpdate deviceStateUpdate);

    /**
     * This method is called whenever a {@link Device} is removed.
     *
     * @param device
     */
    public void onDeviceRemoved(Device device);

    /**
     * This method is called whenever a {@link Device} is added.
     *
     * @param device
     */
    public void onDeviceAdded(Device device);

    /**
     * This method is called whenever a configuration of an {@link Device} has changed. What configuration has changed
     * can be see by the given parameter whatConfig to handle the change.<br>
     * Please have a look at {@link ChangeableDeviceConfigEnum} to see what configuration are changeable.
     *
     * @param whatConfig has changed
     */
    public void onDeviceConfigChanged(ChangeableDeviceConfigEnum whatConfig);

    /**
     * This method is called whenever a scene configuration is added to a device.
     *
     * @param sceneID
     */
    public void onSceneConfigAdded(short sceneID);

    /**
     * Returns the id of this {@link DeviceStatusListener}.
     *
     * @return the device listener id
     */
    public String getDeviceStatusListenerID();
}
