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

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeriodicReconnectPolicy extends AbstractReconnectPolicy {
    private final Logger logger = LoggerFactory.getLogger(PeriodicReconnectPolicy.class);
    private int reconnectFrequency = 60000;
    private int firstReconnectAfter = 10000;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledTask;

    @Override
    public void lostConnection() {
        logger.info("Starting connection helper to periodically try restore connection to broker '{}'",
                brokerConnection.getName());

        this.scheduledTask = scheduler.scheduleWithFixedDelay(() -> {
            try {
                brokerConnection.start();
            } catch (MqttException | ConfigurationException e) {
                logger.warn("Broker connection couldn't be started", e);
            }
        }, getFirstReconnectAfter(), getReconnectFrequency(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void connectionEstablished() {
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
            scheduledTask = null;
        }
    }

    public int getReconnectFrequency() {
        return reconnectFrequency;
    }

    public void setReconnectFrequency(int reconnectFrequency) {
        this.reconnectFrequency = reconnectFrequency;
    }

    public int getFirstReconnectAfter() {
        return firstReconnectAfter;
    }

    public void setFirstReconnectAfter(int firstReconnectAfter) {
        this.firstReconnectAfter = firstReconnectAfter;
    }
}
