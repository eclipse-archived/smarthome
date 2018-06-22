/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.homematic.handler;

import static org.eclipse.smarthome.core.thing.Thing.*;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.binding.homematic.internal.common.HomematicConfig;
import org.eclipse.smarthome.binding.homematic.internal.communicator.HomematicGateway;
import org.eclipse.smarthome.binding.homematic.internal.communicator.HomematicGatewayAdapter;
import org.eclipse.smarthome.binding.homematic.internal.communicator.HomematicGatewayFactory;
import org.eclipse.smarthome.binding.homematic.internal.discovery.HomematicDeviceDiscoveryService;
import org.eclipse.smarthome.binding.homematic.internal.misc.HomematicClientException;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapointConfig;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDevice;
import org.eclipse.smarthome.binding.homematic.internal.model.HmGatewayInfo;
import org.eclipse.smarthome.binding.homematic.internal.type.HomematicTypeGenerator;
import org.eclipse.smarthome.binding.homematic.internal.type.UidUtils;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HomematicBridgeHandler} is the handler for a Homematic gateway and connects it to the framework.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HomematicBridgeHandler extends BaseBridgeHandler implements HomematicGatewayAdapter {
    private final Logger logger = LoggerFactory.getLogger(HomematicBridgeHandler.class);
    private static final long REINITIALIZE_DELAY_SECONDS = 10;
    private static SimplePortPool portPool = new SimplePortPool();

    private HomematicConfig config;
    private HomematicGateway gateway;
    private final HomematicTypeGenerator typeGenerator;
    private final HttpClient httpClient;

    private HomematicDeviceDiscoveryService discoveryService;
    private ServiceRegistration<?> discoveryServiceRegistration;

    private final String ipv4Address;

    public HomematicBridgeHandler(@NonNull Bridge bridge, HomematicTypeGenerator typeGenerator, String ipv4Address,
            HttpClient httpClient) {
        super(bridge);
        this.typeGenerator = typeGenerator;
        this.ipv4Address = ipv4Address;
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        config = createHomematicConfig();
        registerDeviceDiscoveryService();
        final HomematicBridgeHandler instance = this;
        scheduler.execute(() -> {
            try {
                String id = getThing().getUID().getId();
                gateway = HomematicGatewayFactory.createGateway(id, config, instance, httpClient);
                configureThingProperties();
                gateway.initialize();

                // scan for already known devices (new devices will not be discovered,
                // since installMode==true is only achieved if the bridge is online
                discoveryService.startScan(null);
                discoveryService.waitForScanFinishing();

                updateStatus(ThingStatus.ONLINE);
                if (!config.getGatewayInfo().isHomegear()) {
                    try {
                        gateway.loadRssiValues();
                    } catch (IOException ex) {
                        logger.warn("Unable to load RSSI values from bridge '{}'", getThing().getUID().getId());
                        logger.error("{}", ex.getMessage(), ex);
                    }
                }
                gateway.startWatchdogs();
            } catch (IOException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                logger.debug(
                        "Homematic bridge was set to OFFLINE-COMMUNICATION_ERROR due to the following exception: {}",
                        ex.getMessage(), ex);
                dispose();
                scheduleReinitialize();
            }
        });
    }

    private void configureThingProperties() {
        final HmGatewayInfo info = config.getGatewayInfo();
        final Map<String, String> properties = getThing().getProperties();

        if (!properties.containsKey(PROPERTY_FIRMWARE_VERSION)) {
            getThing().setProperty(PROPERTY_FIRMWARE_VERSION, info.getFirmware());
        }
        if (!properties.containsKey(PROPERTY_SERIAL_NUMBER)) {
            getThing().setProperty(PROPERTY_SERIAL_NUMBER, info.getAddress());
        }
        if (!properties.containsKey(PROPERTY_MODEL_ID)) {
            getThing().setProperty(PROPERTY_MODEL_ID, info.getType());
        }
    }

    /**
     * Schedules a reinitialization, if the Homematic gateway is not reachable at bridge startup.
     */
    private void scheduleReinitialize() {
        scheduler.schedule(() -> {
            initialize();
        }, REINITIALIZE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing bridge '{}'", getThing().getUID().getId());
        super.dispose();
        if (discoveryService != null) {
            discoveryService.stopScan();
            unregisterDeviceDiscoveryService();
        }
        if (gateway != null) {
            gateway.dispose();
        }
        if (config != null) {
            portPool.release(config.getXmlCallbackPort());
            portPool.release(config.getBinCallbackPort());
        }
    }

    /**
     * Registers the DeviceDiscoveryService.
     */
    private void registerDeviceDiscoveryService() {
        if (bundleContext != null) {
            logger.trace("Registering HomematicDeviceDiscoveryService for bridge '{}'", getThing().getUID().getId());
            discoveryService = new HomematicDeviceDiscoveryService(this);
            discoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                    discoveryService, new Hashtable<String, Object>());
            discoveryService.activate();
        }
    }

    /**
     * Unregisters the DeviceDisoveryService.
     */
    private void unregisterDeviceDiscoveryService() {
        if (discoveryServiceRegistration != null && bundleContext != null) {
            HomematicDeviceDiscoveryService service = (HomematicDeviceDiscoveryService) bundleContext
                    .getService(discoveryServiceRegistration.getReference());
            if (service != null) {
                service.deactivate();
            }

            discoveryServiceRegistration.unregister();
            discoveryServiceRegistration = null;
            discoveryService = null;
        }
    }

    /**
     * Sets the OFFLINE status for all things of this bridge that has been removed from the gateway.
     */
    @SuppressWarnings("null")
    public void setOfflineStatus() {
        for (Thing hmThing : getThing().getThings()) {
            try {
                gateway.getDevice(UidUtils.getHomematicAddress(hmThing));
            } catch (HomematicClientException e) {
                if (hmThing.getHandler() != null) {
                    ((HomematicThingHandler) hmThing.getHandler()).handleRemoval();
                }
            }
        }
    }

    /**
     * Creates the configuration for the HomematicGateway.
     */
    private HomematicConfig createHomematicConfig() {
        HomematicConfig homematicConfig = getThing().getConfiguration().as(HomematicConfig.class);
        if (homematicConfig.getCallbackHost() == null) {
            homematicConfig.setCallbackHost(this.ipv4Address);
        }
        if (homematicConfig.getXmlCallbackPort() == 0) {
            homematicConfig.setXmlCallbackPort(portPool.getNextPort());
        } else {
            portPool.setInUse(homematicConfig.getXmlCallbackPort());
        }
        if (homematicConfig.getBinCallbackPort() == 0) {
            homematicConfig.setBinCallbackPort(portPool.getNextPort());
        } else {
            portPool.setInUse(homematicConfig.getBinCallbackPort());
        }
        logger.debug("{}", homematicConfig);
        return homematicConfig;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            logger.debug("Refreshing bridge '{}'", getThing().getUID().getId());
            reloadAllDeviceValues();
        }
    }

    /**
     * Returns the TypeGenerator.
     */
    public HomematicTypeGenerator getTypeGenerator() {
        return typeGenerator;
    }

    /**
     * Returns the HomematicGateway.
     */
    public HomematicGateway getGateway() {
        return gateway;
    }

    /**
     * Updates the thing for the given Homematic device.
     */
    private void updateThing(HmDevice device) {
        Thing hmThing = getThingByUID(UidUtils.generateThingUID(device, getThing()));
        if (hmThing != null) {
            HomematicThingHandler thingHandler = (HomematicThingHandler) hmThing.getHandler();
            if (thingHandler != null) {
                thingHandler.thingUpdated(hmThing);
                for (Channel channel : hmThing.getChannels()) {
                    thingHandler.handleRefresh(channel.getUID());
                }
            }
        }
    }

    @Override
    public void onStateUpdated(HmDatapoint dp) {
        Thing hmThing = getThingByUID(UidUtils.generateThingUID(dp.getChannel().getDevice(), getThing()));
        if (hmThing != null) {
            final ThingStatus status = hmThing.getStatus();
            if (status == ThingStatus.ONLINE || status == ThingStatus.OFFLINE) {
                HomematicThingHandler thingHandler = (HomematicThingHandler) hmThing.getHandler();
                if (thingHandler != null) {
                    thingHandler.updateDatapointState(dp);
                }
            }
        }
    }

    @Override
    public HmDatapointConfig getDatapointConfig(HmDatapoint dp) {
        Thing hmThing = getThingByUID(UidUtils.generateThingUID(dp.getChannel().getDevice(), getThing()));
        if (hmThing != null) {
            HomematicThingHandler thingHandler = (HomematicThingHandler) hmThing.getHandler();
            if (thingHandler != null) {
                return thingHandler.getChannelConfig(dp);
            }
        }
        return new HmDatapointConfig();
    }

    @Override
    public void onNewDevice(HmDevice device) {
        onDeviceLoaded(device);
        updateThing(device);
    }

    @SuppressWarnings("null")
    @Override
    public void onDeviceDeleted(HmDevice device) {
        discoveryService.deviceRemoved(device);
        updateThing(device);

        Thing hmThing = getThingByUID(UidUtils.generateThingUID(device, getThing()));
        if (hmThing != null && hmThing.getHandler() != null) {
            ((HomematicThingHandler) hmThing.getHandler()).deviceRemoved();
        }
    }

    @Override
    public void onConnectionLost() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection lost");
    }

    @Override
    public void onConnectionResumed() {
        updateStatus(ThingStatus.ONLINE);
        reloadAllDeviceValues();
    }

    @Override
    public void onDeviceLoaded(HmDevice device) {
        typeGenerator.generate(device);
        if (discoveryService != null) {
            discoveryService.deviceDiscovered(device);
        }

        Thing hmThing = getThingByUID(UidUtils.generateThingUID(device, getThing()));
        if (hmThing != null) {
            HomematicThingHandler thingHandler = (HomematicThingHandler) hmThing.getHandler();
            if (thingHandler != null) {
                thingHandler.deviceLoaded(device);
            }
        }
    }

    @Override
    public void reloadDeviceValues(HmDevice device) {
        updateThing(device);
        if (device.isGatewayExtras()) {
            typeGenerator.generate(device);
        }
    }

    @Override
    public void reloadAllDeviceValues() {
        for (Thing hmThing : getThing().getThings()) {
            try {
                HmDevice device = gateway.getDevice(UidUtils.getHomematicAddress(hmThing));
                gateway.triggerDeviceValuesReload(device);
            } catch (HomematicClientException ex) {
                logger.warn("{}", ex.getMessage());
            }
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (((HomematicThingHandler) childHandler).isDeletionPending()) {
            deleteFromGateway(UidUtils.getHomematicAddress(childThing), false, true, false);
        }
    }

    /**
     * Deletes a device from the gateway.
     *
     * @param address The address of the device to be deleted
     * @param reset <i>true</i> will perform a factory reset on the device before deleting it.
     * @param force <i>true</i> will delete the device even if it is not reachable.
     * @param defer <i>true</i> will delete the device once it becomes available.
     */
    public void deleteFromGateway(String address, boolean reset, boolean force, boolean defer) {
        scheduler.submit(() -> {
            logger.debug("Deleting the device '{}' from gateway '{}'", address, getBridge());
            getGateway().deleteDevice(address, reset, force, defer);
        });
    }

}
