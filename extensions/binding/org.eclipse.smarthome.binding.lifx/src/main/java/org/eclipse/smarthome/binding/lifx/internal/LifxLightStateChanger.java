/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import static org.eclipse.smarthome.binding.lifx.LifxBindingConstants.PACKET_INTERVAL;
import static org.eclipse.smarthome.binding.lifx.internal.LifxUtils.*;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.binding.lifx.LifxBindingConstants;
import org.eclipse.smarthome.binding.lifx.internal.fields.MACAddress;
import org.eclipse.smarthome.binding.lifx.internal.listener.LifxLightStateListener;
import org.eclipse.smarthome.binding.lifx.internal.listener.LifxResponsePacketListener;
import org.eclipse.smarthome.binding.lifx.internal.protocol.AcknowledgementResponse;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetLightPowerRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Packet;
import org.eclipse.smarthome.binding.lifx.internal.protocol.PowerState;
import org.eclipse.smarthome.binding.lifx.internal.protocol.SetColorRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.SetLightPowerRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.SetPowerRequest;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxLightStateChanger} listens to state changes of the {@code pendingLightState}. It sends packets to a
 * light so the change the actual light state to that of the {@code pendingLightState}. When the light does not
 * acknowledge a packet, it resends it (max 3 times).
 *
 * @author Wouter Born - Extracted class from LifxLightHandler, added logic for handling packet loss
 */
public class LifxLightStateChanger implements LifxLightStateListener, LifxResponsePacketListener {

    /**
     * Milliseconds before a packet is considered to be lost (unacknowledged).
     */
    private static final int PACKET_ACKNOWLEDGE_INTERVAL = 250;

    /**
     * The number of times a lost packet will be resent.
     */
    private static final int MAX_RETRIES = 3;

    private final Logger logger = LoggerFactory.getLogger(LifxLightStateChanger.class);

    private final String macAsHex;
    private final LifxLightState pendingLightState;
    private final LifxLightCommunicationHandler communicationHandler;
    private final long fadeTime;

    private final ReentrantLock lock = new ReentrantLock();

    private ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(LifxBindingConstants.THREADPOOL_NAME);

    private ScheduledFuture<?> sendJob;

    private Map<Integer, PendingPacket> pendingPacketMap = new ConcurrentHashMap<Integer, PendingPacket>();

    private class PendingPacket {

        long lastSend;
        int sendCount;
        final Packet packet;

        private PendingPacket(Packet packet) {
            this.packet = packet;
        }
    }

    private Runnable sendRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                lock.lock();

                removeFailedPackets();
                PendingPacket pendingPacket = findPacketToSend();

