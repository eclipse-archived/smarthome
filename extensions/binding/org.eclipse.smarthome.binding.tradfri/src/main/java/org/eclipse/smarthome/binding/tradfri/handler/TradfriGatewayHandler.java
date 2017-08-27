/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri.handler;

import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.DEVICES;
import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.GATEWAY;
import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.GATEWAY_DETAILS;
import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.VERSION;

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
import org.eclipse.smarthome.core.thing.Thing;
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
 */
public class TradfriGatewayHandler extends BaseBridgeHandler implements CoapCallback {

    private final Logger logger = LoggerFactory.getLogger(TradfriGatewayHandler.class);

    private TradfriCoapClient deviceClient;
    private String gatewayURI;
    private String gatewayInfoURI;
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

        this.gatewayURI = "coaps://" + configuration.host + ":" + configuration.port + "/" + DEVICES;
        this.gatewayInfoURI = "coaps://" + configuration.host + ":" + configuration.port + "/" + GATEWAY + "/" + GATEWAY_DETAILS;
        try {
            URI uri = new URI(gatewayURI);
            deviceClient = new TradfriCoapClient(uri);
        } catch (URISyntaxException e) {
            logger.debug("Illegal gateway URI `{}`: {}", gatewayURI, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
        builder.setPskStore(new StaticPskStore("", configuration.code.getBytes()));
        dtlsConnector = new DTLSConnector(builder.build());
        endPoint = new CoapEndpoint(dtlsConnector, NetworkConfig.getStandard());
        deviceClient.setEndpoint(endPoint);
        updateStatus(ThingStatus.UNKNOWN);

        // schedule a new scan every minute
        scanJob = scheduler.scheduleWithFixedDelay(() -> {
            startScan();
        }, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void dispose() {
        if (scanJob != null) {
            scanJob.cancel(true);
            scanJob = null;
        }
        if (deviceClient != null) {
            deviceClient.shutdown();
            deviceClient = null;
        }
        if (endPoint != null) {
            endPoint.destroy();
            endPoint = null;
        }
        super.dispose();
    }

    /**
     * Does a request to the gateway to list all available devices/services.
     * The response is received and processed by the method {@link onUpdate(JsonElement data)}.
     */
    public void startScan() {
        if (endPoint != null) {
            updateGatewayInfo();
            deviceClient.get(new TradfriCoapHandler(this));
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
        logger.debug("onUpdate response: {}", data);

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
    
    private synchronized void updateGatewayInfo() {
        // we are reusing our coap client and merely temporarily set a gateway info to call
        deviceClient.setURI(this.gatewayInfoURI);
        deviceClient.asyncGet().thenAccept(data -> {
            JsonObject json = new JsonParser().parse(data).getAsJsonObject();
            String firmwareVersion = json.get(VERSION).getAsString();
            getThing().setProperty(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
        });
        // restore root URI
        deviceClient.setURI(gatewayURI);
    }

    private synchronized void requestDeviceDetails(String instanceId) {
        // we are reusing our coap client and merely temporarily set a sub-URI to call
        deviceClient.setURI(gatewayURI + "/" + instanceId);
        deviceClient.asyncGet().thenAccept(data -> {
            logger.debug("requestDeviceDetails response: {}", data);
            JsonObject json = new JsonParser().parse(data).getAsJsonObject();
            deviceUpdateListeners.forEach(listener -> listener.onUpdate(instanceId, json));
        });
        // restore root URI
        deviceClient.setURI(gatewayURI);
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
