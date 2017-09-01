/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt.reconnect;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of the {@link AbstractReconnectStrategy}. This
 * strategy tries to reconnect after 10 seconds and then every 60 seconds
 * after a broker connection has been lost.
 *
 * @author David Graeff - Initial contribution
 */
public class PeriodicReconnectStrategy extends AbstractReconnectStrategy {
    private final Logger logger = LoggerFactory.getLogger(PeriodicReconnectStrategy.class);
    private final int reconnectFrequency;
    private final int firstReconnectAfter;

    private ScheduledExecutorService scheduler = null;
    private ScheduledFuture<?> scheduledTask;

    /**
     * Use a default 60s reconnect frequency and try the first reconnect after 10s.
     */
    public PeriodicReconnectStrategy() {
        this(10000, 60000);
    }

    /**
     * Create a {@link PeriodicReconnectStrategy} with the given reconnect frequency and
     * first reconnect time parameters.
     *
     * @param reconnectFrequency This strategy tries to reconnect in this frequency in ms.
     * @param firstReconnectAfter After a connection is lost, the very first reconnect attempt will be performed after
     *            this time in ms.
     */
    public PeriodicReconnectStrategy(int reconnectFrequency, int firstReconnectAfter) {
        this.reconnectFrequency = reconnectFrequency;
        this.firstReconnectAfter = firstReconnectAfter;
    }

    @Override
    public synchronized void start() {
        if (scheduler == null) {
            scheduler = Executors.newScheduledThreadPool(1);
        }
    }

    @Override
    public synchronized void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }

        // If there is a scheduled task ensure it is canceled.
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
            scheduledTask = null;
        }
    }

    /**
     * Returns if the reconnect strategy has been started.
     *
     * @return true if started
     */
    public synchronized boolean isStarted() {
        return scheduler != null;
    }

    @Override
    public synchronized void lostConnection() {
        // Check if we are running (has been started and not stopped) state.
        if (scheduler == null) {
            return;
        }

        // If there is already a scheduled task, we continue only if it has been done (shouldn't be the case at all).
        if (scheduledTask != null && !scheduledTask.isDone()) {
            return;
        }

        logger.info("Try to restore connection to '{}' every {}ms", getBrokerConnection().getName(),
                getReconnectFrequency());

        scheduledTask = scheduler.scheduleWithFixedDelay(() -> {
            try {
                getBrokerConnection().start();
            } catch (MqttException | ConfigurationException e) {
                logger.warn("Broker connection couldn't be started", e);
            }
        }, getFirstReconnectAfter(), getReconnectFrequency(), TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void connectionEstablished() {
        // Stop the reconnect task if existing.
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
            scheduledTask = null;
        }
    }

    @Override
    public synchronized boolean isReconnecting() {
        return scheduledTask != null;
    }

    public int getReconnectFrequency() {
        return reconnectFrequency;
    }

    public int getFirstReconnectAfter() {
        return firstReconnectAfter;
    }
}