                if (pendingPacket != null) {
                    Packet packet = pendingPacket.packet;

                    if (pendingPacket.sendCount == 0) {
                        // sendPacket will set the sequence number
                        logger.debug("{} : Sending {} packet", macAsHex, packet.getClass().getSimpleName());
                        communicationHandler.sendPacket(packet);
                    } else {
                        // resendPacket will reuse the sequence number
                        logger.debug("{} : Resending {} packet", macAsHex, packet.getClass().getSimpleName());
                        communicationHandler.resendPacket(packet);
                    }
                    pendingPacket.lastSend = System.currentTimeMillis();
                    pendingPacket.sendCount++;
                }

            } catch (Exception e) {
                logger.error("Error occured while sending packet", e);
            } finally {
                lock.unlock();
            }
        }
    };

    public LifxLightStateChanger(MACAddress macAddress, LifxLightState pendingLightState,
            LifxLightCommunicationHandler communicationHandler, long fadeTime) {
        this.macAsHex = macAddress.getHex();
        this.pendingLightState = pendingLightState;
        this.communicationHandler = communicationHandler;
        this.fadeTime = fadeTime;
    }

    public void start() {
        try {
            lock.lock();
            communicationHandler.addResponsePacketListener(this);
            pendingLightState.addListener(this);
            if (sendJob == null || sendJob.isCancelled()) {
                sendJob = scheduler.scheduleWithFixedDelay(sendRunnable, 0, PACKET_INTERVAL, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            logger.error("Error occured while starting send packets job", e);
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        try {
            lock.lock();
            communicationHandler.removeResponsePacketListener(this);
            pendingLightState.removeListener(this);
            if (sendJob != null && !sendJob.isCancelled()) {
                sendJob.cancel(true);
                sendJob = null;
            }
            pendingPacketMap.clear();
        } catch (Exception e) {
            logger.error("Error occured while stopping send packets job", e);
        } finally {
            lock.unlock();
        }
    }

    private void addPacketToMap(Packet packet) {
        // the acknowledgement is used to resend the packet in case of packet loss
        packet.setAckRequired(true);
        // the LIFX LAN protocol spec indicates that the response returned for a request would be the
        // previous value
        packet.setResponseRequired(false);

        try {
            lock.lock();
            pendingPacketMap.put(packet.packetType(), new PendingPacket(packet));
        } finally {
            lock.unlock();
        }
    }

    private PendingPacket findPacketToSend() {
        PendingPacket result = null;
        for (PendingPacket pendingPacket : pendingPacketMap.values()) {
            long millisSinceLastSend = System.currentTimeMillis() - pendingPacket.lastSend;
            if (millisSinceLastSend > PACKET_ACKNOWLEDGE_INTERVAL
                    && (result == null || pendingPacket.lastSend < result.lastSend)) {
                result = pendingPacket;
            }
        }

        return result;
    }

    private void removeFailedPackets() {
        Iterator<Integer> it = pendingPacketMap.keySet().iterator();
        while (it.hasNext()) {
            PendingPacket pendingPacket = pendingPacketMap.get(it.next());
            if (pendingPacket.sendCount > MAX_RETRIES) {
                logger.warn("{} failed (unacknowledged {} times)", pendingPacket.packet.getClass().getSimpleName(),
                        pendingPacket.sendCount);
                it.remove();
            }
        }
    }

    private PendingPacket removeAcknowledgedPacket(int sequenceNumber) {
        Iterator<Integer> it = pendingPacketMap.keySet().iterator();
        while (it.hasNext()) {
            PendingPacket pendingPacket = pendingPacketMap.get(it.next());
            if (pendingPacket.packet.getSequence() == sequenceNumber) {
                it.remove();
                return pendingPacket;
            }
        }
        return null;
    }

    @Override
    public void handleHSBChange(HSBType oldHSB, HSBType newHSB) {
        addSetColorRequestToMap();
    }

    @Override
    public void handlePowerStateChange(PowerState oldPowerState, PowerState newPowerState) {
        SetLightPowerRequest packet = new SetLightPowerRequest(pendingLightState.getPowerState());
        addPacketToMap(packet);
    }

    @Override
    public void handleTemperatureChange(PercentType oldTemperature, PercentType newTemperature) {
        addSetColorRequestToMap();
    }

    private void addSetColorRequestToMap() {
        HSBType hsb = pendingLightState.getHSB();
        if (hsb == null) {
            // use default color when temperature is changed while color is unknown
            hsb = LifxBindingConstants.DEFAULT_COLOR;
        }

        PercentType temperature = pendingLightState.getTemperature();
        if (temperature == null) {
            // use default temperature when color is changed while temperature is unknown
            temperature = LifxBindingConstants.DEFAULT_TEMPERATURE;
        }

        int hue = decimalTypeToHue(hsb.getHue());
        int saturation = percentTypeToSaturation(hsb.getSaturation());
        int brightness = percentTypeToBrightness(hsb.getBrightness());
        int kelvin = percentTypeToKelvin(temperature);

        SetColorRequest packet = new SetColorRequest(hue, saturation, brightness, kelvin, fadeTime);
        addPacketToMap(packet);
    }

    @Override
    public void handleResponsePacket(Packet packet) {
        if (packet instanceof AcknowledgementResponse) {
            long ackTimestamp = System.currentTimeMillis();

            PendingPacket pendingPacket;

            try {
                lock.lock();
                pendingPacket = removeAcknowledgedPacket(packet.getSequence());
            } finally {
                lock.unlock();
            }

            if (pendingPacket != null) {
                logger.debug("{} : {} packet was acknowledged in {}ms", macAsHex,
                        pendingPacket.packet.getClass().getSimpleName(), ackTimestamp - pendingPacket.lastSend);

                // when these packets get lost the current state will still be updated by the
                // LifxLightCurrentStateUpdater
                if (pendingPacket.packet instanceof SetPowerRequest) {
                    GetLightPowerRequest powerPacket = new GetLightPowerRequest();
                    communicationHandler.sendPacket(powerPacket);
                } else if (pendingPacket.packet instanceof SetColorRequest) {
                    GetRequest colorPacket = new GetRequest();
                    communicationHandler.sendPacket(colorPacket);
                }
            } else {
                logger.debug("{} : No pending packet found for ack with sequence number: {}", macAsHex,
                        packet.getSequence());
            }
        }
    }

}
