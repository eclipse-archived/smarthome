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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SceneOutputValueReadingJob} is the implementation of a {@link SensorJob}
 * for reading out a scene configuration of a digitalSTROM-Device and store it into the {@link Device}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class SceneOutputValueReadingJob implements SensorJob {

    private static final Logger logger = LoggerFactory.getLogger(SceneOutputValueReadingJob.class);

    private Device device = null;
    private short sceneID = 0;
    private DSID meterDSID = null;
    private long initalisationTime = 0;

    /**
     * Creates a new {@link SceneOutputValueReadingJob} for the given {@link Device} and the given sceneID.
     *
     * @param device
     * @param sceneID
     */
    public SceneOutputValueReadingJob(Device device, short sceneID) {
        this.device = device;
        this.sceneID = sceneID;
        this.meterDSID = device.getMeterDSID();
        this.initalisationTime = System.currentTimeMillis();
    }

    @Override
    public void execute(DsAPI digitalSTROM, String token) {
        int[] sceneValue = digitalSTROM.getSceneValue(token, this.device.getDSID(), this.sceneID);

        if (sceneValue[0] != -1) {
            if (device.isBlind()) {
                device.setSceneOutputValue(this.sceneID, sceneValue[0], sceneValue[1]);
            } else {
                device.setSceneOutputValue(this.sceneID, sceneValue[0]);
            }
            logger.debug("UPDATED sceneOutputValue for dsid: {}, sceneID: {}, value: {}, angle: {}",
                    this.device.getDSID(), sceneID, sceneValue[0], sceneValue[1]);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SceneOutputValueReadingJob) {
            SceneOutputValueReadingJob other = (SceneOutputValueReadingJob) obj;
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
        return "SceneOutputValueReadingJob [sceneID: " + sceneID + ", deviceDSID : " + device.getDSID().getValue()
                + ", meterDSID=" + meterDSID + ", initalisationTime=" + initalisationTime + "]";
    }
}
