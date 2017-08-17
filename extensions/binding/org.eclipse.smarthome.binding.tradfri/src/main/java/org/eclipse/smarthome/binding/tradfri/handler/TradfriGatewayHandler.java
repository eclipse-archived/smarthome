/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri.handler;

import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;
import org.eclipse.smarthome.binding.tradfri.GatewayConfig;
import org.eclipse.smarthome.binding.tradfri.internal.CoapCallback;
import org.eclipse.smarthome.binding.tradfri.internal.DeviceUpdateListener;
import org.eclipse.smarthome.binding.tradfri.internal.TradfriCoapClient;
import org.eclipse.smarthome.binding.tradfri.internal.TradfriCoapHandler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link TradfriGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Mario Smit - Group Handler added
 */
public class TradfriGatewayHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(TradfriGatewayHandler.class);

    public TradfriCoapCallback devices, groups;
    private DTLSConnector dtlsConnector;
    private CoapEndpoint endPoint;

    private Set<DeviceUpdateListener> deviceUpdateListeners = new CopyOnWriteArraySet<>();

    private ScheduledFuture<?> scanJob;

    public TradfriGatewayHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // there are no channels on the gateway yet
    }

    @Override
    public void initialize() {
        GatewayConfig configuration = getConfigAs(GatewayConfig.class);
        if (configuration.host == null || configuration.host.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Host must be specified in the configuration!");
            return;
        }
        if (configuration.code == null || configuration.code.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Security code must be provided in the configuration!");
            return;
        }

        DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
        builder.setPskStore(new StaticPskStore("", configuration.code.getBytes()));
        dtlsConnector = new DTLSConnector(builder.build());
        endPoint = new CoapEndpoint(dtlsConnector, NetworkConfig.getStandard());

        // setup DEVICES scanner
        devices = setupScanner("coaps://" + configuration.host + ":" + configuration.port + "/" + DEVICES);
        if (devices == null) {
            logger.debug("Unable to scan for Tradfri DEVICES");
        }
        groups = setupScanner("coaps://" + configuration.host + ":" + configuration.port + "/" + GROUPS);
        if (groups == null) {
            logger.debug("Unable to scan for Tradfri GROUPS");
        }

        updateStatus(ThingStatus.UNKNOWN);

        // schedule a new scan every minute
        scanJob = scheduler.scheduleWithFixedDelay(() -> {
            if (devices != null) {
                devices.startScan();
            }
            if (groups != null) {
                groups.startScan();
            }
        }, 0, 60, TimeUnit.SECONDS);
    }

    TradfriCoapCallback setupScanner(String url) {
        TradfriCoapCallback scanner = new TradfriCoapCallback();
        scanner.gatewayURI = url;
        try {
            URI uri = new URI(scanner.gatewayURI);
            scanner.client = new TradfriCoapClient(uri);
        } catch (URISyntaxException e) {
            logger.debug("Illegal gateway URI `{}`: {}", scanner.gatewayURI, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return null;
        }
        scanner.client.setEndpoint(endPoint);
        return scanner;
    }

    @Override
    public void dispose() {
        if (scanJob != null) {
            scanJob.cancel(true);
            scanJob = null;
        }
        if (devices != null && devices.client != null) {
            devices.client.shutdown();
            devices.client = null;
        }
        if (groups != null && groups.client != null) {
            groups.client.shutdown();
            groups.client = null;
        }
        if (endPoint != null) {
            endPoint.destroy();
            endPoint = null;
        }
        super.dispose();
    }

    public class TradfriCoapCallback implements CoapCallback {
        private TradfriCoapClient client;
        private String gatewayURI;

        /**
         * Does a request to the gateway to list all available devices/services.
         * The response is received and processed by the method {@link onUpdate(JsonElement data)}.
         */
        public void startScan() {
            if (endPoint != null) {
                client.get(new TradfriCoapHandler(this));
            }
        }

        /**
         * Returns the root URI of the gateway.
         *
         * @return root URI of the gateway with coaps scheme
         */
        public String getGatewayURI() {
            return gatewayURI;
        }

        /**
         * Returns the coap endpoint that can be used within coap clients.
         *
         * @return the coap endpoint
         */
        public CoapEndpoint getEndpoint() {
            return endPoint;
        }

        @Override
        public void onUpdate(JsonElement data) {
            logger.debug("Gateway response: {}", data);

            if (endPoint != null) {
                try {
                    JsonArray array = data.getAsJsonArray();
                    for (int i = 0; i < array.size(); i++) {
                        requestDeviceDetails(array.get(i).getAsString());
                    }
                } catch (JsonSyntaxException e) {
                    logger.debug("JSON error: {}", e.getMessage());
                    setStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            }
        }

        private void requestDeviceDetails(String instanceId) {
            // we are reusing our coap client and merely temporarily set a sub-URI to call
            client.setURI(gatewayURI + "/" + instanceId);
            client.asyncGet().thenAccept(data -> {
                logger.debug("Response: {}", data);
                JsonObject json = new JsonParser().parse(data).getAsJsonObject();
                deviceUpdateListeners.forEach(listener -> listener.onUpdate(instanceId, json));
            });
            // restore root URI
            client.setURI(gatewayURI);
        }

        @Override
        public void setStatus(ThingStatus status, ThingStatusDetail statusDetail) {
            // are we still connected at all?
            if (endPoint != null) {
                updateStatus(status, statusDetail);
                if (dtlsConnector != null && status == ThingStatus.OFFLINE) {
                    try {
                        dtlsConnector.stop();
                        dtlsConnector.start();
                    } catch (IOException e) {
                        logger.debug("Error restarting the DTLS connector: {}", e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Registers a listener, which is informed about device details.
     *
     * @param listener the listener to register
     */
    public void registerDeviceUpdateListener(DeviceUpdateListener listener) {
        this.deviceUpdateListeners.add(listener);
    }

    /**
     * Unregisters a given listener.
     *
     * @param listener the listener to unregister
     */
    public void unregisterDeviceUpdateListener(DeviceUpdateListener listener) {
        this.deviceUpdateListeners.remove(listener);
    }
}
