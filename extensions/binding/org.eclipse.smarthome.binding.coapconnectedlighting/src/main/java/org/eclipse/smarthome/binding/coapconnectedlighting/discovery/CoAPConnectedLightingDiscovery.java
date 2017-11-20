/**
 * Copyright (c) 2016 Microchip Technology Inc. and its subsidiaries and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.binding.coapconnectedlighting.discovery;

import static org.eclipse.smarthome.binding.coapconnectedlighting.CoAPConnectedLightingBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.binding.coapconnectedlighting.handler.CoAPConnectedLightingHandler;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoAPConnectedLightingDiscovery extends AbstractDiscoveryService {
    private Logger logger = LoggerFactory.getLogger(CoAPConnectedLightingHandler.class);

    private final String TAG = "UDPService";
    private final int UDP_PORT = 65527;
    private final int UDP_MAX_LEN = 1024;

    private DatagramSocket serverSocket = null;

    public CoAPConnectedLightingDiscovery() throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, 10);
    }

    UDPServerThread udpListenServer = new UDPServerThread();

    ScheduledFuture<?> udpListenServerTask;

    @Override
    protected void startBackgroundDiscovery() {
        udpListenServerTask = scheduler.schedule(udpListenServer, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        udpListenServer.kill();
        if (udpListenServerTask != null) {
            udpListenServerTask.cancel(true);
        }
    }

    @Override
    protected void startScan() {
        broadcastUDPForCoapDeviceDiscover();
    }

    /**
     * Add a coap Thing in the discovery inbox
     */
    private void discoverCoAP(InetAddress address) {
        Map<String, Object> properties = new HashMap<>(4);
        properties.put(PROPERTY_COAP_SERVER, address.getHostAddress());
        ThingUID uid = new ThingUID(THING_TYPE,
                "iotiny-" + address.getHostAddress().substring(address.getHostAddress().lastIndexOf('.') + 1));
        if (uid != null) {
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel("Internet Of Tiny: " + address.getHostAddress()).build();
            thingDiscovered(result);
        }

    }

    // Scan for coap devices
    private void broadcastUDPForCoapDeviceDiscover() {
        logger.debug(TAG + ": Broadcasting udp");
        DatagramSocket mClientSock;
        try {
            mClientSock = new DatagramSocket();
            InetAddress hostAddress = InetAddress.getByName("255.255.255.255");
            logger.info(hostAddress.getHostAddress());
            logger.info(hostAddress.getHostName());
            logger.info(hostAddress.getCanonicalHostName());

            logger.debug(hostAddress.getHostAddress());
            logger.debug(hostAddress.getHostName());
            logger.debug(hostAddress.getCanonicalHostName());

            String mMsg = "mchp_coap";
            DatagramPacket out = new DatagramPacket(mMsg.getBytes(), mMsg.length(), hostAddress, 65525);
            mClientSock.send(out); // send to the server
            mClientSock.setSoTimeout(5000); // set the timeout in millisecounds.
            while (true) {
                try {
                    byte[] buf = new byte[1000];
                    DatagramPacket in = new DatagramPacket(buf, buf.length);
                    mClientSock.receive(in);
                    String payload = new String(in.getData());
                    logger.debug(TAG + ": Get reply from bcast from:" + in.getAddress().getHostAddress());
                    if (payload.substring(0, 9).equals("mchp_coap")) {
                        discoverCoAP(in.getAddress());
                    }
                } catch (SocketTimeoutException e) {
                    // timeout exception.
                    mClientSock.close();
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * UDPServer class that listens on UDP broadcast for CoAP Devices
     */
    private class UDPServerThread implements Runnable {
        byte[] receivedData = new byte[UDP_MAX_LEN];
        DatagramPacket datagramPacket = new DatagramPacket(receivedData, receivedData.length);
        private volatile boolean shouldStop = false;

        public void kill() {
            shouldStop = true;
        }

        @Override
        public void run() {
            logger.debug(TAG + ": UDPServer is running...");
            while (!shouldStop) {
                try {
                    serverSocket = new DatagramSocket(UDP_PORT);
                    serverSocket.receive(datagramPacket);
                    String data = new String(receivedData, 0, datagramPacket.getLength());
                    logger.debug(TAG + ": Received from:" + datagramPacket.getAddress().getHostAddress());

                    if (data.equals("mchp_coap")) {
                        InetAddress ipAddress = datagramPacket.getAddress();
                        discoverCoAP(ipAddress);
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (serverSocket != null) {
                        serverSocket.close();
                    }
                }
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
            logger.debug(TAG + ": UDPServer stopped...");
        }
    }
}
