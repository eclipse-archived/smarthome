/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.binding.lifx.handler.LifxLightHandler.CurrentLightState;
import org.eclipse.smarthome.binding.lifx.internal.fields.MACAddress;
import org.eclipse.smarthome.binding.lifx.internal.listener.LifxResponsePacketListener;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetEchoRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetServiceRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxLightOnlineStateUpdater} sets the state of a light offline when it no longer responds to echo packets.
 *
 * @author Wouter Born - Extracted class from LifxLightHandler
 */
public class LifxLightOnlineStateUpdater implements LifxResponsePacketListener {

    private static final int ECHO_POLLING_INTERVAL = 15;
    private static final int MAXIMUM_POLLING_RETRIES = 3;

    private final Logger logger = LoggerFactory.getLogger(LifxLightOnlineStateUpdater.class);

    private final String macAsHex;
    private final ScheduledExecutorService scheduler;
    private final CurrentLightState currentLightState;
    private final LifxLightCommunicationHandler communicationHandler;

    private final ReentrantLock lock = new ReentrantLock();

    private ScheduledFuture<?> echoJob;
    private LocalDateTime lastSeen = LocalDateTime.MIN;
    private int unansweredEchoPackets;

    public LifxLightOnlineStateUpdater(MACAddress macAddress, ScheduledExecutorService scheduler,
            CurrentLightState currentLightState, LifxLightCommunicationHandler communicationHandler) {
        this.macAsHex = macAddress.getHex();
        this.scheduler = scheduler;
        this.currentLightState = currentLightState;
        this.communicationHandler = communicationHandler;
    }

    private Runnable echoRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                lock.lock();
                logger.trace("{} : Polling", macAsHex);
                if (currentLightState.isOnline()) {
                    if (Duration.between(lastSeen, LocalDateTime.now()).getSeconds() > ECHO_POLLING_INTERVAL) {
                        if (unansweredEchoPackets < MAXIMUM_POLLING_RETRIES) {
                            ByteBuffer payload = ByteBuffer.allocate(Long.SIZE / 8);
                            payload.putLong(System.currentTimeMillis());

                            GetEchoRequest request = new GetEchoRequest();
                            request.setResponseRequired(true);
                            request.setPayload(payload);

                            communicationHandler.sendPacket(request);
                            unansweredEchoPackets++;
                        } else {
                            currentLightState.setOfflineByCommunicationError();
                            unansweredEchoPackets = 0;
                        }
                    }
                } else {
                    // are we not configured? let's broadcast instead
                    logger.trace("{} : The light is not online, let's broadcast instead", macAsHex);
                    GetServiceRequest packet = new GetServiceRequest();
                    communicationHandler.broadcastPacket(packet);
                }
            } catch (Exception e) {
                logger.error("Error occurred while polling online state", e);
            } finally {
                lock.unlock();
            }
        }
    };

    public void start() {
        try {
            lock.lock();
            communicationHandler.addResponsePacketListener(this);
            if (echoJob == null || echoJob.isCancelled()) {
                echoJob = scheduler.scheduleWithFixedDelay(echoRunnable, 0, ECHO_POLLING_INTERVAL, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error("Error occurred while starting online state poller", e);
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        try {
            lock.lock();
            communicationHandler.removeResponsePacketListener(this);
            if (echoJob != null && !echoJob.isCancelled()) {
                echoJob.cancel(true);
                echoJob = null;
            }
        } catch (Exception e) {
            logger.error("Error occurred while stopping online state poller", e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void handleResponsePacket(Packet packet) {
        lastSeen = LocalDateTime.now();
        unansweredEchoPackets = 0;
    }

}
