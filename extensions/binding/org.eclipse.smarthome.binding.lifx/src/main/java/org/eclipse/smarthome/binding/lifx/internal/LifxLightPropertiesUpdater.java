/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.binding.lifx.LifxBindingConstants;
import org.eclipse.smarthome.binding.lifx.handler.LifxLightHandler.CurrentLightState;
import org.eclipse.smarthome.binding.lifx.internal.fields.MACAddress;
import org.eclipse.smarthome.binding.lifx.internal.listener.LifxPropertiesUpdateListener;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetHostFirmwareRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetVersionRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetWifiFirmwareRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Packet;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Products;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StateHostFirmwareResponse;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StateVersionResponse;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StateWifiFirmwareResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxLightPropertiesUpdater} updates the light properties when a light goes online. When packets get lost
 * the requests are resent when the {@code UPDATE_INTERVAL} elapses.
 *
 * @author Wouter Born - Update light properties when online
 */
public class LifxLightPropertiesUpdater {

    private final Logger logger = LoggerFactory.getLogger(LifxLightPropertiesUpdater.class);

    private static final int UPDATE_INTERVAL = 15;

    private final String logId;
    private final InetSocketAddress ipAddress;
    private final MACAddress macAddress;
    private final CurrentLightState currentLightState;
    private final LifxLightCommunicationHandler communicationHandler;

    private final List<LifxPropertiesUpdateListener> propertiesUpdateListeners = new CopyOnWriteArrayList<>();

    private final List<Packet> requestPackets = Arrays.asList(new GetVersionRequest(), new GetHostFirmwareRequest(),
            new GetWifiFirmwareRequest());
    private final Set<Integer> receivedPacketTypes = new HashSet<>();

    private final ReentrantLock lock = new ReentrantLock();
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> updateJob;

    private final Map<String, String> properties = new HashMap<>();
    private boolean updating;
    private boolean wasOnline;

    public LifxLightPropertiesUpdater(LifxLightContext context, LifxLightCommunicationHandler communicationHandler) {
        this.logId = context.getLogId();
        this.macAddress = context.getConfiguration().getMACAddress();
        this.ipAddress = context.getConfiguration().getHost();
        this.currentLightState = context.getCurrentLightState();
        this.scheduler = context.getScheduler();
        this.communicationHandler = communicationHandler;
    }

    public void updateProperties() {
        if (propertiesUpdateListeners.isEmpty()) {
            logger.debug("{} : Not updating properties because there are no listeners", logId);
            return;
        }

        try {
            lock.lock();

            boolean isOnline = currentLightState.isOnline();
            if (isOnline) {
                if (!wasOnline) {
                    logger.debug("{} : Updating light properties", logId);
                    properties.clear();
                    receivedPacketTypes.clear();
                    updating = true;
                    updateHostProperty();
                    updateMACAddressProperty();
                    sendPropertyRequestPackets();
                } else if (updating && !receivedAllResponsePackets()) {
                    logger.debug("{} : Resending requests for missing response packets", logId);
                    sendPropertyRequestPackets();
                }
            }

            wasOnline = isOnline;
        } catch (Exception e) {
            logger.error("Error occurred while polling online state of a light ({})", logId, e);
        } finally {
            lock.unlock();
        }
    }

    private void updateHostProperty() {
        if (communicationHandler.getIpAddress() != null) {
            properties.put(LifxBindingConstants.PROPERTY_HOST, communicationHandler.getIpAddress().getHostString());
        } else if (ipAddress != null) {
            properties.put(LifxBindingConstants.PROPERTY_HOST, ipAddress.getHostString());
        }
    }

    private void updateMACAddressProperty() {
        if (communicationHandler.getMACAddress() != null) {
            properties.put(LifxBindingConstants.PROPERTY_MAC_ADDRESS,
                    communicationHandler.getMACAddress().getAsLabel());
        } else if (macAddress != null) {
            properties.put(LifxBindingConstants.PROPERTY_MAC_ADDRESS, macAddress.getAsLabel());
        }
    }

    private void sendPropertyRequestPackets() {
        for (Packet packet : requestPackets) {
            if (!receivedPacketTypes.contains(packet.expectedResponses()[0])) {
                communicationHandler.sendPacket(packet);
            }
        }
    }

    public void handleResponsePacket(Packet packet) {
        if (!updating) {
            return;
        }

        if (packet instanceof StateVersionResponse) {
            Products products = Products.getProductFromProductID(((StateVersionResponse) packet).getProduct());
            long productVersion = ((StateVersionResponse) packet).getVersion();

            properties.put(LifxBindingConstants.PROPERTY_PRODUCT_ID, Long.toString(products.getProduct()));
            properties.put(LifxBindingConstants.PROPERTY_PRODUCT_NAME, products.getName());
            properties.put(LifxBindingConstants.PROPERTY_PRODUCT_VERSION, Long.toString(productVersion));
            properties.put(LifxBindingConstants.PROPERTY_VENDOR_ID, Long.toString(products.getVendor()));
            properties.put(LifxBindingConstants.PROPERTY_VENDOR_NAME, products.getVendorName());

            receivedPacketTypes.add(packet.getPacketType());
        } else if (packet instanceof StateHostFirmwareResponse) {
            String hostVersion = ((StateHostFirmwareResponse) packet).getVersion().toString();
            properties.put(LifxBindingConstants.PROPERTY_HOST_VERSION, hostVersion);
            receivedPacketTypes.add(packet.getPacketType());
        } else if (packet instanceof StateWifiFirmwareResponse) {
            String wifiVersion = ((StateWifiFirmwareResponse) packet).getVersion().toString();
            properties.put(LifxBindingConstants.PROPERTY_WIFI_VERSION, wifiVersion);
            receivedPacketTypes.add(packet.getPacketType());
        }

        if (receivedAllResponsePackets()) {
            updating = false;
            propertiesUpdateListeners.forEach(listener -> listener.handlePropertiesUpdate(properties));
            logger.debug("{} : Finished updating light properties", logId);
        }
    }

    private boolean receivedAllResponsePackets() {
        return requestPackets.size() == receivedPacketTypes.size();
    }

    public void addPropertiesUpdateListener(LifxPropertiesUpdateListener listener) {
        propertiesUpdateListeners.add(listener);
    }

    public void removePropertiesUpdateListener(LifxPropertiesUpdateListener listener) {
        propertiesUpdateListeners.remove(listener);
    }

    public void start() {
        try {
            lock.lock();
            communicationHandler.addResponsePacketListener(this::handleResponsePacket);
            if (updateJob == null || updateJob.isCancelled()) {
                updateJob = scheduler.scheduleWithFixedDelay(this::updateProperties, 0, UPDATE_INTERVAL,
                        TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error("Error occurred while starting properties update job for a light ({})", logId, e);
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        try {
            lock.lock();
            communicationHandler.removeResponsePacketListener(this::handleResponsePacket);
            if (updateJob != null && !updateJob.isCancelled()) {
                updateJob.cancel(true);
                updateJob = null;
            }
        } catch (Exception e) {
            logger.error("Error occurred while stopping properties update job for a light ({})", logId, e);
        } finally {
            lock.unlock();
        }
    }
}
