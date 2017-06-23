package org.eclipse.smarthome.binding.wemo.handler;
/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.*;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.wemo.internal.http.WemoHttpCall;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link WemoDimmerHandler} is responsible for handling commands, which are
 * sent to one of the channels and to update their states.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */

public class WemoDimmerHandler extends BaseThingHandler implements UpnpIOParticipant, DiscoveryListener {

    private final Logger logger = LoggerFactory.getLogger(WemoDimmerHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_SOCKET, THING_TYPE_INSIGHT,
            THING_TYPE_LIGHTSWITCH, THING_TYPE_MOTION);

    private Map<String, Boolean> subscriptionState = new HashMap<String, Boolean>();

    private Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<String, String>());

    protected final static int SUBSCRIPTION_DURATION = 600;

    private UpnpIOService service;

    private int currentBrightness;

    /**
     * Set dimming stepsize to 5%
     */
    private static final int DIM_STEPSIZE = 5;

    /**
     * The default refresh interval in Seconds.
     */
    private int DEFAULT_REFRESH_INTERVAL = 120;

    private ScheduledFuture<?> refreshJob;

    private Runnable refreshRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                if (!isUpnpDeviceRegistered()) {
                    logger.debug("WeMo UPnP device {} not yet registered", getUDN());
                }

                updateWemoState();
                onSubscription();

            } catch (Exception e) {
                logger.debug("Exception during poll : {}", e);
            }
        }
    };

    public WemoDimmerHandler(Thing thing, UpnpIOService upnpIOService) {

        super(thing);

        logger.debug("Creating a WemoDimmerHandler V0.6 for thing '{}'", getThing().getUID());

        if (upnpIOService != null) {
            this.service = upnpIOService;
        } else {
            logger.debug("upnpIOService not set.");
        }

    }

    @Override
    public void initialize() {

        Configuration configuration = getConfig();

        if (configuration.get("udn") != null) {
            logger.debug("Initializing WemoDimmerHandler for UDN '{}'", configuration.get("udn"));
            onSubscription();
            onUpdate();
            super.initialize();
        } else {
            logger.debug("Cannot initalize WemoDimmerHandler. UDN not set.");
        }

    }

    @Override
    public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
        if (result.getThingUID().equals(this.getThing().getUID())) {
            if (getThing().getConfiguration().get(UDN).equals(result.getProperties().get(UDN))) {
                logger.trace("Discovered UDN '{}' for thing '{}'", result.getProperties().get(UDN),
                        getThing().getUID());
                updateStatus(ThingStatus.ONLINE);
                onSubscription();
                onUpdate();
            }
        }
    }

    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        if (thingUID.equals(this.getThing().getUID())) {
            logger.trace("Setting status for thing '{}' to OFFLINE", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void dispose() {
        logger.debug("WeMoDimmerHandler disposed.");

        removeSubscription();

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command '{}' received for channel '{}'", command, channelUID);

        if (command instanceof RefreshType) {
            try {
                updateWemoState();
            } catch (Exception e) {
                logger.debug("Exception during poll : {}", e);
            }
        } else {

            String argument = null;
            String value = null;

            switch (channelUID.getId()) {
                case CHANNEL_BRIGHTNESS:
                    argument = "brightness";
                    if (command instanceof PercentType) {
                        int newBrightness = ((PercentType) command).intValue();
                        value = String.valueOf(newBrightness);
                        currentBrightness = newBrightness;
                    } else if (command instanceof OnOffType) {
                        switch (command.toString()) {
                            case "ON":
                                value = "100";
                                break;
                            case "OFF":
                                value = "0";
                                break;
                        }
                    } else if (command instanceof IncreaseDecreaseType) {
                        int newBrightness;
                        switch (command.toString()) {
                            case "INCREASE":
                                newBrightness = currentBrightness + DIM_STEPSIZE;
                                if (newBrightness > 100) {
                                    newBrightness = 100;
                                }
                                value = String.valueOf(newBrightness);
                                currentBrightness = newBrightness;
                                break;
                            case "DECREASE":
                                newBrightness = currentBrightness - DIM_STEPSIZE;
                                if (newBrightness < 0) {
                                    newBrightness = 0;
                                }
                                value = String.valueOf(newBrightness);
                                currentBrightness = newBrightness;
                                break;
                        }
                    }
                    break;
                case CHANNEL_STATE:
                    argument = "BinaryState";
                    if (command.equals(OnOffType.ON)) {
                        value = "1";
                    } else if (command.equals(OnOffType.OFF)) {
                        value = "0";
                    }
                    break;
            }

            if (argument != null && value != null) {

                try {

                    String soapHeader = "\"urn:Belkin:service:basicevent:1#SetBinaryState\"";

                    String content = "<?xml version=\"1.0\"?>"
                            + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                            + "<s:Body>" + "<u:SetBinaryState xmlns:u=\"urn:Belkin:service:basicevent:1\">" + "<"
                            + argument + ">" + value + "</" + argument + ">" + "</u:SetBinaryState>" + "</s:Body>"
                            + "</s:Envelope>";

                    String wemoURL = getWemoURL("basicevent");

                    if (wemoURL != null) {
                        WemoHttpCall.executeCall(wemoURL, soapHeader, content);
                    }
                } catch (Exception e) {
                    logger.error("Failed to send command '{}' for device '{}': {}", command, getThing().getUID(),
                            e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
                updateStatus(ThingStatus.ONLINE);

            }

        }

    }

    @Override
    public void onServiceSubscribed(String service, boolean succeeded) {
        logger.debug("WeMo {}: Subscription to service {} {}", getUDN(), service, succeeded ? "succeeded" : "failed");
        subscriptionState.put(service, succeeded);
    }

    @Override
    public void onValueReceived(String variable, String value, String service) {

        logger.debug("Received pair '{}':'{}' (service '{}') for thing '{}'",
                new Object[] { variable, value, service, this.getThing().getUID() });

        updateStatus(ThingStatus.ONLINE);

        this.stateMap.put(variable, value);

        switch (variable) {
            case "BinaryState":
                State state = variable.equals("0") ? OnOffType.OFF : OnOffType.ON;
                logger.debug("State '{}' for device '{}' received", state, getThing().getUID());
                if (state != null) {
                    updateState(CHANNEL_STATE, state);
                }
                break;
            case "brightness":
                logger.debug("brightness '{}' for device '{}' received", value, getThing().getUID());

                int newBrightnessValue = Integer.valueOf(value);
                State newBrightnessState = new PercentType(newBrightnessValue);
                updateState(CHANNEL_BRIGHTNESS, newBrightnessState);
                currentBrightness = newBrightnessValue;
                break;
            case "fader":
                // not implemented yet
                break;
        }
    }

    private synchronized void onSubscription() {
        if (service.isRegistered(this)) {
            logger.debug("Checking WeMo GENA subscription for '{}'", this);

            String subscription = "basicevent1";

            if ((subscriptionState.get(subscription) == null) || !subscriptionState.get(subscription).booleanValue()) {
                logger.debug("Setting up GENA subscription {}: Subscribing to service {}...", getUDN(), subscription);
                service.addSubscription(this, subscription, SUBSCRIPTION_DURATION);
                subscriptionState.put(subscription, true);
            }

        } else {
            logger.debug("Setting up WeMo GENA subscription for '{}' FAILED - service.isRegistered(this) is FALSE",
                    this);
        }
    }

    private synchronized void removeSubscription() {
        logger.debug("Removing WeMo GENA subscription for '{}'", this);

        if (service.isRegistered(this)) {
            String subscription = "basicevent1";

            if ((subscriptionState.get(subscription) != null) && subscriptionState.get(subscription).booleanValue()) {
                logger.debug("WeMo {}: Unsubscribing from service {}...", getUDN(), subscription);
                service.removeSubscription(this, subscription);
            }

            subscriptionState = new HashMap<String, Boolean>();
            service.unregisterParticipant(this);
        }
    }

    private synchronized void onUpdate() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Configuration config = getThing().getConfiguration();
            int refreshInterval = DEFAULT_REFRESH_INTERVAL;
            Object refreshConfig = config.get("refresh");
            if (refreshConfig != null) {
                refreshInterval = ((BigDecimal) refreshConfig).intValue();
            }
            refreshJob = scheduler.scheduleAtFixedRate(refreshRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    private boolean isUpnpDeviceRegistered() {
        return service.isRegistered(this);
    }

    @Override
    public String getUDN() {
        return (String) this.getThing().getConfiguration().get(UDN);
    }

    /**
     * The {@link updateWemoState} polls the actual state of a WeMo device and
     * calls {@link onValueReceived} to update the statemap and channels..
     *
     */
    protected void updateWemoState() {

        URL descriptorURL = service.getDescriptorURL(this);
        try {
            if (descriptorURL != null) {
                String deviceURL = StringUtils.substringBefore(descriptorURL.toString(), "/setup.xml");
                String wemoCallResponse = WemoHttpCall.getEventServiceXML(deviceURL);
                if (wemoCallResponse != null) {
                    logger.trace("eventservice.xml :");
                    logger.trace(wemoCallResponse);
                }
                wemoCallResponse = WemoHttpCall.getDeviceInfoServiceXML(deviceURL);
                if (wemoCallResponse != null) {
                    logger.trace("deviceinfoservice.xml :");
                    logger.trace(wemoCallResponse);
                }
            }

        } catch (Exception e) {
            logger.error("Failed to get XML for device '{}': {}", getThing().getUID(), e.getMessage());
        }

        String action = "GetBinaryState";
        String variable = null;
        String actionService = "basicevent";
        String value = null;

        String soapHeader = "\"urn:Belkin:service:" + actionService + ":1#" + action + "\"";
        String content = "<?xml version=\"1.0\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Body>" + "<u:" + action + " xmlns:u=\"urn:Belkin:service:" + actionService + ":1\">" + "</u:"
                + action + ">" + "</s:Body>" + "</s:Envelope>";

        try {
            String wemoURL = getWemoURL(actionService);
            if (wemoURL != null) {
                String wemoCallResponse = WemoHttpCall.executeCall(wemoURL, soapHeader, content);
                if (wemoCallResponse != null) {
                    logger.trace("State response '{}' for device '{}' received", wemoCallResponse, getThing().getUID());
                    value = StringUtils.substringBetween(wemoCallResponse, "<BinaryState>", "</BinaryState>");
                    if (value != null) {
                        variable = "BinaryState";
                        logger.trace("New state '{}' for device '{}' received", value, getThing().getUID());
                        this.onValueReceived(variable, value, actionService + "1");
                    }
                    value = StringUtils.substringBetween(wemoCallResponse, "<brightness>", "</brightness>");
                    if (value != null) {
                        variable = "brightness";
                        logger.trace("New brightness '{}' for device '{}' received", value, getThing().getUID());
                        this.onValueReceived(variable, value, actionService + "1");
                    }
                    value = StringUtils.substringBetween(wemoCallResponse, "<fader>", "</fader>");
                    if (value != null) {
                        variable = "fader";
                        logger.trace("New fader value '{}' for device '{}' received", value, getThing().getUID());
                        this.onValueReceived(variable, value, actionService + "1");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get actual state for device '{}': {}", getThing().getUID(), e.getMessage());
        }

    }

    public String getWemoURL(String actionService) {
        URL descriptorURL = service.getDescriptorURL(this);
        String wemoURL = null;
        if (descriptorURL != null) {
            String deviceURL = StringUtils.substringBefore(descriptorURL.toString(), "/setup.xml");
            wemoURL = deviceURL + "/upnp/control/" + actionService + "1";
            return wemoURL;
        }
        return null;
    }

    @Override
    public void onStatusChanged(boolean status) {
    }

    @Override
    public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            Collection<ThingTypeUID> thingTypeUIDs) {
        return null;
    }

}
