/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import static org.eclipse.smarthome.binding.lifx.LifxBindingConstants.PACKET_INTERVAL;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
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

import org.eclipse.smarthome.binding.lifx.LifxBindingConstants;
import org.eclipse.smarthome.binding.lifx.handler.LifxLightHandler.CurrentLightState;
import org.eclipse.smarthome.binding.lifx.internal.fields.MACAddress;
import org.eclipse.smarthome.binding.lifx.internal.listener.LifxResponsePacketListener;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetLightPowerRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetServiceRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Packet;
import org.eclipse.smarthome.binding.lifx.internal.protocol.PacketFactory;
import org.eclipse.smarthome.binding.lifx.internal.protocol.PacketHandler;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StateServiceResponse;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxLightCommunicationHandler} is responsible for the communications with a light.
 *
 * @author Wouter Born - Extracted class from LifxLightHandler
 */
public class LifxLightCommunicationHandler {

    private Logger logger = LoggerFactory.getLogger(LifxLightCommunicationHandler.class);

    private final int BROADCAST_PORT = 56700;

    private CurrentLightState currentLightState;

    private static AtomicInteger lightCounter = new AtomicInteger(1);

    private long source;
    private int service;
    private int port;

    private MACAddress macAddress = null;
    private String macAsHex = null;

    private MACAddress broadcastAddress = new MACAddress("000000000000", true);
    private AtomicInteger sequenceNumber = new AtomicInteger(1);

    private Selector selector;
    private ScheduledFuture<?> networkJob;
    private ReentrantLock lock = new ReentrantLock();

    private InetSocketAddress ipAddress = null;
    private DatagramChannel unicastChannel = null;
    private SelectionKey unicastKey = null;
    private SelectionKey broadcastKey = null;
    private List<InetSocketAddress> broadcastAddresses;
    private List<InetAddress> interfaceAddresses;
    private int bufferSize = 0;

    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(LifxBindingConstants.THREADPOOL_NAME);

    public LifxLightCommunicationHandler(MACAddress macAddress, CurrentLightState currentLightState) {
        this.macAddress = macAddress;
        this.macAsHex = macAddress.getHex();
        this.currentLightState = currentLightState;
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

            logger.debug("Starting LIFX communication handler for bulb '{}'.", macAsHex);

            if (networkJob == null || networkJob.isCancelled()) {
                networkJob = scheduler.scheduleWithFixedDelay(networkRunnable, 0, PACKET_INTERVAL,
                        TimeUnit.MILLISECONDS);
            }

            source = UUID.randomUUID().getLeastSignificantBits() & (-1L >>> 32);
            logger.debug("The LIFX handler will use '{}' as source identifier", Long.toString(source, 16));
            broadcastAddresses = new ArrayList<InetSocketAddress>();
            interfaceAddresses = new ArrayList<InetAddress>();

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface iface = networkInterfaces.nextElement();
                if (iface.isUp() && !iface.isLoopback()) {
                    for (InterfaceAddress ifaceAddr : iface.getInterfaceAddresses()) {
                        if (ifaceAddr.getAddress() instanceof Inet4Address) {
                            logger.debug("Adding '{}' as interface address with MTU {}", ifaceAddr.getAddress(),
                                    iface.getMTU());
                            if (iface.getMTU() > bufferSize) {
                                bufferSize = iface.getMTU();
                            }
                            interfaceAddresses.add(ifaceAddr.getAddress());
                            if (ifaceAddr.getBroadcast() != null) {
                                logger.debug("Adding '{}' as broadcast address", ifaceAddr.getBroadcast());
                                broadcastAddresses.add(new InetSocketAddress(ifaceAddr.getBroadcast(), BROADCAST_PORT));
                            }
                        }
                    }
                }
            }

            selector = Selector.open();

            DatagramChannel broadcastChannel = DatagramChannel.open(StandardProtocolFamily.INET)
                    .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                    .setOption(StandardSocketOptions.SO_BROADCAST, true);
            broadcastChannel.configureBlocking(false);

            int offset = lightCounter.getAndIncrement();
            logger.debug("Binding the broadcast channel on port {}", BROADCAST_PORT + offset);
            broadcastChannel.bind(new InetSocketAddress(BROADCAST_PORT + offset));

            broadcastKey = broadcastChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

            currentLightState.setOffline();

