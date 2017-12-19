/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.tradfri.handler;

import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants;
import org.eclipse.smarthome.binding.tradfri.internal.CoapCallback;
import org.eclipse.smarthome.binding.tradfri.internal.DeviceUpdateListener;
import org.eclipse.smarthome.binding.tradfri.internal.TradfriCoapClient;
import org.eclipse.smarthome.binding.tradfri.internal.TradfriCoapEndpoint;
import org.eclipse.smarthome.binding.tradfri.internal.TradfriCoapHandler;
import org.eclipse.smarthome.binding.tradfri.internal.config.TradfriGatewayConfig;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link TradfriGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class TradfriGatewayHandler extends BaseBridgeHandler implements CoapCallback {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String MIN_SUPPORTED_VERSION = "1.2.42";

    private TradfriCoapClient deviceClient;
    private String gatewayURI;
    private String gatewayInfoURI;
    private DTLSConnector dtlsConnector;
    private CoapEndpoint endPoint;

    private final Set<DeviceUpdateListener> deviceUpdateListeners = new CopyOnWriteArraySet<>();

    private ScheduledFuture<?> scanJob;

    public TradfriGatewayHandler(@NonNull Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // there are no channels on the gateway yet
    }

    @Override
    public void initialize() {
        TradfriGatewayConfig configuration = getConfigAs(TradfriGatewayConfig.class);

        if (isNullOrEmpty(configuration.host)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Host must be specified in the configuration!");
            return;
        }
        if (isNullOrEmpty(configuration.code)) {
            if (isNullOrEmpty(configuration.identity) || isNullOrEmpty(configuration.preSharedKey)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Either security code or identity and pre-shared key must be provided in the configuration!");
                return;
            } else {
                establishConnection();
            }
        } else {
            if (isOldFirmware()) {
                /*
                 * older firmware - fall back to authentication with security code
                 * in this case the Thing configuration will not be persisted
                 */
                logger.warn("Gateway with old firmware - please consider upgrading to the latest version.");

                Configuration editedConfig = editConfiguration();
                editedConfig.put(TradfriBindingConstants.GATEWAY_CONFIG_IDENTITY, "");
                editedConfig.put(TradfriBindingConstants.GATEWAY_CONFIG_PRE_SHARED_KEY, configuration.code);
                updateConfiguration(editedConfig);

                establishConnection();
            } else {
                updateStatus(ThingStatus.UNKNOWN);
                // Running async operation to retrieve new <'identity','key'> pair
                scheduler.execute(() -> {
                    boolean success = obtainIdentityAndPreSharedKey();
                    if (success) {
                        establishConnection();
                    }
                });
            }
        }
    }

    private void establishConnection() {
        TradfriGatewayConfig configuration = getConfigAs(TradfriGatewayConfig.class);

        this.gatewayURI = "coaps://" + configuration.host + ":" + configuration.port + "/" + DEVICES;
        this.gatewayInfoURI = "coaps://" + configuration.host + ":" + configuration.port + "/" + GATEWAY + "/"
                + GATEWAY_DETAILS;
        try {
            URI uri = new URI(gatewayURI);
            deviceClient = new TradfriCoapClient(uri);
        } catch (URISyntaxException e) {
            logger.error("Illegal gateway URI '{}': {}", gatewayURI, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
        builder.setPskStore(new StaticPskStore(configuration.identity, configuration.preSharedKey.getBytes()));
        dtlsConnector = new DTLSConnector(builder.build());
        endPoint = new TradfriCoapEndpoint(dtlsConnector, NetworkConfig.getStandard());
        deviceClient.setEndpoint(endPoint);
        updateStatus(ThingStatus.UNKNOWN);

        // schedule a new scan every minute
        scanJob = scheduler.scheduleWithFixedDelay(this::startScan, 0, 1, TimeUnit.MINUTES);
    }

    /**
     * Authenticates against the gateway with the security code in order to receive a pre-shared key for a newly
     * generated identity.
     * As this requires a remote request, this method might be long-running.
     *
     * @return true, if credentials were successfully obtained, false otherwise
     */
    protected boolean obtainIdentityAndPreSharedKey() {
        TradfriGatewayConfig configuration = getConfigAs(TradfriGatewayConfig.class);

        String identity = UUID.randomUUID().toString().replace("-", "");
        String preSharedKey = null;

        CoapResponse gatewayResponse;
        String authUrl = null;
        String responseText = null;
        try {
            DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
            builder.setPskStore(new StaticPskStore("Client_identity", configuration.code.getBytes()));

            DTLSConnector dtlsConnector = new DTLSConnector(builder.build());
            CoapEndpoint authEndpoint = new CoapEndpoint(dtlsConnector, NetworkConfig.getStandard());
            authUrl = "coaps://" + configuration.host + ":" + configuration.port + "/15011/9063";

            CoapClient deviceClient = new CoapClient(new URI(authUrl));
            deviceClient.setTimeout(TimeUnit.SECONDS.toMillis(10));
            deviceClient.setEndpoint(authEndpoint);

            JsonObject json = new JsonObject();
            json.addProperty(CLIENT_IDENTITY_PROPOSED, identity);

            gatewayResponse = deviceClient.post(json.toString(), 0);

            authEndpoint.destroy();
            deviceClient.shutdown();

            if (gatewayResponse == null) {
                // seems we ran in a timeout, which potentially also happens
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "No response from gateway. Might be due to an invalid security code.");
                return false;
            }

            if (gatewayResponse.isSuccess()) {
                responseText = gatewayResponse.getResponseText();
                json = new JsonParser().parse(responseText).getAsJsonObject();
                preSharedKey = json.get(NEW_PSK_BY_GW).getAsString();

                if (isNullOrEmpty(preSharedKey)) {
                    logger.error("Received pre-shared key is empty for thing {} on gateway at {}", getThing().getUID(),
                            configuration.host);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Pre-shared key was not obtain successfully");
                    return false;
                } else {
                    logger.info("Received pre-shared key for gateway '{}'", configuration.host);
                    logger.debug("Using identity '{}' with pre-shared key '{}'.", identity, preSharedKey);

                    Configuration editedConfig = editConfiguration();
                    editedConfig.put(TradfriBindingConstants.GATEWAY_CONFIG_CODE, null);
                    editedConfig.put(TradfriBindingConstants.GATEWAY_CONFIG_IDENTITY, identity);
                    editedConfig.put(TradfriBindingConstants.GATEWAY_CONFIG_PRE_SHARED_KEY, preSharedKey);
                    updateConfiguration(editedConfig);

                    return true;
                }
            } else {
                logger.warn(
                        "Failed obtaining pre-shared key for identity '{}' (response code '{}', response text '{}')",
                        identity, gatewayResponse.getCode(),
                        isNullOrEmpty(gatewayResponse.getResponseText()) ? "<empty>"
                                : gatewayResponse.getResponseText());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String
                        .format("Failed obtaining pre-shared key with status code '%s'", gatewayResponse.getCode()));
            }
        } catch (URISyntaxException e) {
            logger.error("Illegal gateway URI '{}'", authUrl, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (JsonParseException e) {
            logger.warn("Invalid response recieved from gateway '{}'", responseText, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Invalid response recieved from gateway '%s'", responseText));
        }
        return false;
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
            requestGatewayInfo();
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

    private synchronized void requestGatewayInfo() {
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

    private boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Checks current firmware in the thing properties and compares it with the value of {@link #MIN_SUPPORTED_VERSION}
     *
     * @return true if current firmware is older than {@value #MIN_SUPPORTED_VERSION}
     */
    private boolean isOldFirmware() {
        String currentFirmware = thing.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION);
        return currentFirmware == null || MIN_SUPPORTED_VERSION.compareTo(currentFirmware) > 0;
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);

        logger.info("Bridge configuration updated. Updating paired things (if any).");
        for (Thing t : getThing().getThings()) {
            final ThingHandler thingHandler = t.getHandler();
            if (thingHandler != null) {
                thingHandler.thingUpdated(t);
            }
        }
    }
}
