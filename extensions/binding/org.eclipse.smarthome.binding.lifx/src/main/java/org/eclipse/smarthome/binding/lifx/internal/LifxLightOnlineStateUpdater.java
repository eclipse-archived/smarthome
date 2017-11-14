/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.binding.lifx.handler.LifxLightHandler.CurrentLightState;
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
public class LifxLightOnlineStateUpdater {

    private static final int ECHO_POLLING_INTERVAL = 15;
    private static final int MAXIMUM_POLLING_RETRIES = 3;

    private final Logger logger = LoggerFactory.getLogger(LifxLightOnlineStateUpdater.class);

    private final String logId;
    private final CurrentLightState currentLightState;
    private final ScheduledExecutorService scheduler;
    private final LifxLightCommunicationHandler communicationHandler;

    private final ReentrantLock lock = new ReentrantLock();

    private ScheduledFuture<?> echoJob;
    private LocalDateTime lastSeen = LocalDateTime.MIN;
    private int unansweredEchoPackets;

    public LifxLightOnlineStateUpdater(LifxLightContext context, LifxLightCommunicationHandler communicationHandler) {
        this.logId = context.getLogId();
        this.scheduler = context.getScheduler();
        this.currentLightState = context.getCurrentLightState();
        this.communicationHandler = communicationHandler;
    }

    public void sendEchoPackets() {
        try {
            lock.lock();
            logger.trace("{} : Polling light state", logId);
            if (currentLightState.isOnline()) {
                if (Duration.between(lastSeen, LocalDateTime.now()).getSeconds() > ECHO_POLLING_INTERVAL) {
                    if (unansweredEchoPackets < MAXIMUM_POLLING_RETRIES) {
                        communicationHandler.sendPacket(GetEchoRequest.currentTimeEchoRequest());
                        unansweredEchoPackets++;
                    } else {
                        currentLightState.setOfflineByCommunicationError();
                        unansweredEchoPackets = 0;
                    }
                }
            } else {
                if (communicationHandler.isBroadcastEnabled()) {
                    logger.trace("{} : Light is not online, broadcasting request", logId);
                    communicationHandler.broadcastPacket(new GetServiceRequest());
                } else {
                    logger.trace("{} : Light is not online, unicasting request", logId);
                    communicationHandler.sendPacket(new GetServiceRequest());
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while polling the online state of a light ({})", logId, e);
        } finally {
            lock.unlock();
        }
    }

    public void start() {
        try {
            lock.lock();
            communicationHandler.addResponsePacketListener(this::handleResponsePacket);
            if (echoJob == null || echoJob.isCancelled()) {
                echoJob = scheduler.scheduleWithFixedDelay(this::sendEchoPackets, 0, ECHO_POLLING_INTERVAL,
                        TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error("Error occurred while starting online state poller for a light ({})", logId, e);
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        try {
            lock.lock();
            communicationHandler.removeResponsePacketListener(this::handleResponsePacket);
            if (echoJob != null && !echoJob.isCancelled()) {
                echoJob.cancel(true);
                echoJob = null;
            }
        } catch (Exception e) {
            logger.error("Error occurred while stopping online state poller for a light ({})", logId, e);
        } finally {
            lock.unlock();
        }
    }

    public void handleResponsePacket(Packet packet) {
        lastSeen = LocalDateTime.now();
        unansweredEchoPackets = 0;
        currentLightState.setOnline();
    }

}
