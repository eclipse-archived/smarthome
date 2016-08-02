/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.SensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.impl.SceneConfigReadingJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.impl.SceneOutputValueReadingJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SceneReadingJobExecutor} is the implementation of the {@link AbstractSensorJobExecutor} to execute
 * digitalSTROM-Device scene configuration {@link SensorJob}'s e.g. {@link SceneConfigReadingJob} and
 * {@link SceneOutputValueReadingJob}.
 * <p>
 * In addition priorities can be assigned to jobs therefore the {@link SceneReadingJobExecutor} offers the methods
 * {@link #addHighPriorityJob()}, {@link #addLowPriorityJob()} and {@link #addLowPriorityJob()}.
 * </p>
 * <p>
 * <b>NOTE:</b><br>
 * In contrast to the {@link SensorJobExecutor} the {@link SceneReadingJobExecutor} will execute {@link SensorJob}'s
 * with high priority always before medium priority {@link SensorJob}s and so on.
 * </p>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class SceneReadingJobExecutor extends AbstractSensorJobExecutor {

    private Logger logger = LoggerFactory.getLogger(SceneReadingJobExecutor.class);

    public SceneReadingJobExecutor(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    public void addHighPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null) {
            return;
        }
        sensorJob.setInitalisationTime(0);
        addSensorJobToCircuitScheduler(sensorJob);
        logger.debug("Add SceneReadingJob from device with dSID {} and high-priority to SceneReadingSobExecutor",
                sensorJob.getDSID());

    }

    @Override
    public void addMediumPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null) {
            return;
        }
        sensorJob.setInitalisationTime(1);
        addSensorJobToCircuitScheduler(sensorJob);
        logger.debug("Add SceneReadingJob from device with dSID {} and medium-priority to SceneReadingJobExecutor",
                sensorJob.getDSID());
    }

    @Override
    public void addLowPriorityJob(SensorJob sensorJob) {
        if (sensorJob == null) {
            return;
        }
        sensorJob.setInitalisationTime(2);
        addSensorJobToCircuitScheduler(sensorJob);
        logger.debug("Add SceneReadingJob from device with dSID {} and low-priority to SceneReadingJobExecutor",
                sensorJob.getDSID());
    }

}
