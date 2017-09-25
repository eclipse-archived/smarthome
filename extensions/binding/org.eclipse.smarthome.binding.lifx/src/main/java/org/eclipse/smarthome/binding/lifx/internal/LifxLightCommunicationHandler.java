/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import static org.eclipse.smarthome.binding.lifx.LifxBindingConstants.*;
import static org.eclipse.smarthome.binding.lifx.internal.fields.MACAddress.BROADCAST_ADDRESS;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntUnaryOperator;

import org.eclipse.smarthome.binding.lifx.handler.LifxLightHandler.CurrentLightState;
import org.eclipse.smarthome.binding.lifx.internal.fields.MACAddress;
import org.eclipse.smarthome.binding.lifx.internal.listener.LifxResponsePacketListener;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetServiceRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Packet;
import org.eclipse.smarthome.binding.lifx.internal.protocol.PacketFactory;
import org.eclipse.smarthome.binding.lifx.internal.protocol.PacketHandler;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StateServiceResponse;
import org.eclipse.smarthome.binding.lifx.internal.util.LifxNetworkUtil;
import org.eclipse.smarthome.binding.lifx.internal.util.LifxThrottlingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxLightCommunicationHandler} is responsible for the communications with a light.
 *
 * @author Wouter Born - Extracted class from LifxLightHandler
 */
public class LifxLightCommunicationHandler {

    private static final AtomicInteger LIGHT_COUNTER = new AtomicInteger(1);
    private static final IntUnaryOperator INC_SEQUENCE_NUMBER = (value) -> {
        return ++value > 255 ? 1 : value;
    };

    private final Logger logger = LoggerFactory.getLogger(LifxLightCommunicationHandler.class);

    private final String logId;
    private final CurrentLightState currentLightState;
    private final ScheduledExecutorService scheduler;

    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicInteger sequenceNumber = new AtomicInteger(1);

    private long source;
    private int service;
    private int port;

    private ScheduledFuture<?> networkJob;
    private Selector selector;

    private MACAddress macAddress;
    private InetSocketAddress ipAddress;
    private boolean broadcastEnabled;
    private SelectionKey broadcastKey;
    private SelectionKey unicastKey;

    public LifxLightCommunicationHandler(LifxLightContext context) {
        this.logId = context.getLogId();
        this.macAddress = context.getConfiguration().getMACAddress();
        this.ipAddress = context.getConfiguration().getHost();
        this.currentLightState = context.getCurrentLightState();
        this.scheduler = context.getScheduler();
        this.broadcastEnabled = context.getConfiguration().getHost() == null;
    }

    private List<LifxResponsePacketListener> responsePacketListeners = new CopyOnWriteArrayList<>();

    public void addResponsePacketListener(LifxResponsePacketListener listener) {
        responsePacketListeners.add(listener);
    }

    public void removeResponsePacketListener(LifxResponsePacketListener listener) {
        responsePacketListeners.remove(listener);
    }

