/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import static org.eclipse.smarthome.binding.lifx.LifxBindingConstants.MIN_ZONE_INDEX;
import static org.eclipse.smarthome.binding.lifx.internal.LifxUtils.infraredToPercentType;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.binding.lifx.handler.LifxLightHandler.CurrentLightState;
import org.eclipse.smarthome.binding.lifx.internal.fields.HSBK;
import org.eclipse.smarthome.binding.lifx.internal.fields.MACAddress;
import org.eclipse.smarthome.binding.lifx.internal.listener.LifxResponsePacketListener;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetColorZonesRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetLightInfraredRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetWifiInfoRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Packet;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Products;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StateLightInfraredResponse;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StateLightPowerResponse;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StateMultiZoneResponse;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StatePowerResponse;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StateResponse;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StateWifiInfoResponse;
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
    private final ScheduledExecutorService scheduler;
    private final CurrentLightState currentLightState;
    private final LifxLightCommunicationHandler communicationHandler;
    private final Products product;

    private final ReentrantLock lock = new ReentrantLock();

    private boolean wasOnline;
    private boolean updateSignalStrength;

    private ScheduledFuture<?> statePollingJob;

    private Runnable statePollingRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                lock.lock();
                if (currentLightState.isOnline()) {
                    logger.trace("{} : Polling the state of the light", macAsHex);
                    sendLightStateRequests();
                } else {
                    logger.trace("{} : The light is not online, there is no point polling it", macAsHex);
                }
                wasOnline = currentLightState.isOnline();
            } catch (Exception e) {
                logger.error("Error occurred while polling light state", e);
            } finally {
                lock.unlock();
            }
        }
    };

    public LifxLightCurrentStateUpdater(MACAddress macAddress, ScheduledExecutorService scheduler,
            CurrentLightState currentLightState, LifxLightCommunicationHandler communicationHandler, Products product) {
        this.macAsHex = macAddress.getHex();
        this.currentLightState = currentLightState;
        this.scheduler = scheduler;
        this.communicationHandler = communicationHandler;
        this.product = product;
    }

    public void setUpdateSignalStrength(boolean updateSignalStrength) {
        this.updateSignalStrength = updateSignalStrength;
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
            logger.error("Error occurred while starting light state updater", e);
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
            logger.error("Error occurred while stopping light state updater", e);
        } finally {
            lock.unlock();
        }
    }

    private void sendLightStateRequests() {
        GetRequest statePacket = new GetRequest();
        communicationHandler.sendPacket(statePacket);

        if (product.isInfrared()) {
            GetLightInfraredRequest infraredPacket = new GetLightInfraredRequest();
            communicationHandler.sendPacket(infraredPacket);
        }
        if (product.isMultiZone()) {
            GetColorZonesRequest colorZonesPacket = new GetColorZonesRequest();
            communicationHandler.sendPacket(colorZonesPacket);
        }
        if (updateSignalStrength) {
            GetWifiInfoRequest wifiInfoPacket = new GetWifiInfoRequest();
            communicationHandler.sendPacket(wifiInfoPacket);
        }
    }

    @Override
    public void handleResponsePacket(Packet packet) {
        try {
            lock.lock();

            if (packet instanceof StateResponse) {
                handleLightStatus((StateResponse) packet);
            } else if (packet instanceof StatePowerResponse) {
                handlePowerStatus((StatePowerResponse) packet);
            } else if (packet instanceof StateLightPowerResponse) {
                handleLightPowerStatus((StateLightPowerResponse) packet);
            } else if (packet instanceof StateLightInfraredResponse) {
                handleInfraredStatus((StateLightInfraredResponse) packet);
            } else if (packet instanceof StateMultiZoneResponse) {
                handleMultiZoneStatus((StateMultiZoneResponse) packet);
            } else if (packet instanceof StateWifiInfoResponse) {
                handleWifiInfoStatus((StateWifiInfoResponse) packet);
            }

            currentLightState.setOnline();

            if (currentLightState.isOnline() && !wasOnline) {
                wasOnline = true;
                logger.trace("{} : The light just went online, immediately polling the state of the light", macAsHex);
                sendLightStateRequests();
            }
        } finally {
            lock.unlock();
        }
    }

    private void handleLightStatus(StateResponse packet) {
        currentLightState.setColor(packet.getColor(), MIN_ZONE_INDEX);
        currentLightState.setPowerState(packet.getPower());
    }

    private void handlePowerStatus(StatePowerResponse packet) {
        currentLightState.setPowerState(packet.getState());
    }

    private void handleLightPowerStatus(StateLightPowerResponse packet) {
        currentLightState.setPowerState(packet.getState());
    }

    private void handleInfraredStatus(StateLightInfraredResponse packet) {
        PercentType infrared = infraredToPercentType(packet.getInfrared());
        currentLightState.setInfrared(infrared);
    }

    private void handleMultiZoneStatus(StateMultiZoneResponse packet) {
        HSBK[] colors = currentLightState.getColors();
        if (colors == null || colors.length != packet.getCount()) {
            colors = new HSBK[packet.getCount()];
        }
        for (int i = 0; i < packet.getColors().length && packet.getIndex() + i < colors.length; i++) {
            colors[packet.getIndex() + i] = packet.getColors()[i];
        }

        currentLightState.setColors(colors);
    }

    private void handleWifiInfoStatus(StateWifiInfoResponse packet) {
        currentLightState.setSignalStrength(packet.getSignalStrength());
    }

}
