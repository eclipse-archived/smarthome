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
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceSceneSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SceneConfigReadingJob} is the implementation of a {@link SensorJob}
 * for reading out a scene output value of a digitalSTROM-Device and store it into the {@link Device}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class SceneConfigReadingJob implements SensorJob {

    private static final Logger logger = LoggerFactory.getLogger(SceneOutputValueReadingJob.class);

    private Device device = null;
    private short sceneID = 0;
    private DSID meterDSID = null;
    private long initalisationTime = 0;

    /**
     * Creates a new {@link SceneConfigReadingJob} for the given {@link Device} and the given sceneID.
     *
     * @param device
     * @param sceneID
     */
    public SceneConfigReadingJob(Device device, short sceneID) {
        this.device = device;
        this.sceneID = sceneID;
        this.meterDSID = device.getMeterDSID();
        this.initalisationTime = System.currentTimeMillis();
    }

    @Override
    public void execute(DsAPI digitalSTROM, String token) {
        DeviceSceneSpec sceneConfig = digitalSTROM.getDeviceSceneMode(token, device.getDSID(), null, sceneID);

        if (sceneConfig != null) {
            device.addSceneConfig(sceneID, sceneConfig);
            logger.debug("UPDATED scene configuration for dSID: {}, sceneID: {}, configuration: {}",
                    this.device.getDSID(), sceneID, sceneConfig);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SceneConfigReadingJob) {
            SceneConfigReadingJob other = (SceneConfigReadingJob) obj;
            String str = other.device.getDSID().getValue() + "-" + other.sceneID;
            return (this.device.getDSID().getValue() + "-" + this.sceneID).equals(str);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new String(this.device.getDSID().getValue() + this.sceneID).hashCode();
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
        return "SceneConfigReadingJob [sceneID: " + sceneID + ", deviceDSID : " + device.getDSID().getValue()
                + ", meterDSID=" + meterDSID + ", initalisationTime=" + initalisationTime + "]";
    }
}
