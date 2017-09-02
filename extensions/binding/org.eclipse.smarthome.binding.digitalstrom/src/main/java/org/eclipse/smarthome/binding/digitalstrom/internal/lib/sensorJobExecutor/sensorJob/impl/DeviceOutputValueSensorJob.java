/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.impl;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.SensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.DsAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DSID;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdateImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DeviceOutputValueSensorJob} is the implementation of a {@link SensorJob}
 * for reading out the current device output value of a digitalSTROM-Device and update the {@link Device}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class DeviceOutputValueSensorJob implements SensorJob {

    private static final Logger logger = LoggerFactory.getLogger(DeviceOutputValueSensorJob.class);
    private Device device = null;
    private short index = 0;
    private DSID meterDSID = null;
    private long initalisationTime = 0;

    /**
     * Creates a new {@link DeviceOutputValueSensorJob} for the given {@link Device}.
     *
     * @param device
     */
    public DeviceOutputValueSensorJob(Device device) {
        this.device = device;
        if (device.isShade()) {
            this.index = DeviceConstants.DEVICE_SENSOR_SLAT_POSITION_OUTPUT;
        } else {
            this.index = DeviceConstants.DEVICE_SENSOR_OUTPUT;
        }
        this.meterDSID = device.getMeterDSID();
        this.initalisationTime = System.currentTimeMillis();
    }

    @Override
    public void execute(DsAPI digitalSTROM, String token) {
        int value = digitalSTROM.getDeviceOutputValue(token, this.device.getDSID(), null, index);
        logger.debug("Device output value on Demand : {}, dSID: {}", value, this.device.getDSID().getValue());

        if (value != 1) {
            switch (this.index) {
                case 0:
                    this.device.updateInternalDeviceState(
                            new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS, value));
                    return;
                case 2:
                    this.device.updateInternalDeviceState(
                            new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, value));
                    if (device.isBlind()) {
                        value = digitalSTROM.getDeviceOutputValue(token, this.device.getDSID(), null,
                                DeviceConstants.DEVICE_SENSOR_SLAT_ANGLE_OUTPUT);
                        logger.debug("Device angle output value on Demand : {}, dSID: {}", value,
                                this.device.getDSID().getValue());
                        if (value != 1) {
                            this.device.updateInternalDeviceState(
                                    new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE, value));
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeviceOutputValueSensorJob) {
            DeviceOutputValueSensorJob other = (DeviceOutputValueSensorJob) obj;
            String key = this.device.getDSID().getValue() + this.index;
            return key.equals((other.device.getDSID().getValue() + other.index));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new String(this.device.getDSID().getValue() + this.index).hashCode();
    }

    @Override
    public DSID getDSID() {
        return device.getDSID();
    }

    @Override
    public DSID getMeterDSID() {
        return this.meterDSID;
    }

    @Override
    public long getInitalisationTime() {
        return this.initalisationTime;
    }

    @Override
    public void setInitalisationTime(long time) {
        this.initalisationTime = time;
    }

    @Override
    public String toString() {
        return "DeviceOutputValueSensorJob [deviceDSID : " + device.getDSID().getValue() + ", meterDSID=" + meterDSID
                + ", initalisationTime=" + initalisationTime + "]";
    }
}