    public void start() {
        try {
            lock.lock();

            logger.debug("{} : Starting communication handler", logId);

            if (networkJob == null || networkJob.isCancelled()) {
                networkJob = scheduler.scheduleWithFixedDelay(this::receiveAndHandlePackets, 0, PACKET_INTERVAL,
                        TimeUnit.MILLISECONDS);
            }

            source = UUID.randomUUID().getLeastSignificantBits() & (-1L >>> 32);
            logger.debug("{} : Using '{}' as source identifier", logId, Long.toString(source, 16));

            currentLightState.setOffline();

            selector = Selector.open();

            if (isBroadcastEnabled()) {
                openBroadcastChannel();
                // look for lights on the network
                broadcastPacket(new GetServiceRequest());
            } else {
                openUnicastChannel();
                sendPacket(new GetServiceRequest());
            }
        } catch (IOException e) {
            logger.error("Error occurred while starting LIFX communication handler for light '{}' : {}", logId,
                    e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        try {
            lock.lock();

            if (networkJob != null && !networkJob.isCancelled()) {
                networkJob.cancel(true);
                networkJob = null;
            }

            closeSelector();
            closeBroadcastChannel();
            closeUnicastChannel();

            ipAddress = null;
        } finally {
            lock.unlock();
        }
    }

    private void closeBroadcastChannel() {
        if (broadcastKey != null) {
            try {
                broadcastKey.channel().close();
            } catch (IOException e) {
                logger.warn("An exception occurred while closing the broadcast channel of the light ({}) : '{}'", logId,
                        e.getMessage());
            }
            broadcastKey.cancel();
            broadcastKey = null;
        }
    }

    private void closeUnicastChannel() {
        if (unicastKey != null) {
            try {
                unicastKey.channel().close();
            } catch (IOException e) {
                logger.error("An exception occurred while closing the unicast channel of the light ({}) : '{}'", logId,
                        e.getMessage());
            }
            unicastKey.cancel();
            unicastKey = null;
        }
    }

    private void closeSelector() {
        if (selector != null) {
            try {
                selector.wakeup();

                boolean isContinue = true;
                while (isContinue) {
                    try {
                        for (SelectionKey selectionKey : selector.keys()) {
                            selectionKey.channel().close();
                            selectionKey.cancel();
                        }
                        isContinue = false; // continue till all keys are cancelled
                    } catch (ConcurrentModificationException e) {
                        logger.warn("An exception occurred while closing a selector key of the light ({}) : '{}'",
                                logId, e.getMessage());
                    }
                }

                selector.close();
                selector = null;
            } catch (IOException e) {
                logger.warn("An exception occurred while closing the selector of the light ({}) : '{}'", logId,
                        e.getMessage());
            }
        }
    }

    public InetSocketAddress getIpAddress() {
        return ipAddress;
    }

    public MACAddress getMACAddress() {
        return macAddress;
    }

    public void receiveAndHandlePackets() {
        try {
            lock.lock();

            if (selector != null) {
                try {
                    selector.selectNow();
                } catch (IOException e) {
                    logger.error("An exception occurred while selecting keys for the light ({}) : {}", logId,
                            e.getMessage());
                }

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key;

                    try {
                        key = keyIterator.next();
                    } catch (ConcurrentModificationException e) {
                        // when a StateServiceResponse packet is handled a new unicastChannel may be registered
                        // in the selector which causes this exception, recover from it by restarting the iteration
                        logger.debug("{} : Restarting iteration after ConcurrentModificationException", logId);
                        keyIterator = selector.selectedKeys().iterator();
                        continue;
                    }

                    if (key.isValid() && key.isAcceptable()) {
                        // block of code only for completeness purposes
                        logger.trace("{} : Connection was accepted by a ServerSocketChannel", logId);
                    } else if (key.isValid() && key.isConnectable()) {
                        // block of code only for completeness purposes
                        logger.trace("{} : Connection was established with a remote server", logId);
                    } else if (key.isValid() && key.isReadable()) {
                        logger.trace("{} : Channel is ready for reading", logId);
                        SelectableChannel channel = key.channel();
                        InetSocketAddress address = null;
                        int messageLength = 0;

                        ByteBuffer readBuffer = ByteBuffer.allocate(LifxNetworkUtil.getBufferSize());
                        try {
                            if (channel instanceof DatagramChannel) {
                                address = (InetSocketAddress) ((DatagramChannel) channel).receive(readBuffer);
                            } else if (channel instanceof SocketChannel) {
                                address = (InetSocketAddress) ((SocketChannel) channel).getRemoteAddress();
                                ((SocketChannel) channel).read(readBuffer);
                            }
                            messageLength = readBuffer.position();
                        } catch (Exception e) {
                            logger.warn("An exception occurred while reading data for the light ({}) : '{}'", logId,
                                    e.getMessage());
                        }
                        if (address != null) {
                            if (!LifxNetworkUtil.getInterfaceAddresses().contains(address.getAddress())) {
                                readBuffer.rewind();

                                ByteBuffer packetSize = readBuffer.slice();
                                packetSize.position(0);
                                packetSize.limit(2);
                                int size = Packet.FIELD_SIZE.value(packetSize);

                                if (messageLength == size) {
                                    ByteBuffer packetType = readBuffer.slice();
                                    packetType.position(32);
                                    packetType.limit(34);
                                    int type = Packet.FIELD_PACKET_TYPE.value(packetType);

                                    PacketHandler<?> handler = PacketFactory.createHandler(type);

                                    if (handler == null) {
                                        logger.trace("{} : Unknown packet type: {} (source: {})", logId,
                                                String.format("0x%02X", type), address.toString());
                                        continue;
                                    }

                                    Packet packet = handler.handle(readBuffer);
                                    if (packet == null) {
                                        logger.warn(
                                                "Packet handler '{}' was unable to handle packet for the light ({})",
                                                handler.getClass().getName(), logId);
                                    } else {
                                        handlePacket(packet, address);
                                    }
                                }
                            }
                        }
                    } else if (key.isValid() && key.isWritable()) {
                        // block of code only for completeness purposes
                        logger.trace("{} : Channel is ready for writing", logId);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while receiving a packet from the light ({}) : '{}'", logId,
                    e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    private void handlePacket(final Packet packet, InetSocketAddress address) {
        boolean packetFromConfiguredMAC = macAddress != null && (packet.getTarget().equals(macAddress));
        boolean packetFromConfiguredHost = ipAddress != null && (address.equals(ipAddress));
        boolean broadcastPacket = packet.getTarget().equals(BROADCAST_ADDRESS);
        boolean packetSourceIsHandler = (packet.getSource() == source || packet.getSource() == 0);

        if ((packetFromConfiguredMAC || packetFromConfiguredHost || broadcastPacket) && packetSourceIsHandler) {
            logger.trace("{} : Packet type '{}' received from '{}' for '{}' with sequence '{}' and source '{}'",
                    new Object[] { logId, packet.getClass().getSimpleName(), address.toString(),
                            packet.getTarget().getHex(), packet.getSequence(), Long.toString(packet.getSource(), 16) });

            if (packet instanceof StateServiceResponse) {
                MACAddress discoveredAddress = ((StateServiceResponse) packet).getTarget();
                if (packetFromConfiguredHost && macAddress == null) {
                    macAddress = discoveredAddress;
                    currentLightState.setOnline(discoveredAddress);
                    return;
                } else if (macAddress != null && macAddress.equals(discoveredAddress)) {
                    boolean newIpAddress = ipAddress == null || !address.equals(ipAddress);
                    boolean newPort = port != (int) ((StateServiceResponse) packet).getPort();
                    boolean newService = service != ((StateServiceResponse) packet).getService();

                    if (newIpAddress || newPort || newService || currentLightState.isOffline()) {
                        this.port = (int) ((StateServiceResponse) packet).getPort();
                        this.service = ((StateServiceResponse) packet).getService();

                        if (port == 0) {
                            logger.warn("Light ({}) service with ID '{}' is currently not available", logId, service);
                            currentLightState.setOfflineByCommunicationError();
                        } else {
                            this.ipAddress = new InetSocketAddress(address.getAddress(), port);

                            closeUnicastChannel();

                            try {
                                openUnicastChannel();
                            } catch (Exception e) {
                                logger.warn(
                                        "An exception occurred while opening the unicast channel of the light ({}) : '{}'",
                                        logId, e.getMessage());
                                currentLightState.setOfflineByCommunicationError();
                                return;
                            }

                            currentLightState.setOnline();
                        }
                    }
                }
            }

            // Listeners are notified in a separate thread for better concurrency and to prevent deadlock.
            scheduler.schedule(() -> {
                responsePacketListeners.forEach(listener -> listener.handleResponsePacket(packet));
            }, 0, TimeUnit.MILLISECONDS);
        }
    }

    private void openBroadcastChannel() throws IOException {
        DatagramChannel broadcastChannel = DatagramChannel.open(StandardProtocolFamily.INET)
                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .setOption(StandardSocketOptions.SO_BROADCAST, true);
        broadcastChannel.configureBlocking(false);

        int offset = LIGHT_COUNTER.getAndIncrement();
        logger.debug("{} : Binding the broadcast channel on port {}", logId, BROADCAST_PORT + offset);
        broadcastChannel.bind(new InetSocketAddress(BROADCAST_PORT + offset));

        broadcastKey = broadcastChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    private void openUnicastChannel() throws IOException {
        DatagramChannel unicastChannel = DatagramChannel.open(StandardProtocolFamily.INET)
                .setOption(StandardSocketOptions.SO_REUSEADDR, true);
        unicastChannel.configureBlocking(false);
        unicastKey = unicastChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        unicastChannel.connect(ipAddress);
        logger.trace("{} : Connected to light via {}", logId, unicastChannel.getLocalAddress().toString());
    }

    public boolean isBroadcastEnabled() {
        return broadcastEnabled;
    }

    public void sendPacket(Packet packet) {
        if (ipAddress != null) {
            packet.setSource(source);
            packet.setTarget(macAddress);
            packet.setSequence(sequenceNumber.getAndUpdate(INC_SEQUENCE_NUMBER));
            sendPacket(packet, ipAddress, unicastKey);
        }
    }

    public void resendPacket(Packet packet) {
        if (ipAddress != null) {
            packet.setSource(source);
            packet.setTarget(macAddress);
            sendPacket(packet, ipAddress, unicastKey);
        }
    }

    public void broadcastPacket(Packet packet) {
        packet.setSource(source);
        packet.setSequence(sequenceNumber.getAndUpdate(INC_SEQUENCE_NUMBER));

        for (InetSocketAddress address : LifxNetworkUtil.getBroadcastAddresses()) {
            sendPacket(packet, address, broadcastKey);
        }
    }

    private boolean sendPacket(Packet packet, InetSocketAddress address, SelectionKey selectedKey) {
        boolean result = false;

        try {
            lock.lock();

            if (selectedKey.equals(unicastKey)) {
                LifxThrottlingUtil.lock(macAddress);
            } else {
                LifxThrottlingUtil.lock();
            }

            while (!result) {
                selector.selectNow();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (!result && keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isValid() && key.isWritable() && key.equals(selectedKey)) {
                        SelectableChannel channel = key.channel();
                        if (channel instanceof DatagramChannel) {
                            logger.trace(
                                    "{} : Sending packet type '{}' from '{}' to '{}' for '{}' with sequence '{}' and source '{}'",
                                    new Object[] { logId, packet.getClass().getSimpleName(),
                                            ((InetSocketAddress) ((DatagramChannel) channel).getLocalAddress())
                                                    .toString(),
                                            address.toString(), packet.getTarget().getHex(), packet.getSequence(),
                                            Long.toString(packet.getSource(), 16) });
                            ((DatagramChannel) channel).send(packet.bytes(), address);
                            result = true;
                        } else if (channel instanceof SocketChannel) {
                            ((SocketChannel) channel).write(packet.bytes());
                            result = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("An exception occurred while sending a packet to the light ({}) : '{}'", logId,
                    e.getMessage());
            currentLightState.setOfflineByCommunicationError();
        } finally {
            if (selectedKey.equals(unicastKey)) {
                LifxThrottlingUtil.unlock(macAddress);
            } else {
                LifxThrottlingUtil.unlock();
            }

            lock.unlock();
        }

        return result;
    }

}
