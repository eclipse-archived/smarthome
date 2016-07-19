/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.config.Config;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.SensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.impl.DeviceConsumptionSensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.impl.DeviceOutputValueSensorJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SensorJobExecutor} is the implementation of the {@link AbstractSensorJobExecutor} to execute
 * digitalSTROM-Device {@link SensorJob}'s e.g. {@link DeviceConsumptionSensorJob} and
 * {@link DeviceOutputValueSensorJob}.
 * <p>
 * In addition priorities can be assigned to jobs, but the following list shows the maximum evaluation of a
 * {@link SensorJob} per priority.
 * <ul>
 * <li>low priority: read cycles before execution is set in {@link Config.LOW_PRIORITY_FACTOR}</li>
 * <li>medium priority: read cycles before execution is set in {@link Config.MEDIUM_PRIORITY_FACTOR}</li>
 * <li>high priority: read cycles before execution 0</li>
 * </ul>
 * </p>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class SensorJobExecutor extends AbstractSensorJobExecutor {

    private Logger logger = LoggerFactory.getLogger(SensorJobExecutor.class);

    private long mediumFactor = super.config.getSensorReadingWaitTime() * super.config.getMediumPriorityFactor();
    private long lowFactor = super.config.getSensorReadingWaitTime() * super.config.getLowPriorityFactor();

    public SensorJobExecutor(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    public void addHighPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null) {
            return;
        }
        addSensorJobToCircuitScheduler(sensorJob);
        logger.debug("Add SensorJob from device with dSID {} and high-priority to SensorJobExecutor",
                sensorJob.getDSID());
    }

    @Override
    public void addMediumPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null) {
            return;
        }
        sensorJob.setInitalisationTime(sensorJob.getInitalisationTime() + this.mediumFactor);
        addSensorJobToCircuitScheduler(sensorJob);
        logger.debug("Add SensorJob from device with dSID {} and medium-priority to SensorJobExecutor",
                sensorJob.getDSID());
    }

    @Override
    public void addLowPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null) {
            return;
        }
        sensorJob.setInitalisationTime(sensorJob.getInitalisationTime() + this.lowFactor);
        addSensorJobToCircuitScheduler(sensorJob);
        logger.debug("Add SensorJob from device with dSID {} and low-priority to SensorJobExecutor",
                sensorJob.getDSID());
    }
}
