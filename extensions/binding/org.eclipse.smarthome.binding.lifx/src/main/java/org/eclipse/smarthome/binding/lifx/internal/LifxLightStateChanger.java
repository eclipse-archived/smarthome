/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import static org.eclipse.smarthome.binding.lifx.LifxBindingConstants.PACKET_INTERVAL;
import static org.eclipse.smarthome.binding.lifx.internal.LifxUtils.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.binding.lifx.internal.fields.HSBK;
import org.eclipse.smarthome.binding.lifx.internal.fields.MACAddress;
import org.eclipse.smarthome.binding.lifx.internal.listener.LifxLightStateListener;
import org.eclipse.smarthome.binding.lifx.internal.listener.LifxResponsePacketListener;
import org.eclipse.smarthome.binding.lifx.internal.protocol.AcknowledgementResponse;
import org.eclipse.smarthome.binding.lifx.internal.protocol.ApplicationRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetColorZonesRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetLightInfraredRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetLightPowerRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Packet;
import org.eclipse.smarthome.binding.lifx.internal.protocol.PowerState;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Products;
import org.eclipse.smarthome.binding.lifx.internal.protocol.SetColorRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.SetColorZonesRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.SetLightInfraredRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.SetLightPowerRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.SetPowerRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.SignalStrength;
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
    private final ScheduledExecutorService scheduler;
    private final LifxLightState pendingLightState;
    private final LifxLightCommunicationHandler communicationHandler;
    private final Duration fadeTime;
    private final Products product;

    private final ReentrantLock lock = new ReentrantLock();

    private ScheduledFuture<?> sendJob;

    private Map<Integer, List<PendingPacket>> pendingPacketsMap = new ConcurrentHashMap<>();

    private class PendingPacket {

        long lastSend;
        int sendCount;
        final Packet packet;

        private PendingPacket(Packet packet) {
            this.packet = packet;
        }

        private boolean hasAcknowledgeIntervalElapsed() {
            long millisSinceLastSend = System.currentTimeMillis() - lastSend;
            return millisSinceLastSend > PACKET_ACKNOWLEDGE_INTERVAL;
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
                logger.error("Error occurred while sending packet", e);
            } finally {
                lock.unlock();
            }
        }
    };

    public LifxLightStateChanger(MACAddress macAddress, ScheduledExecutorService scheduler,
            LifxLightState pendingLightState, LifxLightCommunicationHandler communicationHandler, Products product,
            Duration fadeTime) {
        this.macAsHex = macAddress.getHex();
        this.scheduler = scheduler;
        this.pendingLightState = pendingLightState;
        this.communicationHandler = communicationHandler;
        this.product = product;
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
            logger.error("Error occurred while starting send packets job", e);
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
            pendingPacketsMap.clear();
        } catch (Exception e) {
            logger.error("Error occurred while stopping send packets job", e);
        } finally {
            lock.unlock();
        }
    }

    private List<PendingPacket> createPendingPackets(Packet... packets) {
        Integer packetType = null;
        List<PendingPacket> pendingPackets = new ArrayList<>();

        for (Packet packet : packets) {
            // the acknowledgement is used to resend the packet in case of packet loss
            packet.setAckRequired(true);
            // the LIFX LAN protocol spec indicates that the response returned for a request would be the
            // previous value
            packet.setResponseRequired(false);
            pendingPackets.add(new PendingPacket(packet));

            if (packetType == null) {
                packetType = packet.getPacketType();
            } else if (packetType != packet.getPacketType()) {
                throw new RuntimeException("Packets should have same packet type");
            }
        }

        return pendingPackets;
    }

    private void addPacketsToMap(Packet... packets) {
        List<PendingPacket> pendingPackets = createPendingPackets(packets);
        int packetType = packets[0].getPacketType();

        try {
            lock.lock();
            if (pendingPacketsMap.get(packetType) == null) {
                pendingPacketsMap.put(packetType, pendingPackets);
            } else {
                pendingPacketsMap.get(packetType).addAll(pendingPackets);
            }
        } finally {
            lock.unlock();
        }
    }

    private void replacePacketsInMap(Packet... packets) {
        List<PendingPacket> pendingPackets = createPendingPackets(packets);
        int packetType = packets[0].getPacketType();

        try {
            lock.lock();
            pendingPacketsMap.put(packetType, pendingPackets);
        } finally {
            lock.unlock();
        }
    }

    private PendingPacket findPacketToSend() {
        PendingPacket result = null;
        for (List<PendingPacket> pendingPackets : pendingPacketsMap.values()) {
            for (PendingPacket pendingPacket : pendingPackets) {
                if (pendingPacket.hasAcknowledgeIntervalElapsed()
                        && (result == null || pendingPacket.lastSend < result.lastSend)) {
                    result = pendingPacket;
                }
            }
        }

        return result;
    }

    private void removePacketsByType(int packetType) {
        try {
            lock.lock();
            pendingPacketsMap.remove(packetType);
        } finally {
            lock.unlock();
        }
    }

    private void removeFailedPackets() {
        for (Integer key : pendingPacketsMap.keySet()) {
            List<PendingPacket> pendingPackets = pendingPacketsMap.get(key);
            Iterator<PendingPacket> it = pendingPackets.iterator();
            while (it.hasNext()) {
                PendingPacket pendingPacket = it.next();
                if (pendingPacket.sendCount > MAX_RETRIES && pendingPacket.hasAcknowledgeIntervalElapsed()) {
                    logger.warn("{} failed (unacknowledged {} times)", pendingPacket.packet.getClass().getSimpleName(),
                            pendingPacket.sendCount);
                    it.remove();
                }
            }
        }
    }

    private PendingPacket removeAcknowledgedPacket(int sequenceNumber) {
        for (Integer key : pendingPacketsMap.keySet()) {
            List<PendingPacket> pendingPackets = pendingPacketsMap.get(key);
            Iterator<PendingPacket> it = pendingPackets.iterator();
            while (it.hasNext()) {
                PendingPacket pendingPacket = it.next();
                if (pendingPacket.packet.getSequence() == sequenceNumber) {
                    it.remove();
                    return pendingPacket;
                }
            }
        }

        return null;
    }

    @Override
    public void handleColorsChange(HSBK[] oldColors, HSBK[] newColors) {
        if (sameColors(newColors)) {
            SetColorRequest packet = new SetColorRequest(pendingLightState.getColors()[0], fadeTime.toMillis());
            removePacketsByType(SetColorZonesRequest.TYPE);
            replacePacketsInMap(packet);
        } else {
            List<SetColorZonesRequest> packets = new ArrayList<>();
            for (int i = 0; i < newColors.length; i++) {
                if (newColors[i] != null && !newColors[i].equals(oldColors[i])) {
                    packets.add(
                            new SetColorZonesRequest(i, newColors[i], fadeTime.toMillis(), ApplicationRequest.APPLY));
                }
            }
            if (!packets.isEmpty()) {
                removePacketsByType(SetColorRequest.TYPE);
                addPacketsToMap(packets.toArray(new SetColorZonesRequest[packets.size()]));
            }
        }
    }

    @Override
    public void handlePowerStateChange(PowerState oldPowerState, PowerState newPowerState) {
        if (newPowerState != null && !newPowerState.equals(oldPowerState)) {
            SetLightPowerRequest packet = new SetLightPowerRequest(pendingLightState.getPowerState());
            replacePacketsInMap(packet);
        }
    }

    @Override
    public void handleInfraredChange(PercentType oldInfrared, PercentType newInfrared) {
        int infrared = percentTypeToInfrared(pendingLightState.getInfrared());
        SetLightInfraredRequest packet = new SetLightInfraredRequest(infrared);
        replacePacketsInMap(packet);
    }

    @Override
    public void handleSignalStrengthChange(SignalStrength oldSignalStrength, SignalStrength newSignalStrength) {
        // Nothing to handle
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
                Packet sentPacket = pendingPacket.packet;
                logger.debug("{} : {} packet was acknowledged in {}ms", macAsHex, sentPacket.getClass().getSimpleName(),
                        ackTimestamp - pendingPacket.lastSend);

                // when these packets get lost the current state will still be updated by the
                // LifxLightCurrentStateUpdater
                if (sentPacket instanceof SetPowerRequest) {
                    GetLightPowerRequest powerPacket = new GetLightPowerRequest();
                    communicationHandler.sendPacket(powerPacket);
                } else if (sentPacket instanceof SetColorRequest) {
                    GetRequest colorPacket = new GetRequest();
                    communicationHandler.sendPacket(colorPacket);
                    getZonesIfZonesAreSet();
                } else if (sentPacket instanceof SetColorZonesRequest) {
                    getZonesIfZonesAreSet();
                } else if (sentPacket instanceof SetLightInfraredRequest) {
                    GetLightInfraredRequest infraredPacket = new GetLightInfraredRequest();
                    communicationHandler.sendPacket(infraredPacket);
                }
            } else {
                logger.debug("{} : No pending packet found for ack with sequence number: {}", macAsHex,
                        packet.getSequence());
            }
        }
    }

    private void getZonesIfZonesAreSet() {
        if (product.isMultiZone()) {
            List<PendingPacket> pending = pendingPacketsMap.get(SetColorZonesRequest.TYPE);
            if (pending == null || pending.isEmpty()) {
                GetColorZonesRequest zoneColorPacket = new GetColorZonesRequest();
                communicationHandler.sendPacket(zoneColorPacket);
            }
        }
    }

}
