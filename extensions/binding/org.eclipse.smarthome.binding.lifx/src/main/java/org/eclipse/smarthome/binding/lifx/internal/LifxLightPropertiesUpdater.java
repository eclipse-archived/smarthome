/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

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
import org.eclipse.smarthome.binding.lifx.internal.listener.LifxResponsePacketListener;
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

import com.google.common.collect.Lists;

/**
 * The {@link LifxLightPropertiesUpdater} updates the light properties when a light goes online. When packets get lost
 * the requests are resent when the {@code UPDATE_INTERVAL} elapses.
 *
 * @author Wouter Born - Update light properties when online
 */
public class LifxLightPropertiesUpdater implements LifxResponsePacketListener {

    private final Logger logger = LoggerFactory.getLogger(LifxLightPropertiesUpdater.class);

    private static final int UPDATE_INTERVAL = 15;

    private final MACAddress macAddress;
    private final String macAsHex;
    private final CurrentLightState currentLightState;
    private final LifxLightCommunicationHandler communicationHandler;

    private final List<LifxPropertiesUpdateListener> propertiesUpdateListeners = new CopyOnWriteArrayList<>();

    private final List<Packet> requestPackets = Lists.newArrayList(new GetVersionRequest(),
            new GetHostFirmwareRequest(), new GetWifiFirmwareRequest());
    private final Set<Integer> receivedPacketTypes = new HashSet<>();

    private final ReentrantLock lock = new ReentrantLock();
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> updateJob;

    private final Map<String, String> properties = new HashMap<>();
    private boolean updating;
    private boolean wasOnline;

    public LifxLightPropertiesUpdater(MACAddress macAddress, ScheduledExecutorService scheduler,
            CurrentLightState currentLightState, LifxLightCommunicationHandler communicationHandler) {
        this.macAddress = macAddress;
        this.macAsHex = macAddress.getHex();
        this.scheduler = scheduler;
        this.currentLightState = currentLightState;
        this.communicationHandler = communicationHandler;
    }

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                lock.lock();

                if (currentLightState.isOnline()) {
                    if (!wasOnline) {
                        if (propertiesUpdateListeners.size() > 0) {
                            logger.debug("{} : Updating light properties", macAsHex);
                            updating = true;
                            properties.clear();
                            receivedPacketTypes.clear();
                            properties.put(LifxBindingConstants.PROPERTY_MAC_ADDRESS, macAddress.getAsLabel());
                            sendPropertyRequestPackets();
                        }
                    } else if (updating && !receivedAllResponsePackets()) {
                        logger.debug("{} : Resending requests for missing response packets", macAsHex);
                        sendPropertyRequestPackets();
                    }
                }

                wasOnline = currentLightState.isOnline();
            } catch (Exception e) {
                logger.error("Error occurred while polling online state", e);
            } finally {
                lock.unlock();
            }
        }

        private void sendPropertyRequestPackets() {
            for (Packet packet : requestPackets) {
                if (!receivedPacketTypes.contains(packet.expectedResponses()[0])) {
                    communicationHandler.sendPacket(packet);
                }
            }
        }
    };

    @Override
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
            for (LifxPropertiesUpdateListener listener : propertiesUpdateListeners) {
                listener.handlePropertiesUpdate(properties);
            }
            logger.debug("{} : Finished updating light properties", macAsHex);
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
            communicationHandler.addResponsePacketListener(this);
            if (updateJob == null || updateJob.isCancelled()) {
                updateJob = scheduler.scheduleWithFixedDelay(updateRunnable, 0, UPDATE_INTERVAL, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error("Error occurred while starting properties update job", e);
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        try {
            lock.lock();
            communicationHandler.removeResponsePacketListener(this);
            if (updateJob != null && !updateJob.isCancelled()) {
                updateJob.cancel(true);
                updateJob = null;
            }
        } catch (Exception e) {
            logger.error("Error occurred while stopping properties update job", e);
        } finally {
            lock.unlock();
        }
    }
}
