/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.lifx.LifxBindingConstants;
import org.eclipse.smarthome.binding.lifx.internal.fields.MACAddress;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetServiceRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetVersionRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Packet;
import org.eclipse.smarthome.binding.lifx.internal.protocol.PacketFactory;
import org.eclipse.smarthome.binding.lifx.internal.protocol.PacketHandler;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Products;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StateServiceResponse;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StateVersionResponse;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link LifxLightDiscovery} provides support for auto-discovery of LIFX
 * lights.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Karel Goderis - Rewrite for Firmware V2, and remove dependency on external libraries
 */
public class LifxLightDiscovery extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(LifxLightDiscovery.class);

    private List<InetSocketAddress> broadcastAddresses;
    private List<InetAddress> interfaceAddresses;
    private final int BROADCAST_PORT = 56700;
    private static int REFRESH_INTERVAL = 60;
    private static int BROADCAST_TIMEOUT = 5000;
    private static int SELECTOR_TIMEOUT = 10000;
    private int bufferSize = 0;

    private HashMap<MACAddress, StateServiceResponse> serviceResponses = new HashMap<MACAddress, StateServiceResponse>();
    private ArrayList<ConnectionSetupParameter> connectionsToSetUp;
    private Selector selector;
    private DatagramChannel broadcastChannel;
    private long source;
    private boolean isScanning = false;

    private ScheduledFuture<?> discoveryJob;

    private ScheduledFuture<?> networkJob;

    public LifxLightDiscovery() throws IllegalArgumentException {
        super(Sets.newHashSet(LifxBindingConstants.THING_TYPE_COLORLIGHT, LifxBindingConstants.THING_TYPE_WHITELIGHT),
                1, true);
    }

    @Override
    protected void activate(Map<String, Object> configProperties) {
        super.activate(configProperties);

        broadcastAddresses = new ArrayList<InetSocketAddress>();
        interfaceAddresses = new ArrayList<InetAddress>();

        Enumeration<NetworkInterface> networkInterfaces = null;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            logger.debug("An exception occurred while discovering LIFX lights : '{}", e.getMessage());
        }
        if (networkInterfaces != null) {
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface iface = networkInterfaces.nextElement();
                try {
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
                                    broadcastAddresses
                                            .add(new InetSocketAddress(ifaceAddr.getBroadcast(), BROADCAST_PORT));
                                }
                            }
                        }
                    }
                } catch (SocketException e) {
                    logger.debug("An exception occurred while discovering LIFX lights : '{}", e.getMessage());
                }
            }
        }
    }

    @Override
    protected void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting the LIFX device background discovery");

        Runnable discoveryRunnable = new Runnable() {
            @Override
            public void run() {
                doScan();
            }
        };

        if (discoveryJob == null || discoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(discoveryRunnable, 0, REFRESH_INTERVAL, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping LIFX device background discovery");
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            discoveryJob.cancel(true);
            discoveryJob = null;
        }
        if (networkJob != null && !networkJob.isCancelled()) {
            networkJob.cancel(true);
            networkJob = null;
        }
    }

    @Override
    protected void startScan() {
        doScan();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    protected void doScan() {

        try {

            if (!isScanning) {
                isScanning = true;
                if (selector != null) {
                    selector.close();
                }

                if (broadcastChannel != null) {
                    broadcastChannel.close();
                }

                selector = Selector.open();

                broadcastChannel = DatagramChannel.open(StandardProtocolFamily.INET)
                        .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                        .setOption(StandardSocketOptions.SO_BROADCAST, true);
                broadcastChannel.configureBlocking(false);
                broadcastChannel.socket().setSoTimeout(BROADCAST_TIMEOUT);
                broadcastChannel.bind(new InetSocketAddress(BROADCAST_PORT));

                SelectionKey broadcastKey = broadcastChannel.register(selector,
                        SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                networkJob = scheduler.schedule(networkRunnable, 0, TimeUnit.MILLISECONDS);

                source = UUID.randomUUID().getLeastSignificantBits() & (-1L >>> 32);
                logger.debug("The LIFX discovery service will use '{}' as source identifier",
                        Long.toString(source, 16));

                GetServiceRequest packet = new GetServiceRequest();
                broadcastPacket(packet, broadcastKey);
            } else {
                logger.info("A discovery scan for LIFX light is already underway");
            }

        } catch (Exception e) {
            logger.debug("An exception occurred while discovering LIFX lights : '{}", e.getMessage());
        }

    }

    private void broadcastPacket(Packet packet, SelectionKey broadcastKey) {

        packet.setSequence(0);
        packet.setSource(source);

        for (InetSocketAddress address : broadcastAddresses) {
            boolean result = false;
            while (!result) {
                LifxNetworkThrottler.lock();
                result = sendPacket(packet, address, broadcastKey);
                LifxNetworkThrottler.unlock();
            }
        }
    }

    private boolean sendPacket(Packet packet, InetSocketAddress address, SelectionKey selectedKey) {

        boolean result = false;

        try {
            boolean sent = false;

            while (!sent) {
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
                                        "Discovery : Sending packet type '{}' from '{}' to '{}' for '{}' with sequence '{}' and source '{}'",
                                        new Object[] { packet.getClass().getSimpleName(),
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
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("An exception occurred while communicating with the bulb : '{}'", e.getMessage());
        }

        return result;
    }

    private Runnable networkRunnable = new Runnable() {

        @Override
        public void run() {
            try {

                long startStamp = System.currentTimeMillis();

                logger.trace("Entering read loop at {}", startStamp);

                while (System.currentTimeMillis() - startStamp < SELECTOR_TIMEOUT) {

                    connectionsToSetUp = new ArrayList<ConnectionSetupParameter>();

                    if (selector != null && selector.isOpen()) {
                        try {
                            selector.selectNow();
                        } catch (IOException e) {
                            logger.error("An exception occurred while selecting: {}", e.getMessage());
                        }

                        Set<SelectionKey> selectedKeys = selector.selectedKeys();
                        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                        while (keyIterator.hasNext()) {

                            SelectionKey key = keyIterator.next();

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
                                    logger.trace("Receiving data from {}", address.getAddress().toString());
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
                                } else if (key.isValid() && key.isWritable()) {
                                    // a channel is ready for writing
                                    // block of code only for completeness purposes
                                }
                            }
                        }

                        // Iterate through the channels that have to be set up, and the packets that have to be sent
                        // Workaround to avoid a ConcurrentModifictionException on the selector.SelectedKeys() Set
                        for (ConnectionSetupParameter csp : connectionsToSetUp) {
                            DatagramChannel unicastChannel = DatagramChannel.open(StandardProtocolFamily.INET)
                                    .setOption(StandardSocketOptions.SO_REUSEADDR, true);
                            unicastChannel.configureBlocking(false);
                            SelectionKey unicastKey = unicastChannel.register(selector,
                                    SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                            unicastChannel.connect(csp.ipaddress);
                            logger.trace("Connected to a bulb via {}", unicastChannel.getLocalAddress().toString());

                            GetVersionRequest versionPacket = new GetVersionRequest();
                            versionPacket.setTarget(csp.target);
                            versionPacket.setSequence(1);
                            versionPacket.setSource(source);

                            LifxNetworkThrottler.lock();
                            sendPacket(versionPacket, csp.ipaddress, unicastKey);
                            LifxNetworkThrottler.unlock();
                        }
                    }
                }
                isScanning = false;
            } catch (Exception e) {
                logger.error("An exception orccurred while communicating with the bulb : '{}'", e.getMessage(), e);
            }
        }
    };

    private void handlePacket(Packet packet, InetSocketAddress address) {

        logger.trace("Discovery : Packet type '{}' received from '{}' for '{}' with sequence '{}' and source '{}'",
                new Object[] { packet.getClass().getSimpleName(), address.toString(), packet.getTarget().getHex(),
                        packet.getSequence(), Long.toString(packet.getSource(), 16) });

        if (packet.getSource() == source || packet.getSource() == 0) {

            if (packet instanceof StateServiceResponse) {
                serviceResponses.put(packet.getTarget(), (StateServiceResponse) packet);
                int port = (int) ((StateServiceResponse) packet).getPort();

                if (port != 0) {
                    try {
                        ConnectionSetupParameter connectionSetup = new ConnectionSetupParameter();
                        connectionSetup.ipaddress = new InetSocketAddress(address.getAddress(), port);
                        connectionSetup.target = packet.getTarget();
                        connectionsToSetUp.add(connectionSetup);
                    } catch (Exception e) {
                        logger.warn("An exception occurred while connecting to IP address : '{}'", e.getMessage());
                        return;
                    }
                }
            }

            if (packet instanceof StateVersionResponse) {
                StateServiceResponse serviceResponse = serviceResponses.get(packet.getTarget());

                if (serviceResponse != null) {
                    DiscoveryResult discoveryResult = createDiscoveryResult(serviceResponse,
                            (StateVersionResponse) packet);
                    if (discoveryResult != null) {
                        thingDiscovered(discoveryResult);
                    }
                }
            }

        }
    }

    private DiscoveryResult createDiscoveryResult(StateServiceResponse packet, StateVersionResponse returnedPacket) {

        MACAddress discoveredAddress = packet.getTarget();
        try {
            Products product = Products.getProductFromProductID(returnedPacket.getProduct());
            ThingUID thingUID = getUID(discoveredAddress.getAsLabel(), product.isColor());

            String label = "";

            if (StringUtils.isBlank(label)) {
                label = product.getName();
            }

            logger.trace("Discovered a LIFX light : {}", label);

            return DiscoveryResultBuilder.create(thingUID).withLabel(label)
                    .withProperty(LifxBindingConstants.CONFIG_PROPERTY_DEVICE_ID, discoveredAddress.getAsLabel())
                    .withRepresentationProperty(discoveredAddress.getAsLabel()).build();
        } catch (IllegalArgumentException e) {
            logger.trace("Ignoring packet: {}", e);
            return null;
        }
    }

    private ThingUID getUID(String hex, boolean color) {
        if (color) {
            return new ThingUID(LifxBindingConstants.THING_TYPE_COLORLIGHT, hex);
        } else {
            return new ThingUID(LifxBindingConstants.THING_TYPE_WHITELIGHT, hex);
        }
    }

    private class ConnectionSetupParameter {
        public MACAddress target;
        public InetSocketAddress ipaddress;
    }

}
