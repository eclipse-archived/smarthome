/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.config.Config;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.SensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.DsAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DSID;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractSensorJobExecutor} provides the working process to execute implementations of {@link SensorJobs}'s
 * in the time interval set at the {@link Config}.
 * <p>
 * The following methods can be overridden by subclasses to implement a execution priority:
 * <ul>
 * <li>{@link #addLowPriorityJob(SensorJob)}</li>
 * <li>{@link #addMediumPriorityJob(SensorJob)}</li>
 * <li>{@link #addHighPriorityJob(SensorJob)}</li>
 * </ul>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public abstract class AbstractSensorJobExecutor {

    private Logger logger = LoggerFactory.getLogger(AbstractSensorJobExecutor.class);

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(Config.THREADPOOL_NAME);
    private Map<DSID, ScheduledFuture<?>> pollingSchedulers = null;

    private DsAPI dSAPI;
    protected Config config;
    private ConnectionManager connectionManager;

    private List<CircuitScheduler> circuitSchedulerList = new LinkedList<CircuitScheduler>();

    private class ExecutorRunnable implements Runnable {
        private CircuitScheduler circuit;

        public ExecutorRunnable(CircuitScheduler circuit) {
            this.circuit = circuit;
        }

        @Override
        public void run() {
            SensorJob sensorJob = circuit.getNextSensorJob();
            if (sensorJob != null && connectionManager.checkConnection()) {
                sensorJob.execute(dSAPI, connectionManager.getSessionToken());
            }
            if (circuit.noMoreJobs()) {
                logger.debug("no more jobs... stop circuit schedduler with id = {}", circuit.getMeterDSID());
                pollingSchedulers.get(circuit.getMeterDSID()).cancel(true);
            }
        }
    };

    public AbstractSensorJobExecutor(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        config = connectionManager.getConfig();
        this.dSAPI = connectionManager.getDigitalSTROMAPI();
    }

    /**
     * Stops all circuit schedulers.
     */
    public synchronized void shutdown() {
        if (pollingSchedulers != null) {
            for (ScheduledFuture<?> scheduledExecutor : pollingSchedulers.values()) {
                scheduledExecutor.cancel(true);
            }
            pollingSchedulers = null;
            logger.debug("stop all circuit schedulers.");
        }
    }

    /**
     * Starts all circuit schedulers.
     */
    public synchronized void startExecutor() {
        logger.debug("start all circuit schedulers.");
        if (pollingSchedulers == null) {
            pollingSchedulers = new HashMap<DSID, ScheduledFuture<?>>();
        }
        if (circuitSchedulerList != null && !circuitSchedulerList.isEmpty()) {
            for (CircuitScheduler circuit : circuitSchedulerList) {
                startSchedduler(circuit);
            }
        }
    }

    private void startSchedduler(CircuitScheduler circuit) {
        if (pollingSchedulers != null) {
            if (pollingSchedulers.get(circuit.getMeterDSID()) == null
                    || pollingSchedulers.get(circuit.getMeterDSID()).isCancelled()) {
                pollingSchedulers.put(circuit.getMeterDSID(),
                        scheduler.scheduleWithFixedDelay(new ExecutorRunnable(circuit), circuit.getNextExecutionDelay(),
                                config.getSensorReadingWaitTime(), TimeUnit.MILLISECONDS));
            }
        }
    }

    /**
     * Adds a high priority {@link SensorJob}.
     *
     * @param sensorJob
     */
    protected void addHighPriorityJob(SensorJob sensorJob) {
        // TODO: can be Overridden to implement a priority
        addSensorJobToCircuitScheduler(sensorJob);
    }

    /**
     * Adds a medium priority {@link SensorJob}.
     *
     * @param sensorJob
     */
    protected void addMediumPriorityJob(SensorJob sensorJob) {
        // TODO: can be Overridden to implement a priority
        addSensorJobToCircuitScheduler(sensorJob);
    }

    /**
     * Adds a low priority {@link SensorJob}.
     *
     * @param sensorJob
     */
    protected void addLowPriorityJob(SensorJob sensorJob) {
        // TODO: can be Overridden to implement a priority
        addSensorJobToCircuitScheduler(sensorJob);
    }

    /**
     * Adds the given {@link SensorJob}.
     *
     * @param sensorJob
     */
    protected void addSensorJobToCircuitScheduler(SensorJob sensorJob) {
        synchronized (this.circuitSchedulerList) {
            CircuitScheduler circuit = getCircuitScheduler(sensorJob.getMeterDSID());
            if (circuit != null) {
                circuit.addSensorJob(sensorJob);
            } else {
                circuit = new CircuitScheduler(sensorJob, config);
                this.circuitSchedulerList.add(circuit);
            }
            startSchedduler(circuit);
        }
    }

    private CircuitScheduler getCircuitScheduler(DSID dsid) {
        for (CircuitScheduler circuit : this.circuitSchedulerList) {
            if (circuit.getMeterDSID().equals(dsid)) {
                return circuit;
            }
        }
        return null;
    }

    /**
     * Removes all SensorJobs of a specific {@link device}.
     *
     * @param device
     */
    public void removeSensorJobs(Device device) {
        if (device != null) {
            CircuitScheduler circuit = getCircuitScheduler(device.getMeterDSID());
            if (circuit != null) {
                circuit.removeSensorJob(device.getDSID());
            }
        }
    }
}