            // look for lights on the network
            GetServiceRequest packet = new GetServiceRequest();
            broadcastPacket(packet);

        } catch (Exception ex) {
            logger.error("Error occured while initializing LIFX handler: " + ex.getMessage(), ex);
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

            try {
                if (selector != null) {

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
                            logger.warn("An exception occurred while closing a selector key : '{}'", e.getMessage());
                        }
                    }

                    selector.close();
                }
            } catch (IOException e) {
                logger.warn("An exception occurred while closing the selector : '{}'", e.getMessage());
            }

            if (broadcastKey != null) {
                try {
                    broadcastKey.channel().close();
                } catch (IOException e) {
                    logger.warn("An exception occurred while closing the broadcast channel : '{}'", e.getMessage());
                }
            }

            if (unicastKey != null) {
                try {
                    unicastKey.channel().close();
                } catch (IOException e) {
                    logger.warn("An exception occurred while closing the unicast channel : '{}'", e.getMessage());
                }
            }

            ipAddress = null;
        } finally {
            lock.unlock();
        }
    }

    private Runnable networkRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                lock.lock();

                if (selector != null) {
                    try {
                        selector.selectNow();
                    } catch (IOException e) {
                        logger.error("An exception occurred while selecting: {}", e.getMessage());
                    }

                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                    while (keyIterator.hasNext()) {

                        SelectionKey key;

                        try {
                            key = keyIterator.next();
                        } catch (ConcurrentModificationException e) {
                            // when a StateServiceResponse packet is handled a new unicastChannel may be registered
                            // in the selector which causes this exception, recover from it by restarting the iteration
                            logger.debug("{} : Restarting iteration after ConcurrentModificationException", macAsHex);
                            keyIterator = selector.selectedKeys().iterator();
                            continue;
                        }

                        if (key.isValid() && key.isAcceptable()) {
                            // a connection was accepted by a ServerSocketChannel.
                            // block of code only for completeness purposes

                        } else if (key.isValid() && key.isConnectable()) {
                            // a connection was established with a remote server.
                            // block of code only for completeness purposes

                        } else if (key.isValid() && key.isReadable()) {
                            // a channel is ready for reading
                            SelectableChannel channel = key.channel();
                            InetSocketAddress address = null;
                            int messageLength = 0;

                            ByteBuffer readBuffer = ByteBuffer.allocate(bufferSize);
                            try {
                                if (channel instanceof DatagramChannel) {
                                    address = (InetSocketAddress) ((DatagramChannel) channel).receive(readBuffer);
                                } else if (channel instanceof SocketChannel) {
                                    address = (InetSocketAddress) ((SocketChannel) channel).getRemoteAddress();
                                    ((SocketChannel) channel).read(readBuffer);
                                }
                                messageLength = readBuffer.position();
                            } catch (Exception e) {
                                logger.warn("An exception occurred while reading data : '{}'", e.getMessage());
                            }
                            if (address != null) {
                                if (!interfaceAddresses.contains(address.getAddress())) {

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
                                            logger.trace("Unknown packet type: {} (source: {})",
                                                    String.format("0x%02X", type), address.toString());
                                            continue;
                                        }

                                        Packet packet = handler.handle(readBuffer);
                                        if (packet == null) {
                                            logger.warn("Handler {} was unable to handle packet",
                                                    handler.getClass().getName());
                                        } else {
                                            handlePacket(packet, address);
                                        }
                                    }
                                }
                            }
                        } else if (key.isValid() && key.isWritable()) {
                            // a channel is ready for writing
                            // block of code only for completeness purposes
                        }

                    }
                }
            } catch (Exception e) {
                logger.error("An exception occurred while receiving a packet from the bulb : '{}'", e.getMessage());
            } finally {
                lock.unlock();
            }
        }
    };

    private void handlePacket(final Packet packet, InetSocketAddress address) {

        if ((packet.getTarget().equals(macAddress) || packet.getTarget().equals(broadcastAddress))
                && (packet.getSource() == source || packet.getSource() == 0)) {

            logger.trace("{} : Packet type '{}' received from '{}' for '{}' with sequence '{}' and source '{}'",
                    new Object[] { macAsHex, packet.getClass().getSimpleName(), address.toString(),
                            packet.getTarget().getHex(), packet.getSequence(), Long.toString(packet.getSource(), 16) });

            if (packet instanceof StateServiceResponse) {
                MACAddress discoveredAddress = ((StateServiceResponse) packet).getTarget();
                if (macAddress.equals(discoveredAddress)) {
                    if (!address.equals(ipAddress) && port != (int) ((StateServiceResponse) packet).getPort()
                            && service != ((StateServiceResponse) packet).getService()
                            || currentLightState.isOffline()) {
                        this.port = (int) ((StateServiceResponse) packet).getPort();
                        this.service = ((StateServiceResponse) packet).getService();

                        if (port == 0) {
                            logger.warn("The service with ID '{}' is currently not available", service);
                            currentLightState.setOfflineByCommunicationError();
                        } else {

                            if (unicastChannel != null && unicastKey != null) {
                                try {
                                    unicastChannel.close();
                                } catch (IOException e) {
                                    logger.error("An exception occurred while closing the channel : '{}'",
                                            e.getMessage());
                                }
                                unicastKey.cancel();
                            }

                            try {
                                ipAddress = new InetSocketAddress(address.getAddress(), port);
                                unicastChannel = DatagramChannel.open(StandardProtocolFamily.INET)
                                        .setOption(StandardSocketOptions.SO_REUSEADDR, true);
                                unicastChannel.configureBlocking(false);
                                unicastKey = unicastChannel.register(selector,
                                        SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                                unicastChannel.connect(ipAddress);
                                logger.trace("Connected to a bulb via {}", unicastChannel.getLocalAddress().toString());
                            } catch (Exception e) {
                                logger.warn("An exception occurred while connecting to the bulb's IP address : '{}'",
                                        e.getMessage());
                                currentLightState.setOfflineByCommunicationError();
                                return;
                            }

                            currentLightState.setOnline();

                            // populate the current state variables
                            GetLightPowerRequest powerPacket = new GetLightPowerRequest();
                            sendPacket(powerPacket);

                            GetRequest colorPacket = new GetRequest();
                            sendPacket(colorPacket);
                        }
                    }
                }
            }

            // Listeners are notified in a separate thread for better concurrency and to prevent deadlock.
            Runnable notifyListenersRunnable = new Runnable() {
                @Override
                public void run() {
                    for (LifxResponsePacketListener listener : responsePacketListeners) {
                        listener.handleResponsePacket(packet);
                    }
                }
            };
            scheduler.schedule(notifyListenersRunnable, 0, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Atomically increases the sequence number. When Java 8 is available this can be replaced by using the
     * {@code getAndUpdate} method.
     */
    private int getAndIncreaseSequenceNumber() {
        int prev, next;
        do {
            prev = sequenceNumber.get();
            next = prev + 1;
            if (next > 255) {
                next = 1;
            }
        } while (!sequenceNumber.compareAndSet(prev, next));
        return prev;
    }

    public void sendPacket(Packet packet) {
        if (ipAddress != null) {
            packet.setSource(source);
            packet.setTarget(macAddress);
            packet.setSequence(getAndIncreaseSequenceNumber());
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
        packet.setSequence(getAndIncreaseSequenceNumber());

        for (InetSocketAddress address : broadcastAddresses) {
            boolean result = false;
            while (!result) {
                result = sendPacket(packet, address, broadcastKey);
                if (!result) {
                    try {
                        Thread.sleep(PACKET_INTERVAL);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    private boolean sendPacket(Packet packet, InetSocketAddress address, SelectionKey selectedKey) {

        boolean result = false;

        try {
            lock.lock();

            if (selectedKey == unicastKey) {
                LifxNetworkThrottler.lock(macAsHex);
            } else {
                LifxNetworkThrottler.lock();
            }

            boolean sent = false;

            while (!sent && selector.isOpen()) {
                try {
                    selector.selectNow();
                } catch (IOException e) {
                    logger.error("An exception occurred while selecting: {}", e.getMessage());
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isValid() && key.isWritable() && key.equals(selectedKey)) {
                        SelectableChannel channel = key.channel();
                        try {
                            if (channel instanceof DatagramChannel) {
                                logger.trace(
                                        "{} : Sending packet type '{}' from '{}' to '{}' for '{}' with sequence '{}' and source '{}'",
                                        new Object[] { macAsHex, packet.getClass().getSimpleName(),
                                                ((InetSocketAddress) ((DatagramChannel) channel).getLocalAddress())
                                                        .toString(),
                                                address.toString(), packet.getTarget().getHex(), packet.getSequence(),
                                                Long.toString(packet.getSource(), 16) });
                                ((DatagramChannel) channel).send(packet.bytes(), address);
                                sent = true;
                                result = true;
                            } else if (channel instanceof SocketChannel) {
                                ((SocketChannel) channel).write(packet.bytes());
                            }
                        } catch (Exception e) {
                            logger.error("An exception occurred while writing data : '{}'", e.getMessage());
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while sending a packet to the bulb : '{}'", e.getMessage());
        } finally {

            if (selectedKey == unicastKey) {
                LifxNetworkThrottler.unlock(macAsHex);
            } else {
                LifxNetworkThrottler.unlock();
            }

            lock.unlock();
        }

        return result;
    }
}
