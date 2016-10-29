/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import static org.eclipse.smarthome.binding.lifx.internal.LifxUtils.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.binding.lifx.LifxBindingConstants;
import org.eclipse.smarthome.binding.lifx.handler.LifxLightHandler.CurrentLightState;
import org.eclipse.smarthome.binding.lifx.internal.fields.MACAddress;
import org.eclipse.smarthome.binding.lifx.internal.listener.LifxResponsePacketListener;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetLightPowerRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Packet;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StatePowerResponse;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StateResponse;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxLightCurrentStateUpdater} sends packets to a light in order to update the {@code currentLightState} to
 * the actual light state.
 *
 * @author Wouter Born - Extracted class from LifxLightHandler
 */
public class LifxLightCurrentStateUpdater implements LifxResponsePacketListener {

    private static final int STATE_POLLING_INTERVAL = 3;

    private final Logger logger = LoggerFactory.getLogger(LifxLightCurrentStateUpdater.class);

    private final String macAsHex;
    private final CurrentLightState currentLightState;
    private final LifxLightCommunicationHandler communicationHandler;

    private final ReentrantLock lock = new ReentrantLock();

    private ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(LifxBindingConstants.THREADPOOL_NAME);

    private ScheduledFuture<?> statePollingJob;

    private Runnable statePollingRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                lock.lock();
                if (currentLightState.isOnline()) {
                    logger.trace("{} : Polling the state of the light", macAsHex);

                    GetLightPowerRequest powerPacket = new GetLightPowerRequest();
                    communicationHandler.sendPacket(powerPacket);

                    GetRequest colorPacket = new GetRequest();
                    communicationHandler.sendPacket(colorPacket);
                } else {
                    logger.trace("{} : The light is not online, there is no point polling it", macAsHex);
                }
            } catch (Exception e) {
                logger.error("Error occured while polling light state", e);
            } finally {
                lock.unlock();
            }
        }
    };

    public LifxLightCurrentStateUpdater(MACAddress macAddress, CurrentLightState currentLightState,
            LifxLightCommunicationHandler communicationHandler) {
        this.macAsHex = macAddress.getHex();
        this.currentLightState = currentLightState;
        this.communicationHandler = communicationHandler;
    }

    public void start() {
        try {
            lock.lock();
            communicationHandler.addResponsePacketListener(this);
            if (statePollingJob == null || statePollingJob.isCancelled()) {
                statePollingJob = scheduler.scheduleWithFixedDelay(statePollingRunnable, 0, STATE_POLLING_INTERVAL,
                        TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error("Error occured while starting light state updater", e);
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        try {
            lock.lock();
            communicationHandler.removeResponsePacketListener(this);
            if (statePollingJob != null && !statePollingJob.isCancelled()) {
                statePollingJob.cancel(true);
                statePollingJob = null;
            }
        } catch (Exception e) {
            logger.error("Error occured while stopping light state updater", e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void handleResponsePacket(Packet packet) {
        if (packet instanceof StateResponse) {
            handleLightStatus((StateResponse) packet);
        } else if (packet instanceof StatePowerResponse) {
            handlePowerStatus((StatePowerResponse) packet);
        }
    }

    public void handleLightStatus(StateResponse packet) {
        DecimalType hue = hueToDecimalType(packet.getHue());
        PercentType saturation = saturationToPercentType(packet.getSaturation());
        PercentType brightness = brightnessToPercentType(packet.getBrightness());
        PercentType temperature = kelvinToPercentType(packet.getKelvin());

        currentLightState.setHSB(new HSBType(hue, saturation, brightness));
        currentLightState.setTemperature(temperature);
        currentLightState.setOnline();
    }

    public void handlePowerStatus(StatePowerResponse packet) {
        currentLightState.setPowerState(packet.getState());
        currentLightState.setOnline();
    }

}
