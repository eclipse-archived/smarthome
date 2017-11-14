/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.AbstractSensorJobExecutor;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.SceneReadingJobExecutor;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.SensorJobExecutor;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.DsAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DSID;

/**
 * The {@link SensorJob} represents an executable job to read out digitalSTROM-Sensors or device configurations like
 * scene values.<br>
 * It can be added to an implementation of the {@link AbstractSensorJobExecutor} e.g. {@link SceneReadingJobExecutor} or
 * {@link SensorJobExecutor}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface SensorJob {

    /**
     * Returns the dSID of the {@link Device} for which this job is to be created.
     *
     * @return dSID from the device
     */
    public DSID getDSID();

    /**
     * Returns the dSID of the digitalSTROM-Meter on which this job is to be created.
     *
     * @return dSID from the device meter
     */
    public DSID getMeterDSID();

    /**
     * Executes the SensorJob.
     *
     * @param dSAPI
     * @param sessionToken
     */
    public void execute(DsAPI dSAPI, String sessionToken);

    /**
     * Returns the time when the {@link SensorJob} was initialized.
     *
     * @return the initialization time
     */
    public long getInitalisationTime();

    /**
     * Sets the time when the {@link SensorJob} was initialized e.g. to manages the priority of this {@link SensorJob}.
     *
     * @param time to set
     */
    public void setInitalisationTime(long time);
}
